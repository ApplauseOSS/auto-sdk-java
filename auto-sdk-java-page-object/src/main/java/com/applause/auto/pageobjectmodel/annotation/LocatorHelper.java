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
import com.applause.auto.pageobjectmodel.factory.Locator;
import java.lang.reflect.Field;
import lombok.NonNull;

/** Helper class for looking up locator annotations on a given field */
public final class LocatorHelper {
  private LocatorHelper() {}

  /**
   * Checks to see if the field is annotated with either a @Locate or @Locates annotation
   *
   * @param field The field to check
   * @return True if the field has a Locator annotation
   */
  public static boolean hasLocator(final @NonNull Field field) {
    return field.isAnnotationPresent(Locate.class) || field.isAnnotationPresent(Locates.class);
  }

  /**
   * Gets the appropriate @Locate annotation for a particular field based on the current Platform.
   * If the field doesn't have an annotation for that platform (or a suitable fallback), returns
   * null (which is handled later in LazyWebElement or LazyList, since we don't want to blow up
   * unless we have to).
   *
   * @param field the field to inspect for the correct @Locate annotation
   * @param platform the platform type to retrieve locator for
   * @return a Locator populated with the information from that @Locate annotation
   * @throws IllegalArgumentException if the specified field has no @Locate annotation at all - if
   *     this occurs, it's not user error; something in the factory is broken
   */
  public static @NonNull Locator getLocator(final Field field, final Platform platform) {
    if (!LocatorHelper.hasLocator(field)) {
      throw new IllegalArgumentException(
          String.format(
              "Attempted to retrieve the Locate annotation for a field "
                  + "with no Locate annotations. Field [%s.%s].",
              field.getDeclaringClass().getSimpleName(), field.getName()));
    }
    final var matches =
        PlatformFilter.lookupAnnotations(
            platform, field, Locates.class, Locate.class, Locates::value, Locate::on);
    if (matches.matches().isEmpty()) {
      throw new IllegalArgumentException(
          "Attempted to retrieve the Locate annotation for a field with no Locate annotations for platform [%s]. Field [%s.%s]."
              .formatted(platform, field.getDeclaringClass().getSimpleName(), field.getName()));
    }
    if (matches.matches().size() > 1) {
      throw new IllegalArgumentException(
          "Attempted to retrieve the Locate annotation for a field with more than one annotation matching the platform [%s]. Field [%s.%s]."
              .formatted(
                  matches.platform(), field.getDeclaringClass().getSimpleName(), field.getName()));
    }
    final var locateAnnotation = matches.matches().stream().findAny().get();
    return new Locator(
        locateAnnotation, field.getDeclaringClass().getSimpleName() + "#" + field.getName());
  }
}
