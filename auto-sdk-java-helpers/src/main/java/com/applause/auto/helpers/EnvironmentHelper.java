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
package com.applause.auto.helpers;

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.context.IPageObjectExtension;
import com.applause.auto.data.enums.DriverType;
import java.util.Locale;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Helper class to provide information about the current Driver, namely, what platform is it, what
 * driver type, which browser, which version.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@AllArgsConstructor
public class EnvironmentHelper implements IPageObjectExtension {
  private static final Logger logger = LogManager.getLogger(EnvironmentHelper.class);
  private IPageObjectContext pageObjectContext;

  /**
   * determine the cast class type of the created driver object and set the DriverType into the
   * arraylist by position. This will be used to determine the right method to apply in the utility
   * classes.
   *
   * @return DriverType of the driver
   */
  public DriverType getDriverType() {
    return pageObjectContext.getDriverType();
  }

  // browser specific

  /**
   * checks if driver is a Safari driver
   *
   * @return is safari
   */
  public boolean isSafari() {
    return pageObjectContext.getDriverType() == DriverType.SAFARI;
  }

  /**
   * checks if driver is a Chrome driver
   *
   * @return is Chrome
   */
  public boolean isChrome() {
    return pageObjectContext.getDriverType() == DriverType.CHROME;
  }

  /**
   * checks if driver is a Firefox driver
   *
   * @return is Firefox
   */
  public boolean isFirefox() {
    return pageObjectContext.getDriverType() == DriverType.FIREFOX;
  }

  /**
   * checks if driver is a IE driver
   *
   * @return is IE
   */
  public boolean isIE() {
    return pageObjectContext.getDriverType() == DriverType.INTERNETEXPLORER;
  }

  /**
   * checks if driver is an Edge driver
   *
   * @return is Edge
   */
  public boolean isEdge() {
    return pageObjectContext.getDriverType() == DriverType.EDGE;
  }

  // platform-specific

  /**
   * Checks in a roundabout way if the driver is web on Android
   *
   * @return is mobile + web + Chrome
   */
  public boolean isAndroidMobileWeb() {
    String browserName =
        ((RemoteWebDriver) pageObjectContext.getDriver()).getCapabilities().getBrowserName();
    return isMobileAndroid() && null != browserName && "chrome".equalsIgnoreCase(browserName);
  }

  /**
   * Checks in a roundabout way if the driver is web on iOS
   *
   * @return is mobile + iOS + safari
   */
  public boolean isIOSMobileWeb() {
    String browserName =
        ((RemoteWebDriver) pageObjectContext.getDriver()).getCapabilities().getBrowserName();
    return isMobileIOS() && null != browserName && "safari".equalsIgnoreCase(browserName);
  }

  /**
   * checks if driver is Mobile Android
   *
   * @return is Mobile Android
   */
  public boolean isMobileAndroid() {
    return pageObjectContext.getDriverType() == DriverType.ANDROID;
  }

  /**
   * checks if driver is a Mobile iOS
   *
   * @return is Mobile iOS
   */
  public boolean isMobileIOS() {
    return pageObjectContext.getDriverType() == DriverType.IOS;
  }

  /**
   * checks if driver is a phone
   *
   * @return is driver a phone
   */
  public boolean isPhone() {
    // This is about as suspect as isTablet but with better accuracy
    return isAppiumDriver() && !isTablet();
  }

  /**
   * Checks in a roundabout way if the driver is a tablet (may not always be reliable)
   *
   * @return is probably a tablet
   */
  public boolean isTablet() {
    Object capability =
        ((RemoteWebDriver) pageObjectContext.getDriver())
            .getCapabilities()
            .getCapability("deviceName");
    if (null == capability) {
      logger.debug("Can't tell if driver is tablet based on no deviceName in capability.");
      return false;
    }
    if (isMobileAndroid()) {
      // This is such a hack. However, the only drivers for android tablets right now are Galaxy Tab
      return capability.toString().toLowerCase(Locale.ENGLISH).contains("tab");
    } else if (isMobileIOS()) {
      return capability.toString().toLowerCase(Locale.ENGLISH).contains("ipad");
    } else {
      return false;
    }
  }

  /**
   * checks if driver is Mobile
   *
   * @return is Mobile
   */
  public boolean isAppiumDriver() {
    return pageObjectContext.getDriverType() == DriverType.ANDROID
        || pageObjectContext.getDriverType() == DriverType.IOS;
  }

  /**
   * checks if driver is Android
   *
   * @return is Android
   */
  public boolean isAndroid() {
    return pageObjectContext.getDriverType() == DriverType.ANDROID;
  }

  /**
   * checks if driver is iOS
   *
   * @return is iOS
   */
  public boolean isiOS() {
    return pageObjectContext.getDriverType() == DriverType.IOS;
  }

  /**
   * checks driver version
   *
   * @return driver version string
   */
  public String getVersion() {
    return ((RemoteWebDriver) pageObjectContext.getDriver()).getCapabilities().getBrowserVersion();
  }
}
