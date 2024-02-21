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
package com.applause.auto.testng.dataprovider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.util.Strings;

/** Helper class for keeping track of TestNG Data Provider Iterations */
public final class DataProviderHelper {
  // Keeps Track of the number of executions by class + # + method for default iteration tags
  private static final Map<String, Integer> nextExecutionCountByClassAndMethod =
      new ConcurrentHashMap<>();

  // For a given iteration, keeps track of the iteration number, so we can record
  // retries correctly for default iteration tags
  private static final Map<String, String> recordedExecutionCountByIterationTagKey =
      new ConcurrentHashMap<>();

  private DataProviderHelper() {}

  /**
   * Checks the parameters of the testResult to see if the TestRail Case ID was overridden
   *
   * @param testResult The TestNG result
   * @return An Optional containing the overridden id, if present
   */
  public static Optional<String> checkForTestRailCaseIdOverride(
      final @NonNull ITestResult testResult) {
    return Arrays.stream(testResult.getParameters())
        .filter(p -> ITestRailCaseIdOverride.class.isAssignableFrom(p.getClass()))
        .map(p -> ((ITestRailCaseIdOverride) p).testRailTestCaseId())
        .findFirst();
  }

  /**
   * Checks the parameters of the testResult to see if the Applause Case Ids were overridden
   *
   * @param testResult The TestNG result
   * @return An Optional containing the overridden ids, if present
   */
  public static Optional<Collection<String>> checkForApplauseCaseIdOverride(
      final @NonNull ITestResult testResult) {
    return Arrays.stream(testResult.getParameters())
        .filter(p -> IApplauseTestCaseIdOverride.class.isAssignableFrom(p.getClass()))
        .map(p -> ((IApplauseTestCaseIdOverride) p).applauseTestCaseId())
        .findFirst();
  }

  /**
   * Checks the parameters of the testResult to see an iteration tag is present. Otherwise, we will
   * generate a default.
   *
   * @param testResult The TestNG result
   * @return An iteration tag
   */
  public static String getTestIterationTag(final @NonNull ITestResult testResult) {
    if (!isDataProviderExecution(testResult)) {
      return null;
    }

    return Arrays.stream(testResult.getParameters())
        .filter(p -> ITestIterationTag.class.isAssignableFrom(p.getClass()))
        .map(p -> ((ITestIterationTag) p).testIterationTag())
        .findFirst()
        .orElseGet(() -> getDefaultIterationTag(testResult));
  }

  /**
   * Checks the parameter list of the test result to see if a parameter string override is present.
   * Otherwise, we will just call toString on all the parameters.
   *
   * @param testResult The TestNG result
   * @return The parameters joined as a String
   */
  public static String getParameterString(final @NonNull ITestResult testResult) {
    return Arrays.stream(testResult.getParameters())
        .filter(p -> IParameterOverride.class.isAssignableFrom(p.getClass()))
        .map(p -> ((IParameterOverride) p).parameterString())
        .findFirst()
        .orElseGet(() -> getDefaultParameterString(testResult));
  }

  /**
   * Checks to see if the @Test contains the data provider configuration
   *
   * @param testResult The TestNG result
   * @return true if the dataProvider configuration is set
   */
  public static boolean isDataProviderExecution(final @NonNull ITestResult testResult) {
    Test testAnnotation =
        testResult.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Test.class);
    return Strings.isNotNullAndNotEmpty(testAnnotation.dataProvider());
  }

  /**
   * Generates a default iteration tag if one is not provided. Includes logic to keep track of the
   * iteration number in cases where TestNG retries are used.
   *
   * @param testResult The TestNG result
   * @return The default iteration number
   */
  private static String getDefaultIterationTag(final @NonNull ITestResult testResult) {
    String classAndMethodKey =
        testResult.getTestClass().getRealClass().getCanonicalName() + "#" + testResult.getName();
    String iterationTagKey = classAndMethodKey + "#" + getParameterString(testResult);

    // First check to see if we have seen this iteration
    if (recordedExecutionCountByIterationTagKey.containsKey(iterationTagKey)) {
      return recordedExecutionCountByIterationTagKey.get(iterationTagKey);
    }

    // If we haven't seen it, get a new iteration number
    if (!nextExecutionCountByClassAndMethod.containsKey(classAndMethodKey)) {
      nextExecutionCountByClassAndMethod.put(classAndMethodKey, 0);
    }

    int iteration = nextExecutionCountByClassAndMethod.get(classAndMethodKey);

    // Increment the iteration count for this test class/method
    nextExecutionCountByClassAndMethod.put(classAndMethodKey, iteration + 1);

    // Generate the iteration tag and save it off in case we see it again.
    String iterationTag = "iteration_" + iteration;
    recordedExecutionCountByIterationTagKey.put(iterationTagKey, iterationTag);

    return iterationTag;
  }

  /**
   * Stringifies the parameter list using the default toString and joining with commas.
   *
   * @param testResult The TestNG result
   * @return The parameters serialized as a string
   */
  private static String getDefaultParameterString(final @NonNull ITestResult testResult) {
    return Stream.of(testResult.getParameters())
        .map(Object::toString)
        .collect(Collectors.joining(", "));
  }
}
