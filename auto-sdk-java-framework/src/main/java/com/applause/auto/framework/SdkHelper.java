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

import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.context.FrameworkContext;
import com.applause.auto.context.IContextConnector;
import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.helpers.AnalyticsHelper;
import com.applause.auto.helpers.EnvironmentHelper;
import com.applause.auto.helpers.QueryHelper;
import com.applause.auto.helpers.SyncHelper;
import com.applause.auto.helpers.control.BrowserControl;
import com.applause.auto.helpers.control.DeviceControl;
import com.applause.auto.pageobjectmodel.base.BaseComponent;
import com.applause.auto.pageobjectmodel.base.BaseElement;
import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;
import com.applause.auto.templates.TemplateManager;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import lombok.NonNull;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;

/** This helper class gives hooks into the current context */
public final class SdkHelper {

  private static final ContextManager contextManager = ContextManager.INSTANCE;

  private SdkHelper() {
    // utility class
  }

  /**
   * Sets up a new PageObjectModel.
   *
   * <p>Note: This is only applicable to a Driver context. In a Driverless Context, this will throw
   * an error and fail the test.
   *
   * @param pageObjectClass the page object class to set up
   * @param <T> must extend BaseComponent
   * @param args constructor arguments for the class - may be left unspecified to use the default
   *     no-args constructor
   * @return a new instance of the component class T
   * @deprecated Use PageObjectBuilder
   */
  @Deprecated(forRemoval = true)
  public static <T extends BaseComponent> T create(
      final @NonNull Class<T> pageObjectClass, final Object... args) {
    if (EnvironmentConfigurationManager.INSTANCE.get().performanceLogging()) {
      return PageObjectBuilder.withContext(getDriverContext())
          .forBaseComponent(pageObjectClass)
          .withInterceptor(getAnalyticsHelper().getInterceptor())
          .withFormat(args)
          .initialize();
    } else {
      return PageObjectBuilder.withContext(getDriverContext())
          .forBaseComponent(pageObjectClass)
          .withFormat(args)
          .initialize();
    }
  }

  /**
   * Sets up a new PageObjectModel.
   *
   * <p>Note: This is only applicable to a Driver context. In a Driverless Context, this will throw
   * an error and fail the test.
   *
   * @param pageObjectClass the page object class to set up
   * @param parent the parent of the page object
   * @param <T> must extend BaseComponent
   * @param args constructor arguments for the class - may be left unspecified to use the default
   *     no-args constructor
   * @return a new instance of the component class T
   * @deprecated Use PageObjectBuilder
   */
  @Deprecated(forRemoval = true)
  public static <T extends BaseComponent> T create(
      final @NonNull Class<T> pageObjectClass,
      final @NonNull BaseElement parent,
      final Object... args) {
    if (EnvironmentConfigurationManager.INSTANCE.get().performanceLogging()) {
      return PageObjectBuilder.withContext(getDriverContext())
          .forBaseComponent(pageObjectClass)
          .withInterceptor(getAnalyticsHelper().getInterceptor())
          .withFormat(args)
          .initialize(parent);
    } else {
      return PageObjectBuilder.withContext(getDriverContext())
          .forBaseComponent(pageObjectClass)
          .withFormat(args)
          .initialize();
    }
  }

  /**
   * Gets the Current driver.
   *
   * <p>Note: This is only applicable to a Driver context. In a Driverless Context, this will throw
   * an error and fail the test.
   *
   * @return The WebDriver in the current context
   */
  public static WebDriver getDriver() {
    return getDriverContext().getDriver();
  }

  /**
   * Gets the Screenshot Helper.
   *
   * <p>Note: This is only applicable to a Driver context. In a Driverless Context, this will throw
   * an error and fail the test.
   *
   * @return The ScreenshotHelper in the current context
   * @throws TemplateManager.TemplateProcessException If the output path could not be resolved
   */
  public static Path getOutputPath() throws TemplateManager.TemplateProcessException {
    return getFrameworkContext().getOutputPath();
  }

  /**
   * Gets the default wait set on the current context.
   *
   * <p>Note: This is only applicable to a Driver context. In a Driverless Context, this will throw
   * an error and fail the test.
   *
   * @return The Default Wait in the current context
   */
  public static Wait<WebDriver> getDefaultWait() {
    return getDriverContext().getWait();
  }

  /**
   * Sets the timeout duration for all Waits in the SyncHelper.
   *
   * @param timeout the duration
   */
  public static void setTimeout(final @NonNull Duration timeout) {
    getDriverContext().setTimeout(timeout);
  }

