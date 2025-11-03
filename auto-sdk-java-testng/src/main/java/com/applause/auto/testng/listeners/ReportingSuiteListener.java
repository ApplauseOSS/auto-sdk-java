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

import com.applause.auto.config.*;
import com.applause.auto.data.enums.ResultsMode;
import com.applause.auto.helpers.*;
import com.applause.auto.integrations.assets.AssetsUtil;
import com.applause.auto.reporting.ApplauseReporter;
import com.applause.auto.reporting.config.ApplauseReporterConfig;
import com.applause.auto.reporting.config.ApplauseTestCycleReportingConfig;
import com.applause.auto.reporting.config.ApplauseTestRailConfig;
import com.applause.auto.reporting.params.ApplauseRunCreation;
import com.applause.auto.reporting.params.ApplauseRunEnd;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestResult;

/** A TestNG Suite Listener implementation for starting and ending an Applause TestRun. */
public class ReportingSuiteListener implements ISuiteListener {
  private static final Logger logger = LogManager.getLogger(ReportingSuiteListener.class);

  /**
   * Triggered on start of test run
   *
   * @param suite test context
   */
  @Override
  public void onStart(final ISuite suite) {
    // Verify that we are actually able to run
    final Set<String> allTestMethodNames =
        suite.getAllMethods().stream()
            .map(method -> method.getTestClass().getName() + "." + method.getMethodName())
            .collect(Collectors.toSet());
    if (allTestMethodNames.isEmpty()) {
      throw new RuntimeException(
          String.format(
              "Found 0 tests to run. TestNG groups='%s', Check the 'groups' parameter and the suiteFile='%s'",
              String.join(",", suite.getMethodsByGroups().keySet()),
              suite.getXmlSuite().getFileName()));
    }

    // Configure the reporter if reporting is enabled
    final ApplauseSdkConfigBean applauseConfigBean =
        ApplauseEnvironmentConfigurationManager.INSTANCE.get();
    if (!applauseConfigBean.reportingEnabled()) {
      return;
    }
    ApplauseReporter.INSTANCE.configure(
        new ApplauseReporterConfig(
            applauseConfigBean.autoApiUrl(),
            applauseConfigBean.apiKey(),
            applauseConfigBean.productId(),
            ApplauseConfigHelper.getHttpProxy()));
    if (applauseConfigBean.applauseTestCycleId() != null) {
      ApplauseReporter.INSTANCE.enableTestCycleReporting(
          new ApplauseTestCycleReportingConfig(
              applauseConfigBean.applauseTestCycleId(), applauseConfigBean.applauseTestRunName()));
    }

    final SdkConfigBean sdkConfigBean = EnvironmentConfigurationManager.INSTANCE.get();
    final TestRailConfigBean testRailConfigBean = TestRailConfigurationManager.INSTANCE.get();

    if (applauseConfigBean.testRailReportingEnabled()
        && !sdkConfigBean.sdkTestRailResultSubmissionEnabled()) {
      ApplauseReporter.INSTANCE.enableTestRail(
          new ApplauseTestRailConfig(
              testRailConfigBean.addAllTestsToPlan(),
              testRailConfigBean.testRailProjectId(),
              testRailConfigBean.testRailSuiteId(),
              testRailConfigBean.testRailPlanName(),
              testRailConfigBean.testRailRunName()));
    }

    // Finalize the configuration of the reporter
    ApplauseReporter.INSTANCE.startTestRun(
        new ApplauseRunCreation()
            .addPrecreatedResults(allTestMethodNames)
            .setDriverConfig(sdkConfigBean.capsFile()));
  }

  /**
   * At this point, all tests have been run. Which means, if we're running client- or server-side,
   * we need to let the API know that this Test Session is complete.
   *
   * @param suite test context
   */
  @Override
  public void onFinish(final ISuite suite) {
    if (!ApplauseEnvironmentConfigurationManager.INSTANCE.get().reportingEnabled()) {
      return;
    }
    ApplauseReporter.INSTANCE.endTestRun(new ApplauseRunEnd());
    if (suite.getAllMethods().isEmpty()) {
      logger.error("No tests found, check your configuration");
    }
    ResultsMode resultsMode =
        ApplauseEnvironmentConfigurationManager.INSTANCE.get().downloadResults();
    if (resultsMode == ResultsMode.ALWAYS
        || resultsMode == ResultsMode.ON_FAILURE
            && suite.getAllInvokedMethods().stream()
                .anyMatch(m -> m.getTestResult().getStatus() == ITestResult.FAILURE)) {
      logger.info("ResultsMode [{}]. Polling until results bundle is available.", resultsMode);
      AssetsUtil.waitForTestRunAssets(ApplauseReporter.INSTANCE.getTestRunId());
    } else {
      logger.info("ResultsMode [{}]. Skipping download of results bundle.", resultsMode);
    }
  }
}
