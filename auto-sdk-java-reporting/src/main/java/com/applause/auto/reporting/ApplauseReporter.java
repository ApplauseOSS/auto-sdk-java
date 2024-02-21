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

import com.applause.auto.logging.ResultPropertyMap;
import com.applause.auto.reporting.config.ApplauseReporterConfig;
import com.applause.auto.reporting.config.ApplauseTestCycleReportingConfig;
import com.applause.auto.reporting.config.ApplauseTestRailConfig;
import com.applause.auto.reporting.params.ApplauseResultCreation;
import com.applause.auto.reporting.params.ApplauseResultSubmission;
import com.applause.auto.reporting.params.ApplauseRunCreation;
import com.applause.auto.reporting.params.ApplauseRunEnd;
import com.applause.auto.util.autoapi.TestRunAssetLinksDto;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;

/** Singleton reporter implementation that handles only a single run/session */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public final class ApplauseReporter implements IApplauseReporter, ISessionInitializer {
  /** The Singleton Instance of the Applause Reporter */
  public static final ApplauseReporter INSTANCE = new ApplauseReporter();

  private ApplauseReporterConfig configuration;
  private ApplauseTestRailConfig testRailConfig;
  private ApplauseTestCycleReportingConfig testCycleConfig;

  // Handles the state of the reporter
  @Getter private ApplauseReporterState state = ApplauseReporterState.CONFIGURING;

  // Once the session is started, handles the actual session management
  private IApplauseReporter delegate;

  /**
   * Configures the ApplauseReporter. This is a necessary step to set up the reporter settings. The
   * configuration is then modifiable until the session is created.
   *
   * @param config The config to configure
   * @return The configuration
   */
  @Synchronized
  public ApplauseReporterConfig configure(final @NonNull ApplauseReporterConfig config) {
    if (!ApplauseReporterState.CONFIGURATION_STATES.contains(state)) {
      throw new RuntimeException("Cannot configure reporter when in state: " + state);
    }
    this.configuration = config;
    INSTANCE.state = ApplauseReporterState.CONFIGURED;
    return INSTANCE.configuration;
  }

  /**
   * Enables TestRail configuration. This optional configuration can only be set before the start of
   * the session. Passing a null value will disable TestRail reporting
   *
   * @param config The config to configure
   * @return The TestRail Configuration
   */
  @Synchronized
  public ApplauseTestRailConfig enableTestRail(final @Nullable ApplauseTestRailConfig config) {
    if (!ApplauseReporterState.CONFIGURATION_STATES.contains(state)) {
      throw new RuntimeException("Cannot configure testrail properties when in state: " + state);
    }
    this.testRailConfig = config;
    return this.testRailConfig;
  }

  /**
   * Enables the Applause Test Cycle Reporting. This optional configuration can only be set before
   * the start of the session. Passing a null value will disable TestCycle reporting
   *
   * @param config The config to configure
   * @return The Test cycle reporting configuration object
   */
  @Synchronized
  public ApplauseTestCycleReportingConfig enableTestCycleReporting(
      final @Nullable ApplauseTestCycleReportingConfig config) {
    if (!ApplauseReporterState.CONFIGURATION_STATES.contains(state)) {
      throw new RuntimeException(
          "Cannot configure test cycle properties when in state: " + state.toString());
    }
    this.testCycleConfig = config;
    return this.testCycleConfig;
  }

  /**
   * Starts a new TestRun using the parameters passed in.
   *
   * @param params The run creation parameters
   * @return The ApplauseReporter instance.
   */
  @Override
  @Synchronized
  public ApplauseReporter startTestRun(final @NonNull ApplauseRunCreation params) {
    if (INSTANCE.state != ApplauseReporterState.CONFIGURED) {
      throw new RuntimeException(
          "Cannot start test run when in state: " + INSTANCE.state.toString());
    }
    this.delegate =
        new SessionInitializer(configuration, testRailConfig, testCycleConfig).startTestRun(params);
    this.state = ApplauseReporterState.STARTED;
    ResultPropertyMap.setGlobalProperty("testRunId", this.delegate.getTestRunId());
    return this;
  }

  /**
   * Ends the TestRun using the parameters passed in.
   *
   * @param params The run end parameters
   * @return The ApplauseReporter instance.
   */
  @Override
  @Synchronized
  public IApplauseReporter endTestRun(final @NonNull ApplauseRunEnd params) {
    if (state != ApplauseReporterState.STARTED) {
      throw new RuntimeException("Cannot end test run when in state: " + state.toString());
    }
    this.delegate.endTestRun(params);
    this.state = ApplauseReporterState.ENDED;
    return this;
  }

  /**
   * Creates a new test result with the given parameters
   *
   * @param params The result creation params
   * @return The result record for the newly created result
   */
  @Override
  public ResultRecord createTestResult(
      final @NonNull String testResultUuid, final @NonNull ApplauseResultCreation params) {
    if (state != ApplauseReporterState.STARTED) {
      throw new RuntimeException("Cannot create test result when in state: " + state.toString());
    }
    return this.delegate.createTestResult(testResultUuid, params);
  }

  /**
   * Submits an existing test result with the given parameters
   *
   * @param params The result submission params
   * @return The updated result record for the submitted result
   */
  @Override
  public ResultRecord submitTestResult(
      final @NonNull String testResultUuid, final @NonNull ApplauseResultSubmission params) {
    return delegate.submitTestResult(testResultUuid, params);
  }

  /**
   * After the run is ended, fetch the asset links for the run
   *
   * @return The asset links generated by this session
   */
  @Override
  public TestRunAssetLinksDto getAssets() {
    if (state != ApplauseReporterState.ENDED) {
      throw new RuntimeException("Cannot get asset links until the run is ended");
    }
    return delegate.getAssets();
  }

  @Override
  public Long getTestRunId() {
    if (ApplauseReporterState.CONFIGURATION_STATES.contains(state)) {
      throw new RuntimeException("Cannot get test run id until the run is started");
    }
    return delegate.getTestRunId();
  }

  @Override
  public Map<String, ResultRecord> getResults() {
    return delegate.getResults();
  }
}
