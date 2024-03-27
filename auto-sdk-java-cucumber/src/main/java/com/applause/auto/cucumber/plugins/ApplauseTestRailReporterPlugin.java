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
package com.applause.auto.cucumber.plugins;

import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.config.TestRailConfigBean;
import com.applause.auto.config.TestRailConfigurationManager;
import com.applause.auto.cucumber.utils.CucumberUtils;
import com.applause.auto.cucumber.utils.TestRailConfigBeanMapper;
import com.applause.auto.helpers.ApplauseConfigHelper;
import com.applause.auto.helpers.SyncHelper;
import com.applause.auto.reporting.ApplauseReporter;
import com.applause.auto.reporting.ApplauseReporterState;
import com.applause.auto.testrail.client.TestRailResultUploader;
import com.applause.auto.testrail.client.TestRailResultUploader.ProjectConfiguration;
import com.applause.auto.testrail.client.TestRailResultUploader.UploadResultDto;
import com.applause.auto.testrail.client.enums.TestResultStatus;
import com.applause.auto.testrail.client.errors.TestRailException;
import com.applause.auto.testrail.client.models.config.TestRailConfig;
import com.applause.auto.util.autoapi.PublicAssetLinkDto;
import com.applause.auto.util.autoapi.TestResultAssetLinksDto;
import com.applause.auto.util.autoapi.TestRunAssetLinksDto;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

/** A Cucumber Plugin to handle TestRail Reporting */
@Log4j2
public class ApplauseTestRailReporterPlugin implements ConcurrentEventListener {
  private static final RetryPolicy<?> testRailUploadRetryPolicy =
      RetryPolicy.builder()
          .handle(RuntimeException.class)
          .withBackoff(500, 5000, ChronoUnit.MILLIS)
          .build();
  private final Map<String, UploadResultDto> testRailResults = new HashMap<>();
  private static final String TEST_RAIL_COMMENT_SECTION_SEPARATOR = "\n\n";

  @Synchronized
  private void onTestCaseFinished(final TestCaseFinished event) {
    final var testRailCaseId =
        CucumberUtils.getTestRailTestCaseIdsFromTestCase(event.getTestCase());
    if (testRailCaseId == null) {
      return;
    }
    final var testCaseId = event.getTestCase().getId().toString();
    this.testRailResults.put(
        testCaseId,
        new UploadResultDto(
            testRailCaseId,
            CucumberUtils.cucumberStatusToTestRailStatus(event.getResult().getStatus()),
            ""));
  }

  /**
   * When the Cucumber TestRun is finished, handle uploading all results to TestRail
   *
   * @param event The Cucumber event emitted when the run is finished
   */
  public void onTestRunFinished(final TestRunFinished event) {
    this.waitForApplauseReportingToFinish();
    if (EnvironmentConfigurationManager.INSTANCE.get().sdkTestRailResultSubmissionEnabled()) {
      Failsafe.with(testRailUploadRetryPolicy)
          .onFailure(
              exception ->
                  log.error("failed to upload results to TestRail at end of suite {}", exception))
          .run(this::handleUpload);
    }
  }

  private void handleUpload() throws TestRailException {
    final TestRailConfigBean testRailConfigBean = TestRailConfigurationManager.INSTANCE.get();
    final ProjectConfiguration projectConfig =
        TestRailConfigBeanMapper.projectConfigFromBean(testRailConfigBean);
    final TestRailConfig testRailConfig =
        TestRailConfigBeanMapper.testRailConfigFromBean(testRailConfigBean);
    final var uploader =
        TestRailResultUploader.initialize(
            testRailConfig, projectConfig, ApplauseConfigHelper.getHttpProxy());
    final Set<UploadResultDto> results;
    if (ApplauseEnvironmentConfigurationManager.INSTANCE.get().reportingEnabled()) {
      final var applauseResultRecords = ApplauseReporter.INSTANCE.getResults();
      final var applauseAssets = ApplauseReporter.INSTANCE.getAssets();
      results =
          this.testRailResults.entrySet().stream()
              .map(
                  entry -> {
                    if (!applauseResultRecords.containsKey(entry.getKey())) {
                      return new UploadResultDto(
                          entry.getValue().testCaseId(),
                          entry.getValue().status(),
                          buildResultComment(
                              null, null, entry.getValue().status(), applauseAssets));
                    }
                    final var record = applauseResultRecords.get(entry.getKey());
                    return new UploadResultDto(
                        entry.getValue().testCaseId(),
                        entry.getValue().status(),
                        buildResultComment(
                            record.getTestResultId(),
                            null,
                            entry.getValue().status(),
                            applauseAssets));
                  })
              .collect(Collectors.toSet());
    } else {
      results =
          this.testRailResults.values().stream()
              .map(
                  uploadResultDto ->
                      new UploadResultDto(
                          uploadResultDto.testCaseId(),
                          uploadResultDto.status(),
                          buildResultComment(null, null, uploadResultDto.status(), null)))
              .collect(Collectors.toSet());
    }
    uploader.uploadResults(results);
  }

