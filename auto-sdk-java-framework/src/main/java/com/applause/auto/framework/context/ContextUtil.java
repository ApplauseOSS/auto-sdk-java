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
package com.applause.auto.framework.context;

import com.applause.auto.data.enums.ContextType;
import com.applause.auto.framework.context.annotations.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

/** A util to help with looking up a context related annotation for a class or method */
public final class ContextUtil {
  private static final Logger logger = LogManager.getLogger(ContextUtil.class);

  private ContextUtil() {}

  /**
   * Gets the type of context that should be created.
   *
   * @param underlyingClass The class to check.
   * @return The ContextType of the result.
   */
  public static ContextType getContextType(final @NonNull Class<?> underlyingClass) {
    Class<?> currentClass = underlyingClass;
    while (Objects.nonNull(currentClass)) {
      if (Objects.nonNull(currentClass.getAnnotation(WithDriver.class))) {
        return ContextType.DRIVER;
      } else if (Objects.nonNull(currentClass.getAnnotation(Driverless.class))) {
        return ContextType.DRIVERLESS;
      }
      currentClass = currentClass.getSuperclass();
    }
    return ContextType.DRIVERLESS;
  }

  /**
   * Get the Capabilities Overrider for the Class.
   *
   * @param underlyingClass The class to check
   * @return The Capabilities Override Function
   */
  public static Function<Capabilities, MutableCapabilities> getCapsOverrider(
      final @NonNull Class<?> underlyingClass) {
    WithCapsOverride capsOverriderAnnotation =
        getAnnotationForClass(underlyingClass, WithCapsOverride.class);
    return Optional.ofNullable(capsOverriderAnnotation)
        .map(c -> getCapsOverrider(underlyingClass, c.value()))
        .orElse(null);
  }

  /**
   * Selects a capabilities overrider from the given test class
   *
   * @param testClass The test class to scan
   * @param capsOverrider The name given to the overrider (or null/empty for any matching)
   * @return The chosen capabilities override function
   */
  public static Function<Capabilities, MutableCapabilities> getCapsOverrider(
      final @NonNull Class<?> testClass, final @NonNull String capsOverrider) {

    // Get all methods for the class and check the following:
    //  - That it has a CapsOverrider annotation
    //  - That the annotation matches the provided name (if a certain one was requested)
    //  - That the function matches the correct signature
    List<Method> matchingMethods =
        Arrays.stream(testClass.getMethods())
            .filter(m -> Objects.nonNull(m.getAnnotation(CapsOverrider.class)))
            .filter(
                m ->
                    Strings.isBlank(capsOverrider)
                        || m.getAnnotation(CapsOverrider.class).value().equals(capsOverrider))
            .filter(ContextUtil::validateCapsOverrideMethod)
            .toList();
    if (matchingMethods.isEmpty()) {
      return null;
    } else if (matchingMethods.size() == 1) {
      return methodToFunction(matchingMethods.get(0));
    } else {
      logger.warn(
          "Multiple capability override functions detected for test class. Selecting the first matching entry.");
      return methodToFunction(matchingMethods.get(0));
    }
  }

  /**
   * Validates that a given capsOverrider method matches the correct signature. This checks: - That
   * it has one parameter that is a capabilities object - That it returns a MutableCapabilities
   * object - It is static
   *
   * @param m The method to check
   * @return true if it is a valid caps override method
   */
  private static boolean validateCapsOverrideMethod(final @NonNull Method m) {
    List<String> issues = new ArrayList<>();
    if (m.getParameterCount() != 1 || m.getParameterTypes()[0] != Capabilities.class) {
      issues.add(
          "Invalid number of parameters. Expected 1 parameter of type: " + Capabilities.class);
    }
    if (m.getReturnType() != MutableCapabilities.class) {
      issues.add(
          "Invalid Return type: "
              + m.getReturnType()
              + ". Expected a return type of: "
              + MutableCapabilities.class);
    }
    if (!Modifier.isStatic(m.getModifiers())) {
      issues.add("Expected capabilities overrider to be static");
    }

    if (!issues.isEmpty()) {
      logger.warn(
          "Encountered issue(s) with capabilities overrider function.\n\t"
              + StringUtils.join(issues, "\n\t"));
      return false;
    }
    return true;
  }

  /**
   * At this point, we should know that it is a valid capability overrider. Pretty much just convert
   * it to a function.
   *
   * @param m The method to convert
   * @return A function invoking the method statically
   */
  private static Function<Capabilities, MutableCapabilities> methodToFunction(
      final @NonNull Method m) {
    return caps -> {
      try {
        return (MutableCapabilities) m.invoke(null, caps);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        // This shouldn't happen
        logger.error("Could not invoke capability override function", e);
        return null;
      }
    };
  }

  private static <A extends Annotation> A getAnnotationForClass(
      final @NonNull Class<?> clazz, final @NonNull Class<A> annotation) {
    return Optional.ofNullable(clazz.getAnnotation(annotation))
        .orElseGet(
            () ->
                Optional.ofNullable(clazz.getSuperclass())
                    .map(parent -> ContextUtil.getAnnotationForClass(parent, annotation))
                    .orElse(null));
  }
}
