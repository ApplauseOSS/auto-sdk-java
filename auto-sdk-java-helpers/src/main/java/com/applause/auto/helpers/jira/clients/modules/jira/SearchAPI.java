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
import com.applause.auto.helpers.util.GenericObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import java.util.Objects;
import lombok.NonNull;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SearchAPI {

  private static final Logger logger = LogManager.getLogger(SearchAPI.class);

  /**
   * Filters Jira issues based on a provided JQL query.
   *
   * @param jqlQuery The JQL query used to filter issues. For example: \"labels IN
   *     (\"xray_automation\") AND issuetype=12106 AND summary~\"Test Description\""
   * @param maxResults The maximum number of results to return. If null, the Jira default limit
   *     (usually 50) is used.
   * @return A {@link JqlFilteredResults} object containing the total number of issues found and a
   *     list of the retrieved issues.
   * @throws JsonProcessingException If an error occurs during JSON processing of the Jira response.
   */
  public JqlFilteredResults filterIssues(@NonNull final String jqlQuery, final Integer maxResults)
      throws JsonProcessingException {
    Response response = getIssuesByJqlFiltering(jqlQuery, maxResults);
    checkResponseInRange(response, Range.of(200, 300), "Get issue by jql filter");
    JqlFilteredResults results =
        GenericObjectMapper.getObjectMapper()
            .readValue(response.asString(), JqlFilteredResults.class);
    if (results.total() == 0) {
      logger.warn("JQL search returned 0 results");
    }
    return results;
  }

  public Response getIssuesByJqlFiltering(
      @NonNull final String jqlQuery, final Integer maxResults) {
    logger.info("Searching issues by JQL query [ {} ] ", jqlQuery);
    StringBuilder apiEndpoint = new StringBuilder();
    apiEndpoint.append(LATEST_API).append('/').append(SEARCH).append("?jql=").append(jqlQuery);
    if (Objects.nonNull(maxResults)) {
      apiEndpoint.append("&maxResults=").append(maxResults);
    }
    return getRestClient().given().when().get(apiEndpoint.toString()).then().extract().response();
  }

  public Response getIssueResponseObject(@NonNull final String jiraTicketID) {
    logger.info("Returning issue response for {}", jiraTicketID);
    String apiEndpoint = ISSUE_PATH + "/" + jiraTicketID;
    return getRestClient().given().when().get(apiEndpoint).then().extract().response();
  }
}
