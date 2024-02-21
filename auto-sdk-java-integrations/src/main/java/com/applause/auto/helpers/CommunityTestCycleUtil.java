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
import java.io.IOException;
import java.net.Proxy;
import java.util.*;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/** Helper Class for performing common tasks with the Applause Platform Public API */
@Log4j2
public final class CommunityTestCycleUtil {

  private final ApplausePublicApi client;

  /**
   * Sets up a Public API Instance
   *
   * @param applausePublicApiUrl The Base URL for the public API
   * @param apiKey The API Key used to interface with the public API
   * @param proxy An optional HTTP proxy
   */
  public CommunityTestCycleUtil(
      @NonNull final String applausePublicApiUrl,
      @NonNull final String apiKey,
      @Nullable final Proxy proxy) {
    this.client = ApplausePublicApiClient.getClient(applausePublicApiUrl, apiKey, proxy);
  }

  /**
   * Looks up a Test Cycle by name or ID
   *
   * @param nameOrId The Test Cycle name or ID as a String
   * @param productId The ID of the product
   * @return An Optional containing the test cycle ID, if it exists
   */
  public Optional<Long> findCommunityTestCycleByNameOrId(
      @NonNull final String nameOrId, @NonNull final Long productId) {
    // Platform allows TestCycles with duplicate names so sort with the newest first and look
    // backwards
    // This has potential for picking the wrong TestCycle.  Issues:
    // 1) Specifying the exact testCycleId should avoid all issues
    // 2) Ensure all TestCycles have distinct names. Duplicates can cause wrong "find"
    // 3) Ensure TestCycle names do not look like numbers. We have overloaded nameOrId field.
    Long parsedId = null;
    try {
      parsedId = Long.parseLong(nameOrId);
    } catch (NumberFormatException e) {
      log.trace("nameOrId must be a name, since we couldn't parse it.");
    }
    final var realId = parsedId;

    final var res =
        getCommunityTestCycleForProductMatchingCondition(
                productId,
                cycle -> cycle.id().equals(realId) || cycle.name().equalsIgnoreCase(nameOrId))
            .map(CommunityTestCycleDto::id);

    res.ifPresentOrElse(
        cycleId -> log.debug("Filtered known TestCycles to: {}", cycleId),
        () ->
            log.warn(
                "Unable to retrieve TestCycles for productId: {} with name or id {}",
                productId,
                nameOrId));
    return res;
  }

  /**
   * Clones a Test Cycle
   *
   * @param testCycleIdToClone The ID of the test cycle to clone
   * @param cloneDto The Details used to override values on the cloned DTO
   * @return The ID of the new test cycle
   */
  public Optional<Long> cloneCommunityTestCycle(
      @NonNull final Long testCycleIdToClone, @NonNull final CommunityTestCycleCreateDto cloneDto) {
    final var res =
        this.client
            .communityTestCycles()
            .cloneTestCycle(cloneDto, testCycleIdToClone, false)
            .join();
    if (!res.isSuccessful()) {
      try {
        log.warn(
            "Unable to clone test cycle: received error {} from public api. Details {}",
            res.code(),
            res.errorBody().string());
      } catch (IOException e) {
        log.warn("Unable to clone test cycle: received error {} from public api.", res.code());
      }
      return Optional.empty();
    }
    final var newTestCycle = res.body();
    if (newTestCycle == null) {
      log.warn("Unable to clone test cycle: invalid response received");
      return Optional.empty();
    }
    final var newTestCycleId = newTestCycle.id();
    final var newTestCycleName = newTestCycle.name();
    log.info(
        String.format(
            "Successfully cloned TestCycle [%s -> %s/%s]",
            testCycleIdToClone, newTestCycleId, newTestCycleName));
    return Optional.ofNullable(newTestCycleId);
  }

  /**
   * Clones a Community Test Cycle
   *
   * @param testCycleIdToClone The ID of the test cycle to clone
   * @param productId The ID of the proxy tied to the test cycle
   * @param newTestCycleName An optional override of the test cycle name
   * @param start An optional start time for the new test cycleId
   * @param end An optional end time for the new test cycleId
   * @return The ID of the new test cycle
   */
  public Optional<Long> cloneCommunityTestCycle(
      @NonNull final Long testCycleIdToClone,
      @NonNull final Long productId,
      @Nullable final String newTestCycleName,
      @NonNull final Date start,
      @NonNull final Date end) {
    final var cloneDto = setupTestCyclesCloneDto(productId, newTestCycleName, start, end);
    return this.cloneCommunityTestCycle(testCycleIdToClone, cloneDto);
  }

