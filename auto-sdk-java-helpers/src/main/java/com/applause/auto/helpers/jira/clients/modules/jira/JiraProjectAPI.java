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
import static com.applause.auto.helpers.jira.restclient.XrayRestAssuredClient.getRestClient;

import com.applause.auto.helpers.jira.dto.requestmappers.JiraCreateTicketRequest;
import com.applause.auto.helpers.jira.dto.responsemappers.AvailableIssueTypes;
import com.applause.auto.helpers.jira.dto.responsemappers.AvailableProjects;
import com.applause.auto.helpers.jira.dto.responsemappers.JiraCreateTicketResponse;
import com.applause.auto.helpers.jira.dto.responsemappers.steps.Steps;
import com.applause.auto.helpers.jira.dto.shared.Issuetype;
import com.applause.auto.helpers.util.GenericObjectMapper;
import com.applause.auto.helpers.util.XrayRequestHeaders;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class JiraProjectAPI {

  private static final Logger logger = LogManager.getLogger(JiraProjectAPI.class);

  /**
   * Creates a simple JIRA ticket. Additional fields can be created as per project's needs. This
   * method can be used to create a new test case, test plan, test execution, etc. <a
   * href="https://developer.atlassian.com/server/jira/platform/jira-rest-api-examples/">...</a>
   *
   * @param jiraCreateTicketRequest The request object containing the ticket details.
   * @return The ticket key extracted from the response (e.g., "PROJECT-123").
   * @throws JsonProcessingException If an error occurs during JSON processing.
   */
  public String createTicket(@NonNull final JiraCreateTicketRequest jiraCreateTicketRequest)
      throws JsonProcessingException {
    logger.info("Creating Jira ticket: {}", jiraCreateTicketRequest.toString());
    Response response = postTicket(jiraCreateTicketRequest);
    checkResponseInRange(response, Range.of(200, 300), "Creating new Jira Ticket");
    JiraCreateTicketResponse jiraCreateTicketResponse =
        GenericObjectMapper.getObjectMapper()
            .readValue(response.asString(), JiraCreateTicketResponse.class);
    String createdJiraTicketId = jiraCreateTicketResponse.key();
    logger.info("Created Jira ticket: {}", createdJiraTicketId);
    return createdJiraTicketId;
  }

  /**
   * Modify existing Jira Ticket by sending the short or full json data
   *
   * @param jiraTicketID the Jira ticket ID
   * @param jsonAsString JIRA ticket JSON contents {@code @example:} { "fields" : { "labels": [
   *     "my_new_label" ] } }
   */
  public void updateTicket(@NonNull final String jiraTicketID, @NonNull final String jsonAsString) {
    logger.info("Updating Jira ticket: {} with [ {} ]", jiraTicketID, jsonAsString);
    Response response = putUpdateTicket(jiraTicketID, jsonAsString);
    checkResponseInRange(response, Range.of(200, 300), "Updating Jira Ticket");
  }

  /**
   * Delete existing Jira Ticket
   *
   * @param jiraTicketID JIRA ticket ID
   */
  public void deleteTicket(@NonNull final String jiraTicketID) {
    logger.info("Deleting Jira ticket: {}", jiraTicketID);
    Response response = deleteExistingTicket(jiraTicketID);
    checkResponseInRange(response, Range.of(200, 300), "Deleting Jira Ticket");
  }

  /**
   * Get Test Case steps
   *
   * @param jiraTicketID JIRA ticket ID
   * @return Steps object
   */
  public Steps getTestCaseSteps(@NonNull final String jiraTicketID) throws JsonProcessingException {
    Response response = getJiraTestCaseSteps(jiraTicketID);
    Steps steps = GenericObjectMapper.getObjectMapper().readValue(response.asString(), Steps.class);
    checkResponseInRange(response, Range.of(200, 300), "Get Test Case Steps");
    return steps;
  }

  /**
   * Upload file to specific jira ticket (issue, testcase, execution, plan etc). Checks performed:
   *
   * <ol>
   *   <li>If provided file exists
   *   <li>If response contains file name and if status code is in 200 range
   * </ol>
   *
   * @param jiraTicketID represents the jira ticket identifier.
   * @param pathToFile full path to file with its extension.
   * @throws FileNotFoundException If the file specified by {@code pathToFile} does not exist.
   */
  public void uploadAttachment(@NonNull final String jiraTicketID, @NonNull final String pathToFile)
      throws FileNotFoundException {
    Response response = postAttachment(jiraTicketID, pathToFile);
    String fileName = new File(pathToFile).getName();
    if (!response.getBody().asString().contains(fileName)) {
      logger.error("Failed to upload attachment {}", fileName);
    }
    checkResponseInRange(response, Range.of(200, 300), "Adding attachment");
  }

  /**
   * Retrieves the project ID associated with a given project key.
   *
   * @param projectKey The Jira project identifier (e.g., "CARQA-1234", where "CARQA" is the project
   *     identifier). Must not be null.
   * @return The project ID as a String.
   * @throws JsonProcessingException If there is an error processing the JSON response.
   * @throws NullPointerException If the provided projectKey is null.
   */
  public String getProjectId(@NonNull final String projectKey) throws JsonProcessingException {
    Response response = getProjectCode(projectKey);
    checkResponseInRange(response, Range.of(200, 300), "Determine project Id");
    AvailableProjects[] availableProjects =
        GenericObjectMapper.getObjectMapper()
            .readValue(response.asString(), AvailableProjects[].class);
    return Arrays.stream(availableProjects)
        .filter(project -> project.key().equalsIgnoreCase(projectKey))
        .findFirst()
        .get()
        .id();
  }

  /**
   * Retrieves a list of available issue types for a given project.
   *
   * @param projectId the identifier of the project.
   * @return a list of Issuetype objects representing the available issue types.
   * @throws JsonProcessingException if there is an error processing the JSON response.
   */
  public List<Issuetype> getAvailableIssueTypes(@NonNull final String projectId)
      throws JsonProcessingException {
    Response response = getAvailableIssues(projectId);
    checkResponseInRange(response, Range.of(200, 300), "Get project issue types");
    return GenericObjectMapper.getObjectMapper()
        .readValue(response.asString(), AvailableIssueTypes.class)
        .values();
  }

  /**
   * Attach label to Jira ticket
   *
   * @param jiraTicketID JIRA ticket ID
   * @param labelName Ticket label name
   */
  public void addLabel(@NonNull final String jiraTicketID, @NonNull final String labelName) {
    logger.info("Updating Jira ticket: {} with [ {} ] label", jiraTicketID, labelName);
    Response response = putLabelToTicket(jiraTicketID, labelName);
    checkResponseInRange(response, Range.of(200, 300), "Adding Jira Ticket label");
  }

  private Response postTicket(@NonNull final JiraCreateTicketRequest jiraCreateTicketRequestMapping)
      throws JsonProcessingException {
    logger.info("Creating Jira ticket: {}", jiraCreateTicketRequestMapping.toString());
    return getRestClient()
        .given()
        .and()
        .body(
            GenericObjectMapper.getObjectMapper()
                .writeValueAsString(jiraCreateTicketRequestMapping))
        .when()
        .post(ISSUE_PATH)
        .then()
        .extract()
        .response();
  }

  private Response putUpdateTicket(
      @NonNull final String jiraTicketID, @NonNull final String jsonAsString) {
    logger.info("Updating Jira Ticket {} with [ {} ]", jiraTicketID, jsonAsString);
    final var apiEndpoint = ISSUE_PATH + "/" + jiraTicketID;
    return getRestClient()
        .given()
        .and()
        .body(jsonAsString)
        .when()
        .put(apiEndpoint)
        .then()
        .extract()
        .response();
  }

  private Response getJiraTestCaseSteps(@NonNull final String jiraTicketID) {
    logger.info("Getting X-Ray Test Case Steps response for case: {}", jiraTicketID);
    String apiEndpoint = XRAY_PATH + TEST + "/" + jiraTicketID + "/" + STEPS;
    return getRestClient().given().when().get(apiEndpoint).then().extract().response();
  }

  private Response postAttachment(
      @NonNull final String jiraTicketID, @NonNull final String pathToFile)
      throws FileNotFoundException {
    logger.info("Uploading attachment {} to Jira Ticket {}", pathToFile, jiraTicketID);
    File attachment = new File(pathToFile);
    if (!attachment.exists()) {
      throw new FileNotFoundException(String.format("Unable to find file %s", pathToFile));
    }
    String apiEndpoint = XRAY_PATH + ISSUE_PATH + "/" + jiraTicketID + "/" + ATTACHMENTS;
    return getRestClient()
        .given()
        .header(XrayRequestHeaders.getContentTypeMultipartFormDataHeader())
        .header(XrayRequestHeaders.getAtlassianNoCheckHeader())
        .multiPart("file", attachment)
        .and()
        .when()
        .post(apiEndpoint)
        .then()
        .extract()
        .response();
  }

  private Response getProjectCode(@NonNull final String projectKey) {
    logger.info("Returning project code for project key {}", projectKey);
    String apiEndpoint = LATEST_API + "/" + PROJECT;
    return getRestClient().given().when().get(apiEndpoint).then().extract().response();
  }

  private Response getAvailableIssues(@NonNull final String projectId) {
    logger.info("Returning available issues for project {}", projectId);
    String apiEndpoint = ISSUE_PATH + "/" + CREATEMETA + "/" + projectId + "/" + ISSUE_TYPES;
    return getRestClient().given().when().get(apiEndpoint).then().extract().response();
  }

  private Response deleteExistingTicket(@NonNull final String jiraTicketID) {
    logger.info("Deleting issue {}", jiraTicketID);
    String apiEndpoint = ISSUE_PATH + "/" + jiraTicketID;
    return getRestClient().given().when().delete(apiEndpoint).then().extract().response();
  }

  private Response putLabelToTicket(@NonNull final String jiraTicketID, final String labelName) {
    String apiEndpoint = ISSUE_PATH + "/" + jiraTicketID;
    return getRestClient()
        .given()
        .and()
        .body(String.format("{\"update\":{\"labels\":[{\"add\":\"%s\"}]}}", labelName))
        .when()
        .put(apiEndpoint)
        .then()
        .extract()
        .response();
  }
}
