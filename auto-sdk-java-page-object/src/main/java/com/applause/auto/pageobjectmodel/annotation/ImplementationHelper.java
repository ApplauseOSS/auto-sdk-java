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
import com.applause.auto.pageobjectmodel.base.BaseComponent;

/**
 * Helper class for looking up different implementations of page object models based on the current
 * Platform
 */
public final class ImplementationHelper {

  private ImplementationHelper() {
    // Hidden utility constructor
  }

  /**
   * Retrieves the correct Implementation annotation for a particular class based on the current
   * Platform. If the class doesn't have an annotation for that platform, returns the original
   * class.
   *
   * @param clazz the class to inspect for @Implementation annotations
   * @param <T> must extend BaseComponent
   * @param platform the platform to find an implementation of
   * @return a class extending BaseComponent pointed to by the @Implementation annotation
   */
  @SuppressWarnings({"unchecked"})
  public static <T extends BaseComponent> Class<T> getImplementation(
      final Class<T> clazz, final Platform platform) {
    // If Implementation is present, there's only one instance of the annotation.
    final var matches =
        PlatformFilter.lookupAnnotations(platform, clazz, Implementation.class, Implementation::on);
    if (!matches.matches().isEmpty()) {
      return (Class<T>) matches.matches().stream().findAny().get().is();
    }
    // If Implementations is present, there's multiple instances of the annotation. Iterate to find
    // the right one.
    final var aggregateMatches =
        PlatformFilter.lookupAnnotations(
            platform,
            clazz,
            Implementations.class,
            Implementation.class,
            Implementations::value,
            Implementation::on);
    if (!aggregateMatches.matches().isEmpty()) {
      return (Class<T>) aggregateMatches.matches().stream().findAny().get().is();
    }
    return clazz;
  }
}