  @Override
  public void setEventPublisher(final EventPublisher publisher) {
    if (EnvironmentConfigurationManager.INSTANCE.get().sdkTestRailResultSubmissionEnabled()) {
      publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
      publisher.registerHandlerFor(TestRunFinished.class, this::onTestRunFinished);
    }
  }

  private static String buildResultComment(
      final @Nullable Long testResultId,
      final @Nullable String failureReason,
      final @NonNull TestResultStatus status,
      @Nullable final TestRunAssetLinksDto assetLinks) {
    StringBuilder sb = new StringBuilder();
    // Header Info, run/session id
    sb.append("## Applause RunID: ")
        .append(
            Optional.ofNullable(assetLinks)
                .map(TestRunAssetLinksDto::getTestRunId)
                .map(Object::toString)
                .orElse("[unavailable]"))
        .append(TEST_RAIL_COMMENT_SECTION_SEPARATOR);
    final var testResultAssetLinks =
        Optional.ofNullable(assetLinks)
            .map(TestRunAssetLinksDto::getTestResults)
            .orElse(Collections.emptyList())
            .stream()
            .filter(testResultAssets -> testResultAssets.getTestResultId().equals(testResultId))
            .findFirst()
            .orElse(new TestResultAssetLinksDto());

    // Append Failure/skip reason
    if (status.equals(TestResultStatus.FAILED)) {
      // Only add failure screenshot if we have one
      if (Objects.nonNull(testResultAssetLinks.getFailureScreenshot())) {
        sb.append("## FAILURE SCREENSHOT\n")
            .append(buildEmbeddedAssetComment(testResultAssetLinks.getFailureScreenshot()))
            .append(TEST_RAIL_COMMENT_SECTION_SEPARATOR);
      }

      sb.append("## FAILURE REASON\n")
          .append(failureReason)
          .append(TEST_RAIL_COMMENT_SECTION_SEPARATOR);
    } else if (status.equals(TestResultStatus.SKIPPED)) {
      sb.append("## SKIPPED REASON\n")
          .append(failureReason)
          .append(TEST_RAIL_COMMENT_SECTION_SEPARATOR);
    }

    // Provider Session Specific Asset Section
    for (final var providerSessionAssets : testResultAssetLinks.getProviderSessions()) {
      sb.append("\n---------------------\n")
          .append("\n## Provider SessionID: ")
          .append(providerSessionAssets.getProviderSessionGuid())
          .append("\n\n");

      for (final var link : providerSessionAssets.getAssets()) {
        sb.append(buildAssetComment(link)).append('\n');
      }
    }

    return sb.toString();
  }

  // Builds an embedded comment string for a given asset with a given name
  private static String buildEmbeddedAssetComment(final PublicAssetLinkDto asset) {
    if (Objects.isNull(asset)) {
      return "";
    }
    return String.format("![%s](%s)", asset.getAssetName(), asset.getAssetPublicUrl());
  }

  // Builds a comment string for a given asset
  static String buildAssetComment(final PublicAssetLinkDto asset) {
    if (Objects.isNull(asset)) {
      return "";
    }
    return String.format("[%s](%s)", asset.getAssetName(), asset.getAssetPublicUrl());
  }

  private void waitForApplauseReportingToFinish() {
    if (!ApplauseEnvironmentConfigurationManager.INSTANCE.get().reportingEnabled()) {
      return;
    }
    log.info("Waiting for Applause reporting to finish");
    for (int i = 0; i < 10; i++) {
      if (ApplauseReporter.INSTANCE.getState() == ApplauseReporterState.ENDED) {
        break;
      }
      SyncHelper.sleep(30000);
    }
  }
}
