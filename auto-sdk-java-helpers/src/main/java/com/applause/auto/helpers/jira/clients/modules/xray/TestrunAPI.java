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
package com.applause.auto.helpers.jira.clients.modules.xray;

import static com.applause.auto.helpers.jira.constants.XrayEndpoints.*;
import static com.applause.auto.helpers.jira.helper.FilesHelper.*;
import static com.applause.auto.helpers.jira.helper.ResponseValidator.checkResponseInRange;
import static com.applause.auto.helpers.jira.restclient.XrayRestAssuredClient.getRestClient;

import com.applause.auto.helpers.jira.dto.requestmappers.StepIterationAttachment;
import com.applause.auto.helpers.jira.dto.responsemappers.JiraCreateTicketResponse;
import com.applause.auto.helpers.jira.dto.responsemappers.XrayTestRunDetails;
import com.applause.auto.helpers.jira.dto.responsemappers.iteration.TestRunIteration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import java.io.IOException;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestrunAPI {

  private static final Logger logger = LogManager.getLogger(TestrunAPI.class);
  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Get Test Run ID which can be used in updating Test status in a certain Test Execution
   *
   * @param jiraCreateTicketResponseMapping, to get value of testExecKey,
   * @param testKey,
   * @return testRunID,
   * @throws JsonProcessingException
   */
  public int getTestRunID(JiraCreateTicketResponse jiraCreateTicketResponseMapping, String testKey)
      throws JsonProcessingException {
    Response response = getTestRunIdOfTestFromExecution(jiraCreateTicketResponseMapping, testKey);
    XrayTestRunDetails xrayTestRunDetailsMapping =
        mapper.readValue(response.asString(), XrayTestRunDetails.class);
    checkResponseInRange(response, Range.between(200, 300), "Collecting Test Run ID");
    return xrayTestRunDetailsMapping.getId();
  }

  /**
   * Updates Test Run status,
   *
   * @param testRunId,
   * @param statusToUpdate: Can be extracted after test execution from ITestResult using its
   *     getStatus() and then converted into one of the following values accepted by X-Ray as per
   *     project needs:
   *     <p>EXECUTING – Test is being executed; this is a non-final status;
   *     <p>FAIL – Test failed
   *     <p>ABORTED – Test was aborted
   *     <p>PASS – Test passed successfully
   */
  public void updateTestRun(int testRunId, String statusToUpdate) {
    Response response = putTestRunStatus(testRunId, statusToUpdate);
    checkResponseInRange(response, Range.between(200, 300), "Update test run");
  }

  /**
   * Get Test Run information
   *
   * @param testRunId
   * @return XrayTestRunDetails object
   */
  public XrayTestRunDetails getTestRunData(int testRunId) throws JsonProcessingException {
    Response response = getTestRunBasedOnID(testRunId);
    XrayTestRunDetails xrayTestRunDetailsMapping =
        mapper.readValue(response.asString(), XrayTestRunDetails.class);
    checkResponseInRange(response, Range.between(200, 300), "Get Test Run Data");
    return xrayTestRunDetailsMapping;
  }

  /**
   * Get Test Run Iteration information
   *
   * @param testRunId
   * @param iterationId
   * @return TestRunIteration object
   */
  public TestRunIteration getTestRunIterationData(int testRunId, int iterationId)
      throws JsonProcessingException {
    Response response = getTestRunIterationBasedOnID(testRunId, iterationId);
    TestRunIteration testRunIteration =
        mapper.readValue(response.asString(), TestRunIteration.class);
    checkResponseInRange(response, Range.between(200, 300), "Get Test Run Iteration Data");
    return testRunIteration;
  }

  /**
   * Upload Test Run attachment
   *
   * @param testRunId
   * @param filePath - path to file
   */
  public void uploadTestRunAttachment(int testRunId, String filePath) throws IOException {
    Response response = postTestRunAttachment(testRunId, filePath);
    checkResponseInRange(response, Range.between(200, 300), "Upload Test Run attachment");
  }

  /**
   * Post comment to Test Run
   *
   * @param testRunId
   * @param comment
   */
  public void postTestRunComment(int testRunId, String comment) {
    Response response = postComment(testRunId, comment);
    checkResponseInRange(response, Range.between(200, 300), "Posting Test Run comment");
  }

  private Response getTestRunIdOfTestFromExecution(
      JiraCreateTicketResponse jiraCreateTicketResponseMapping, String testKey) {
    logger.info("Getting X-Ray Test Run ID for test: {}", testKey);
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint
        .append(TEST_RUN)
        .append("?")
        .append(testExecIssueKeyParam)
        .append(jiraCreateTicketResponseMapping.getKey())
        .append("&")
        .append(testIssueKeyParam)
        .append(testKey);
    return getRestClient().given().when().get(apiEndpoint.toString()).then().extract().response();
  }

  private Response putTestRunStatus(int testRunId, String statusToUpdate) {
    logger.info("Updating X-Ray Test Run: {} with status: {}", testRunId, statusToUpdate);
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint
        .append(TEST_RUN + "/")
        .append(testRunId)
        .append("/")
        .append(STATUS)
        .append("?")
        .append(statusParam)
        .append(statusToUpdate);
    return getRestClient().given().when().put(apiEndpoint.toString()).then().extract().response();
  }

  private Response getTestRunBasedOnID(int testRunId) {
    logger.info("Getting X-Ray Test Run response for ID: {}", testRunId);
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint.append(TEST_RUN).append("/").append(testRunId);
    return getRestClient().given().when().get(apiEndpoint.toString()).then().extract().response();
  }

  private Response getTestRunIterationBasedOnID(int testRunId, int iterationId) {
    logger.info("Getting X-Ray Test Run {} iteration response for ID: {}", testRunId, iterationId);
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint
        .append(TEST_RUN)
        .append("/")
        .append(testRunId)
        .append("/")
        .append(ITERATION)
        .append("/")
        .append(iterationId);
    return getRestClient().given().when().get(apiEndpoint.toString()).then().extract().response();
  }

  private Response postTestRunAttachment(int testRunId, String filePath) throws IOException {
    logger.info("Attaching {} to X-Ray Test Run {}", filePath, testRunId);
    StepIterationAttachment stepIterationAttachment = new StepIterationAttachment();
    stepIterationAttachment.setData(encodeBase64File(filePath));
    stepIterationAttachment.setFilename(getFileNameFromPath(filePath));
    stepIterationAttachment.setContentType(getFileType(filePath));
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint.append(TEST_RUN).append("/").append(testRunId).append("/").append(ATTACHMENT);
    return getRestClient()
        .given()
        .and()
        .body(mapper.writeValueAsString(stepIterationAttachment))
        .when()
        .post(apiEndpoint.toString())
        .then()
        .extract()
        .response();
  }

  private Response postComment(int testRunId, String comment) {
    logger.info("Posting comment [{}] to X-Ray Test Run {}", comment, testRunId);
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint.append(TEST_RUN).append("/").append(testRunId).append("/").append(COMMENT);
    return getRestClient()
        .given()
        .and()
        .body(comment)
        .when()
        .put(apiEndpoint.toString())
        .then()
        .extract()
        .response();
  }
}
