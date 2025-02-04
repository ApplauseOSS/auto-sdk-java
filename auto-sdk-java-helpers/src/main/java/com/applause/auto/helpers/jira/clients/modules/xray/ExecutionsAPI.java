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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExecutionsAPI {

  private static final Logger logger = LogManager.getLogger(ExecutionsAPI.class);
  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Adds Test Execution to existing Test Plan, request example: "{"add":["testExecKey"]}",
   * Response: 200 OK, if Test Execution was added successfully
   *
   * @param xrayAddToMapping, Test Execution you want to associate to a specific Test Plan
   * @return statusCode
   * @throws JsonProcessingException
   */
  public void addExecutionToTestPlan(XrayAddTo xrayAddToMapping, String testPlanKey)
      throws JsonProcessingException {
    Response response = postTestExecutionToTestPlan(xrayAddToMapping, testPlanKey);
    checkResponseInRange(response, Range.between(200, 300), "Add test execution to test plan");
  }

  /**
   * Adds Test to an existing Test Execution, request example: "{"add":["testKey"]}". Response: 200
   * OK, if test was added successfully
   *
   * @param jiraCreateTicketResponseMapping, to get Test Execution key from
   * @param xrayAddToMapping, Test key/s you want to associate to a specific Test Execution
   * @return statusCode
   * @throws JsonProcessingException
   */
  public void addTestToTestExecution(
      JiraCreateTicketResponse jiraCreateTicketResponseMapping, XrayAddTo xrayAddToMapping)
      throws JsonProcessingException {
    Response response = postTestToTestExecution(jiraCreateTicketResponseMapping, xrayAddToMapping);
    checkResponseInRange(response, Range.between(200, 300), "Add test to test execution");
  }

  private Response postTestExecutionToTestPlan(XrayAddTo xrayAddToMapping, String testPlanKey)
      throws JsonProcessingException {
    logger.info(
        "Adding X-Ray Test Execution {} to X-Ray Test Plan {}", xrayAddToMapping, testPlanKey);
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint
        .append(TEST_PLAN)
        .append("/")
        .append(testPlanKey)
        .append("/")
        .append(TEST_EXECUTION);
    return getRestClient()
        .given()
        .and()
        .body(mapper.writeValueAsString(xrayAddToMapping))
        .when()
        .post(apiEndpoint.toString())
        .then()
        .extract()
        .response();
  }

  private Response postTestToTestExecution(
      JiraCreateTicketResponse jiraCreateTicketResponseMapping, XrayAddTo xrayAddToMapping)
      throws JsonProcessingException {
    logger.info(
        "Adding X-Ray Test(s) [ {} ] to X-Ray Test Execution [ {} ]",
        xrayAddToMapping.toString(),
        jiraCreateTicketResponseMapping.getKey());
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint
        .append(TEST_EXEC)
        .append("/")
        .append(jiraCreateTicketResponseMapping.getKey())
        .append("/")
        .append(TEST);
    return getRestClient()
        .given()
        .and()
        .body(mapper.writeValueAsString(xrayAddToMapping))
        .when()
        .post(apiEndpoint.toString())
        .then()
        .extract()
        .response();
  }
}
