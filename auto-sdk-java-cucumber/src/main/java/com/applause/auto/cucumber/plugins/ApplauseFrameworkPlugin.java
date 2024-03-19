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
package com.applause.auto.cucumber.plugins;

import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.config.ApplauseSdkConfigBean;
import com.applause.auto.config.AutoApiPropertyHelper;
import com.applause.auto.config.ConfigUtils;
import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.config.SdkConfigBean;
import com.applause.auto.framework.ApplauseFramework;
import com.applause.auto.framework.ContextManager;
import com.applause.auto.framework.json.BadJsonFormatException;
import com.applause.auto.helpers.AnalyticsHelper;
import com.applause.auto.helpers.ApplauseAppPushHelper;
import com.applause.auto.helpers.ApplauseConfigHelper;
import com.applause.auto.helpers.EnvironmentHelper;
import com.applause.auto.helpers.QueryHelper;
import com.applause.auto.helpers.ScreenshotHelper;
import com.applause.auto.helpers.SyncHelper;
import com.applause.auto.helpers.control.BrowserControl;
import com.applause.auto.helpers.control.DeviceControl;
import com.applause.auto.integrations.CapabilityOverriders;
import com.applause.auto.integrations.TestCycleCloneUtil;
import com.applause.auto.logging.LogOutputSingleton;
import com.applause.auto.logging.ResultPropertyMap;
import com.applause.auto.templates.TemplateManager;
import com.google.common.collect.Sets;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunStarted;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/** Cucumber plugin to handle the configuration of the applause framework */
@Log4j2
public class ApplauseFrameworkPlugin implements ConcurrentEventListener {
  private static final String CONFIG_DIR = "cfg";

  @SneakyThrows
  @SuppressWarnings("PMD.UnusedFormalParameter")
  private void testRunStarted(final TestRunStarted event) {
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

    performDriverChecks();

    // Reload the config and add them to the ResultPropertyMap
    ResultPropertyMap.loadGlobalProperties(
        ConfigUtils.toPropertyMap(
            EnvironmentConfigurationManager.INSTANCE.get(), SdkConfigBean.class));
    ResultPropertyMap.loadGlobalProperties(
        ConfigUtils.toPropertyMap(
            ApplauseEnvironmentConfigurationManager.INSTANCE.get(), ApplauseSdkConfigBean.class));
    ResultPropertyMap.loadGlobalProperties(ConfigUtils.getSystemProperties());

    // blow away logs, so they contain only stuff from each test
    LogOutputSingleton.flush();
  }

  @Override
  public void setEventPublisher(final EventPublisher publisher) {
    publisher.registerHandlerFor(TestRunStarted.class, this::testRunStarted);
  }

  private void performDriverChecks() throws BadJsonFormatException {
    if (EnvironmentConfigurationManager.INSTANCE.get().capsFile() == null) {
      log.trace("No capsFile to perform checks on.");
      return;
    }
    final var expectedDrivers = Set.of(EnvironmentConfigurationManager.INSTANCE.get().capsFile());
    final var missingDrivers =
        Sets.difference(expectedDrivers, ContextManager.INSTANCE.getDriverMap().keySet());
    if (!missingDrivers.isEmpty()) {
      throw new RuntimeException(
          "Unable to start run, missing local driver config files: "
              + String.join(", ", missingDrivers));
    }

    if (EnvironmentConfigurationManager.INSTANCE.get().useLocalDrivers()) {
      log.trace("App auto-detection/upload is disabled for local drivers");
      return;
    }

    // For every driver that we are aware of at this time, check to see if we might need an app for
    // any of them
    for (var driver : expectedDrivers) {
      final var expectedDriverCaps =
          ContextManager.INSTANCE.lookupDriver(driver).getCurrentCapabilities();
      if (!expectedDriverCaps.getApplauseOptions().isMobileNative()) {
        continue;
      }
      if (expectedDriverCaps.getCapabilityNames().contains("app")) {
        continue;
      }
      ApplauseAppPushHelper.performApplicationPushIfNecessary();
      ApplauseAppPushHelper.autoDetectBuildIfNecessary();
      break;
    }
  }
}
