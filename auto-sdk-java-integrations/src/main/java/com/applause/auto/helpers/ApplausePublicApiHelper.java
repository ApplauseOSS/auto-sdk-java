/*
 *
 * Copyright © 2024 Applause App Quality, Inc.
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
package com.applause.auto.helpers;

import com.applause.auto.util.applausepublicapi.*;
import com.applause.auto.util.applausepublicapi.dto.*;
import java.net.Proxy;
import java.util.*;
import javax.annotation.Nullable;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/** Helper Class for performing common tasks with the Applause Platform Public API */
@Data
@Log4j2
public class ApplausePublicApiHelper {

  private final ApplausePublicApi client;

  /**
   * Sets up a Public API Instance
   *
   * @param applausePublicApiUrl The Base URL for the public API
   * @param apiKey The API Key used to interface with the public API
   * @param proxy An optional HTTP proxy
   */
  public ApplausePublicApiHelper(
      @NonNull final String applausePublicApiUrl,
      @NonNull final String apiKey,
      @Nullable final Proxy proxy) {
    this.client = ApplausePublicApiClient.getClient(applausePublicApiUrl, apiKey, proxy);
  }

  /**
   * Gets a map of test cases for the given product
   *
   * @param productId The ID of the product to look up test cases for
   * @return A Map of test case ids to names
   */
  public Map<Long, String> getTestCases(final Long productId) {
    final var res =
        this.client.testCases().getTestCases(productId, null, null, null, null, null).join();
    if (!res.isSuccessful()) {
      log.warn("Unable to retrieve TestCases: {}", res.message());
      return new HashMap<>();
    }
    if (res.body() == null) {
      log.warn("Unable to retrieve TestCases: null body returned");
      return new HashMap<>();
    }
    final var testcases = res.body().content();
    final var cases = new HashMap<Long, String>();
    testcases.forEach(c -> cases.put(c.testCaseId(), c.name()));
    log.debug("Retrieved TestCases: " + testcases);
    return cases;
  }

  /**
   * Gets a list of test case results for a test cycle
   *
   * @param testCycleId The ID of the test cycle
   * @return The list of results for that cycle
   */
  public List<BaseTestCaseResultDto> getTestCaseResultsForTestCycle(
      @NonNull final Long testCycleId) {
    final var res =
        this.client
            .testCaseResults()
            .getTestCaseResults(testCycleId, null, null, null, null, null, null)
            .join();
    if (!res.isSuccessful()) {
      log.warn("Unable to retrieve Test Cycle results: " + res.message());
      return new ArrayList<>();
    }
    if (res.body() == null) {
      log.warn("Unable to retrieve Test Cycle results: null body returned");
      return new ArrayList<>();
    }
    final var results = res.body().content();
    log.debug("Retrieved TestCycleTestCaseResults: " + results);
    return res.body().content();
  }

  /**
   * Gets a given test result by ID
   *
   * @param testResultId The ID of the test result
   * @return The Test Case Result
   */
  public AutoTestCaseResultDto getAutomatedResult(@NonNull final Long testResultId) {
    final var res = this.client.testCaseResults().getAutoTestCaseResultDetails(testResultId).join();
    if (!res.isSuccessful()) {
      log.warn("Unable to retrieve automation TestCaseResult: " + res.message());
      return null;
    }
    final AutoTestCaseResultDto results = res.body();
    log.debug("Retrieved results: " + results);
    return results;
  }
}
