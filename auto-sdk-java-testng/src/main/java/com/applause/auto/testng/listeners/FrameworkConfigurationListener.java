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
package com.applause.auto.testng.listeners;

import com.applause.auto.config.*;
import com.applause.auto.framework.ApplauseFramework;
import com.applause.auto.framework.ContextManager;
import com.applause.auto.framework.json.BadJsonFormatException;
import com.applause.auto.helpers.*;
import com.applause.auto.helpers.control.BrowserControl;
import com.applause.auto.helpers.control.DeviceControl;
import com.applause.auto.integrations.CapabilityOverriders;
import com.applause.auto.integrations.TestCycleCloneUtil;
import com.applause.auto.logging.LogOutputSingleton;
import com.applause.auto.logging.ResultPropertyMap;
import com.applause.auto.templates.TemplateManager;
import com.applause.auto.testng.TestNgContextUtils;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/** Configures the ApplauseFramework before execution */
@Log4j2
public class FrameworkConfigurationListener implements ISuiteListener {
  private static final String CONFIG_DIR = "cfg/";

  /**
   * Triggered on start of test run
   *
   * @param suite test context
   */
  @Override
  @SuppressWarnings({"PMD.PreserveStackTrace"})
  @SneakyThrows({
    MalformedURLException.class,
    URISyntaxException.class,
    BadJsonFormatException.class
  })
  public void onStart(final ISuite suite) {
    SdkConfigBean sdkConfigBean = EnvironmentConfigurationManager.INSTANCE.get();
    ApplauseSdkConfigBean applauseConfigBean =
        ApplauseEnvironmentConfigurationManager.INSTANCE.get();

    // A bunch of validation and app handling
    AutoApiPropertyHelper.dumpConfigProperties();
    ApplauseConfigHelper.validateConfiguration();

    // Configure the framework
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    ApplauseFramework.INSTANCE
        .setDefaultDriver(sdkConfigBean.capsFile())
        .registerDrivers(Objects.requireNonNull(loader.getResource(CONFIG_DIR)).toURI())
        .registerDrivers(Paths.get("src", "main", "resources", CONFIG_DIR))
        .setDefaultTimeout(Duration.ofSeconds(sdkConfigBean.defaultTimeoutSeconds()))
        .setDefaultPollingInterval(
            Duration.ofSeconds(sdkConfigBean.defaultPollingIntervalSeconds()))
        .registerCapabilityOverrider(CapabilityOverriders::addApplauseOptions)
        .registerCapabilityOverrider(CapabilityOverriders::enablePerformanceLogging)
        .registerPageObjectExtension(AnalyticsHelper.class, AnalyticsHelper::new)
        .registerPageObjectExtension(EnvironmentHelper.class, EnvironmentHelper::new)
        .registerPageObjectExtension(QueryHelper.class, QueryHelper::new)
        .registerPageObjectExtension(SyncHelper.class, SyncHelper::new)
        .registerPageObjectExtension(BrowserControl.class, BrowserControl::new)
        .registerPageObjectExtension(DeviceControl.class, DeviceControl::new)
        .registerFrameworkExtension(ScreenshotHelper.class, ScreenshotHelper::new);
    try {
      ApplauseFramework.INSTANCE.setOutputPath(
          sdkConfigBean.localResultsDirectory() == null
              ? "results/${testRunId!\"local\"}/"
              : sdkConfigBean.localResultsDirectory());
    } catch (TemplateManager.TemplateGenerationException e) {
      log.error(
          "Could not parse local results directory template, not setting local results directory to user configured value (using default)",
          e);
      try {
        ApplauseFramework.INSTANCE.setOutputPath(sdkConfigBean.localResultsDirectory());
      } catch (TemplateManager.TemplateGenerationException ex) {
        throw new RuntimeException(ex);
      }
    }

    // Handling of the DriverManager setup
    if (sdkConfigBean.useSeleniumGrid()) {
      ApplauseFramework.INSTANCE.pointToSeleniumGrid(sdkConfigBean.seleniumGridLocation());
    } else if (sdkConfigBean.useLocalDrivers()) {
      ApplauseFramework.INSTANCE.useLocalDrivers(sdkConfigBean.localAppiumUrl());
    } else {
      ApplauseFramework.INSTANCE.pointToSeleniumGrid(
          applauseConfigBean.seleniumProxyUrl(), "ApplauseKey", applauseConfigBean.apiKey());
    }
    TestCycleCloneUtil.performTestCycleCloneIfNecessary();

    // Only run app auto-detect/upload logic if: we are using a remote driver, have
    // a capability file, and that capability file is a mobile native driver
    if (!sdkConfigBean.useLocalDrivers()
        && sdkConfigBean.capsFile() != null
        && ContextManager.INSTANCE
            .lookupDriver(sdkConfigBean.capsFile())
            .getCurrentCapabilities()
            .getApplauseOptions()
            .isMobileNative()) {
      ApplauseAppPushHelper.autoDetectBuildIfNecessary();
      ApplauseAppPushHelper.performApplicationPushIfNecessary();
    }

    // Reload the config and add them to the ResultPropertyMap
    ResultPropertyMap.loadGlobalProperties(
        ConfigUtils.toPropertyMap(
            EnvironmentConfigurationManager.INSTANCE.get(), SdkConfigBean.class));
    ResultPropertyMap.loadGlobalProperties(
        ConfigUtils.toPropertyMap(
            ApplauseEnvironmentConfigurationManager.INSTANCE.get(), ApplauseSdkConfigBean.class));
    ResultPropertyMap.loadGlobalProperties(ConfigUtils.getSystemProperties());

    final var drivers = TestNgContextUtils.extractDriversFromSuiteFile(suite);
    drivers.removeAll(ContextManager.INSTANCE.getDriverMap().keySet());
    if (!drivers.isEmpty()) {
      throw new RuntimeException(
          "Unable to start run, missing local driver config files: " + String.join(", ", drivers));
    }

    // blow away logs, so they contain only stuff from each test
    LogOutputSingleton.flush();
  }
}
