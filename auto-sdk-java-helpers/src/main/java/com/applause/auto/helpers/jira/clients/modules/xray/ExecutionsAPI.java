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
import static com.applause.auto.helpers.jira.helper.ResponseValidator.checkResponseInRange;
import static com.applause.auto.helpers.jira.restclient.XrayRestAssuredClient.getRestClient;

import com.applause.auto.helpers.jira.dto.requestmappers.XrayAddTo;
import com.applause.auto.helpers.jira.dto.responsemappers.JiraCreateTicketResponse;
import com.applause.auto.helpers.util.GenericObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import lombok.NonNull;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class ExecutionsAPI {

  private static final Logger logger = LogManager.getLogger(ExecutionsAPI.class);

  /**
   * Adds Test Execution to existing Test Plan.
   *
   * <p>Request example: `{"add":["testExecKey"]}`
   *
   * <p>Response: 200 OK, if Test Execution was added successfully
   *
   * @param xrayAddToMapping The Test Execution to associate with the specified Test Plan.
   * @param testPlanKey The key of the Test Plan to which the Test Execution should be added.
   * @throws JsonProcessingException If an error occurs during JSON processing.
   */
  public void addExecutionToTestPlan(
      @NonNull final XrayAddTo xrayAddToMapping, @NonNull final String testPlanKey)
      throws JsonProcessingException {
    Response response = postTestExecutionToTestPlan(xrayAddToMapping, testPlanKey);
    checkResponseInRange(response, Range.of(200, 300), "Add test execution to test plan");
  }

  /**
   * Adds Test to an existing Test Execution. Request example: "{"add":["testKey"]}". Response: 200
   * OK, if test was added successfully.
   *
   * @param jiraCreateTicketResponseMapping The response containing the Test Execution key.
   * @param xrayAddToMapping The Test key(s) to associate with the Test Execution.
   * @throws JsonProcessingException If a JSON processing error occurs.
   */
  public void addTestToTestExecution(
      @NonNull final JiraCreateTicketResponse jiraCreateTicketResponseMapping,
      @NonNull final XrayAddTo xrayAddToMapping)
      throws JsonProcessingException {
    Response response = postTestToTestExecution(jiraCreateTicketResponseMapping, xrayAddToMapping);
    checkResponseInRange(response, Range.of(200, 300), "Add test to test execution");
  }

  private Response postTestExecutionToTestPlan(
      @NonNull final XrayAddTo xrayAddToMapping, @NonNull final String testPlanKey)
      throws JsonProcessingException {
    logger.info(
        "Adding X-Ray Test Execution {} to X-Ray Test Plan {}", xrayAddToMapping, testPlanKey);

    String apiEndpoint = XRAY_PATH + TEST_PLAN + "/" + testPlanKey + "/" + TEST_EXECUTION;

    return getRestClient()
        .given()
        .and()
        .body(GenericObjectMapper.getObjectMapper().writeValueAsString(xrayAddToMapping))
        .when()
        .post(apiEndpoint)
        .then()
        .extract()
        .response();
  }

  private Response postTestToTestExecution(
      @NonNull final JiraCreateTicketResponse jiraCreateTicketResponseMapping,
      @NonNull final XrayAddTo xrayAddToMapping)
      throws JsonProcessingException {
    logger.info(
        "Adding X-Ray Test(s) [ {} ] to X-Ray Test Execution [ {} ]",
        xrayAddToMapping.toString(),
        jiraCreateTicketResponseMapping.key());

    String apiEndpoint =
        XRAY_PATH + TEST_EXEC + "/" + jiraCreateTicketResponseMapping.key() + "/" + TEST;

    return getRestClient()
        .given()
        .and()
        .body(GenericObjectMapper.getObjectMapper().writeValueAsString(xrayAddToMapping))
        .when()
        .post(apiEndpoint)
        .then()
        .extract()
        .response();
  }
}
