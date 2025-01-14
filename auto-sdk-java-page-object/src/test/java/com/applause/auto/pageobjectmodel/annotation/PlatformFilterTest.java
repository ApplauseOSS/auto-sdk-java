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
package com.applause.auto.pageobjectmodel.annotation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.applause.auto.data.enums.Platform;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import org.testng.annotations.Test;

public class PlatformFilterTest {

  @Retention(RetentionPolicy.RUNTIME)
  public @interface TestAnnotations {
    TestAnnotation[] value();
  }

  @Repeatable(TestAnnotations.class)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface TestAnnotation {
    Platform platform();
  }

  @TestAnnotation(platform = Platform.MOBILE_ANDROID)
  private static class AnnotatedClass {}

  @TestAnnotation(platform = Platform.MOBILE_IOS)
  private static class AnotherAnnotatedClass {}

  @TestAnnotation(platform = Platform.MOBILE_ANDROID)
  @TestAnnotation(platform = Platform.MOBILE_IOS)
  private static class DoubleAnnotatedClass {}

  @TestAnnotation(platform = Platform.MOBILE_ANDROID)
  @TestAnnotation(platform = Platform.MOBILE_ANDROID)
  private static class DoubleIdenticalAnnotatedClass {}

  @TestAnnotation(platform = Platform.DEFAULT)
  @TestAnnotation(platform = Platform.MOBILE_ANDROID)
  @TestAnnotation(platform = Platform.MOBILE_ANDROID)
  private static class IdenticalAndDefaultAnnotatedClass {}

  @Test
  public void testFilterByPlatform() {
    var annotations =
        List.of(
            AnnotatedClass.class.getAnnotation(TestAnnotation.class),
            AnotherAnnotatedClass.class.getAnnotation(TestAnnotation.class));

    var result =
        PlatformFilter.filterByPlatform(
            annotations, Platform.MOBILE_ANDROID, TestAnnotation::platform);
    assertEquals(result.platform(), Platform.MOBILE_ANDROID);
    assertEquals(result.matches().size(), 1);
    assertEquals(result.matches().iterator().next().platform(), Platform.MOBILE_ANDROID);
  }

  @Test
  public void testLookupAnnotations() {
    var result =
        PlatformFilter.lookupAnnotations(
            Platform.MOBILE_ANDROID,
            AnnotatedClass.class,
            TestAnnotation.class,
            TestAnnotation::platform);
    assertEquals(result.platform(), Platform.MOBILE_ANDROID);
    assertEquals(result.matches().size(), 1);
    assertEquals(result.matches().iterator().next().platform(), Platform.MOBILE_ANDROID);
  }

  @Test
  public void testLookupAnnotationsWithNoMatch() {
    var result =
        PlatformFilter.lookupAnnotations(
            Platform.WEB, AnnotatedClass.class, TestAnnotation.class, TestAnnotation::platform);
    assertEquals(result.platform(), Platform.DEFAULT);
    assertTrue(result.matches().isEmpty());
  }

  @Test
  public void testFilterByPlatformWithMultipleAnnotations() {
    var annotations =
        List.of(DoubleAnnotatedClass.class.getAnnotationsByType(TestAnnotation.class));

    var result =
        PlatformFilter.filterByPlatform(
            annotations, Platform.MOBILE_ANDROID, TestAnnotation::platform);
    assertEquals(result.platform(), Platform.MOBILE_ANDROID);
    assertEquals(result.matches().size(), 1);
    assertEquals(result.matches().iterator().next().platform(), Platform.MOBILE_ANDROID);
  }

  @Test
  public void testLookupAnnotationsWithAggregateAnnotation() {
    var result =
        PlatformFilter.lookupAnnotations(
            Platform.MOBILE_ANDROID,
            DoubleAnnotatedClass.class,
            TestAnnotations.class,
            TestAnnotation.class,
            TestAnnotations::value,
            TestAnnotation::platform);
    assertEquals(result.platform(), Platform.MOBILE_ANDROID);
    assertEquals(result.matches().size(), 1);
    assertEquals(result.matches().iterator().next().platform(), Platform.MOBILE_ANDROID);
  }

  @Test
  public void testFilterByPlatformWithSamePlatformAnnotations() {
    var annotations =
        List.of(DoubleIdenticalAnnotatedClass.class.getAnnotationsByType(TestAnnotation.class));

    var result =
        PlatformFilter.filterByPlatform(
            annotations, Platform.MOBILE_ANDROID, TestAnnotation::platform);
    assertEquals(result.platform(), Platform.MOBILE_ANDROID);
    assertEquals(result.matches().size(), 2);
    assertEquals(result.matches().iterator().next().platform(), Platform.MOBILE_ANDROID);
  }

  @Test
  public void testLookupAnnotationsWithAggregateSamePlatformAnnotations() {
    var result =
        PlatformFilter.lookupAnnotations(
            Platform.MOBILE_ANDROID,
            DoubleIdenticalAnnotatedClass.class,
            TestAnnotations.class,
            TestAnnotation.class,
            TestAnnotations::value,
            TestAnnotation::platform);
    assertEquals(result.platform(), Platform.MOBILE_ANDROID);
    assertEquals(result.matches().size(), 2);
    assertEquals(result.matches().iterator().next().platform(), Platform.MOBILE_ANDROID);
  }

  @Test
  public void testLookupAnnotationsWithAggregateSamePlatformWithDefaultAnnotations() {
    var result =
        PlatformFilter.lookupAnnotations(
            Platform.MOBILE_ANDROID,
            IdenticalAndDefaultAnnotatedClass.class,
            TestAnnotations.class,
            TestAnnotation.class,
            TestAnnotations::value,
            TestAnnotation::platform);
    assertEquals(result.platform(), Platform.MOBILE_ANDROID);
    assertEquals(result.matches().size(), 2);
    assertEquals(result.matches().iterator().next().platform(), Platform.MOBILE_ANDROID);
  }
}
