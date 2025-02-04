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
import com.applause.auto.helpers.util.GenericObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import java.io.IOException;
import lombok.NonNull;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class TestrunAPI {

  private static final Logger logger = LogManager.getLogger(TestrunAPI.class);

  /**
   * Get Test Run ID which can be used in updating Test status in a certain Test Execution.
   *
   * @param jiraCreateTicketResponseMapping The Jira create ticket response mapping, used to get the
   *     testExecKey.
   * @param testKey The test issue key.
   * @return The testRunID.
   * @throws JsonProcessingException If the JSON response is invalid.
   */
  public int getTestRunID(
      @NonNull final JiraCreateTicketResponse jiraCreateTicketResponseMapping,
      @NonNull final String testKey)
      throws JsonProcessingException {
    Response response = getTestRunIdOfTestFromExecution(jiraCreateTicketResponseMapping, testKey);
    XrayTestRunDetails xrayTestRunDetailsMapping =
        GenericObjectMapper.getObjectMapper()
            .readValue(response.asString(), XrayTestRunDetails.class);
    checkResponseInRange(response, Range.of(200, 300), "Collecting Test Run ID");
    return xrayTestRunDetailsMapping.id();
  }

  /**
   * Updates Test Run status.
   *
   * @param testRunId the test run ID
   * @param statusToUpdate the status to update. Can be extracted after test execution from
   *     ITestResult using its getStatus() and then converted into one of the following values
   *     accepted by X-Ray as per project needs:
   *     <ul>
   *       <li>EXECUTING – Test is being executed; this is a non-final status;
   *       <li>FAIL – Test failed
   *       <li>ABORTED – Test was aborted
   *       <li>PASS – Test passed successfully
   *     </ul>
   *
   * @throws NullPointerException if statusToUpdate is null
   */
  public void updateTestRun(final int testRunId, @NonNull final String statusToUpdate) {
    Response response = putTestRunStatus(testRunId, statusToUpdate);
    checkResponseInRange(response, Range.of(200, 300), "Update test run");
  }

  /**
   * Get Test Run information
   *
   * @param testRunId test run ID
   * @return XrayTestRunDetails object
   */
  public XrayTestRunDetails getTestRunData(final int testRunId) throws JsonProcessingException {
    Response response = getTestRunBasedOnID(testRunId);
    XrayTestRunDetails xrayTestRunDetailsMapping =
        GenericObjectMapper.getObjectMapper()
            .readValue(response.asString(), XrayTestRunDetails.class);
    checkResponseInRange(response, Range.of(200, 300), "Get Test Run Data");
    return xrayTestRunDetailsMapping;
  }

  /**
   * Get Test Run Iteration information
   *
   * @param testRunId test run ID
   * @param iterationId test run iteration ID
   * @return TestRunIteration object
   */
  public TestRunIteration getTestRunIterationData(final int testRunId, final int iterationId)
      throws JsonProcessingException {
    Response response = getTestRunIterationBasedOnID(testRunId, iterationId);
    TestRunIteration testRunIteration =
        GenericObjectMapper.getObjectMapper()
            .readValue(response.asString(), TestRunIteration.class);
    checkResponseInRange(response, Range.of(200, 300), "Get Test Run Iteration Data");
    return testRunIteration;
  }

  /**
   * Upload Test Run attachment
   *
   * @param testRunId test run ID
   * @param filePath - path to file
   */
  public void uploadTestRunAttachment(final int testRunId, @NonNull final String filePath)
      throws IOException {
    Response response = postTestRunAttachment(testRunId, filePath);
    checkResponseInRange(response, Range.of(200, 300), "Upload Test Run attachment");
  }

  /**
   * Post comment to Test Run
   *
   * @param testRunId test run ID
   * @param comment test run comment
   */
  public void postTestRunComment(final int testRunId, @NonNull final String comment) {
    Response response = postComment(testRunId, comment);
    checkResponseInRange(response, Range.of(200, 300), "Posting Test Run comment");
  }

  private Response getTestRunIdOfTestFromExecution(
      @NonNull final JiraCreateTicketResponse jiraCreateTicketResponseMapping,
      @NonNull final String testKey) {
    logger.info("Getting X-Ray Test Run ID for test: {}", testKey);
    String apiEndpoint =
        XRAY_PATH
            + TEST_RUN
            + "?"
            + testExecIssueKeyParam
            + jiraCreateTicketResponseMapping.key()
            + "&"
            + testIssueKeyParam
            + testKey;
    return getRestClient().given().when().get(apiEndpoint).then().extract().response();
  }

  private Response putTestRunStatus(final int testRunId, @NonNull final String statusToUpdate) {
    logger.info("Updating X-Ray Test Run: {} with status: {}", testRunId, statusToUpdate);
    String apiEndpoint =
        XRAY_PATH + TEST_RUN + "/" + testRunId + "/" + STATUS + "?" + statusParam + statusToUpdate;
    return getRestClient().given().when().put(apiEndpoint).then().extract().response();
  }

  private Response getTestRunBasedOnID(final int testRunId) {
    logger.info("Getting X-Ray Test Run response for ID: {}", testRunId);
    String apiEndpoint = XRAY_PATH + TEST_RUN + "/" + testRunId;
    return getRestClient().given().when().get(apiEndpoint).then().extract().response();
  }

  private Response getTestRunIterationBasedOnID(final int testRunId, final int iterationId) {
    logger.info("Getting X-Ray Test Run {} iteration response for ID: {}", testRunId, iterationId);
    String apiEndpoint =
        XRAY_PATH + TEST_RUN + "/" + testRunId + "/" + ITERATION + "/" + iterationId;
    return getRestClient().given().when().get(apiEndpoint).then().extract().response();
  }

  private Response postTestRunAttachment(final int testRunId, @NonNull final String filePath)
      throws IOException {
    logger.info("Attaching {} to X-Ray Test Run {}", filePath, testRunId);
    StepIterationAttachment stepIterationAttachment =
        new StepIterationAttachment(
            encodeBase64File(filePath), getFileNameFromPath(filePath), getFileType(filePath));
    String apiEndpoint = XRAY_PATH + TEST_RUN + "/" + testRunId + "/" + ATTACHMENT;
    return getRestClient()
        .given()
        .and()
        .body(GenericObjectMapper.getObjectMapper().writeValueAsString(stepIterationAttachment))
        .when()
        .post(apiEndpoint)
        .then()
        .extract()
        .response();
  }

  private Response postComment(final int testRunId, @NonNull final String comment) {
    logger.info("Posting comment [{}] to X-Ray Test Run {}", comment, testRunId);
    String apiEndpoint = XRAY_PATH + TEST_RUN + "/" + testRunId + "/" + COMMENT;
    return getRestClient()
        .given()
        .and()
        .body(comment)
        .when()
        .put(apiEndpoint)
        .then()
        .extract()
        .response();
  }
}
