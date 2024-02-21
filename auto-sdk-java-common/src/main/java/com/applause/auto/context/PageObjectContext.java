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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.WebDriver;

/** A context wrapping the WebDriver, used for construction of the PageObject model */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class PageObjectContext implements IPageObjectContext {
  @NonNull private final WebDriver driver;
  @NonNull private final DriverType driverType;
  @Setter private Platform platform = Platform.DEFAULT;
  @Setter private PageObjectOptions options = new PageObjectOptions();

  /**
   * Sets up a new PageObjectContext
   *
   * @param driver The WebDriver
   * @param driverType The type of web driver
   * @param timeout The timeout for this context
   * @param pollingInterval The polling interval for this context
   * @param platform The current platform
   */
  public PageObjectContext(
      final WebDriver driver,
      final DriverType driverType,
      final Duration timeout,
      final Duration pollingInterval,
      final Platform platform) {
    this(driver, driverType);
    this.platform = platform;
    this.options.setTimeout(timeout);
    this.options.setPollingInterval(pollingInterval);
  }

  @Override
  public Duration getTimeout() {
    return options.getTimeout();
  }

  @Override
  public final IPageObjectContext setTimeout(final Duration timeout) {
    options.setTimeout(timeout);
    return this;
  }

  @Override
  public Duration getPollingInterval() {
    return options.getPollingInterval();
  }

  @Override
  public final IPageObjectContext setPollingInterval(final Duration pollingInterval) {
    options.setPollingInterval(pollingInterval);
    return this;
  }
}
