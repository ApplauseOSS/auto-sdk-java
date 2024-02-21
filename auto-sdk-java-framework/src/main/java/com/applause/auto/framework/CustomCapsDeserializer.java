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
package com.applause.auto.framework;

import com.google.common.math.DoubleMath;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used to deserialize the Capabilities object in our local mode configurations. Overly complex
 * because Selenium Java client provides no mechanism to parse JSON into capabilities (even though
 * they get sent as JSON on the wire!)
 */
public final class CustomCapsDeserializer {
  private static final Logger logger = LogManager.getLogger();
  private static final int DEFAULT_RECURSION_DEPTH = 5;

  private CustomCapsDeserializer() {}

  /**
   * Converts int-like doubles contained in object tree into actual ints
   *
   * @param caps map of capabilities
   * @return a new Map with int-like doubles replaced with ints
   */
  public static Map<String, Object> removeIntLikeDoublesFromCaps(final Map<String, Object> caps) {
    return removeIntLikeDoublesFromCaps(caps, DEFAULT_RECURSION_DEPTH);
  }

  /**
   * Converts int-like doubles contained in object tree into actual ints
   *
   * @param caps map of capabilities
   * @param maxRecursionDepth The maximum number of recursive calls to make
   * @return a new Map with int-like doubles replaced with ints
   */
  public static Map<String, Object> removeIntLikeDoublesFromCaps(
      final Map<String, Object> caps, final int maxRecursionDepth) {
    return caps.entrySet().stream()
        .collect(
            Collectors.toMap(
                Entry<String, Object>::getKey,
                entry -> cloneValueWithDoublesReplaced(entry.getValue(), maxRecursionDepth)));
  }

  @SuppressWarnings("unchecked")
  private static Object cloneValueWithDoublesReplaced(final Object value, int recursionDepthLeft) {
    if (value == null) {
      return null; // No value, nothing to check/convert
    }

    // Don't recurse too far
    if (recursionDepthLeft <= 0) {
      throw new RuntimeException("Object tree too deep or contains cycles!");
    }

    // Convert doubles
    if (value instanceof Double) {
      return convertToIntIfClose((Double) value);
    }
    // is map (recurse)
    if (Map.class.isAssignableFrom(value.getClass())) {
      return removeIntLikeDoublesFromCaps((Map<String, Object>) value, recursionDepthLeft - 1);
    }
    // If we don't need to make a change, then just use the current value (this is not a real deep
    // copy)
    return value;
  }

  /**
   * Converts a double to an int if its "close" to being a valid int value
   *
   * @param fieldVal value of the field
   * @return int value of field if it was close, otherwise empty
   */
  static Optional<Integer> convertToIntIfClose(final Double fieldVal) {
    if (fieldVal == null) {
      return Optional.empty();
    }
    if (DoubleMath.isMathematicalInteger(fieldVal)) {
      val intVal = fieldVal.intValue();
      logger.debug("overwriting field " + fieldVal + " to integer " + intVal);
      return Optional.of(fieldVal.intValue());
    }
    return Optional.empty();
  }
}
