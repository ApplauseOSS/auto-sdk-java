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
package com.applause.auto.helpers.jira.listeners;

import com.applause.auto.helpers.jira.annotations.scanner.JiraAnnotationsScanner;
import com.applause.auto.helpers.jira.clients.JiraXrayClient;
import com.applause.auto.helpers.jira.dto.requestmappers.XrayAddTo;
import com.applause.auto.helpers.jira.dto.responsemappers.JiraCreateTicketResponse;
import com.applause.auto.helpers.jira.exceptions.JiraAnnotationException;
import com.applause.auto.helpers.jira.exceptions.UnidentifiedExecutionStatusException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class XrayListener extends TestListenerAdapter {

  private static final Logger logger = LogManager.getLogger(XrayListener.class);
  private JiraXrayClient jiraXrayClient = new JiraXrayClient();

  @Override
  public void onTestFailure(ITestResult result) {
    logger.info("Setting status for FAILED test: [{}]", result.getTestName());
    super.onTestFailure(result);
    updateJiraXray(result);
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    logger.info("Setting status for PASSED test: [{}]", result.getTestName());
    super.onTestSuccess(result);
    updateJiraXray(result);
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    logger.info("Setting status for SKIPPED test: [{}]", result.getTestName());
    super.onTestSkipped(result);
    updateJiraXray(result);
  }

  /**
   * Updates Jira X-Ray with execution results
   *
   * @param result
   */
  private void updateJiraXray(ITestResult result) {
    try {
      int testRunId = addTestToTestExecution(result);
      jiraXrayClient.getTestrunAPI().updateTestRun(testRunId, getExecutionStatus(result));
    } catch (JsonProcessingException | JiraAnnotationException e) {
      logger.error("Failed to add Test to Test Execution and update its execution status.");
      e.printStackTrace();
    }
  }

  /**
   * Adds test to X-Ray Test Execution based on data passed as @JiraID annotation
   *
   * @param result
   * @return testRunId, String value that represents identifier of the test case executed under a
   *     Test Execution
   * @throws JsonProcessingException
   */
  private int addTestToTestExecution(ITestResult result)
      throws JsonProcessingException, JiraAnnotationException {
    JiraCreateTicketResponse jiraCreateTicketResponseMapping = new JiraCreateTicketResponse();
    ITestContext context = result.getTestContext();
    String jiraTestIdentifier = JiraAnnotationsScanner.getJiraIdentifier(result);
    // Assuming testExecKey refers to Test Execution key (ticket ID) the current test is using which
    // must be created before execution of the test
    String testExecutionKey = context.getAttribute("testExecKey").toString();
    jiraCreateTicketResponseMapping.setKey(testExecutionKey);
    XrayAddTo xrayAddToMapping = new XrayAddTo();
    xrayAddToMapping.setAdd(Arrays.asList(jiraTestIdentifier));
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
   * @param result
   * @return status
   */
  private String getExecutionStatus(ITestResult result) {
    String testKey = result.getMethod().getMethodName();
    String status = "";
    logger.info("Getting execution status for test key: {}", testKey);
    int statusCode = result.getStatus();
    switch (statusCode) {
      case 1:
        status = "PASS";
        break;
      case 2:
        status = "FAIL";
        break;
      case 3:
        status = "BLOCKED";
        break;
      default:
        throw new UnidentifiedExecutionStatusException(
            String.format("Unable to determine status for code %s", statusCode));
    }
    return status;
  }
}
