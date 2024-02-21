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
package com.applause.auto.testng.testidentification;

import com.applause.auto.testng.dataprovider.DataProviderHelper;
import com.google.common.collect.ImmutableSet;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

/** A Utility class for parsing test case id annotations for TestNG test identification */
@Data
@NoArgsConstructor
public class TestCaseIds {
  private static final Logger log = LogManager.getLogger(TestCaseIds.class);
  private Set<String> applauseTestCaseIds = new HashSet<>();
  private String testRailTestCaseId;
  private boolean noTestCaseId;

  @Override
  public String toString() {
    return "applause_test_case_id="
        + String.join(", ", applauseTestCaseIds)
        + ", test_rail_test_case_id="
        + testRailTestCaseId;
  }

  /**
   * Attempt to create the set of TestCase ID's from data passed in by testng
   *
   * @param testResult The testng testResult data.
   * @return The test case IDs if they can be extracted from the data
   */
  public static TestCaseIds fromMethod(final ITestResult testResult) {
    final ITestNGMethod testMethod = testResult.getMethod();
    Method underlyingMethod = getUnderlyingMethod(testMethod);
    Optional<TestRailTestCaseId> testRailMeta =
        findAnnotationForMethod(underlyingMethod, TestRailTestCaseId.class);
    Optional<ApplauseTestCaseId> applauseMeta =
        findAnnotationForMethod(underlyingMethod, ApplauseTestCaseId.class);
    Optional<NoTestCaseIdRequired> noTestIdMeta =
        findAnnotationForMethod(underlyingMethod, NoTestCaseIdRequired.class);
    final String description = testMethod.getDescription();
    TestCaseIds ids =
        fromAnnotations(
            applauseMeta.orElse(null),
            testRailMeta.orElse(null),
            noTestIdMeta.orElse(null),
            description);

    // Check for overridden case ids
    DataProviderHelper.checkForApplauseCaseIdOverride(testResult)
        .ifPresent(override -> ids.setApplauseTestCaseIds(ImmutableSet.copyOf(override)));
    DataProviderHelper.checkForTestRailCaseIdOverride(testResult)
        .ifPresent(ids::setTestRailTestCaseId);

    return ids;
  }

  private static Method getUnderlyingMethod(final ITestNGMethod testMethod) {
    final String methodName = testMethod.getMethodName();
    final var underlyingClass = testMethod.getRealClass();

    return Optional.ofNullable(testMethod.getConstructorOrMethod().getMethod())
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Unable to find method: '"
                        + methodName
                        + "' in class "
                        + underlyingClass.getCanonicalName()));
  }

  /**
   * Extract the test case identifiers from the different annotations that have been found/parsed
   *
   * @param applauseData The ApplauseTestCaseId if present
   * @param testRailData The TestRailTestCaseId if present
   * @param noTestCaseIdAllowed The annotation indicating that no test case ID is required (if
   *     present)
   * @param description The description field from the Test annotation if present
   * @return The TestCaseIds parsed from the data (if present)
   * @throws IllegalStateException if the configuration in invalid
   */
  public static TestCaseIds fromAnnotations(
      final ApplauseTestCaseId applauseData,
      final TestRailTestCaseId testRailData,
      final NoTestCaseIdRequired noTestCaseIdAllowed,
      final String description) {
    TestCaseIds testCaseIds = new TestCaseIds();
    // We prefer to have the testRail ID in the testRailData annotation, HOWEVER
    // We will accept it in the description field for legacy implementations
    if (applauseData != null) {
      testCaseIds.setApplauseTestCaseIds(new HashSet<>(Arrays.asList(applauseData.value())));
    }

    if (testRailData != null) {
      testCaseIds.setTestRailTestCaseId(testRailData.value());
    } else if (StringUtils.isNotEmpty(description)) {
      testCaseIds.setTestRailTestCaseId(description);
    }

    testCaseIds.setNoTestCaseId(noTestCaseIdAllowed != null);

    if (!testCaseIds.noTestCaseId
        && testCaseIds.applauseTestCaseIds.isEmpty()
        && testCaseIds.testRailTestCaseId == null) {
      log.warn(
          "Applause Test Case Ids or TestRail Ids may be missing. If this case does not have an associated case id, "
              + "add the @NoTestCaseIdAllowed annotation to get rid of this warning.");
    }
    return testCaseIds;
  }

  static <T extends Annotation> Optional<T> findAnnotationForMethod(
      final Method method, final Class<T> annotationClass) {
    if (method.isAnnotationPresent(annotationClass)) {
      return Optional.of(method.getAnnotation(annotationClass));
    }
    // No annotation found.  Return empty
    return Optional.empty();
  }
}
