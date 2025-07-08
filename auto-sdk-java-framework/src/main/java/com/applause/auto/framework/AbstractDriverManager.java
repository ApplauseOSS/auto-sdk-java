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

import com.applause.auto.data.enums.DriverType;
import com.applause.auto.framework.selenium.EnhancedCapabilities;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.RetryPolicyBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

/**
 * Abstract class defining the framework for setting up new drivers using different methods
 *
 * @see LocalDriverManager For setting up local drivers
 * @see RemoteDriverManager For setting up remote drivers
 */
@RequiredArgsConstructor
@Log4j2
public abstract class AbstractDriverManager {
  protected static final Gson gsonLenient = new GsonBuilder().create();
  private final RetryPolicyBuilder<Object> policyBuilder =
      RetryPolicy.builder()
          .handle(WebDriverException.class)
          .onFailedAttempt(att -> log.error("Driver Connection attempt failed {}", att));

  private final int driverCreationRetry;

  /** Default constructor with a driver retry count of 0 */
  public AbstractDriverManager() {
    this.driverCreationRetry = 0;
  }

  /**
   * Sets up a new driver with the provided capabilities
   *
   * @param caps The Applause capabilities
   * @return The newly created WebDriver
   */
  public WebDriver getDriver(final EnhancedCapabilities caps) {
    RetryPolicy<Object> policy;
    if (driverCreationRetry > 0) {
      policy = policyBuilder.withMaxAttempts(driverCreationRetry).build();
    } else {
      log.debug("No retries provided for driver");
      policy = policyBuilder.build();
    }
    return Failsafe.with(policy).get(() -> createDriver(caps));
  }

  protected abstract WebDriver createDriver(EnhancedCapabilities config);

  /**
   * Quits the given driver
   *
   * @param driver The driver to quit
   * @return true if the driver was successfully ended
   */
  public boolean quitDriver(final @NonNull WebDriver driver) {
    try {
      driver.quit();
      return true;
    } catch (WebDriverException e) {
      log.warn("Failed to quit driver", e);
      return false;
    }
  }

  /**
   * Handles setting up and merging the default set of capabilities
   *
   * @param <T> The type of capabilities
   * @param caps The desired capabilities of the driver config
   * @param clazz The capabilities class
   * @return The fully configured capabilities
   */
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public <T extends MutableCapabilities> T getDefaultCapabilities(
      final EnhancedCapabilities caps, final Class<T> clazz) {
    final var res = (T) clazz.getConstructor().newInstance().merge(caps);
    logDriverCreation(caps.getApplauseOptions().getDriverType(), res);
    return res;
  }

  /**
   * Logs local driver creation
   *
   * @param driverType driver type
   * @param caps capabilities
   */
  protected void logDriverCreation(final DriverType driverType, final MutableCapabilities caps) {
    log.info(
        "Create remote {} driver instance with caps {}",
        driverType,
        gsonLenient.toJson(caps.asMap()));
  }
}
