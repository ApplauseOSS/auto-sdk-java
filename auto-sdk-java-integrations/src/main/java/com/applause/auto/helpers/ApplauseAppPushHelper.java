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

import com.applause.auto.apppush.ApplauseAppPushConfig;
import com.applause.auto.config.AppPushConfig;
import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.exceptions.RetryableRuntimeException;
import com.applause.auto.framework.selenium.apppush.AppPushHelper;
import com.applause.auto.framework.selenium.apppush.AppPushTarget;
import com.applause.auto.util.autoapi.AutoApi;
import com.applause.auto.util.autoapi.AutoApiClient;
import com.applause.auto.util.autoapi.AutoApiImagePushDataDto;
import com.applause.auto.util.seleniumproxy.AppPushProxyDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.HttpMethod;
import lombok.NonNull;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Helper class for uploading apps to a provider */
@SuppressWarnings({"checkstyle:MultipleStringLiterals"})
public final class ApplauseAppPushHelper {
  private static final Logger logger = LogManager.getLogger(ApplauseAppPushHelper.class);
  private static final String PROXY_URI = "extensions/app-push";
  private static final RetryPolicy<Object> retryPolicy =
      RetryPolicy.builder()
          .handle(RetryableRuntimeException.class)
          .withBackoff(5, 60, ChronoUnit.SECONDS)
          .withMaxRetries(3)
          .build();
  private static final Proxy httpProxy = ApplauseConfigHelper.getHttpProxy();
  private static final AutoApi apiClient =
      AutoApiClient.getClient(
          ApplauseEnvironmentConfigurationManager.INSTANCE.get().autoApiUrl(),
          ApplauseEnvironmentConfigurationManager.INSTANCE.get().apiKey(),
          httpProxy);

  private static final OkHttpClient providerClient =
      new OkHttpClient.Builder()
          .connectTimeout(1, TimeUnit.MINUTES)
          .writeTimeout(10, TimeUnit.MINUTES)
          .readTimeout(10, TimeUnit.MINUTES)
          .proxy(httpProxy)
          .build();

  private ApplauseAppPushHelper() {
    // this is just static methods
  }

  /** Performs the application push to the provider specified, updating global config */
  public static void performApplicationPushIfNecessary() {
    ApplauseAppPushConfig featureConfig = ApplauseAppPushConfig.fromApplauseSdkConfigBean();
    if (StringUtils.isBlank(featureConfig.appPushProvider())) {
      // The feature is no enabled.  We do NOT check for other properties being present
      // The use-case assumes that many of these properties might be set in a file and only
      // the 'appPushProvider' parameter used to activate the feature
      return;
    }
    // We need to see if we can map the Provider to a class
    // The feature is active, check to see if it's complete and do any necessary action
    // This is a little complicated.  We need to make sure  we can get a targetProvider
    // without overwriting what we have
    AppPushTarget targetProvider = AppPushHelper.getTarget(featureConfig);
    AppPushConfig basicConfig = featureConfig.getBasicConfig();
    basicConfig.appPushClass(targetProvider.getClass().getCanonicalName());
    if (basicConfig.isComplete()) {
      // We can perform the feature without any communication with auto-api, do it
      Optional<String> app = AppPushHelper.pushApplication(featureConfig, targetProvider);
      if (app.isPresent()) {

        logger.debug("App parameter being overwritten with " + app.get());
        // There is no existing "app" parameter in the config, and we have an app value that's been
        // returned.  Add that value to the existing config so other parts of the system can use it
        EnvironmentConfigurationManager.INSTANCE.override(ImmutableMap.of("app", app.get()));
        return;
      } else {
        logger.warn("App upload failed, trying to continue.");
      }
    }
    // We can't perform the feature without some assistance from auto-api
    featureConfig = augmentConfigFromApplause(featureConfig);
    if (!featureConfig.isComplete()) {
      // At this point, an incomplete config means we can't do the requested feature.
      // Throw an exception to indicate failure
      throw new RuntimeException(
          "Unable to push application to provider='"
              + featureConfig.appPushProvider()
              + "', missing parameters: "
              + String.join(", ", featureConfig.missingParameters()));
    }
    // This call will throw an exception on error with the source input
    AppPushHelper.validateSource(featureConfig.appPushSource());
    // The feature is complete, do it
    String app = null;
    try {
      app = pushApplication(featureConfig, targetProvider);
    } catch (MalformedURLException | URISyntaxException e) {
      logger.error("Could not push application", e);
    }
    if (app != null && StringUtils.isBlank(EnvironmentConfigurationManager.INSTANCE.get().app())) {
      // There is no existing "app" parameter in the config, and we have an app value that's been
      // returned.  Add that value to the existing config so other parts of the system can use it
      EnvironmentConfigurationManager.INSTANCE.override(ImmutableMap.of("app", app));
    }
  }

