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

import com.applause.auto.config.ConfigUtils;
import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.data.enums.DriverType;
import com.applause.auto.framework.selenium.EnhancedCapabilities;
import io.appium.java_client.AppiumClientConfig;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import java.net.URL;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.ClientConfig;

/** A DriverManager that initializes remote WebDrivers from a driver config */
@Log4j2
public class RemoteDriverManager extends AbstractDriverManager {
  private final URL seleniumGridUrl;
  private final String httpProxyUrl;

  /**
   * Constructs a new RemoteDriverManager pointed at the provided selenium grid
   *
   * @param seleniumGridUrl The URL of the selenium grid
   */
  public RemoteDriverManager(final URL seleniumGridUrl) {
    this(seleniumGridUrl, EnvironmentConfigurationManager.INSTANCE.get().httpProxyUrl());
  }

  /**
   * Constructs a new RemoteDriverManager pointed at the provided selenium grid
   *
   * @param seleniumGridUrl The URL of the selenium grid
   * @param httpProxyUrl The URL of the http proxy
   */
  public RemoteDriverManager(
      final @NonNull URL seleniumGridUrl, final @Nullable String httpProxyUrl) {
    super(EnvironmentConfigurationManager.INSTANCE.get().driverRetryCount());
    this.seleniumGridUrl = seleniumGridUrl;
    this.httpProxyUrl = httpProxyUrl;
  }

  @Override
  protected WebDriver createDriver(final EnhancedCapabilities driverConfig) {
    MutableCapabilities caps = this.getDefaultCapabilities(driverConfig, MutableCapabilities.class);

    final String type =
        Optional.ofNullable(driverConfig.getApplauseOptions().getOsName())
            .map(os -> os.toUpperCase(Locale.US))
            .orElse("");
    var config =
        ClientConfig.defaultConfig()
            .readTimeout(
                Duration.ofMinutes(
                    EnvironmentConfigurationManager.INSTANCE.get().seleniumReadTimeoutMinutes()))
            .baseUrl(seleniumGridUrl);

    // If we have an http proxy, set it on the ClientConfig
    if (Strings.isNotBlank(this.httpProxyUrl)) {
      config = config.proxy(ConfigUtils.getHttpProxy(this.httpProxyUrl));
    }
    return switch (type) {
      case "ANDROID" -> new AndroidDriver(AppiumClientConfig.fromClientConfig(config), caps);
      case "IOS" -> new IOSDriver(AppiumClientConfig.fromClientConfig(config), caps);
      default -> new RemoteWebDriver(new HttpCommandExecutor(config), caps);
    };
  }

  /**
   * Logs local driver creation
   *
   * @param driverType driver type
   * @param caps capabilities
   */
  @Override
  protected void logDriverCreation(final DriverType driverType, final MutableCapabilities caps) {
    log.info(
        "Create remote {} driver instance at url {} with caps {}",
        driverType,
        this.seleniumGridUrl,
        gsonLenient.toJson(caps.asMap()));
  }
}
