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
package com.applause.auto.testng;

import com.applause.auto.testng.dataprovider.DriverConfigOverride;
import com.applause.auto.testng.dataprovider.annotations.DriverConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.testng.IClass;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

/** A Utility Class for helping parse data out of TestNG */
@SuppressWarnings("AbbreviationAsWordInName")
@Log4j2
public final class TestNGUtils {

  private TestNGUtils() {
    // Empty body to hide constructor
  }

  /**
   * Extracts a parameter from a class instance variable
   *
   * @param baseClassInstance the class to extract from
   * @param parameter the name of the parameter to extract
   * @return an optional
   */
  @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
  @SuppressFBWarnings("REFLF_REFLECTION_MAY_INCREASE_ACCESSIBILITY_OF_FIELD")
  public static Optional<String> extractParameter(
      final @NonNull Object baseClassInstance, final @NonNull String parameter) {
    final Class<?> clazz = baseClassInstance.getClass();
    Field field;
    try {
      field = clazz.getDeclaredField(parameter);
    } catch (NoSuchFieldException | SecurityException e) {
      return Optional.empty();
    }
    field.setAccessible(true);
    boolean isString = field.getType().equals(String.class);
    if (!isString) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable((String) field.get(baseClassInstance));
    } catch (IllegalArgumentException | IllegalAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * Extracts a parameter from a method and a set of parameters
   *
   * @param parameters an array of parameters passed to the method
   * @param method the method used to extract from
   * @param parameter the name of the parameter to extract
   * @return an optional string containing the parameter value
   */
  public static Optional<String> extractParameter(
      final @NonNull Object[] parameters,
      final @NonNull Method method,
      final @NonNull String parameter) {
    final Parameter[] methodParams = method.getParameters();
    for (int i = 0; i < methodParams.length; i++) {
      if (i >= parameters.length) {
        return Optional.empty();
      }
      Parameter p = methodParams[i];
      if (p.getType() != String.class) {
        continue;
      }
      if (p.getName().equals(parameter)) {
        return Optional.of((String) parameters[i]);
      }
      if (Objects.nonNull(p.getAnnotation(DriverConfig.class))) {
        return Optional.of((String) parameters[i]);
      }
    }
    // We didn't find any matching strings, next, check for interface or class implementations:
    return Arrays.stream(parameters)
        .filter(p -> DriverConfigOverride.class.isAssignableFrom(p.getClass()))
        .map(p -> ((DriverConfigOverride) p).driverConfig())
        .filter(Objects::nonNull)
        .findFirst();
  }

  /**
   * Extracts a parameter from a TestNg Suite File
   *
   * @param suite The TestNG Suite
   * @param parameter The Parameter to extract
   * @return An Optional containing the value of the provided parameter, if it exists
   */
  public static Optional<String> extractParameter(
      final @NonNull ISuite suite, final @NonNull String parameter) {
    return Optional.ofNullable(suite.getParameter(parameter));
  }

  /**
   * Extracts a parameter from a TestNg Suite File
   *
   * @param suite The TestNG XmlSuite
   * @param parameter The Parameter to extract
   * @return An Optional containing the value of the provided parameter, if it exists
   */
  public static Optional<String> extractParameter(
      final @NonNull XmlSuite suite, final @NonNull String parameter) {
    return Optional.ofNullable(suite.getParameter(parameter));
  }

  /**
   * Extracts a parameter from a TestNg Suite XML
   *
   * @param test The TestNG XmlTest
   * @param parameter The Parameter to extract
   * @return An Optional containing the value of the provided parameter, if it exists
   */
  public static Optional<String> extractParameter(
      final @NonNull XmlTest test, final @NonNull String parameter) {
    return Optional.ofNullable(test.getParameter(parameter));
  }

  /**
   * Extracts a parameter from a TestNg Suite XML
   *
   * @param xmlClass The TestNG XmlClass
   * @param parameter The Parameter to extract
   * @return An Optional containing the value of the provided parameter, if it exists
   */
  public static Optional<String> extractParameter(
      final @NonNull XmlClass xmlClass, final @NonNull String parameter) {
    return Optional.ofNullable(xmlClass.getAllParameters().get(parameter));
  }

  /**
   * Extracts a parameter for a test result
   *
   * @param testResult The Test result to extract the parameter from
   * @param testClass The instance of the class where that test result is getting executed
   * @param testResultParameters The parameters passed to this test result
   * @param parameter the name of the parameter to extract
   * @return The parameter, if it exists
   */
  public static Optional<String> extractParameter(
      final @NonNull ITestResult testResult,
      final @NonNull Object testClass,
      final @NonNull Object[] testResultParameters,
      final @NonNull String parameter) {
    // First, check the parameters passed to the method using reflection
    Optional<String> option =
        extractParameter(
            testResultParameters,
            testResult.getMethod().getConstructorOrMethod().getMethod(),
            parameter);
    if (option.isPresent()) {
      return option;
    }
    // Next step is to check the class instance variables
    option = extractParameter(testClass, parameter);
    if (option.isPresent()) {
      return option;
    }
    // Next up is the XML Class
    option =
        findXmlClass(testResult, testClass.getClass())
            .flatMap(xmlClass -> extractParameter(xmlClass, parameter));
    if (option.isPresent()) {
      return option;
    }
    // Next up is the XML Test
    option = findXmlTest(testResult).flatMap(xmlTest -> extractParameter(xmlTest, parameter));
    if (option.isPresent()) {
      return option;
    }
    // Then the XML Suite
    option = findXmlSuite(testResult).flatMap(xmlSuite -> extractParameter(xmlSuite, parameter));
    if (option.isPresent()) {
      return option;
    }
    // Finally, the parsed suite
    option = findRealSuite(testResult).flatMap(realSuite -> extractParameter(realSuite, parameter));
    return option;
  }

  /**
   * Extracts a parameter from the TestNG test context
   *
   * @param testContext The TestNG context
   * @param testClass The instance of the current test class
   * @param parameter the name of the parameter to extract
   * @return The parameter, if it exists
   */
  public static Optional<String> extractParameter(
      final @NonNull ITestContext testContext,
      final @NonNull Object testClass,
      final @NonNull String parameter) {
    // Next step is to check the class instance variables
    Optional<String> option = extractParameter(testClass, parameter);
    for (Optional<String> s :
        Arrays.asList(
            findXmlClass(testContext, testClass.getClass())
                .flatMap(xmlClass -> extractParameter(xmlClass, parameter)),
            findXmlTest(testContext).flatMap(xmlTest -> extractParameter(xmlTest, parameter)),
            findXmlSuite(testContext).flatMap(xmlSuite -> extractParameter(xmlSuite, parameter)),
            findRealSuite(testContext)
                .flatMap(realSuite -> extractParameter(realSuite, parameter)))) {
      if (option.isPresent()) {
        return option;
      }
      // Next up is the XML Class
      option = s;
    }
    // Next up is the XML Test
    // Then the XML Suite
    // Finally, the parsed suite
    return option;
  }

  /**
   * Checks for a given parallel mode configuration
   *
   * @param testResult The TestNG result
   * @param mode The parallel mode to check for
   * @return true if TestNG is configured for that TestNG mode
   */
  public static boolean checkParallelMode(
      final @NonNull ITestResult testResult, final @NonNull ParallelMode mode) {
    return Optional.ofNullable(testResult.getTestContext())
        .map(ITestContext::getSuite)
        .map(ISuite::getParallel)
        .map(parallel -> StringUtils.equalsAnyIgnoreCase(parallel, mode.toString()))
        .orElse(false);
  }

  /**
   * Finds a matching class in the current xml test of the TestNG context
   *
   * @param testResult The TestNG Result
   * @param classToFind Class to find
   * @return The Matching XML Class, if it exists
   */
  public static Optional<XmlClass> findXmlClass(
      final @NonNull ITestResult testResult, final @NonNull Class<?> classToFind) {
    Optional<XmlClass> option =
        Optional.ofNullable(testResult.getTestClass()).map(IClass::getXmlClass);
    if (option.isPresent()) {
      return option;
    }
    option =
        Optional.ofNullable(testResult.getTestClass())
            .map(IClass::getXmlTest)
            .flatMap(xmlTest -> findXmlClass(xmlTest.getClasses(), classToFind));
    if (option.isPresent()) {
      return option;
    }
    return findXmlTest(testResult)
        .flatMap(xmlTest -> findXmlClass(xmlTest.getClasses(), classToFind));
  }

  /**
   * Finds a matching class in the current xml test of the TestNG context
   *
   * @param testContext TestNG Context
   * @param classToFind Class to find
   * @return The Matching XML Class, if it exists
   */
  public static Optional<XmlClass> findXmlClass(
      final @NonNull ITestContext testContext, final @NonNull Class<?> classToFind) {
    return findXmlClass(testContext.getCurrentXmlTest().getClasses(), classToFind);
  }

  /**
   * Finds a matching class in the current xml test of the TestNG context
   *
   * @param listOfClasses A List of XML Classes
   * @param classToFind Class to find
   * @return The XML Class, if it exists
   */
  public static Optional<XmlClass> findXmlClass(
      final @NonNull List<XmlClass> listOfClasses, final @NonNull Class<?> classToFind) {
    final List<XmlClass> xmlClasses =
        listOfClasses.stream()
            .filter(xmlClass -> xmlClass.getName().equals(classToFind.getName()))
            .toList();
    if (xmlClasses.size() > 1) {
      log.warn("Found multiple matching xml classes, choosing the first one...");
    }
    return xmlClasses.stream().findFirst();
  }

  /**
   * Finds the xml test from a test result
   *
   * @param testResult The test result
   * @return An XML Test is one is accessible
   */
  public static Optional<XmlTest> findXmlTest(final @NonNull ITestResult testResult) {
    Optional<XmlTest> option =
        Optional.ofNullable(testResult.getTestClass()).map(IClass::getXmlTest);
    if (option.isPresent()) {
      return option;
    }
    option = Optional.ofNullable(testResult.getTestContext()).map(ITestContext::getCurrentXmlTest);
    if (option.isPresent()) {
      return option;
    }
    return Optional.ofNullable(testResult.getMethod()).map(ITestNGMethod::getXmlTest);
  }

  /**
   * Finds the xml test in the test context
   *
   * @param testContext The test result
   * @return An XML Test is one is accessible
   */
  public static Optional<XmlTest> findXmlTest(final @NonNull ITestContext testContext) {
    return Optional.ofNullable(testContext.getCurrentXmlTest());
  }

  /**
   * Finds the xml suite
   *
   * @param testResult The test result
   * @return XML Suite
   */
  public static Optional<XmlSuite> findXmlSuite(final @NonNull ITestResult testResult) {
    Optional<XmlSuite> option = findXmlTest(testResult).map(XmlTest::getSuite);
    if (option.isPresent()) {
      return option;
    }
    return findRealSuite(testResult).map(ISuite::getXmlSuite);
  }

  /**
   * Finds the xml suite
   *
   * @param testContext The testng context
   * @return XML Suite
   */
  public static Optional<XmlSuite> findXmlSuite(final @NonNull ITestContext testContext) {
    return findRealSuite(testContext).map(ISuite::getXmlSuite);
  }

  /**
   * Finds a real suite
   *
   * @param testResult The test result
   * @return XML Suite
   */
  public static Optional<ISuite> findRealSuite(final @NonNull ITestResult testResult) {
    return Optional.ofNullable(testResult.getTestContext()).map(ITestContext::getSuite);
  }

  /**
   * Finds a real suite
   *
   * @param testContext The testng context
   * @return XML Suite
   */
  public static Optional<ISuite> findRealSuite(final @NonNull ITestContext testContext) {
    return Optional.ofNullable(testContext.getSuite());
  }
}
