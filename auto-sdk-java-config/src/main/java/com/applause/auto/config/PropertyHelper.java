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
package com.applause.auto.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import lombok.NonNull;
import org.aeonbits.owner.Config;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** A Utility Class for verifying and debugging properties */
@SuppressWarnings({
  "checkstyle:MultipleStringLiterals",
  "checkstyle:CyclomaticComplexity",
  "PMD.GodClass",
  "PMD.UseUtilityClass"
})
public class PropertyHelper {
  private static final Logger logger = LogManager.getLogger(PropertyHelper.class);
  private static final String NO_VALUE = "UNABLE TO EXTRACT VALUE";
  private static final String RECURSIVE_VALUE = "VALUE IS SELF-REFERENTIAL";
  private static final String SEP = " | ";
  private static final String HEADER_NAME = "Configuration Name";
  private static final String HEADER_VAL = "Configuration Value";
  private static final String HEADER_PROP_VAL = "Prop File Value";
  private static final int MIN_PASSWORD_LEN = 4;
  private static final int MAX_PASSWORD_CHARS_TO_SHOW = 8;

  protected PropertyHelper() {
    // Empty CTOR.  don't let this  class be instantiated
  }

  static @NonNull Properties loadProperties() {
    File propFile = new File("src/main/resources/props/system.properties");
    if (!propFile.exists()) {
      logger.info("Properties file: " + propFile.getAbsolutePath() + " does not exist");
      return new Properties();
    }
    if (!propFile.canRead()) {
      logger.info("Properties file: " + propFile.getAbsolutePath() + " not readable");
      return new Properties();
    }
    try (FileInputStream fis = new FileInputStream(propFile)) {
      Properties props = new Properties();
      props.load(fis);
      // If  the load failed, we'll have an exception
      return props;
    } catch (FileNotFoundException e) {
      logger.warn("Properties file: " + propFile.getAbsolutePath() + " could not be found", e);
    } catch (IOException e) {
      logger.warn("Properties file: " + propFile.getAbsolutePath() + " could not be read", e);
    }
    return new Properties();
  }

  static <T extends Config> Set<String> configMethods(final Class<T> configClass) {
    Set<String> result = new HashSet<>();
    Method[] methods = configClass.getMethods();
    for (Method method : methods) {
      if (isGetter(method)) {
        result.add(method.getName());
      }
    }
    return result;
  }

  static boolean isGetter(final Method meth) {
    // A getter is public, does NOT return void, and take no parameters
    return Modifier.isPublic(meth.getModifiers())
        && !meth.getReturnType().equals(Void.TYPE)
        && meth.getParameterCount() == 0
        && !"toString".equals(meth.getName());
  }

  static <T extends Config> void logConfiguration(
      final T configBean,
      final Class<T> configInterface,
      final Properties properties,
      final Set<String> propertyFilter) {
    if (configBean == null) {
      logger.error("No configuration available to log");
      return;
    }

    Method[] methods = configInterface.getMethods();
    Set<String> desireMethodNames = configMethods(configInterface);
    int maxNameLen = HEADER_NAME.length();
    int maxValueLen = HEADER_VAL.length();
    int maxPropLen = HEADER_PROP_VAL.length();
    Map<String, Pair<String, String>> name2Values = new TreeMap<>();
    for (Method method : methods) {
      if (!isGetter(method)) {
        continue; // Not a public getter method, ignore
      }
      if (!propertyFilter.isEmpty() && !propertyFilter.contains(method.getName())) {
        continue; // There is a filter set AND this method is NOT in the desired set
      }
      final String methodName = method.getName();
      if (desireMethodNames.contains(methodName)) {
        maxNameLen = Math.max(maxNameLen, methodName.length());
        // Now we have a method that will return some type of value to us.  This method
        // either returns an object or a primitive (long, int, boolean, etc.)
        // We need to obfuscate apiKey and any passwords
        String methodValue = obfuscate(methodName, callForValueAsString(method, configBean));
        maxValueLen = Math.max(maxValueLen, methodValue.length());
        final Object tmpPropVal = properties != null ? properties.get(methodName) : "No properties";
        String propValue = obfuscate(methodName, tmpPropVal);
        maxPropLen = Math.max(maxPropLen, propValue.length());
        name2Values.put(methodName, Pair.of(methodValue, propValue));
      }
    }
    // Now create a nice table of the configuration value, the current value, the value from the
    // prop file
    // Because we used TreeMap, the names are sorted
    final String header =
        StringUtils.center(HEADER_NAME, maxNameLen)
            + SEP
            + StringUtils.center(HEADER_VAL, maxValueLen)
            + SEP
            + StringUtils.center(HEADER_PROP_VAL, maxPropLen);
    StringBuilder sb = new StringBuilder();
    sb.append(header)
        .append(System.lineSeparator())
        .append("_".repeat(header.length()))
        .append(System.lineSeparator());
    for (Map.Entry<String, Pair<String, String>> entry : name2Values.entrySet()) {
      final String line =
          StringUtils.leftPad(entry.getKey(), maxNameLen)
              + SEP
              + StringUtils.rightPad(entry.getValue().getLeft(), maxValueLen)
              + SEP
              + StringUtils.rightPad(entry.getValue().getRight(), maxPropLen);
      sb.append(line).append(System.lineSeparator());
    }
    logger.info("Configuration:" + System.lineSeparator() + sb);
  }