  /**
   * Do the auto-detection of an application if no source is provided, updating global config.
   * Throws an exception on error
   */
  public static void autoDetectBuildIfNecessary() {
    // If we already have an app, do not auto-detect the build
    if (EnvironmentConfigurationManager.INSTANCE.get().app() != null) {
      return;
    }

    // If we have app push enabled and already have a source, then we do not need to auto-detect the
    // app url
    if (isAppPushEnabled() && hasAppPushSource()) {
      return;
    }

    String appUrl = AutoBuildHelper.determineBuildUrl();

    if (appUrl == null) {
      return;
    }
    // Pipe the auto-detected build to app push if app push is enabled without a source
    if (!hasAppPushSource() && isAppPushEnabled()) {
      logger.info("Using auto-detected build '{}' as input to app-push", appUrl);
      EnvironmentConfigurationManager.INSTANCE.override(ImmutableMap.of("appPushSource", appUrl));
    } else {
      EnvironmentConfigurationManager.INSTANCE.override(ImmutableMap.of("app", appUrl));
    }
  }

  public static boolean isAppPushEnabled() {
    return EnvironmentConfigurationManager.INSTANCE.get().appPushProvider() != null;
  }

  public static boolean hasAppPushSource() {
    return EnvironmentConfigurationManager.INSTANCE.get().appPushSource() != null;
  }

  static ApplauseAppPushConfig augmentConfigFromApplause(
      @NonNull final ApplauseAppPushConfig sdkInputs) {
    // The Applause Servers _are_ involved.  We need to query auto-api for the data.
    // The API client here has already been configured with the necessary API-Key and possible HTTP
    // proxy information.  All we need to provide are the retries
    AutoApiImagePushDataDto autoApiData =
        Failsafe.with(retryPolicy)
            .onFailure(failure -> logger.warn("failed to get provider information from auto-api"))
            .get(() -> fetchAutoApiConfigData(sdkInputs.appPushProvider()));
    if (autoApiData == null) {
      throw new RuntimeException(
          "Unable to communicate with auto-api and communication is required");
    }
    return mergeCfg(sdkInputs, autoApiData);
  }

  static AutoApiImagePushDataDto fetchAutoApiConfigData(final String provider) {
    retrofit2.Response<AutoApiImagePushDataDto> rsp =
        apiClient.getAppPushForProvider(provider).join();
    if (rsp.isSuccessful() && rsp.code() == 200) {
      return rsp.body();
    }
    // We got an error from auto-api.  Expose it
    String errMsg = "Not Available";
    try (var errBody = rsp.errorBody()) {
      if (errBody != null) {
        errMsg = errBody.string();
      }
    } catch (IOException ioe) {
      logger.trace(ioe);
    }

    if (rsp.code() >= 500) {
      throw new RetryableRuntimeException(
          "Unable to fetch necessary data from auto-api. status="
              + rsp.code()
              + ", msg= "
              + errMsg);
    }
    throw new RuntimeException(
        "Unable to fetch necessary data from auto-api.  status=" + rsp.code() + ", msg= " + errMsg);
  }

  static ApplauseAppPushConfig mergeCfg(
      @NonNull final ApplauseAppPushConfig cfg,
      @NonNull final AutoApiImagePushDataDto autoApiData) {
    cfg.setUserFromAutoApi(autoApiData.isHasUserName());
    cfg.setPasswdFromAutoApi(autoApiData.isHasApiKey());
    return cfg;
  }

