/*
 *
 * Copyright © 2025 Applause App Quality, Inc.
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

import com.applause.auto.helpers.jira.annotations.scanner.JiraAnnotationsScanner;
import com.applause.auto.helpers.jira.clients.JiraXrayClient;
import com.applause.auto.helpers.jira.dto.requestmappers.XrayAddTo;
import com.applause.auto.helpers.jira.dto.responsemappers.JiraCreateTicketResponse;
import com.applause.auto.helpers.jira.exceptions.JiraAnnotationException;
import com.applause.auto.helpers.jira.exceptions.UnidentifiedExecutionStatusException;
import com.applause.auto.testng.TestNgContextUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.lang.reflect.Method;
import java.util.Collections;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class XrayListener extends TestListenerAdapter {

  private static final Logger logger = LogManager.getLogger(XrayListener.class);
  private final JiraXrayClient jiraXrayClient = new JiraXrayClient();

  @Override
  public void onTestFailure(@NonNull final ITestResult result) {
    logger.info("Setting status for FAILED test: [{}]", result.getTestName());
    super.onTestFailure(result);
    updateJiraXray(result);
  }

  @Override
  public void onTestSuccess(@NonNull final ITestResult result) {
    logger.info("Setting status for PASSED test: [{}]", result.getTestName());
    super.onTestSuccess(result);
    updateJiraXray(result);
  }

  @Override
  public void onTestSkipped(@NonNull final ITestResult result) {
    logger.info("Setting status for SKIPPED test: [{}]", result.getTestName());
    super.onTestSkipped(result);
    updateJiraXray(result);
  }

  /**
   * Updates Jira X-Ray with execution results
   *
   * @param result test result
   */
  private void updateJiraXray(@NonNull final ITestResult result) {
    try {
      int testRunId = addTestToTestExecution(result);
      jiraXrayClient.getTestrunAPI().updateTestRun(testRunId, getExecutionStatus(result));
    } catch (JsonProcessingException | JiraAnnotationException e) {
      logger.error("Failed to add Test to Test Execution and update its execution status.", e);
    }
  }

  /**
   * Adds test to X-Ray Test Execution based on data passed as @JiraID annotation
   *
   * @param result test result
   * @return testRunId, String value that represents identifier of the test case executed under a
   *     Test Execution
   * @throws JsonProcessingException when response JSON is invalid
   */
  private int addTestToTestExecution(@NonNull final ITestResult result)
      throws JsonProcessingException, JiraAnnotationException {
    ITestContext context = result.getTestContext();
    final Pair<Class<?>, Method> classAndMethod =
        TestNgContextUtils.getUnderlyingClassAndMethod(result);
    final Method underlyingMethod = classAndMethod.getRight();
    String jiraTestIdentifier = JiraAnnotationsScanner.getJiraIdentifier(underlyingMethod);
    // Assuming testExecKey refers to Test Execution key (ticket ID) the current test is using which
    // must be created before execution of the test
    String testExecutionKey = context.getAttribute("testExecKey").toString();
    JiraCreateTicketResponse jiraCreateTicketResponseMapping =
        new JiraCreateTicketResponse(null, testExecutionKey, null);
    XrayAddTo xrayAddToMapping = new XrayAddTo(Collections.singletonList(jiraTestIdentifier));
    jiraXrayClient
        .getExecutionsAPI()
        .addTestToTestExecution(jiraCreateTicketResponseMapping, xrayAddToMapping);
    return jiraXrayClient
        .getTestrunAPI()
        .getTestRunID(jiraCreateTicketResponseMapping, jiraTestIdentifier);
  }

  /**
   * Gets test execution status based on the ITestResult and maps it to accepted X-Ray values
   *
   * @param result test result
   * @return status
   */
  private String getExecutionStatus(@NonNull final ITestResult result) {
    String testKey = result.getMethod().getMethodName();
    String status = "";
    logger.info("Getting execution status for test key: {}", testKey);
    int statusCode = result.getStatus();
    status =
        switch (statusCode) {
          case 1 -> "PASS";
          case 2 -> "FAIL";
          case 3 -> "BLOCKED";
          default ->
              throw new UnidentifiedExecutionStatusException(
                  String.format("Unable to determine status for code %s", statusCode));
        };
    return status;
  }
}
