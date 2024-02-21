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
package com.applause.auto.cucumber.utils;

import com.applause.auto.testrail.client.enums.TestResultStatus;
import com.applause.auto.util.autoapi.TestResultEndStatus;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/** Utility methods for performing common Applause Framework actions using the Cucumber reporter */
@Log4j2
public final class CucumberUtils {
  private static final String APPLAUSE_TESTCASE_ID_TAG_PREFIX = "@Applause-Test-Case-";
  private static final String TESTRAIL_TESTCASE_ID_TAG_PREFIX = "@TestRail-Test-Case-";

  private CucumberUtils() {}

  /**
   * Parses out the Applause test case ids from the Cucumber Test Case
   *
   * @param testCase The Cucumber Test Case
   * @return A set of @Applause-Test-Case IDs, parsed
   */
  public static Set<String> getApplauseTestCaseIdsFromTestCase(final @NonNull TestCase testCase) {
    return extractAllTagsWithPrefix(testCase, APPLAUSE_TESTCASE_ID_TAG_PREFIX, true);
  }

  /**
   * Parses out the TestRail test case ids from the Cucumber Test Case
   *
   * @param testCase The Cucumber Test Case
   * @return A set of @TestRail-Test-Case Ids, parsed
   */
  public static String getTestRailTestCaseIdsFromTestCase(final @NonNull TestCase testCase) {
    final var ids = extractAllTagsWithPrefix(testCase, TESTRAIL_TESTCASE_ID_TAG_PREFIX, true);
    if (ids.size() > 1) {
      log.warn("TestCase has more than one TestRail Case ID Tag");
    }
    return ids.stream().findAny().orElse(null);
  }

  /**
   * Extracts tags from a Cucumber test case that begin with the provided prefix
   *
   * @param testCase The cucumber test case
   * @param prefix The prefix of the cucumber tag
   * @return A set of tags from the test case
   */
  public static Set<String> extractAllTagsWithPrefix(
      final @NonNull TestCase testCase, final @NonNull String prefix) {
    return extractAllTagsWithPrefix(testCase, prefix, false);
  }

  /**
   * Extracts tags from a Cucumber test case that begin with the provided prefix
   *
   * @param testCase The cucumber test case
   * @param prefix The prefix of the cucumber tag
   * @param removePrefix Whether to filter out the prefix or not
   * @return A set of tags from the test case
   */
  public static Set<String> extractAllTagsWithPrefix(
      final @NonNull TestCase testCase, final @NonNull String prefix, final boolean removePrefix) {
    return testCase.getTags().stream()
        .filter(tag -> tag.startsWith(prefix))
        .map(tag -> removePrefix ? tag.replace(prefix, "") : tag)
        .collect(Collectors.toSet());
  }

  /**
   * Maps a cucumber status to an Applause TestResultEndStatus for submission of the result
   *
   * @param status The Cucumber Status
   * @return The Applause TestResultEndStatus
   */
  public static TestResultEndStatus cucumberStatusToApplauseStatus(final Status status) {
    return switch (status) {
      case PASSED -> TestResultEndStatus.PASSED;
      case FAILED -> TestResultEndStatus.FAILED;
      default -> TestResultEndStatus.SKIPPED;
    };
  }

  /**
   * Maps a cucumber status to an Applause TestResultEndStatus for submission of the result
   *
   * @param status The Cucumber Status
   * @return The Applause TestResultEndStatus
   */
  public static TestResultStatus cucumberStatusToTestRailStatus(final Status status) {
    return switch (status) {
      case PASSED -> TestResultStatus.PASSED;
      case FAILED -> TestResultStatus.FAILED;
      default -> TestResultStatus.SKIPPED;
    };
  }
}
