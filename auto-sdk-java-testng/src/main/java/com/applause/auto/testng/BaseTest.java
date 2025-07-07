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
import com.applause.auto.framework.ContextManager;
import com.applause.auto.framework.context.annotations.WithDriver;
import com.applause.auto.framework.json.BadJsonFormatException;
import com.applause.auto.integrations.assets.AssetsUtil;
import com.applause.auto.testng.listeners.FrameworkConfigurationListener;
import com.applause.auto.testng.listeners.ReportingSuiteListener;
import com.applause.auto.testng.listeners.ReportingTestListener;
import com.applause.auto.testng.listeners.SdkTestRailSuiteListener;
import java.util.Objects;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

/** Base TestNG Class to ensure all needed items are wired up properly for test execution. */
// Listeners are run in reverse order - we want the SDK Testrail work to be done last, so we can
// fetch
// appropriate data from the session close
@Listeners({
  FrameworkConfigurationListener.class,
  SdkTestRailSuiteListener.class,
  ReportingSuiteListener.class,
  ReportingTestListener.class,
})
@WithDriver
public class BaseTest {
  protected final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());
  protected static final ContextManager contextManager = ContextManager.INSTANCE;

  /** Prints a helpful message indicating that the test class is about to begin. */
  @BeforeClass(alwaysRun = true)
  public final void classStartPrint() {
    logger.info(
        "********* Starting Test Class : " + this.getClass().getSimpleName() + " *********");
  }

  /** Prints a helpful message indicating that the test class is about to end. */
  @AfterClass(alwaysRun = true)
  public final void classFinishPrint() {
    logger.info(
        "********* Finished with Test Class : " + this.getClass().getSimpleName() + " *********");
  }

  /** Hook executed before context setup */
  public void beforeContextSetup() {
    // Purposefully empty for overrides
  }

  /**
   * Hook executed after context setup
   *
   * @param context The context that was set up
   */
  public void afterContextSetup(final @NonNull FrameworkContext context) {
    // Purposefully empty for overrides
  }

  /**
   * Sets up a new context using the TestNG ITestContext
   *
   * @param context The TestNG context
   */
  public void setupContext(final ITestContext context) {
    try {
      this.setupContext(context, new Object[] {});
    } catch (final BadJsonFormatException e) {
      throw new RuntimeException("Could not setup context", e);
    }
  }

  /**
   * Sets up a new Applause Framework Context using the TestNG context and a set of Test Arguments
   *
   * @param testngContext The TestNG Context
   * @param testArgs An array of test parameters
   * @throws BadJsonFormatException If the driver could not be set up
   */
  @SuppressWarnings("PMD.UseVarargs")
  public void setupContext(final ITestContext testngContext, final Object[] testArgs)
      throws BadJsonFormatException {
    this.beforeContextSetup();
    final FrameworkContext context;
    if (Objects.nonNull(Reporter.getCurrentTestResult())) {
      context = TestNgContextUtils.setupContext(Reporter.getCurrentTestResult(), this, testArgs);
    } else {
      context = TestNgContextUtils.setupContext(testngContext, this);
    }
    this.afterContextSetup(context);
  }

  /**
   * Ties the Applause Framework Context to the TestNG result
   *
   * @param testResult The TestNG Result
   * @param testArgs The Test Parameters for this result
   */
  @BeforeMethod(alwaysRun = true)
  @SuppressWarnings("PMD.UseVarargs")
  public final void setTestResultFields(final ITestResult testResult, final Object[] testArgs) {
    // In cases where the SDETs add in a @BeforeClass to initialize the context, it may already be
    // setup. If it isn't, create one
    final var context =
        contextManager
            .getCurrentContext()
            .orElseGet(
                () -> {
                  try {
                    TestNgContextUtils.setupContext(testResult, this, testArgs);
                  } catch (BadJsonFormatException e) {
                    throw new RuntimeException("Could not setup context", e);
                  }
                  return ContextManager.INSTANCE.getCurrentContext().orElse(null);
                });

    // If the context type is NO_CONTEXT, this may still be null
    if (Objects.nonNull(context)) {
      TestNgContextUtils.registerWithTestResult(testResult, context);
    }
  }

  /**
   * Captures all Test Assets for the drivers
   *
   * @param testResult The TestNg Test Result
   */
  @AfterMethod(alwaysRun = true)
  public final void captureTestCaseAssets(final ITestResult testResult) {
    AssetsUtil.captureAssetsForDriver(testResult.getStatus() == ITestResult.FAILURE);
  }

  /** Resets the Applause Framework Context after the test completes */
  @AfterMethod(alwaysRun = true, dependsOnMethods = "captureTestCaseAssets")
  public void resetContext() {
    try {
      ContextManager.INSTANCE.resetContext();
    } catch (Exception e) {
      logger.error("Caught error trying to reset framework context.", e);
    }
  }
}
