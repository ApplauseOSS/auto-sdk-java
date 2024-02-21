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
package com.applause.auto.testng.annotations;

import com.applause.auto.testng.testidentification.ApplauseTestCaseId;
import com.applause.auto.testng.testidentification.NoTestCaseIdRequired;
import com.applause.auto.testng.testidentification.TestCaseIds;
import com.applause.auto.testng.testidentification.TestRailTestCaseId;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SdkAnnotationsTest {

  private static class TestRailTestCaseIdTest {
    private final Logger logger = LogManager.getLogger(TestRailTestCaseIdTest.class);

    @TestRailTestCaseId("12345")
    public void testMethod1() {
      logger.info("in TestRailTestCaseIdTest#testMethod1");
    }
  }

  private static class ApplauseTestCaseIdsTest {
    private final Logger logger = LogManager.getLogger(ApplauseTestCaseIdsTest.class);

    @ApplauseTestCaseId("54321")
    public void testMethod1() {
      logger.info("in TestRailTestCaseIdTest#testMethod1");
    }
  }

  private static class MultipleApplauseTestCaseIdsTest {
    private final Logger logger = LogManager.getLogger(MultipleApplauseTestCaseIdsTest.class);

    @ApplauseTestCaseId({"54321", "54322"})
    public void testMethod1() {
      logger.info("in TestRailTestCaseIdTest#testMethod1");
    }
  }

  private static class NoTestCaseIdTest {
    private final Logger logger = LogManager.getLogger(NoTestCaseIdTest.class);

    @NoTestCaseIdRequired()
    public void testMethod1() {
      logger.info("in NoTestCaseIdTest#testMethod1");
    }
  }

  @Test
  public void testApplauseTestCaseId() {
    ApplauseTestCaseIdsTest objUnderTest = new ApplauseTestCaseIdsTest();
    Annotation foundAnnotation = findAnnotationForMethod(objUnderTest, ApplauseTestCaseId.class);
    Assert.assertNotNull(foundAnnotation);
    if (foundAnnotation instanceof ApplauseTestCaseId expectedAnnotation) {
      Assert.assertEquals(expectedAnnotation.value(), new String[] {"54321"});
    } else {
      Assert.fail(
          "Incorrect annotation type: "
              + ApplauseTestCaseId.class.getCanonicalName()
              + " expected, actually "
              + foundAnnotation.getClass().getCanonicalName());
    }
  }

  @Test
  public void testMultipleApplauseTestCaseId() {
    MultipleApplauseTestCaseIdsTest objUnderTest = new MultipleApplauseTestCaseIdsTest();
    Annotation foundAnnotation = findAnnotationForMethod(objUnderTest, ApplauseTestCaseId.class);
    Assert.assertNotNull(foundAnnotation);
    if (foundAnnotation instanceof ApplauseTestCaseId expectedAnnotation) {
      Assert.assertEquals(expectedAnnotation.value(), new String[] {"54321", "54322"});
    } else {
      Assert.fail(
          "Incorrect annotation type: "
              + ApplauseTestCaseId.class.getCanonicalName()
              + " expected, actually "
              + foundAnnotation.getClass().getCanonicalName());
    }
  }

  @Test
  public void testTestRailTestCaseId() {
    TestRailTestCaseIdTest objUnderTest = new TestRailTestCaseIdTest();
    Annotation foundAnnotation = findAnnotationForMethod(objUnderTest, TestRailTestCaseId.class);
    Assert.assertNotNull(foundAnnotation);
    if (foundAnnotation instanceof TestRailTestCaseId expectedAnnotation) {
      Assert.assertEquals(expectedAnnotation.value(), "12345");
    } else {
      Assert.fail(
          "Incorrect annotation type: "
              + TestRailTestCaseId.class.getCanonicalName()
              + " expected, actually "
              + foundAnnotation.getClass().getCanonicalName());
    }
  }

  @Test
  public void testNoTestCaseIdRequired() {
    NoTestCaseIdTest objUnderTest = new NoTestCaseIdTest();
    Annotation foundAnnotation = findAnnotationForMethod(objUnderTest, NoTestCaseIdRequired.class);
    Assert.assertNotNull(foundAnnotation);
    if (foundAnnotation instanceof NoTestCaseIdRequired expectedAnnotation) {
      Assert.assertNotNull(expectedAnnotation);
    } else {
      Assert.fail(
          "Incorrect annotation type: "
              + TestRailTestCaseId.class.getCanonicalName()
              + " expected, actually "
              + foundAnnotation.getClass().getCanonicalName());
    }
  }

  @Test
  public void testFromAnnotationsShouldBeEmptyAndWarn() {
    // This should RTE because it's not a valid configuration
    TestCaseIds testCases = TestCaseIds.fromAnnotations(null, null, null, null);
    Assert.assertNull(testCases.getTestRailTestCaseId());
    Assert.assertTrue(testCases.getApplauseTestCaseIds().isEmpty());
  }

  @Test
  public void testFromAnnotationsShouldReturnEmpty() {
    // This should not RTE because it's a value configuration
    TestCaseIds result =
        TestCaseIds.fromAnnotations(
            null,
            null,
            new NoTestCaseIdRequired() {
              @Override
              public Class<? extends Annotation> annotationType() {
                return null;
              }
            },
            null);
    // There were no ID's, but we had the annotation saying this is OK, so we should return an
    // empty ids
    Assert.assertNull(result.getTestRailTestCaseId());
    Assert.assertTrue(result.getApplauseTestCaseIds().isEmpty());
  }

  @Test
  public void testFromAnnotationsJustTestRail() {
    // This should not RTE because it's a value configuration
    TestCaseIds result =
        TestCaseIds.fromAnnotations(
            null,
            new TestRailTestCaseId() {
              @Override
              public Class<? extends Annotation> annotationType() {
                return TestRailTestCaseId.class;
              }

              @Override
              public String value() {
                return "56789";
              }
            },
            null,
            null);
    Assert.assertEquals(result.getApplauseTestCaseIds(), new HashSet<>());
    Assert.assertEquals(result.getTestRailTestCaseId(), "56789");
  }

  @Test
  public void testFromAnnotationsJustApplause() {
    // This should not RTE because it's a value configuration
    TestCaseIds result =
        TestCaseIds.fromAnnotations(
            new ApplauseTestCaseId() {
              @Override
              public Class<? extends Annotation> annotationType() {
                return ApplauseTestCaseId.class;
              }

              @Override
              public String[] value() {
                return new String[] {"a-testCase-name"};
              }
            },
            null,
            null,
            null);
    Assert.assertEquals(result.getApplauseTestCaseIds(), new HashSet<>(List.of("a-testCase-name")));
    Assert.assertNull(result.getTestRailTestCaseId());
  }

  @Test
  public void testFromAnnotationsLegacyPath() {
    // This should not RTE because it's a value configuration
    TestCaseIds result = TestCaseIds.fromAnnotations(null, null, null, "98765");
    Assert.assertEquals(result.getApplauseTestCaseIds(), new HashSet<>());
    Assert.assertEquals(result.getTestRailTestCaseId(), "98765");
  }

  @Test
  public void testFromAnnotationsShouldBoth() {
    // This should not RTE because it's a valid configuration
    TestCaseIds result =
        TestCaseIds.fromAnnotations(
            new ApplauseTestCaseId() {
              @Override
              public Class<? extends Annotation> annotationType() {
                return ApplauseTestCaseId.class;
              }

              @Override
              public String[] value() {
                return new String[] {"12345"};
              }
            },
            new TestRailTestCaseId() {
              @Override
              public Class<? extends Annotation> annotationType() {
                return TestRailTestCaseId.class;
              }

              @Override
              public String value() {
                return "4444";
              }
            },
            null,
            "6666");
    // There were no ID's, but we had the annotation saying this is OK, so we should return an
    // empty optional
    Assert.assertEquals(result.getApplauseTestCaseIds(), new HashSet<>(List.of("12345")));
    Assert.assertEquals(result.getTestRailTestCaseId(), "4444");
  }

  Annotation findAnnotationForMethod(
      final Object objUnderTest, final Class<? extends Annotation> annotationClass) {
    @SuppressWarnings("rawtypes")
    Class clazz = objUnderTest.getClass();
    // We have to find the method
    Method method = findMethod(clazz, "testMethod1");
    if (method == null) {
      Assert.fail(
          "Unable to find method = 'testMethod1' in class " + annotationClass.getCanonicalName());
    }
    Annotation[] annotations = method.getDeclaredAnnotations();
    if (annotations.length == 0) {
      Assert.fail(
          "No annotations available for class "
              + Arrays.toString(clazz.getAnnotatedInterfaces())
              + ", method '"
              + method.getName()
              + "' "
              + " expected "
              + annotationClass.getCanonicalName());
    }
    // Look for the annotation that we expect
    if (!method.isAnnotationPresent(annotationClass)) {
      Assert.fail(
          "Unable to find "
              + annotationClass.getCanonicalName()
              + clazz.getCanonicalName()
              + "#"
              + method.getName());
    }
    // We found the annotation.  Read the value
    return method.getAnnotation(annotationClass);
  }

  List<Annotation> findAnnotationsForMethod(
      final Object objUnderTest,
      final String methodName,
      final Class<? extends Annotation> annotationClass) {
    @SuppressWarnings("rawtypes")
    Class clazz = objUnderTest.getClass();
    // We have to find the method
    Method method = findMethod(clazz, methodName);
    if (method == null) {
      Assert.fail(
          "Unable to find method = '"
              + methodName
              + "' in class  "
              + annotationClass.getCanonicalName());
    }
    Annotation[] annotations = method.getAnnotationsByType(annotationClass);
    if (annotations == null || annotations.length == 0) {
      Assert.fail(
          "No annotations available for class "
              + Arrays.toString(clazz.getAnnotatedInterfaces())
              + ", method '"
              + method.getName()
              + "' "
              + " expected at least one "
              + annotationClass.getCanonicalName());
    }
    // We found the annotation.  Read the value
    return Arrays.asList(annotations);
  }

  @SuppressWarnings("rawtypes")
  private Method findMethod(Class clazz, String methodName) {
    Method[] methods = clazz.getMethods();

    for (Method method : methods) {
      if (methodName.equals(method.getName())) {
        return method;
      }
    }
    return null;
  }
}
