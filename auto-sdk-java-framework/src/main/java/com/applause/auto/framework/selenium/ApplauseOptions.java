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
package com.applause.auto.framework.selenium;

import com.applause.auto.data.enums.DriverType;
import com.applause.auto.data.enums.Platform;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.openqa.selenium.MutableCapabilities;

/** Applause Option */
public class ApplauseOptions extends MutableCapabilities {
  /**
   * Initializes an ApplauseOptions instance from a capability map
   *
   * @param map The capability map
   */
  public ApplauseOptions(final @NonNull Map<String, Object> map) {
    super(map);
  }

  /**
   * Returns the value of the isMobileNative capability inside the applause:options field. Defaults
   * to false.
   *
   * @return If the capabilities are defined for a mobile native driver
   */
  public boolean isMobileNative() {
    // By default, we are not Mobile Native.  If it's desired, we need to turn it on
    return Optional.ofNullable(getCapability(ApplauseCapabilitiesConstants.IS_MOBILE_NATIVE))
        .map(Object::toString)
        .map(Boolean::valueOf)
        .orElse(false);
  }

  /**
   * Returns the value of the driverType capability inside the applause:options field.
   *
   * @return The DriverType
   */
  public DriverType getDriverType() {
    return Optional.ofNullable(getCapability(ApplauseCapabilitiesConstants.DRIVER_TYPE))
        .map(Object::toString)
        .map(DriverType::fromString)
        .orElse(null);
  }

  /**
   * * Returns the value of the osName capability inside the applause:options field.
   *
   * @return The OS name represented as a String
   */
  public String getOsName() {
    return Optional.ofNullable(getCapability(ApplauseCapabilitiesConstants.OS_NAME))
        .map(Object::toString)
        .orElse(null);
  }

  /**
   * * Returns the value of the factoryKey capability inside the applause:options field.
   *
   * @return the factory key
   */
  public Platform getFactoryKey() {
    return Optional.ofNullable(getCapability(ApplauseCapabilitiesConstants.FACTORY_KEY))
        .map(Object::toString)
        .map(Platform::getPlatform)
        .orElse(null);
  }
}
