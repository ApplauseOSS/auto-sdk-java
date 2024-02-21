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
package com.applause.auto.reporting;

import com.applause.auto.data.enums.ContextType;
import com.applause.auto.reporting.config.ApplauseReporterConfig;
import com.applause.auto.reporting.config.ApplauseTestCycleReportingConfig;
import com.applause.auto.reporting.config.ApplauseTestRailConfig;
import com.applause.auto.reporting.params.ApplauseResultCreation;
import com.applause.auto.reporting.params.ApplauseResultSubmission;
import com.applause.auto.reporting.params.ApplauseRunEnd;
import com.applause.auto.util.autoapi.*;
import com.google.common.base.Functions;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import retrofit2.Response;

/** Handles a reporting logic for a single session */
@Log4j2
public final class SessionHandler implements IApplauseReporter {
  private static final Executor executor = Executors.newSingleThreadScheduledExecutor();
  private final @NonNull ApplauseReporterConfig config;
  private final @Nullable ApplauseTestRailConfig testRailConfig;
  private final @Nullable ApplauseTestCycleReportingConfig testCycleConfig;
  private final @NonNull AutoApi autoApi;
  @Getter private final @NonNull Long testRunId;
  private @Getter TestRunAssetLinksDto assets;
  private final ConcurrentHashMap<String, ResultRecord> resultRecords = new ConcurrentHashMap<>();

  private final SdkHeartBeatService heartBeatService;

  SessionHandler(
      final @NonNull ApplauseReporterConfig config,
      final @Nullable ApplauseTestRailConfig testRailConfig,
      final @Nullable ApplauseTestCycleReportingConfig testCycleConfig,
      final long testRunId) {
    this.config = config;
    this.testRailConfig = testRailConfig;
    this.testCycleConfig = testCycleConfig;
    this.autoApi =
        AutoApiClient.getClient(config.getAutoApiUrl(), config.getApiKey(), config.getProxy());
    this.testRunId = testRunId;
    this.heartBeatService = new SdkHeartBeatService(testRunId, this.autoApi);
    this.heartBeatService.addListener(new SdkHeartbeatListener(), executor);
    this.heartBeatService.startAsync();
    try {
      this.heartBeatService.awaitRunning(Duration.ofMinutes(1));
    } catch (TimeoutException e) {
      log.warn("SDK Heartbeat startup timed out. This may cause session timeouts.");
    }
    ShutdownHandler sdh = ShutdownHandler.getInstance();
    sdh.setTestRunId(testRunId);
    sdh.setClient(this.autoApi);
  }

  @Override
  public IApplauseReporter endTestRun(final @NonNull ApplauseRunEnd params) {
    Response<TestRunAssetLinksDto> response =
        autoApi.updateTestRunStatus(testRunId, TestRunStatus.COMPLETE).join();
    if (!response.isSuccessful()) {
      log.error("error updating test run " + testRunId + " to complete.");
    }
    this.assets = response.body();
    this.heartBeatService.stopAsync();
    try {
      this.heartBeatService.awaitTerminated(Duration.ofMinutes(1));
    } catch (TimeoutException e) {
      log.warn("SDK Heartbeat close timed out...");
    }
    ShutdownHandler.getInstance().setNormalExit(true);
    return this;
  }

  @Override
  public ResultRecord createTestResult(
      final @NonNull String testResultUuid, final @NonNull ApplauseResultCreation params) {
    final CreateTestResultParamDto updateMsg =
        new CreateTestResultParamDto(
            params.getTestRailCaseId(),
            params.getTestCaseName(),
            params.getTestCaseIterationTag(),
            params.getParameterString(),
            params.getApplauseTestCaseIds(),
            params.getProviderSessionIds(),
            params.getTestRunId());
    Response<CreateTestResultDto> testResultInfo = autoApi.createTestResult(updateMsg).join();
    if (!testResultInfo.isSuccessful() || testResultInfo.body() == null) {
      throw new RuntimeException("could not create a test result for " + params.getTestCaseName());
    }
    this.checkForTestRailInconsistency(params.getTestRailCaseId(), testResultInfo.body());
    this.checkForTestCycleInconsistency(params.getApplauseTestCaseIds(), testResultInfo.body());
    return this.setupResultRecord(testResultUuid, params, testResultInfo.body());
  }

  @Override
  public ResultRecord submitTestResult(
      final @NonNull String testResultUuid, final @NonNull ApplauseResultSubmission params) {
    final var record = this.resultRecords.get(testResultUuid);
    Response<Void> response =
        autoApi
            .submitTestResult(
                new TestResultParamDto(
                    record.getTestResultId(),
                    params.getStatus(),
                    params.getFailureReason(),
                    params.getProviderSessionIds()))
            .join();
    if (!response.isSuccessful()) {
      throw new RuntimeException(
          "error calling post test result endpoint for test result " + record.getTestResultId());
    }
    return record.setStatus(params.getStatus()).setSubmitted(true);
  }

  private ResultRecord setupResultRecord(
      final @NonNull String testResultUuid,
      final @NonNull ApplauseResultCreation params,
      final @NonNull CreateTestResultDto resultDto) {
    final var record =
        new ResultRecord(testResultUuid)
            .setTestResultId(resultDto.getTestResultId())
            .setApplauseTestCaseId(resultDto.getApplauseTestCaseId())
            .setContextType(
                params.getProviderSessionIds().isEmpty()
                    ? ContextType.DRIVERLESS
                    : ContextType.DRIVER)
            .setParameterString(params.getParameterString())
            .setProviderSessionIds(params.getProviderSessionIds())
            .setTestCaseIterationTag(params.getTestCaseIterationTag())
            .setTestCaseName(params.getTestCaseName())
            .setTestRailCaseId(resultDto.getTestRailTestCaseId());
    resultRecords.put(testResultUuid, record);
    return record;
  }

  private void checkForTestRailInconsistency(
      final @Nullable String requestedId, final @NonNull CreateTestResultDto result) {
    if (testRailConfig != null && requestedId != null && result.getTestRailTestCaseId() == null) {
      // We requested a TestRail case id and got back null
      log.warn(
          "The result of this test WILL NOT be persisted to TestRail due to an invalid"
              + " TestRail case ID. Please check this @Test annotation's description "
              + "and ensure it's of the format '1234' or 'C1234' for TestRail "
              + "testRailId="
              + requestedId);
    }
  }

  private void checkForTestCycleInconsistency(
      final @Nullable Set<String> requestedCaseIds, final @NonNull CreateTestResultDto result) {
    if (testCycleConfig != null
        && CollectionUtils.isNotEmpty(requestedCaseIds)
        && result.getApplauseTestCaseId() == null) {
      // We requested at least one Applause Test Case ID and got back none.
      log.warn(
          "The result of this test WILL NOT be persisted to Applause due to an invalid"
              + " Applause case ID. Please check this @ApplauseTestCaseId annotation "
              + "and ensure it contains a valid id that matches the given product"
              + "applauseTestCaseIds="
              + String.join(", ", requestedCaseIds)
              + ", productId= "
              + config.getProductId());
    }
  }

  @Override
  public Map<String, ResultRecord> getResults() {
    return this.resultRecords.values().stream()
        .collect(Collectors.toMap(ResultRecord::getTestResultUuid, Functions.identity()));
  }
}