  private CommunityTestCycleCreateDto setupTestCyclesCloneDto(
      @Nullable final Long productId,
      @Nullable final String newTestCycleName,
      @NonNull final Date start,
      @NonNull final Date end) {
    return new CommunityTestCycleCreateDto(
        productId,
        start,
        end,
        newTestCycleName,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }

  /**
   * Activates an inactive Test Cycle
   *
   * @param testCycleId The ID of the test cycle to activate
   * @return True if the test cycle was successfully activated
   */
  public boolean activateCommunityTestCycle(final Long testCycleId) {
    final var res = this.client.communityTestCycles().activateTestCycle(testCycleId).join();
    if (!res.isSuccessful()) {
      log.warn("Unable to activate test cycle: " + testCycleId + " : " + res.message());
      return false;
    }
    log.info("Successfully activated TestCycle " + testCycleId);
    return true;
  }

  /**
   * Clones a test cycle, and then activates it
   *
   * @param testCycleIdToClone The ID of the test cycle to clone
   * @param productId The ID of the proxy tied to the test cycle
   * @param newTestCycleName An optional override of the test cycle name
   * @param start An optional start time for the new test cycle
   * @param end An optional end time for the new test cycle
   * @return an optional containing the new Test Cycle ID
   */
  public Optional<Long> cloneThenActivateCommunityTestCycle(
      @NonNull final Long testCycleIdToClone,
      @NonNull final Long productId,
      @Nullable final String newTestCycleName,
      @NonNull final Date start,
      @NonNull final Date end) {
    final var cloneDto = setupTestCyclesCloneDto(productId, newTestCycleName, start, end);
    final var maybeNewTestCycleId = this.cloneCommunityTestCycle(testCycleIdToClone, cloneDto);
    if (maybeNewTestCycleId.isEmpty()) {
      return maybeNewTestCycleId;
    }
    this.activateCommunityTestCycle(maybeNewTestCycleId.get());
    return maybeNewTestCycleId;
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
   * Moves a test cycle's status to LOCKED
   *
   * @param testCycleId The ID of the test cycle to transition
   * @return True if the lock was successful
   */
  public boolean lockCommunityTestCycle(@NonNull final Long testCycleId) {
    final var res = this.client.communityTestCycles().lockTestCycle(testCycleId).join();
    if (!res.isSuccessful()) {
      log.warn("Unable to lock TestCycle: " + res.message());
      return false;
    }
    log.debug("Locked TestCycle: " + testCycleId);
    return true;
  }

  /**
   * Moves a test cycle's status to CLOSED
   *
   * @param testCycleId The ID of the test cycle to transition
   * @return True if the close was successful
   */
  public boolean closeCommunityTestCycle(@NonNull final Long testCycleId) {
    final var res = this.client.communityTestCycles().closeTestCycle(testCycleId).join();
    if (!res.isSuccessful()) {
      log.warn("Unable to close TestCycle: " + res.message());
      return false;
    }
    log.debug("Closed TestCycle: " + testCycleId);
    return true;
  }

  /**
   * Moves a test cycle's status to LOCKED, then to CLOSED
   *
   * @param testCycleId The ID of the test cycle to transition
   * @return True if the close was successful
   */
  public boolean lockThenCloseTestCycle(@NonNull final Long testCycleId) {
    final var locked = this.lockCommunityTestCycle(testCycleId);
    final var closed = this.closeCommunityTestCycle(testCycleId);
    return locked && closed;
  }

  /**
   * Gets the first test cycle that matches the given condition using default paging parameters
   *
   * <ul>
   *   <li>pageSize: 50
   *   <li>maxPages: 100
   *   <li>sortType: createDate,DESC
   * </ul>
   *
   * <br>
   *
   * @param productId The ID of the product to search for
   * @param condition The condition to match
   * @return An optional containing the first matching test cycle, if found
   */
  public Optional<CommunityTestCycleDto> getCommunityTestCycleForProductMatchingCondition(
      @NonNull final Long productId, @NonNull final Predicate<CommunityTestCycleDto> condition) {
    // We will only look at the first 100 pages to save time. This means we expect a match in the
    // newest
    // 5000 test cycle
    return getCommunityTestCycleForProductMatchingCondition(
        productId, condition, 50L, 100L, "createDate,DESC");
  }

  /**
   * Gets the first test cycle that matches the given condition using the provided paging parameters
   *
   * @param productId The ID of the product to search for
   * @param condition The condition to match
   * @param pageSize The size of the page to fetch
   * @param maxPages The maximum number of pages to check
   * @param sortType The type to sort
   * @return An optional containing the first matching test cycle, if found
   */
  public Optional<CommunityTestCycleDto> getCommunityTestCycleForProductMatchingCondition(
      final long productId,
      @NonNull final Predicate<CommunityTestCycleDto> condition,
      final long pageSize,
      final long maxPages,
      final String sortType) {
    for (int pageIdx = 0; pageIdx < maxPages; pageIdx++) {
      final var page = getCommunityTestCyclesPage(productId, pageIdx, pageSize, sortType);
      if (page.isEmpty()) {
        // Public API used to be 0 based, but is now 1 based. This check will allow public API to
        // switch back to 0 based pages without breaking us.
        if (pageIdx == 0) {
          continue;
        }
        break;
      }
      // Ignore all values
      final var potentialMatch = page.stream().dropWhile(tc -> !condition.test(tc)).findFirst();
      if (potentialMatch.isPresent()) {
        return potentialMatch;
      }
    }
    return Optional.empty();
  }

  private List<CommunityTestCycleDto> getCommunityTestCyclesPage(
      long productId, long page, long pageSize, final String sortType) {
    log.debug("Looking through TestCycles for given product page " + page);
    final var res =
        client
            .communityTestCycles()
            .getTestCycles(List.of(productId), pageSize, page, sortType)
            .join();
    if (!res.isSuccessful()) {
      // It is expected to get an error on page 0 as long as public API uses 1 based pagination. If
      // they
      // do switch back and this is a real error, we will log on page 2.
      if (page != 0) {
        log.error(
            "Could not load test cycle page {} from platform: server returned status {}.",
            page,
            res.code());
      }
      return Collections.emptyList();
    }
    final var body = res.body();
    if (body == null || body.content() == null) {
      log.error(
          "Could not load test cycle page {} from platform: server returned empty of invalid body.",
          page);
      return Collections.emptyList();
    }
    return body.content();
  }
}