  /**
   * Push to the provider going through the Applause Selenium Proxy
   *
   * @param featureConfig All information (minus the URLs for talking to Applause)
   * @param targetProvider app push target
   * @return the app returned by the provider
   * @throws MalformedURLException If the proxy url cannot be resolved
   * @throws URISyntaxException If the proxy url cannot be resolved
   */
  static String pushApplication(
      @NonNull final ApplauseAppPushConfig featureConfig,
      @NonNull final AppPushTarget targetProvider)
      throws MalformedURLException, URISyntaxException {
    // NOTE:  Need to call the init method early.  Some providers have dynamic URL a
    // that gets assembled via the init method
    final String pushSrc = featureConfig.appPushSource();
    if (pushSrc.startsWith("http://") || pushSrc.startsWith("https://")) {
      targetProvider.init(pushSrc, featureConfig);
    } else {
      targetProvider.init(new File(pushSrc), featureConfig);
    }
    final String targetUrl = targetProvider.getTargetUrl();
    logger.info("Pushing app to '{}'", targetUrl);
    AppPushProxyDto proxyPushData =
        new AppPushProxyDto(
            featureConfig.appPushProvider(),
            targetUrl,
            ApplauseEnvironmentConfigurationManager.INSTANCE.get().apiKey());
    final String encodedPushData = base64EncodePushData(proxyPushData);
    // Assemble the URL on the proxy that we need POST the data to
    final var proxyUrlTarget = buildProxyUrl();
    logger.info("proxyUrlTarget: " + proxyUrlTarget);
    Request.Builder bldr =
        new Request.Builder()
            .url(proxyUrlTarget)
            .addHeader(HttpHeaders.AUTHORIZATION, encodedPushData);
    final String httpMethod = targetProvider.httpMethod();
    final RequestBody httpBody = targetProvider.getRequestBody();
    if (HttpMethod.PUT.equalsIgnoreCase(httpMethod)) {
      bldr.put(httpBody);
    } else if (HttpMethod.POST.equalsIgnoreCase(httpMethod)) {
      bldr.post(httpBody);
    } else {
      // Right now, we do puts and posts
      throw new RuntimeException(
          "Unsupported HTTP method for provider="
              + targetProvider.getClass().getCanonicalName()
              + " = "
              + httpMethod);
    }
    Call call = providerClient.newCall(bldr.build());
    Response response;
    try {
      response = call.execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to execute", e);
    }
    AppPushHelper.verifyHttp200OrThrow(response, featureConfig, proxyUrlTarget.toString());
    return targetProvider.getApplicationValue(response);
  }

  static String proxyPushData2JsonStr(@NonNull final AppPushProxyDto data) {
    // Use Jackson (Not GSON) for encoding.  We tried Gson.  We ran into problems
    // When the data included '=', Gson encoded the '=' as unicode.  Jackson is the safer
    // serializer.
    try {
      return new ObjectMapper().writeValueAsString(data);
    } catch (JsonProcessingException jpe) {
      throw new RuntimeException("Unable to encode data for proxy", jpe);
    }
  }

  static String base64EncodePushData(@NonNull final AppPushProxyDto data) {
    final String asStr = proxyPushData2JsonStr(data);
    final byte[] asBytes = asStr.getBytes(StandardCharsets.UTF_8);
    return Base64.getUrlEncoder().encodeToString(asBytes);
  }

  static URL buildProxyUrl() throws MalformedURLException, URISyntaxException {
    URL seleniumGridUrl =
        new URI(ApplauseEnvironmentConfigurationManager.INSTANCE.get().seleniumProxyUrl()).toURL();
    return new HttpUrl.Builder()
        .scheme(seleniumGridUrl.getProtocol())
        .addPathSegments(PROXY_URI)
        .host(seleniumGridUrl.getHost())
        .port(seleniumGridUrl.getPort())
        .build()
        .url();
  }
}
