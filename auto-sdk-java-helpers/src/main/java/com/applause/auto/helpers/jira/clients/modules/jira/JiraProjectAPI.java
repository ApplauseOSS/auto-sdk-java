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
package com.applause.auto.helpers.jira.clients.modules.jira;

import static com.applause.auto.helpers.jira.constants.XrayEndpoints.*;
import static com.applause.auto.helpers.jira.helper.ResponseValidator.checkResponseInRange;
import static com.applause.auto.helpers.jira.requestData.XrayRequestHeaders.getAtlassianNoCheckHeader;
import static com.applause.auto.helpers.jira.requestData.XrayRequestHeaders.getContentTypeMultipartFormDataHeader;
import static com.applause.auto.helpers.jira.restclient.XrayRestAssuredClient.getRestClient;

import com.applause.auto.helpers.jira.dto.requestmappers.JiraCreateTicketRequest;
import com.applause.auto.helpers.jira.dto.responsemappers.AvailableIssueTypes;
import com.applause.auto.helpers.jira.dto.responsemappers.AvailableProjects;
import com.applause.auto.helpers.jira.dto.responsemappers.JiraCreateTicketResponse;
import com.applause.auto.helpers.jira.dto.responsemappers.steps.Steps;
import com.applause.auto.helpers.jira.dto.shared.Issuetype;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JiraProjectAPI {

  private static final Logger logger = LogManager.getLogger(JiraProjectAPI.class);
  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Creates a simple JIRA ticket, additional fields can be created as per project's needs. This
   * method can be used to create a new test case, test plan, test execution etc.
   * https://developer.atlassian.com/server/jira/platform/jira-rest-api-examples/
   *
   * @param jiraCreateTicketRequestMapping, Object of JiraCreateTicketRequestMapping ticket's title
   * @return ticket key extracted from response: {"id":"", "key":"", "self":""}
   * @throws JsonProcessingException
   */
  public String createTicket(JiraCreateTicketRequest jiraCreateTicketRequestMapping)
      throws JsonProcessingException {
    logger.info("Creating Jira ticket: {}", jiraCreateTicketRequestMapping.toString());
    Response response = postTicket(jiraCreateTicketRequestMapping);
    checkResponseInRange(response, Range.between(200, 300), "Creating new Jira Ticket");
    JiraCreateTicketResponse jiraCreateTicketResponseMapping =
        mapper.readValue(response.asString(), JiraCreateTicketResponse.class);
    String createdJiraTicketId = jiraCreateTicketResponseMapping.getKey();
    logger.info("Created Jira ticket: {}", createdJiraTicketId);
    return createdJiraTicketId;
  }

  /**
   * Modify existing Jira Ticket by sending the short or full json data
   *
   * @param jiraTicketID
   * @param jsonAsString
   * @example: { "fields" : { "labels": [ "my_new_label" ] } }
   */
  public void updateTicket(String jiraTicketID, String jsonAsString) {
    logger.info("Updating Jira ticket: {} with [ {} ]", jiraTicketID, jsonAsString);
    Response response = putUpdateTicket(jiraTicketID, jsonAsString);
    checkResponseInRange(response, Range.between(200, 300), "Updating Jira Ticket");
  }

  /**
   * Delete existing Jira Ticket
   *
   * @param jiraTicketID
   */
  public void deleteTicket(String jiraTicketID) {
    logger.info("Deleting Jira ticket: {}", jiraTicketID);
    Response response = deleteExistingTicket(jiraTicketID);
    checkResponseInRange(response, Range.between(200, 300), "Deleting Jira Ticket");
  }

  /**
   * Get Test Case steps
   *
   * @param jiraTicketID
   * @return Steps object
   */
  public Steps getTestCaseSteps(String jiraTicketID) throws JsonProcessingException {
    Response response = getJiraTestCaseSteps(jiraTicketID);
    Steps steps = mapper.readValue(response.asString(), Steps.class);
    checkResponseInRange(response, Range.between(200, 300), "Get Test Case Steps");
    return steps;
  }

  /**
   * Upload file to specific jira ticket (issue, testcase, execution, plan etc) Checks performed: 1.
   * If provided file exists 2. If response contains file name and if status code is in 200 range
   *
   * @param jiraTicketID, represents the jira ticket identifier
   * @param pathToFile, full path to file with its extension
   * @return void
   */
  public void uploadAttachment(String jiraTicketID, String pathToFile)
      throws FileNotFoundException {
    Response response = postAttachment(jiraTicketID, pathToFile);
    String fileName = new File(pathToFile).getName();
    if (!response.getBody().asString().contains(fileName)) {
      logger.error("Failed to upload attachment {}", fileName);
    }
    checkResponseInRange(response, Range.between(200, 300), "Adding attachment");
  }

  /**
   * @param projectKey, represents the jira project identifier Example CARQA-1234, where CARQA is
   *     the project identifier
   * @return projectId
   */
  public String getProjectId(String projectKey) throws JsonProcessingException {
    StringBuilder apiEndpoint = new StringBuilder();
    apiEndpoint.append(LATEST_API).append("/").append(PROJECT);
    Response response = getProjectCode(projectKey);
    checkResponseInRange(response, Range.between(200, 300), "Determine project Id");
    AvailableProjects[] availableProjects =
        mapper.readValue(response.asString(), AvailableProjects[].class);
    return Arrays.stream(availableProjects)
        .filter(project -> project.getKey().equalsIgnoreCase(projectKey))
        .findFirst()
        .get()
        .getId();
  }

  /**
   * @param projectId, represents the project identifier code
   * @return list of Issuetype objects
   */
  public List<Issuetype> getAvailableIssueTypes(String projectId) throws JsonProcessingException {
    Response response = getAvailableIssues(projectId);
    checkResponseInRange(response, Range.between(200, 300), "Get project issue types");
    return mapper.readValue(response.asString(), AvailableIssueTypes.class).getValues();
  }

  /**
   * Attach label to Jira ticket
   *
   * @param jiraTicketID
   * @param labelName
   */
  public void addLabel(String jiraTicketID, String labelName) {
    logger.info("Updating Jira ticket: {} with [ {} ] label", jiraTicketID, labelName);
    Response response = putLabelToTicket(jiraTicketID, labelName);
    checkResponseInRange(response, Range.between(200, 300), "Adding Jira Ticket label");
  }

  private Response postTicket(JiraCreateTicketRequest jiraCreateTicketRequestMapping)
      throws JsonProcessingException {
    logger.info("Creating Jira ticket: {}", jiraCreateTicketRequestMapping.toString());
    return getRestClient()
        .given()
        .and()
        .body(mapper.writeValueAsString(jiraCreateTicketRequestMapping))
        .when()
        .post(ISSUE_PATH)
        .then()
        .extract()
        .response();
  }

  private Response putUpdateTicket(String jiraTicketID, String jsonAsString) {
    logger.info("Updating Jira Ticket {} with [ {} ]", jiraTicketID, jsonAsString);
    StringBuilder apiEndpoint = new StringBuilder(ISSUE_PATH);
    apiEndpoint.append("/").append(jiraTicketID);
    return getRestClient()
        .given()
        .and()
        .body(jsonAsString)
        .when()
        .put(apiEndpoint.toString())
        .then()
        .extract()
        .response();
  }

  private Response getJiraTestCaseSteps(String jiraTicketID) {
    logger.info("Getting X-Ray Test Case Steps response for case: {}", jiraTicketID);
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint.append(TEST).append("/").append(jiraTicketID).append("/").append(STEPS);
    return getRestClient().given().when().get(apiEndpoint.toString()).then().extract().response();
  }

  private Response postAttachment(String jiraTicketID, String pathToFile)
      throws FileNotFoundException {
    logger.info("Uploading attachment {} to Jira Ticket {}", pathToFile, jiraTicketID);
    File attachment = new File(pathToFile);
    if (!attachment.exists()) {
      throw new FileNotFoundException(String.format("Unable to find file %s", pathToFile));
    }
    StringBuilder apiEndpoint = new StringBuilder(XRAY_PATH);
    apiEndpoint.append(ISSUE_PATH).append("/").append(jiraTicketID).append("/").append(ATTACHMENTS);
    return getRestClient()
        .given()
        .header(getContentTypeMultipartFormDataHeader())
        .header(getAtlassianNoCheckHeader())
        .multiPart("file", attachment)
        .and()
        .when()
        .post(apiEndpoint.toString())
        .then()
        .extract()
        .response();
  }

  private Response getProjectCode(String projectKey) {
    logger.info("Returning project code for project key {}", projectKey);
    StringBuilder apiEndpoint = new StringBuilder();
    apiEndpoint.append(LATEST_API).append("/").append(PROJECT);
    return getRestClient().given().when().get(apiEndpoint.toString()).then().extract().response();
  }

  private Response getAvailableIssues(String projectId) {
    logger.info("Returning available issues for project {}", projectId);
    StringBuilder apiEndpoint = new StringBuilder();
    apiEndpoint
        .append(ISSUE_PATH)
        .append("/")
        .append(CREATEMETA)
        .append("/")
        .append(projectId)
        .append("/")
        .append(ISSUE_TYPES);
    return getRestClient().given().when().get(apiEndpoint.toString()).then().extract().response();
  }

  private Response deleteExistingTicket(String jiraTicketID) {
    logger.info("Deleting issue {}", jiraTicketID);
    StringBuilder apiEndpoint = new StringBuilder(ISSUE_PATH);
    apiEndpoint.append("/").append(jiraTicketID);
    return getRestClient()
        .given()
        .when()
        .delete(apiEndpoint.toString())
        .then()
        .extract()
        .response();
  }

  private Response putLabelToTicket(String jiraTicketID, String labelName) {
    StringBuilder apiEndpoint = new StringBuilder(ISSUE_PATH);
    apiEndpoint.append("/").append(jiraTicketID);
    return getRestClient()
        .given()
        .and()
        .body(String.format("{\"update\":{\"labels\":[{\"add\":\"%s\"}]}}", labelName))
        .when()
        .put(apiEndpoint.toString())
        .then()
        .extract()
        .response();
  }
}
