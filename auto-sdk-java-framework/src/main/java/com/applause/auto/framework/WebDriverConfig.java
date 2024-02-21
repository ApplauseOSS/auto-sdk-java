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
import com.applause.auto.data.enums.Platform;
import java.util.Map;
import org.openqa.selenium.MutableCapabilities;

/**
 * Configuration object to be passed between the SessionManager and DriverManager. All fields are
 * used by SessionManager during the driver creation process.
 *
 * @param driverType A string indicating the current driver type, such as ANDROID or CHROME.
 * @param factoryKey Used to set the current Platform, so page objects can find their locators.
 * @param caps Passed to the driver/provider, to ensure that the driver can do everything we need
 * @param osName Used to switch between Android, iOS, and remote web drivers.
 */
public record WebDriverConfig(
    DriverType driverType, Platform factoryKey, Map<String, Object> caps, String osName) {
  /**
   * Create an instance of the WebDriverConfig
   *
   * @param driverType The driverType
   * @param factoryKey The factory key
   * @param caps the capabilities
   * @param osName the osName
   */
  public WebDriverConfig(
      final DriverType driverType,
      final Platform factoryKey,
      final Map<String, Object> caps,
      final String osName) {
    this.driverType = driverType;
    this.factoryKey = factoryKey;
    this.caps = CustomCapsDeserializer.removeIntLikeDoublesFromCaps(caps);
    this.osName = osName;
  }

  /**
   * Converts the capabilities String Map to a DesiredCapabilities object.
   *
   * @return a DesiredCapabilities object
   */
  public MutableCapabilities getDesiredCapabilities() {
    return new MutableCapabilities(caps());
  }
}
