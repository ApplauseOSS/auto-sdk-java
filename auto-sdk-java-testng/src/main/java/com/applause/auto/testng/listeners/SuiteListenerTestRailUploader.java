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
package com.applause.auto.testng.listeners;

import com.applause.auto.config.TestRailConfigBean;
import com.applause.auto.config.TestRailConfigurationManager;
import com.applause.auto.helpers.ApplauseConfigHelper;
import com.applause.auto.testng.TestRailConfigBeanMapper;
import com.applause.auto.testng.testidentification.TestCaseIds;
import com.applause.auto.testrail.client.TestRailResultUploader;
import com.applause.auto.testrail.client.TestRailResultUploader.ProjectConfiguration;
import com.applause.auto.testrail.client.enums.TestResultStatus;
import com.applause.auto.testrail.client.errors.TestRailException;
import com.applause.auto.testrail.client.models.config.TestRailConfig;
import com.applause.auto.util.autoapi.PublicAssetLinkDto;
import com.applause.auto.util.autoapi.TestResultAssetLinksDto;
import com.applause.auto.util.autoapi.TestRunAssetLinksDto;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.testng.ISuiteResult;
import org.testng.ITestResult;

/** Handles uploading the TestRail Results after a TestRail Suite Ends */
@Log4j2
public enum SuiteListenerTestRailUploader {
  ;

  private static final String TEST_RAIL_COMMENT_SECTION_SEPARATOR = "\n\n";

  private static TestRailResultUploader.UploadResultDto extractToResultDto(
      @NonNull final ITestResult testResult,
      @NonNull final TestResultStatus status,
      @Nullable final TestRunAssetLinksDto assetLinks) {
    final var testRailCaseId = TestCaseIds.fromMethod(testResult).getTestRailTestCaseId();
    if (Objects.isNull(testRailCaseId)) {
      return null;
    }
    return new TestRailResultUploader.UploadResultDto(
        testRailCaseId, status, buildResultComment(testResult, status, assetLinks));
  }

  /**
   * Sends the provided results to TestRail
   *
   * @param results The Map of results
   * @param assetLinks A collection of Asset Links
   */
  public static void sendResultsToTestRail(
      @NonNull final Map<String, ISuiteResult> results,
      @Nullable final TestRunAssetLinksDto assetLinks) {
    // permute the TestNG test results into the format uploader wants
    final var resultsToUpload =
        results.values().stream()
            .flatMap(
                result ->
                    Streams.concat(
                        result.getTestContext().getPassedTests().getAllResults().stream()
                            .map(
                                testResult ->
                                    extractToResultDto(
                                        testResult, TestResultStatus.PASSED, assetLinks)),
                        result.getTestContext().getFailedTests().getAllResults().stream()
                            .map(
                                testResult ->
                                    extractToResultDto(
                                        testResult, TestResultStatus.FAILED, assetLinks)),
                        result.getTestContext().getSkippedTests().getAllResults().stream()
                            .map(
                                testResult ->
                                    extractToResultDto(
                                        testResult, TestResultStatus.SKIPPED, assetLinks))))
            .filter(Objects::nonNull)
            .collect(ImmutableSet.toImmutableSet());

    // build the two needed config objects from the bean
    try {
      final TestRailConfigBean testRailConfigBean = TestRailConfigurationManager.INSTANCE.get();
      final ProjectConfiguration projectConfig =
          TestRailConfigBeanMapper.projectConfigFromBean(testRailConfigBean);
      final TestRailConfig testRailConfig =
          TestRailConfigBeanMapper.testRailConfigFromBean(testRailConfigBean);
      final var uploader =
          TestRailResultUploader.initialize(
              testRailConfig, projectConfig, ApplauseConfigHelper.getHttpProxy());
      uploader.uploadResults(resultsToUpload);
    } catch (TestRailException e) {
      log.error("error logging to TestRail, error status is " + e.getStatus(), e);
    } catch (Exception | Error e) {
      log.error("error logging to TestRail", e);
    }
  }

  /**
   * Build a result comment for a TestNG result
   *
   * @param testResult The TestNG result
   * @param status The Test Result Status
   * @param assetLinks The asset links for the completed test run
   * @return A String to be included as a comment on the TestRail result
   */
  public static String buildResultComment(
      @NonNull final ITestResult testResult,
      @NonNull final TestResultStatus status,
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
    final Long testResultId = (Long) testResult.getAttribute("testResultId");
    final var testResultAssetLinks =
        Optional.ofNullable(assetLinks)
            .map(TestRunAssetLinksDto::getTestResults)
            .orElse(Collections.emptyList())
            .stream()
            .filter(testResultAssets -> testResultAssets.getTestResultId().equals(testResultId))
            .findFirst()
            .orElse(new TestResultAssetLinksDto());

    // Append Failure/skip reason
    final var failureReason =
        Optional.ofNullable(testResult.getThrowable())
            .map(Throwable::getMessage)
            .orElse("[unavailable]");
    if (ITestResult.FAILURE == testResult.getStatus()) {
      // Only add failure screenshot if we have one
      if (Objects.nonNull(testResultAssetLinks.getFailureScreenshot())) {
        sb.append("## FAILURE SCREENSHOT\n")
            .append(buildEmbeddedAssetComment(testResultAssetLinks.getFailureScreenshot()))
            .append(TEST_RAIL_COMMENT_SECTION_SEPARATOR);
      }

      sb.append("## FAILURE REASON\n")
          .append(failureReason)
          .append(TEST_RAIL_COMMENT_SECTION_SEPARATOR);
    } else if (ITestResult.SKIP == testResult.getStatus()) {
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
}
