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
import com.applause.auto.helpers.util.GenericObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import java.io.IOException;
import lombok.NonNull;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class StepsAPI {

  private static final Logger logger = LogManager.getLogger(StepsAPI.class);

  /**
   * Modify Test Run Step by using PUT API This method is used to update: status, comment, actual
   * result Should be used when test is NOT parametrised and dataset is NOT present
   *
   * @param testRunId the test run ID
   * @param stepId test step ID
   * @param fields - fields which will be updated
   */
  public void updateTestRunStep(
      final int testRunId, final int stepId, @NonNull final StepFieldsUpdate fields)
      throws JsonProcessingException {
    Response response = putTestRunStep(testRunId, stepId, fields);
    checkResponseInRange(response, Range.of(200, 300), "Update Test Run Step");
  }

  /**
   * Modify Test Run Iteration Step by using PUT API This method is used to update: status, comment,
   * actual result Should be used when test is parametrised and dataset is present
   *
   * @param testRunId the test run ID
   * @param iterationId test run iteration step
   * @param stepId test step ID
   * @param fields - fields which will be updated
   */
  public void updateTestRunIterationStep(
      int testRunId, int iterationId, int stepId, @NonNull final StepFieldsUpdate fields)
      throws JsonProcessingException {
    Response response = putTestRunIterationStep(testRunId, iterationId, stepId, fields);
    checkResponseInRange(response, Range.of(200, 300), "Update Test Run Iteration Step");
  }

  /**
   * Upload Test Run Step attachment
   *
   * @param testRunId test run ID
   * @param stepId test step ID
   * @param filePath - path to file
   */
  public void uploadTestRunStepAttachment(
      final int testRunId, final int stepId, @NonNull final String filePath) throws IOException {
    Response response = postTestRunStepAttachment(testRunId, stepId, filePath);
    checkResponseInRange(response, Range.of(200, 300), "Upload Test Run Step attachment");
  }

  /**
   * Upload Test Run Iteration Step attachment
   *
   * @param testRunId test run ID
   * @param iterationId test step iteration ID
   * @param stepId test step ID
   * @param filePath - path to file
   */
  public void uploadTestRunIterationStepAttachment(
      final int testRunId, final int iterationId, final int stepId, @NonNull final String filePath)
      throws IOException {
    Response response =
        postTestRunIterationStepAttachment(testRunId, iterationId, stepId, filePath);
    checkResponseInRange(response, Range.of(200, 300), "Upload Test Run Iteration Step attachment");
  }

  private Response postTestRunStepAttachment(
      final int testRunId, final int stepId, @NonNull final String filePath) throws IOException {
    logger.info("Attaching {} to X-Ray Test Run {} step {}", filePath, testRunId, stepId);
    StepIterationAttachment stepIterationAttachment =
        new StepIterationAttachment(
            encodeBase64File(filePath), getFileNameFromPath(filePath), getFileType(filePath));
    final var apiEndpoint =
        XRAY_PATH + TEST_RUN + "/" + testRunId + "/" + STEP + "/" + stepId + "/" + ATTACHMENT;

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

  private Response postTestRunIterationStepAttachment(
      final int testRunId, final int iterationId, final int stepId, @NonNull final String filePath)
      throws IOException {
    logger.info(
        "Attaching {} to X-Ray Test Run {} iteration {} step {}",
        filePath,
        testRunId,
        iterationId,
        stepId);
    StepIterationAttachment stepIterationAttachment =
        new StepIterationAttachment(
            encodeBase64File(filePath), getFileNameFromPath(filePath), getFileType(filePath));
    String apiEndpoint =
        XRAY_PATH
            + TEST_RUN
            + "/"
            + testRunId
            + "/"
            + ITERATION
            + "/"
            + iterationId
            + "/"
            + STEP
            + "/"
            + stepId
            + "/"
            + ATTACHMENT;

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

  private Response putTestRunStep(
      final int testRunId, final int stepId, @NonNull final StepFieldsUpdate stepFieldsUpdate)
      throws JsonProcessingException {
    logger.info(
        "Updating X-Ray Test Run {} step {} with {}",
        testRunId,
        stepId,
        stepFieldsUpdate.toString());
    String apiEndpoint = XRAY_PATH + TEST_RUN + "/" + testRunId + "/" + STEP + "/" + stepId;

    return getRestClient()
        .given()
        .and()
        .body(GenericObjectMapper.getObjectMapper().writeValueAsString(stepFieldsUpdate))
        .when()
        .put(apiEndpoint)
        .then()
        .extract()
        .response();
  }

  private Response putTestRunIterationStep(
      final int testRunId,
      final int iterationId,
      final int stepId,
      @NonNull final StepFieldsUpdate stepFieldsUpdate)
      throws JsonProcessingException {
    logger.info(
        "Updating X-Ray Test Run {} iteration {} step {} with {}",
        testRunId,
        iterationId,
        stepId,
        stepFieldsUpdate.toString());
    String apiEndpoint =
        XRAY_PATH
            + TEST_RUN
            + "/"
            + testRunId
            + "/"
            + ITERATION
            + "/"
            + iterationId
            + "/"
            + STEP
            + "/"
            + stepId;

    return getRestClient()
        .given()
        .and()
        .body(GenericObjectMapper.getObjectMapper().writeValueAsString(stepFieldsUpdate))
        .when()
        .put(apiEndpoint)
        .then()
        .extract()
        .response();
  }
}
