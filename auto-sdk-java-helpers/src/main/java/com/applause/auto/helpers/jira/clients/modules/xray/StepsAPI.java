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

import com.applause.auto.helpers.jira.dto.requestmappers.StepFieldsUpdate;
import com.applause.auto.helpers.jira.dto.requestmappers.StepIterationAttachment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import java.io.IOException;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StepsAPI {

  private static final Logger logger = LogManager.getLogger(StepsAPI.class);
  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Modify Test Run Step by using PUT API This method is used to update: status, comment, actual
   * result Should be used when test is NOT parametrised and dataset is NOT present
   *
   * @param testRunId
   * @param stepId
   * @param fields - fields which will be updated
   */
  public void updateTestRunStep(int testRunId, int stepId, StepFieldsUpdate fields)
      throws JsonProcessingException {
    Response response = putTestRunStep(testRunId, stepId, fields);
    checkResponseInRange(response, Range.between(200, 300), "Update Test Run Step");
  }

  /**
   * Modify Test Run Iteration Step by using PUT API This method is used to update: status, comment,
   * actual result Should be used when test is parametrised and dataset is present
   *
   * @param testRunId
   * @param iterationId
   * @param stepId
   * @param fields - fields which will be updated
   */
  public void updateTestRunIterationStep(
      int testRunId, int iterationId, int stepId, StepFieldsUpdate fields)
      throws JsonProcessingException {
    Response response = putTestRunIterationStep(testRunId, iterationId, stepId, fields);
    checkResponseInRange(response, Range.between(200, 300), "Update Test Run Iteration Step");
  }

  /**
   * Upload Test Run Step attachment
   *
   * @param testRunId
   * @param stepId
   * @param filePath - path to file
   */
  public void uploadTestRunStepAttachment(int testRunId, int stepId, String filePath)
      throws IOException {
    Response response = postTestRunStepAttachment(testRunId, stepId, filePath);
    checkResponseInRange(response, Range.between(200, 300), "Upload Test Run Step attachment");
  }

  /**
   * Upload Test Run Iteration Step attachment
   *
   * @param testRunId
   * @param iterationId
   * @param stepId
   * @param filePath - path to file
   */
  public void uploadTestRunIterationStepAttachment(
      int testRunId, int iterationId, int stepId, String filePath) throws IOException {
    Response response =
        postTestRunIterationStepAttachment(testRunId, iterationId, stepId, filePath);
    checkResponseInRange(
        response, Range.between(200, 300), "Upload Test Run Iteration Step attachment");
  }

  private Response postTestRunStepAttachment(int testRunId, int stepId, String filePath)
      throws IOException {
    logger.info("Attaching {} to X-Ray Test Run {} step {}", filePath, testRunId, stepId);
    StepIterationAttachment stepIterationAttachment = new StepIterationAttachment();
    stepIterationAttachment.setData(encodeBase64File(filePath));
    stepIterationAttachment.setFilename(getFileNameFromPath(filePath));
    stepIterationAttachment.setContentType(getFileType(filePath));
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint
        .append(TEST_RUN)
        .append("/")
        .append(testRunId)
        .append("/")
        .append(STEP)
        .append("/")
        .append(stepId)
        .append("/")
        .append(ATTACHMENT);
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

  private Response postTestRunIterationStepAttachment(
      int testRunId, int iterationId, int stepId, String filePath) throws IOException {
    logger.info(
        "Attaching {} to X-Ray Test Run {} iteration {} step {}",
        filePath,
        testRunId,
        iterationId,
        stepId);
    StepIterationAttachment stepIterationAttachment = new StepIterationAttachment();
    stepIterationAttachment.setData(encodeBase64File(filePath));
    stepIterationAttachment.setFilename(getFileNameFromPath(filePath));
    stepIterationAttachment.setContentType(getFileType(filePath));
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint
        .append(TEST_RUN)
        .append("/")
        .append(testRunId)
        .append("/")
        .append(ITERATION)
        .append("/")
        .append(iterationId)
        .append("/")
        .append(STEP)
        .append("/")
        .append(stepId)
        .append("/")
        .append(ATTACHMENT);
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

  private Response putTestRunStep(int testRunId, int stepId, StepFieldsUpdate stepFieldsUpdate)
      throws JsonProcessingException {
    logger.info(
        "Updating X-Ray Test Run {} step {} with {}",
        testRunId,
        stepId,
        stepFieldsUpdate.toString());
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint
        .append(TEST_RUN)
        .append("/")
        .append(testRunId)
        .append("/")
        .append(STEP)
        .append("/")
        .append(stepId);
    return getRestClient()
        .given()
        .and()
        .body(mapper.writeValueAsString(stepFieldsUpdate))
        .when()
        .put(apiEndpoint.toString())
        .then()
        .extract()
        .response();
  }

  private Response putTestRunIterationStep(
      int testRunId, int iterationId, int stepId, StepFieldsUpdate stepFieldsUpdate)
      throws JsonProcessingException {
    logger.info(
        "Updating X-Ray Test Run {} iteration {} step {} with {}",
        testRunId,
        iterationId,
        stepId,
        stepFieldsUpdate.toString());
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint
        .append(TEST_RUN)
        .append("/")
        .append(testRunId)
        .append("/")
        .append(ITERATION)
        .append("/")
        .append(iterationId)
        .append("/")
        .append(STEP)
        .append("/")
        .append(stepId);
    return getRestClient()
        .given()
        .and()
        .body(mapper.writeValueAsString(stepFieldsUpdate))
        .when()
        .put(apiEndpoint.toString())
        .then()
        .extract()
        .response();
  }
}
