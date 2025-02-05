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

import com.applause.auto.context.FrameworkContext;
import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.data.enums.ContextType;
import com.applause.auto.framework.ContextBuilder;
import com.applause.auto.framework.ContextManager;
import com.applause.auto.framework.DriverBuilder;
import com.applause.auto.framework.context.ContextUtil;
import com.applause.auto.framework.context.annotations.Driverless;
import com.applause.auto.framework.context.annotations.WithCapsOverride;
import com.applause.auto.framework.context.annotations.WithDriver;
import com.applause.auto.framework.json.BadJsonFormatException;
import com.applause.auto.logging.ResultPropertyMap;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

/** TestNG Specific Context Logic */
public final class TestNgContextUtils {
  private static final String CAPS_FILE = "capsFile";

  private TestNgContextUtils() {}

  /**
   * Gets the type of context that should be created.
   *
   * @param testResult The TestNG result.
   * @return The ContextType of the result.
   */
  public static ContextType getContextType(final @NonNull ITestResult testResult) {
    final Pair<Class<?>, Method> classAndMethod = getUnderlyingClassAndMethod(testResult);
    final Method underlyingMethod = classAndMethod.getRight();

    // Check the method since this is the first choice.
    if (Objects.nonNull(underlyingMethod.getAnnotation(WithDriver.class))) {
      return ContextType.DRIVER;
    } else if (Objects.nonNull(underlyingMethod.getAnnotation(Driverless.class))) {
      return ContextType.DRIVERLESS;
    }

    // If there is no context type set on the method, check the class
    return ContextUtil.getContextType(classAndMethod.getLeft());
  }

