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

import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.framework.ContextManager;
import com.applause.auto.logging.ResultPropertyMap;
import com.applause.auto.reporting.ApplauseReporter;
import com.applause.auto.reporting.params.ApplauseResultCreation;
import com.applause.auto.reporting.params.ApplauseResultSubmission;
import com.applause.auto.testng.TestNgContextConnector;
import com.applause.auto.testng.dataprovider.DataProviderHelper;
import com.applause.auto.testng.testidentification.TestCaseIds;
import com.applause.auto.util.autoapi.TestResultEndStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * A TestNG TestListener for reporting test results to a TestRun created by the
 * ReportingSuiteListener.
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class ReportingTestListener extends TestListenerAdapter {

  private static final Logger logger = LogManager.getLogger(ReportingTestListener.class);

  /**
   * Triggered on start of test run
   *
   * @param context test context
   */
  @Override
  public void onStart(final ITestContext context) {
    logger.debug(
        "Starting <test> {} with thread count {} and parallel mode {}.",
        context.getCurrentXmlTest().getName(),
        context.getCurrentXmlTest().getThreadCount(),
        context.getCurrentXmlTest().getParallel().toString());
  }

  /**
   * Method called on the start of every test. Writes out a log marker to indicate that we are
   * starting a new test.
   *
   * @param testResult is the context of the current test
   */
  @Override
  public void onTestStart(final ITestResult testResult) {
    logger.info(
        String.format(
            "********* Starting Test : %s *********", testResult.getMethod().getMethodName()));
    if (!ApplauseEnvironmentConfigurationManager.INSTANCE.get().reportingEnabled()) {
      return;
    }
    final var testCaseStartRecord =
        ApplauseReporter.INSTANCE.createTestResult(
            testResult.getMethod().getId(),
            new ApplauseResultCreation(
                    testResult.getTestClass().getName()
                        + "."
                        + testResult.getMethod().getMethodName())
                .setTestCaseIterationTag(DataProviderHelper.getTestIterationTag(testResult))
                .setParameterString(DataProviderHelper.getParameterString(testResult))
                .addApplauseTestCaseIds(TestCaseIds.fromMethod(testResult).getApplauseTestCaseIds())
                .setTestRailCaseId(TestCaseIds.fromMethod(testResult).getTestRailTestCaseId())
                .addProviderSessionIds(
                    ContextManager.INSTANCE.getProviderSessionIdsForContext(
                        (String) testResult.getAttribute("contextId")))
                .setTestRunId((Long) ResultPropertyMap.getProperty("testRunId")));
    // Save the result ID to the TestNG test result.
    logger.info(
        "Saving testResultId ({}) to: {}",
        testCaseStartRecord.getTestResultId(),
        testResult.getName());
    testResult.setAttribute("testResultId", testCaseStartRecord.getTestResultId());
    ContextManager.INSTANCE
        .getCurrentContext()
        .ifPresent(context -> context.setConnector(new TestNgContextConnector(testResult)));
  }

  /**
   * Triggered on Test success
   *
   * @param testResult the test result
   */
  @Override
  public void onTestSuccess(final ITestResult testResult) {
    logger.info(
        String.format(
            "********* Test Pass : %s *********", testResult.getMethod().getMethodName()));
    submitResult(testResult, null, TestResultEndStatus.PASSED);
  }

  /**
   * Triggered on Test skipped
   *
   * @param testResult the result
   */
  @Override
  public void onTestSkipped(final ITestResult testResult) {
    logger.info(
        String.format(
            "********* Test Skipped : %s *********", testResult.getMethod().getMethodName()));

    if (testResult.getThrowable() != null) {
      logger.error(
          "********* Skipped test threw exception!!! *********", testResult.getThrowable());
    }
    submitResult(testResult, testResult.getThrowable().getMessage(), TestResultEndStatus.SKIPPED);
  }

  /**
   * Triggered on Test failure
   *
   * @param testResult the result
   */
  @Override
  public void onTestFailure(final ITestResult testResult) {
    logger.info(
        String.format(
            "********* Test Failed : %s *********", testResult.getMethod().getMethodName()));
    String failureReason = testResult.getThrowable().getMessage();
    logger.warn("testNg detected a test failure: " + failureReason, testResult.getThrowable());
    submitResult(testResult, failureReason, TestResultEndStatus.FAILED);
  }

  private void submitResult(
      final ITestResult result, final String failureReason, final TestResultEndStatus status) {
    if (!ApplauseEnvironmentConfigurationManager.INSTANCE.get().reportingEnabled()) {
      return;
    }
    ApplauseReporter.INSTANCE.submitTestResult(
        result.getMethod().getId(),
        new ApplauseResultSubmission(status)
            .setFailureReason(failureReason)
            .addProviderSessionIds(
                ContextManager.INSTANCE.getProviderSessionIdsForContext(
                    (String) result.getAttribute("contextId"))));
  }
}
