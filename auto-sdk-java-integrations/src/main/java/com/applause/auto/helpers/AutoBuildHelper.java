/*
 *
 * Copyright © 2024 Applause App Quality, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.applause.auto.helpers;

import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.framework.selenium.EnhancedCapabilities;
import com.applause.auto.util.applausepublicapi.ApplausePublicApi;
import com.applause.auto.util.applausepublicapi.ApplausePublicApiClient;
import com.applause.auto.util.applausepublicapi.dto.AttachmentWithHashesDto;
import com.applause.auto.util.applausepublicapi.dto.PageProductVersionDetailsDto;
import com.applause.auto.util.applausepublicapi.dto.ProductVersionDetailsDto;
import com.applause.auto.util.applausepublicapi.dto.TimelineDto;
import com.applause.auto.util.autoapi.AutoApi;
import com.applause.auto.util.autoapi.AutoApiImagePushDataDto;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import java.io.Serializable;
import java.net.Proxy;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import retrofit2.Response;

/** Helper to retrieve build information on Applause public Api service */
public final class AutoBuildHelper {
  private static final Logger logger = LogManager.getLogger();

  // Set up a retry policy to retry communication with auto-api.  This depends on the
  // communication mechanism throwing some type of exception on failure.  On a failure
  // we will wait at least 5 seconds and do an exponential back-off.  We retry up to 3 times
  private static final int MAX_RETIES = 3;
  private static final long MIN_WAIT = 5; //  5 seconds
  private static final long MAX_WAIT = 60; // 60 seconds
  private static final RetryPolicy<Object> retryPolicy =
      RetryPolicy.builder()
          .handle(Exception.class)
          .withBackoff(MIN_WAIT, MAX_WAIT, ChronoUnit.SECONDS)
          .withMaxRetries(MAX_RETIES)
          .build();
  private static final Proxy httpProxy = ApplauseConfigHelper.getHttpProxy();

  @Setter(value = AccessLevel.PACKAGE)
  private static ApplausePublicApi publicApiClient =
      ApplausePublicApiClient.getClient(
          ApplauseEnvironmentConfigurationManager.INSTANCE.get().applausePublicApiUrl(),
          ApplauseEnvironmentConfigurationManager.INSTANCE.get().apiKey(),
          httpProxy);

  private AutoBuildHelper() {}