  /**
   * Gets the underlying class and method for a TestNG result.
   *
   * @param testResult The TestNG result
   * @return A Tuple containing the Class and Method used for the result.
   */
  public static Pair<Class<?>, Method> getUnderlyingClassAndMethod(
      final @NonNull ITestResult testResult) {
    final ITestNGMethod testMethod = testResult.getMethod();
    final String methodName = testMethod.getMethodName();
    final Class<?> underlyingClass = testMethod.getRealClass();
    Method underlyingMethod =
        Optional.ofNullable(testMethod.getConstructorOrMethod().getMethod())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Unable to find method: '"
                            + methodName
                            + "' in class "
                            + underlyingClass.getCanonicalName()));
    return Pair.of(underlyingClass, underlyingMethod);
  }

  /**
   * Get the Capabilities Overrider for the TestResult.
   *
   * @param testResult The TestNG result
   * @return The Capabilities Override Function
   */
  public static Function<Capabilities, MutableCapabilities> getCapsOverrider(
      final @NonNull ITestResult testResult) {
    final Pair<Class<?>, Method> classAndMethod = getUnderlyingClassAndMethod(testResult);
    final Class<?> underlyingClass = classAndMethod.getLeft();
    final Method underlyingMethod = classAndMethod.getRight();
    WithCapsOverride capsOverriderAnnotation =
        Optional.ofNullable(underlyingMethod.getAnnotation(WithCapsOverride.class))
            .orElseGet(() -> getAnnotationForClass(underlyingClass, WithCapsOverride.class));
    return Optional.ofNullable(capsOverriderAnnotation)
        .map(c -> ContextUtil.getCapsOverrider(underlyingClass, c.value()))
        .orElse(null);
  }

  /**
   * Creates a new Context for a class
   *
   * @param context The TestNG Context
   * @param testClassInstance The class used to create the context
   * @return the created context
   * @throws BadJsonFormatException If the driver cannot be resolved
   */
  public static FrameworkContext setupContext(
      final @NonNull ITestContext context, final @NonNull Object testClassInstance)
      throws BadJsonFormatException {
    final ContextType contextType = ContextUtil.getContextType(testClassInstance.getClass());

    // If we are not using a driver, we do not need to detect a driver name to use
    if (contextType == ContextType.DRIVERLESS) {
      return ContextBuilder.setup().getAsMainContext();
    }

    // We are using a driver, try to extract the driver name
    final String driverName =
        TestNGUtils.extractParameter(context, testClassInstance, CAPS_FILE)
            .orElse(ContextManager.INSTANCE.getDefaultDriverConfig());
    return DriverBuilder.create(driverName)
        .overrideCaps(ContextUtil.getCapsOverrider(testClassInstance.getClass()))
        .map(ContextBuilder::fromDriver)
        .getAsMainContext();
  }

  /**
   * Creates a new Context for the TestNG result
   *
   * @param testResult the TestNG result used to create the context
   * @param testClassInstance The instance of the base class where the driver is being set up
   * @param parameterList A list of parameters to be passed to the test method
   * @return the created context
   * @throws BadJsonFormatException If the driver cannot be resolved
   */
  @SuppressWarnings("PMD.UseVarargs")
  public static FrameworkContext setupContext(
      final @NonNull ITestResult testResult,
      final @NonNull Object testClassInstance,
      final Object[] parameterList)
      throws BadJsonFormatException {
    final ContextType contextType = getContextType(testResult);

    // If we are not using a driver, we do not need to detect a driver name to use
    if (contextType == ContextType.DRIVERLESS) {
      return ContextBuilder.setup().getAsMainContext();
    }

    // We are using a driver, try to extract the driver name
    final String driverName =
        TestNGUtils.extractParameter(testResult, testClassInstance, parameterList, CAPS_FILE)
            .orElse(ContextManager.INSTANCE.getDefaultDriverConfig());
    final var context =
        DriverBuilder.create(driverName)
            .overrideCaps(getCapsOverrider(testResult))
            .map(ContextBuilder::fromDriver)
            .getAsMainContext();
    registerWithTestResult(testResult, context);
    return context;
  }

  /**
   * Registers that a test result was run in the current context
   *
   * @param testResult The TestNG result
   * @param context The context to register
   */
  public static void registerWithTestResult(
      final @NonNull ITestResult testResult, final @NonNull FrameworkContext context) {
    ResultPropertyMap.setLocalProperty(
        "testName",
        testResult.getMethod().getMethodName()
            + "-"
            + testResult.getMethod().getCurrentInvocationCount());
    testResult.setAttribute("contextId", context.getContextId());
    if (context.getPageObjectContext().map(IPageObjectContext::getDriver).orElse(null)
        instanceof RemoteWebDriver) {
      final String sessionId =
          context
              .getPageObjectContext()
              .map(IPageObjectContext::getDriver)
              .map(driver -> (RemoteWebDriver) driver)
              .map(RemoteWebDriver::getSessionId)
              .map(SessionId::toString)
              .orElse(null);
      testResult.setAttribute("providerSessionId", sessionId);
    }
    context.setConnector(new TestNgContextConnector(testResult));
  }

  private static <A extends Annotation> A getAnnotationForClass(
      final @NonNull Class<?> clazz, final @NonNull Class<A> annotation) {
    return Optional.ofNullable(clazz.getAnnotation(annotation))
        .orElseGet(
            () ->
                Optional.ofNullable(clazz.getSuperclass())
                    .map(parent -> getAnnotationForClass(parent, annotation))
                    .orElse(null));
  }

  /**
   * Extracts out all the driver config parameters from a TestNG suite
   *
   * @param suite The TestNG Suite
   * @return A Set of Driver Config paths
   */
  public static Set<String> extractDriversFromSuiteFile(final ISuite suite) {
    final Set<String> fileNames = new HashSet<>();
    TestNGUtils.extractParameter(suite, CAPS_FILE).ifPresent(fileNames::add);
    suite
        .getXmlSuite()
        .getTests()
        .forEach(
            test -> {
              TestNGUtils.extractParameter(test, CAPS_FILE).ifPresent(fileNames::add);
              test.getClasses()
                  .forEach(
                      xmlClass ->
                          TestNGUtils.extractParameter(xmlClass, CAPS_FILE)
                              .ifPresent(fileNames::add));
            });
    return fileNames;
  }
}
