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
import com.applause.auto.framework.selenium.EnhancedCapabilities;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import java.net.URL;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

/** DriverManager implementation for creating local drivers */
public class LocalDriverManager extends AbstractDriverManager {
  private final URL appiumUrl;

  /**
   * Set up a local driver manager with a local appium grid
   *
   * @param appiumUrl The local appium grid
   */
  public LocalDriverManager(final @Nullable URL appiumUrl) {
    super(EnvironmentConfigurationManager.INSTANCE.get().driverRetryCount());
    this.appiumUrl = appiumUrl;
  }

  @Override
  public WebDriver createDriver(final @NonNull EnhancedCapabilities driverConfig) {
    return switch (driverConfig.getApplauseOptions().getDriverType()) {
      case CHROME ->
          new ChromeDriver(this.getDefaultCapabilities(driverConfig, ChromeOptions.class));
      case EDGE -> new EdgeDriver(this.getDefaultCapabilities(driverConfig, EdgeOptions.class));
      case FIREFOX ->
          new FirefoxDriver(this.getDefaultCapabilities(driverConfig, FirefoxOptions.class));
      case SAFARI ->
          new SafariDriver(this.getDefaultCapabilities(driverConfig, SafariOptions.class));
      case INTERNETEXPLORER ->
          new InternetExplorerDriver(
              this.getDefaultCapabilities(driverConfig, InternetExplorerOptions.class));
      case ANDROID ->
          new AndroidDriver(
              appiumUrl, this.getDefaultCapabilities(driverConfig, MutableCapabilities.class));
      case IOS ->
          new IOSDriver(
              appiumUrl, this.getDefaultCapabilities(driverConfig, MutableCapabilities.class));
      case MOBILENATIVE ->
          new AppiumDriver(
              appiumUrl, this.getDefaultCapabilities(driverConfig, MutableCapabilities.class));
      default ->
          throw new RuntimeException(
              "Could not create Applause Driver: Invalid Driver Type "
                  + driverConfig.getApplauseOptions().getDriverType().toString());
    };
  }
}