  /**
   * Get the latest build from all versions of product
   *
   * @param client The client interface to use when communicating
   * @return BuildDto
   */
  public static AttachmentWithHashesDto getLatestBuild(final @NonNull ApplausePublicApi client) {
    // hierarchy is product -> versions -> builds
    // product is related to apiKey or explicit through automation properties file

    // get all versions, then find the latest (assuming latest version date has the latest build
    // also)
    Comparator<ProductVersionDetailsDto> versionComparator =
        new ProductVersionDetailsDtoComparator();
    List<ProductVersionDetailsDto> versions = getAllBuilds();
    ProductVersionDetailsDto latestVersion =
        versions.stream()
            .max(versionComparator)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Unable to find latest version of product from the Applause Public API"));

    // get the latest build
    Comparator<AttachmentWithHashesDto> buildComparator = new AttachmentWithHashesDtoComparator();
    List<AttachmentWithHashesDto> builds = getAttachments(latestVersion.id());
    return builds.stream()
        .max(buildComparator)
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Unable to find latest build of product matching version "
                        + latestVersion.id()
                        + " from the Applause Public API"));
  }

  /**
   * Check app property. If not specified, autodetect and place build-url as app
   *
   * @param appCaps : The Applause Capabilities
   */
  public static void autoDetectAppBuilds(final @NonNull EnhancedCapabilities appCaps) {
    // First, attempt to pull the app from the capabilities
    // If it is not defined there, we can check the EnvironmentConfiguration
    final String definedApp =
        Optional.ofNullable(appCaps.getApp())
            .orElseGet(EnvironmentConfigurationManager.INSTANCE.get()::app);

    // if app already specified, we do not need to auto-detect the app
    if (!Strings.isNullOrEmpty(definedApp)) {
      return;
    }

    boolean requiresApp = false;
    if (!EnvironmentConfigurationManager.INSTANCE.get().useLocalDrivers()) {
      requiresApp = appCaps.getApplauseOptions().isMobileNative();
    }
    if (requiresApp) {
      logger.info("No app property specified for mobileNative driver.  Attempting auto-detection.");
      final String buildUrl = determineBuildUrl();
      EnvironmentConfigurationManager.INSTANCE.override(ImmutableMap.of("app", buildUrl));
    }
  }

  /**
   * If build auto-detection is required, do the auto-detection operation by sending a message to
   * the applause services. This will throw an exception if no image is available.
   *
   * @return The 'app' field to use
   */
  public static String determineBuildUrl() {
    final var applauseConfigBean = ApplauseEnvironmentConfigurationManager.INSTANCE.get();
    AttachmentWithHashesDto build;
    if (applauseConfigBean.buildId() != null && applauseConfigBean.versionId() != null) {
      // get build based on version and build ids, because they have been specified
      build =
          getBuild(publicApiClient, applauseConfigBean.versionId(), applauseConfigBean.buildId())
              .attachments()
              .stream()
              .max(new AttachmentWithHashesDtoComparator())
              .get();
    } else {
      // try to auto-detect build - get latest build based on apiKey
      build = getLatestBuild(publicApiClient);
    }
    logger.info("Detected build to use for app: {}", build.url());
    return build.url();
  }

  static void checkForAutoApiError(final @NonNull Response<?> response) {
    if (response.isSuccessful()) {
      return;
    }
    int httpErrorCode = response.code();
    // Normally we get some text or a chunk of JSON.  We are going to pass the whole error body
    // back to the user we just need to make sure we can decode it
    String errMessage;
    try (var errorBody = response.errorBody()) {

      if (errorBody != null) {

        errMessage = "Http " + httpErrorCode + " returned from auto-api: " + errorBody.string();
      } else {
        errMessage = "Http " + httpErrorCode + " returned from auto-api: no error body";
      }
    } catch (Exception e) {
      // Something went wrong
      throw new RuntimeException(
          "Http " + httpErrorCode + " returned from auto-api: unable to parse error body", e);
    }
    throw new RuntimeException(errMessage);
  }

  /**
   * Request information from auto-api about the provider related to pushing application images
   *
   * @param client the client to use for communication
   * @param provider the identity of the provider
   * @return Push data associated with the provider from auto-api
   */
  public static AutoApiImagePushDataDto getImagePushData(
      final @NonNull AutoApi client, final @NonNull String provider) {
    return Failsafe.with(retryPolicy)
        .onFailure(
            failure ->
                logger.warn("failed to get version information from auto-api.  Is it running?"))
        .get(() -> client.getAppPushForProvider(provider).join().body());
  }

  /**
   * Gets a specific build from a specific version
   *
   * @param client The client to use when fetching the data. Passed as a parameter to allow mocking
   *     in unit tests
   * @param versionId the versionId
   * @param buildId the buildId
   * @return BuildDto
   */
  public static ProductVersionDetailsDto getBuild(
      final @NonNull ApplausePublicApi client,
      final @NonNull Long versionId,
      final @NonNull Long buildId) {
    Response<ProductVersionDetailsDto> response = client.builds().getBuildDetails(buildId).join();
    return response.body();
  }

  /**
   * Get all builds from a version
   *
   * @return List of BuildDto
   */
  static List<ProductVersionDetailsDto> getAllBuilds() {
    List<ProductVersionDetailsDto> builds = new ArrayList<>();
    final var firstPage = getBuildPage(1L, 100L, "createDate,desc");
    builds.addAll(firstPage.content());
    for (long i = 2; i <= firstPage.totalPages(); i++) {
      final var page = getBuildPage(i, 100L, "createDate,desc");
      builds.addAll(page.content());
    }
    return builds;
  }

  /**
   * Get all builds from a version
   *
   * @param page the page number
   * @param size the number of results in a page
   * @param sort the sort direction
   * @return The contents of the page
   */
  @SneakyThrows
  private static PageProductVersionDetailsDto getBuildPage(
      final Long page, final Long size, final String sort) {
    final var response =
        publicApiClient
            .builds()
            .getBuilds(
                ApplauseEnvironmentConfigurationManager.INSTANCE.get().productId(),
                page,
                size,
                sort)
            .join();
    if (!response.isSuccessful()) {
      logger.error(response.errorBody().string());
    }
    return response.body();
  }

  /**
   * Get all builds from a version
   *
   * @param buildId the versionId
   * @return List of BuildDto
   */
  private static List<AttachmentWithHashesDto> getAttachments(final Long buildId) {
    final var response = publicApiClient.builds().getBuildDetails(buildId).join();
    return response.body().attachments();
  }

  /**
   * Gets the Applause Public API Instance
   *
   * @return The Applause Public API Instance
   */
  public static ApplausePublicApi getPublicApi() {
    return publicApiClient;
  }

  static class AttachmentWithHashesDtoComparator
      implements Comparator<AttachmentWithHashesDto>, Serializable {

    @Override
    public int compare(final AttachmentWithHashesDto o1, final AttachmentWithHashesDto o2) {
      if (Strings.isNullOrEmpty(o1.fileName()) && !Strings.isNullOrEmpty(o2.fileName())) {
        return -1;
      }
      if (!Strings.isNullOrEmpty(o1.fileName()) && Strings.isNullOrEmpty(o2.fileName())) {
        return 1;
      }
      final TimelineComparator timelineComparator = new TimelineComparator();
      return timelineComparator.compare(o1.timeline(), o2.timeline());
    }
  }

  static class ProductVersionDetailsDtoComparator
      implements Comparator<ProductVersionDetailsDto>, Serializable {

    @Override
    public int compare(
        final @NonNull ProductVersionDetailsDto o1, final @NonNull ProductVersionDetailsDto o2) {
      if (CollectionUtils.isEmpty(o1.attachments()) && CollectionUtils.isEmpty(o2.attachments())) {
        return 0;
      }
      if (CollectionUtils.isEmpty(o1.attachments()) && !CollectionUtils.isEmpty(o2.attachments())) {
        return -1;
      }
      if (!CollectionUtils.isEmpty(o1.attachments()) && CollectionUtils.isEmpty(o2.attachments())) {
        return 1;
      }
      final AttachmentWithHashesDtoComparator buildComparator =
          new AttachmentWithHashesDtoComparator();
      // We already checked that neither version's attachments are empty, so these should never
      // actually throw
      final AttachmentWithHashesDto o1MaxAttachment =
          o1.attachments().stream().max(buildComparator).orElseThrow();
      final AttachmentWithHashesDto o2MaxAttachment =
          o2.attachments().stream().max(buildComparator).orElseThrow();
      return buildComparator.compare(o1MaxAttachment, o2MaxAttachment);
    }
  }

  static class TimelineComparator implements Comparator<TimelineDto>, Serializable {

    @Override
    public int compare(final TimelineDto o1, final TimelineDto o2) {
      return o1.createDate().compareTo(o2.createDate());
    }
  }
}