  static String obfuscate(final String methodName, final Object value) {
    if (value == null) {
      return "null";
    }
    final String valueAsString = value.toString();
    if ("apiKey".equalsIgnoreCase(methodName)) {
      // Sometimes we don't have a real API key.  When developers run in mocks mode
      return hidePasswordOrKey(valueAsString);
    }
    if (methodName.contains("password") || methodName.contains("Password")) {
      return hidePasswordOrKey(valueAsString);
    }
    return valueAsString;
  }

  static String hidePasswordOrKey(final String value) {
    if (value.length() <= MIN_PASSWORD_LEN) {
      return value;
    }
    int charsToShow = Math.min(value.length() - MIN_PASSWORD_LEN, MAX_PASSWORD_CHARS_TO_SHOW);
    // Just show the last X characters of the key
    final StringBuilder sb = new StringBuilder();
    final int showAfter = value.length() - charsToShow;
    for (int ii = 0; ii < value.length(); ii++) {
      char cc = value.charAt(ii);
      if (cc == '-') {
        sb.append('-'); // Always print '-' to preserve formatting
        continue;
      }
      if (ii < showAfter) {
        sb.append('x');
      } else {
        sb.append(cc);
      }
    }
    return sb.toString();
  }

  @SuppressWarnings("PMD.CognitiveComplexity")
  static <T extends Config> String callForValueAsString(
      final @NonNull Method method, final @NonNull T configBean) {
    String defaultReturnValue = NO_VALUE;
    if (method.getReturnType().equals(Boolean.TYPE)) {
      // It returns a boolean.  We want to normalize the values into true/false
      // first call the method
      try {
        return Boolean.toString((boolean) method.invoke(configBean));
      } catch (IllegalAccessException e) {
        logger.warn("Unable to access method: " + method.getName(), e);
      } catch (InvocationTargetException e) {
        logger.warn("Unable to invoke method: " + method.getName(), e);
        defaultReturnValue = RECURSIVE_VALUE;
      }
      return defaultReturnValue;
    }
    if (method.getReturnType().equals(Integer.TYPE) || method.getReturnType().equals(Long.TYPE)) {
      // It returns a number.
      // first call the method
      try {
        Object res = method.invoke(configBean);
        if (res instanceof Long) {
          return Long.toString((Long) res);
        }
        if (res instanceof Integer) {
          return Integer.toString((Integer) res);
        }
      } catch (IllegalAccessException e) {
        logger.warn("Unable to access method: " + method.getName());
      } catch (InvocationTargetException e) {
        logger.warn("Unable to invoke method: " + method.getName(), e);
        defaultReturnValue = RECURSIVE_VALUE;
      }
      return defaultReturnValue;
    }
    // Otherwise, it's a String
    try {
      Object objValue = method.invoke(configBean);
      if (objValue == null) {
        return null;
      }
      return objValue.toString();
    } catch (IllegalAccessException e) {
      logger.warn("Unable to access method: " + method.getName(), e);
    } catch (InvocationTargetException e) {
      logger.warn("Unable to invoke method: " + method.getName(), e);
      defaultReturnValue = RECURSIVE_VALUE;
    }
    return defaultReturnValue;
  }

  /**
   * Return any values that are self-referential
   *
   * @param configBean The config settings the SDK has calculated
   * @param configInterface The interface used for the config
   * @param <T> the type of the config being used
   * @return a set of values that are recursive
   */
  public static <T extends Config> Set<String> findRecursiveValues(
      final @NonNull T configBean, final @NonNull Class<T> configInterface) {

    Method[] methods = configInterface.getMethods();
    Set<String> methodsToTest = configMethods(configInterface);
    Set<String> recursiveValues = new HashSet<>();
    for (Method method : methods) {
      if (!isGetter(method)) {
        continue; // Not a public getter method, ignore
      }
      final String methodName = method.getName();
      if (methodsToTest.contains(methodName)) {
        String methodValue = callForValueAsString(method, configBean);
        if (RECURSIVE_VALUE.equals(methodValue)) {
          recursiveValues.add(methodName);
        }
      }
    }
    return recursiveValues;
  }
}