  /**
   * Sets the timeout duration for all Waits in the SyncHelper.
   *
   * @param seconds the duration, in integer seconds
   */
  public static void setTimeout(int seconds) {
    setTimeout(Duration.ofSeconds(seconds));
  }

  /**
   * Sets the polling interval for all Waits in the SyncHelper.
   *
   * @param pollingInterval the duration, in integer seconds
   */
  public static void setPollingInterval(final @NonNull Duration pollingInterval) {
    getDriverContext().setPollingInterval(pollingInterval);
  }

  /**
   * Sets the polling interval for all Waits in the SyncHelper.
   *
   * @param seconds the duration, in integer seconds
   */
  public static void setPollingInterval(int seconds) {
    setPollingInterval(Duration.ofSeconds(seconds));
  }

  /**
   * Gets the Analytics Helper.
   *
   * <p>Note: This is only applicable to a Driver context. In a Driverless Context, this will throw
   * an error and fail the test.
   *
   * @return The AnalyticsHelper in the current context
   */
  public static AnalyticsHelper getAnalyticsHelper() {
    return contextManager
        .getCurrentContext()
        .map(context -> context.getExtension(AnalyticsHelper.class))
        .orElseThrow();
  }

  /**
   * Gets the Environment Helper.
   *
   * <p>Note: This is only applicable to a Driver context. In a Driverless Context, this will throw
   * an error and fail the test.
   *
   * @return The EnvironmentHelper in the current context
   */
  public static EnvironmentHelper getEnvironmentHelper() {
    return contextManager
        .getCurrentContext()
        .map(context -> context.getExtension(EnvironmentHelper.class))
        .orElseThrow();
  }

  /**
   * Gets the Query Helper.
   *
   * <p>Note: This is only applicable to a Driver context. In a Driverless Context, this will throw
   * an error and fail the test.
   *
   * @return The QueryHelper in the current context
   */
  public static QueryHelper getQueryHelper() {
    return contextManager
        .getCurrentContext()
        .map(context -> context.getExtension(QueryHelper.class))
        .orElseThrow();
  }

  /**
   * Gets the Sync Helper.
   *
   * <p>Note: This is only applicable to a Driver context. In a Driverless Context, this will throw
   * an error and fail the test.
   *
   * @return The SyncHelper in the current context
   */
  public static SyncHelper getSyncHelper() {
    return contextManager
        .getCurrentContext()
        .map(context -> context.getExtension(SyncHelper.class))
        .orElseThrow();
  }

  /**
   * Gets the DeviceControl Helper.
   *
   * <p>Note: This is only applicable to a Driver context. In a Driverless Context, this will throw
   * an error and fail the test.
   *
   * @return The DeviceControl in the current context
   */
  public static DeviceControl getDeviceControl() {
    return contextManager
        .getCurrentContext()
        .map(context -> context.getExtension(DeviceControl.class))
        .orElseThrow();
  }

  /**
   * Gets the Test Result ID, if provided
   *
   * @return The Test Result ID
   */
  public static Long getTestResultId() {
    return Optional.of(getFrameworkContext().getConnector())
        .map(IContextConnector::getResultId)
        .orElse(null);
  }

  /**
   * Gets the Browser Control Helper.
   *
   * <p>Note: This is only applicable to a Driver context. In a Driverless Context, this will throw
   * an error and fail the test.
   *
   * @return The BrowserControl in the current context
   */
  public static BrowserControl getBrowserControl() {
    return contextManager
        .getCurrentContext()
        .map(context -> context.getExtension(BrowserControl.class))
        .orElseThrow();
  }

  /**
   * Gets the current context as a DriverContext. This will throw an error in a Driverless context.
   *
   * @return The current DriverContext.
   */
  public static IPageObjectContext getDriverContext() {
    final var context =
        contextManager
            .getCurrentContext()
            .orElseThrow(() -> new RuntimeException("No Context not Set Up"));
    return context
        .getPageObjectContext()
        .orElseThrow(() -> new RuntimeException("Current context not a driver context"));
  }

  /**
   * Gets the current context as a DriverlessContext.
   *
   * @return The current DriverlessContext.
   */
  public static FrameworkContext getFrameworkContext() {
    return contextManager
        .getCurrentContext()
        .orElseThrow(() -> new RuntimeException("No Context not Set Up"));
  }
}
