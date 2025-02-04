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

import com.applause.auto.helpers.jira.dto.jql.JqlFilteredResults;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import java.util.Objects;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SearchAPI {

  private static final Logger logger = LogManager.getLogger(SearchAPI.class);
  private ObjectMapper mapper = new ObjectMapper();

  /**
   * @param jqlQuery, represents the JQL query used in Jira
   * @param maxResults, overrides default limited 50 results. Can be set to null for default.
   * @example : "labels IN ("xray_automation") AND issuetype=12106 AND summary~"Test Description""
   * @return JqlFilteredResults object containing number of findings and list of issues
   */
  public JqlFilteredResults filterIssues(String jqlQuery, Integer maxResults)
      throws JsonProcessingException {
    Response response = getIssuesByJqlFiltering(jqlQuery, maxResults);
    checkResponseInRange(response, Range.between(200, 300), "Get issue by jql filter");
    JqlFilteredResults results = mapper.readValue(response.asString(), JqlFilteredResults.class);
    if (results.getTotal() == 0) {
      logger.warn("JQL search returned 0 results");
    }
    return results;
  }

  public Response getIssuesByJqlFiltering(String jqlQuery, Integer maxResults) {
    logger.info("Searching issues by JQL query [ {} ] ", jqlQuery);
    StringBuilder apiEndpoint = new StringBuilder();
    apiEndpoint.append(LATEST_API).append("/").append(SEARCH).append("?jql=").append(jqlQuery);
    if (Objects.nonNull(maxResults)) {
      apiEndpoint.append("&maxResults=").append(maxResults);
    }
    return getRestClient().given().when().get(apiEndpoint.toString()).then().extract().response();
  }

  public Response getIssueResponseObject(String jiraTicketID) {
    logger.info("Returning issue response for {}", jiraTicketID);
    StringBuilder apiEndpoint = new StringBuilder();
    apiEndpoint.append(ISSUE_PATH).append("/").append(jiraTicketID);
    return getRestClient().given().when().get(apiEndpoint.toString()).then().extract().response();
  }
}
