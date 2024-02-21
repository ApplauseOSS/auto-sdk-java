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
package com.applause.auto.pageobjectmodel.annotation;

import com.applause.auto.data.enums.Platform;
import com.google.common.collect.Sets;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;

/** A utility class that filters annotations by a given platform */
public final class PlatformFilter {

  private PlatformFilter() {
    // Hidden utility constructor
  }

  /**
   * Filters a collection of annotations by the platform
   *
   * @param <T> The type of annotation to look at
   * @param annotations An array of annotations to filter
   * @param platform The current platform
   * @param platformLookup The method to look up the platform for the annotation
   * @return A ResultSet of annotations that match at the most specific platform
   */
  public static <T extends Annotation> ResultSet<T> filterByPlatform(
      final @NonNull Collection<T> annotations,
      final @NonNull Platform platform,
      final @NonNull Function<T, Platform> platformLookup) {
    var currentPlatform = platform;
    while (currentPlatform != null) {
      final var setPlatform = currentPlatform;
      final var matches =
          annotations.stream()
              .filter(a -> platformLookup.apply(a) == setPlatform)
              .collect(Collectors.toList());
      if (!matches.isEmpty()) {
        return new ResultSet<>(currentPlatform, matches);
      }
      currentPlatform = currentPlatform.getFallback();
    }
    return new ResultSet<>(Platform.DEFAULT, Collections.emptyList());
  }

  /**
   * Looks up annotations of a given type and filters them by the given platform
   *
   * @param <T> The type of annotation
   * @param platform The platform to search for
   * @param classToSearch The class to check for the annotation on
   * @param annotationClass The type of the annotation
   * @param platformLookup The function used to look up the platform on the annotation
   * @return A ResultSet of matching annotations
   */
  public static <T extends Annotation> ResultSet<T> lookupAnnotations(
      final @NonNull Platform platform,
      final @NonNull Class<?> classToSearch,
      final @NonNull Class<T> annotationClass,
      final @NonNull Function<T, Platform> platformLookup) {
    if (!classToSearch.isAnnotationPresent(annotationClass)) {
      return new ResultSet<>(Platform.DEFAULT, Collections.emptyList());
    }
    T[] annotation = classToSearch.getAnnotationsByType(annotationClass);
    final var matching = filterByPlatform(Arrays.asList(annotation), platform, platformLookup);
    if (!matching.matches.isEmpty()) {
      return matching;
    }
    return new ResultSet<>(Platform.DEFAULT, Collections.emptyList());
  }

  /**
   * Looks up annotations using an aggregate annotation type and filters them by the given platform
   *
   * @param <T> The aggregate annotation type
   * @param <U> The type of annotation
   * @param platform The platform to search for
   * @param classToSearch The class to check for the annotation on
   * @param aggregateAnnotationClass The type of the aggregate annotation
   * @param annotationClass The type of the annotation
   * @param aggregateLookup The function used to look up the platform on the aggregate
   * @param platformLookup The function used to look up the platform on the annotation
   * @return A ResultSet of matching annotations
   */
  public static <T extends Annotation, U extends Annotation> ResultSet<U> lookupAnnotations(
      final @NonNull Platform platform,
      final @NonNull Class<?> classToSearch,
      final @NonNull Class<T> aggregateAnnotationClass,
      final @NonNull Class<U> annotationClass,
      final @NonNull Function<T, U[]> aggregateLookup,
      final @NonNull Function<U, Platform> platformLookup) {
    if (!classToSearch.isAnnotationPresent(aggregateAnnotationClass)) {
      return new ResultSet<>(Platform.DEFAULT, Collections.emptyList());
    }
    T aggregateAnnotation = classToSearch.getAnnotation(aggregateAnnotationClass);
    U[] annotations = aggregateLookup.apply(aggregateAnnotation);
    final var matching = filterByPlatform(Arrays.asList(annotations), platform, platformLookup);
    if (!matching.matches.isEmpty()) {
      return matching;
    }
    return new ResultSet<>(Platform.DEFAULT, Collections.emptyList());
  }

  /**
   * Looks up annotations using an aggregate annotation type and filters them by the given platform
   *
   * @param <T> The annotation type
   * @param platform The platform to search for
   * @param field The field to check for the annotation on
   * @param annotationClass The type of the annotation
   * @param platformLookup The function used to look up the platform on the annotation
   * @return A ResultSet of matching annotations
   */
  public static <T extends Annotation> ResultSet<T> lookupAnnotations(
      final @NonNull Platform platform,
      final @NonNull Field field,
      final @NonNull Class<T> annotationClass,
      final @NonNull Function<T, Platform> platformLookup) {
    if (!field.isAnnotationPresent(annotationClass)) {
      return new ResultSet<>(Platform.DEFAULT, Collections.emptyList());
    }
    T[] annotation = field.getAnnotationsByType(annotationClass);
    final var matching = filterByPlatform(Arrays.asList(annotation), platform, platformLookup);
    if (!matching.matches.isEmpty()) {
      return matching;
    }
    return new ResultSet<>(Platform.DEFAULT, Collections.emptyList());
  }

  /**
   * Looks up annotations using an aggregate annotation type and filters them by the given platform
   *
   * @param <T> The aggregate annotation type
   * @param <U> The type of annotation
   * @param platform The platform to search for
   * @param field The field to check for the annotation on
   * @param aggregateAnnotationClass The type of the aggregate annotation
   * @param annotationClass The type of the annotation
   * @param aggregateLookup The function used to look up the platform on the aggregate
   * @param platformLookup The function used to look up the platform on the annotation
   * @return A ResultSet of matching annotations
   */
  public static <T extends Annotation, U extends Annotation> ResultSet<U> lookupAnnotations(
      final @NonNull Platform platform,
      final @NonNull Field field,
      final @NonNull Class<T> aggregateAnnotationClass,
      final @NonNull Class<U> annotationClass,
      final @NonNull Function<T, U[]> aggregateLookup,
      final @NonNull Function<U, Platform> platformLookup) {
    if (!field.isAnnotationPresent(aggregateAnnotationClass)
        && !field.isAnnotationPresent(annotationClass)) {
      return new ResultSet<>(Platform.DEFAULT, Collections.emptyList());
    }
    List<U> aggregate =
        Optional.ofNullable(field.getAnnotation(aggregateAnnotationClass))
            .map(aggregateLookup)
            .map(Arrays::asList)
            .orElseGet(Collections::emptyList);
    List<U> annotations = Arrays.asList(field.getAnnotationsByType(annotationClass));
    var allAnnotations =
        Sets.union(
            aggregate.stream().collect(Collectors.toSet()),
            annotations.stream().collect(Collectors.toSet()));
    final var matching = filterByPlatform(allAnnotations, platform, platformLookup);
    if (!matching.matches.isEmpty()) {
      return matching;
    }
    return new ResultSet<>(Platform.DEFAULT, Collections.emptyList());
  }

  /**
   * A Record of the filter results for a given platform
   *
   * @param platform The platform that was matched on
   * @param matches The matches for that platform
   * @param <T> The type of annotation searched
   */
  public record ResultSet<T extends Annotation>(Platform platform, Collection<T> matches) {}
}
