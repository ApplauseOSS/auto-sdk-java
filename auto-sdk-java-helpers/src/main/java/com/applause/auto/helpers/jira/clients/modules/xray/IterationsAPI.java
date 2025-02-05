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

import com.applause.auto.helpers.jira.dto.responsemappers.steps.Step;
import com.applause.auto.helpers.util.GenericObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class IterationsAPI {

  private static final Logger logger = LogManager.getLogger(IterationsAPI.class);

  /**
   * Get Test Run Iteration Steps information. Returned object will contain also each step ID which
   * is needed for further step update request.
   *
   * @param testRunId The ID of the test run.
   * @param iterationId The ID of the iteration.
   * @return An array of Step objects.
   * @throws JsonProcessingException If there is an error processing the JSON response.
   */
  public Step[] getTestRunIterationStepsData(final int testRunId, final int iterationId)
      throws JsonProcessingException {
    Response response = getTestRunIterationSteps(testRunId, iterationId);
    Step[] steps =
        GenericObjectMapper.getObjectMapper().readValue(response.asString(), Step[].class);
    checkResponseInRange(response, Range.of(200, 300), "Get Test Run Iteration Steps Data");
    return steps;
  }

  private Response getTestRunIterationSteps(final int testRunId, final int iterationId) {
    logger.info(
        "Getting X-Ray Test Run {} iteration steps response for ID: {}", testRunId, iterationId);
    String apiEndpoint =
        XRAY_PATH + TEST_RUN + "/" + testRunId + "/" + ITERATION + "/" + iterationId + "/" + STEP;
    return getRestClient().given().when().get(apiEndpoint).then().extract().response();
  }
}
