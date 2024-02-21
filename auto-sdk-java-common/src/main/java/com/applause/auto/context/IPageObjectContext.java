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
package com.applause.auto.context;

import com.applause.auto.data.enums.DriverType;
import com.applause.auto.data.enums.Platform;
import java.time.Duration;
import java.util.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

/** Interface defining the information needed for the PageObject model. */
public interface IPageObjectContext {

  /**
   * Gets the WebDriver for this context
   *
   * @return The WebDriver
   */
  WebDriver getDriver();

  /**
   * Gets the Driver Type for this context
   *
   * @return The driver type
   */
  DriverType getDriverType();

  /**
   * Gets the current platform for this context
   *
   * @return The current Platform
   */
  Platform getPlatform();

  /**
   * Updates the current platform for this context
   *
   * @param platform The new platform
   * @return The updated context
   */
  IPageObjectContext setPlatform(Platform platform);

  /**
   * Gets the current timeout for this context
   *
   * @return The current timeout duration
   */
  Duration getTimeout();

  /**
   * Updates the current timeout for this context
   *
   * @param timeout The new timeout
   * @return The updated context
   */
  IPageObjectContext setTimeout(Duration timeout);

  /**
   * Gets the current polling interval for this context
   *
   * @return The current polling interval
   */
  Duration getPollingInterval();

  /**
   * Updates the current polling interval for this context
   *
   * @param pollingInterval The new polling interval
   * @return The updated context
   */
  IPageObjectContext setPollingInterval(Duration pollingInterval);

  /**
   * Gets the page object options for the context
   *
   * @return The current options
   */
  PageObjectOptions getOptions();

  /**
   * Gets a WebDriver Wait using the current timeout and polling interval
   *
   * @return The WebDriver wait
   */
  default Wait<WebDriver> getWait() {
    return this.getWait(getTimeout(), getPollingInterval());
  }

  /**
   * Gets a webDriver wait for this driver
   *
   * @param timeout The timeout durations
   * @param pollingInterval The polling interval
   * @return The WebDriver Wait Object
   */
  default Wait<WebDriver> getWait(final Duration timeout, final Duration pollingInterval) {
    return new FluentWait<>(getDriver())
        .withTimeout(timeout)
        .pollingEvery(pollingInterval)
        .ignoring(NoSuchElementException.class, WebDriverException.class);
  }
}
