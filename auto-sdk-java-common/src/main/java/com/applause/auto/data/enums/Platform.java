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
package com.applause.auto.data.enums;

import java.util.Locale;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Enum representing the runtime configurations available under the Applause Automation framework.
 * When a component is created by the PageObjectFactory, all of its sub-elements and subcomponents
 * are found using locators specified for the current Platform in a @Locate annotation. Each
 * Platform has a "fallback" Platform, which the PageObjectFactory will attempt to use if an exact
 * match isn't available - for instance, if the current Platform is WEB_DESKTOP_CHROME, the
 * PageObjectFactory will try to find a WEB_DESKTOP locator if no WEB_DESKTOP_CHROME locator is
 * available. The current Platform is automatically populated at driver creation, but can be
 * overridden by setting a new Platform in the EnvironmentHelper.
 */
@Getter
public enum Platform {
  DEFAULT("Default"),
  MOBILE("Mobile"),
  MOBILE_ANDROID("MobileAndroid"),
  MOBILE_ANDROID_PHONE("MobileAndroidPhone"),
  MOBILE_ANDROID_TABLET("MobileAndroidTablet"),
  MOBILE_ANDROID_SMALL_TABLET("MobileAndroidSmallTablet"),
  MOBILE_IOS("MobileIOS"),
  MOBILE_IOS_PHONE("MobileIOSPhone"),
  MOBILE_IOS_TABLET("MobileIOSTablet"),
  MOBILE_IOS_SMALL_TABLET("MobileIOSSmallTablet"),
  WEB("Web"),
  WEB_DESKTOP("WebDesktop"),
  WEB_DESKTOP_CHROME("WebDesktopChrome"),
  WEB_DESKTOP_EDGE("WebDesktopEdge"),
  WEB_DESKTOP_FIREFOX("WebDesktopFirefox"),
  WEB_DESKTOP_IE("WebDesktopIE"),
  WEB_DESKTOP_SAFARI("WebDesktopSafari"),
  WEB_MOBILE("WebMobile"),
  WEB_MOBILE_PHONE("WebMobilePhone"),
  WEB_ANDROID_PHONE("WebAndroidPhone"),
  WEB_IOS_PHONE("WebIOSPhone"),
  WEB_MOBILE_TABLET("WebMobileTablet"),
  WEB_ANDROID_TABLET("WebAndroidTablet"),
  WEB_IOS_TABLET("WebIOSTablet"),
  WEB_MOBILE_SMALL_TABLET("WebMobileSmallTablet"),
  WEB_ANDROID_SMALL_TABLET("WebAndroidSmallTablet"),
  WEB_IOS_SMALL_TABLET("WebIOSSmallTablet"),
  OTT("OTT"),
  OTT_FIRE_TV("OttFireTv"),
  OTT_FIRE_TV_4K("OttFireTv4k"),
  OTT_APPLE_TV("OttAppleTv"),
  OTT_APPLE_TV_4K("OttAppleTv4k"),
  OTT_CHROMECAST("OttChromecast"),
  OTT_ANDROID_TV("OttAndroidTv");

  private static final Logger logger = LogManager.getLogger(Platform.class);
  private final String friendlyName;

  Platform(final String friendlyName) {
    this.friendlyName = friendlyName;
  }

  /**
   * Gets the Platform enum corresponding to a particular string.
   *
   * @param friendlyName the string value representing a particular Platform
   * @return the corresponding Platform
   */
  public static Platform getPlatform(final String friendlyName) {
    // Check for a concatenated friendlyName, like WebDesktopChrome
    for (Platform platform : values()) {
      if (platform.friendlyName.equalsIgnoreCase(friendlyName)) {
        return platform;
      }
    }
    // Check for an underscored friendly name, like WEB_DESKTOP_CHROME
    try {
      return valueOf(friendlyName.toUpperCase(Locale.ENGLISH));
    } catch (IllegalArgumentException ignored) {
      // IGNORED INTENTIONALLY
    }
    // Return DEFAULT if we haven't found either one of the above
    logger.error(
        String.format("Unrecognized Platform [%s]. Returning Platform [DEFAULT].", friendlyName));
    return DEFAULT;
  }

  /**
   * Gets the fallback configuration for the current Platform.
   *
   * @return the fallback Platform for the current Platform
   */
  @SuppressWarnings("PMD.CyclomaticComplexity")
  public Platform getFallback() {
    return switch (this) {
      case MOBILE, WEB, OTT -> DEFAULT;
      case MOBILE_ANDROID, MOBILE_IOS -> MOBILE;
      case MOBILE_ANDROID_PHONE, MOBILE_ANDROID_TABLET, MOBILE_ANDROID_SMALL_TABLET ->
          MOBILE_ANDROID;
      case MOBILE_IOS_PHONE, MOBILE_IOS_TABLET, MOBILE_IOS_SMALL_TABLET -> MOBILE_IOS;
      case WEB_DESKTOP, WEB_MOBILE -> WEB;
      case WEB_DESKTOP_CHROME,
              WEB_DESKTOP_EDGE,
              WEB_DESKTOP_FIREFOX,
              WEB_DESKTOP_IE,
              WEB_DESKTOP_SAFARI ->
          WEB_DESKTOP;
      case WEB_MOBILE_PHONE, WEB_MOBILE_TABLET, WEB_MOBILE_SMALL_TABLET -> WEB_MOBILE;
      case WEB_ANDROID_PHONE, WEB_IOS_PHONE -> WEB_MOBILE_PHONE;
      case WEB_ANDROID_TABLET, WEB_IOS_TABLET -> WEB_MOBILE_TABLET;
      case WEB_ANDROID_SMALL_TABLET, WEB_IOS_SMALL_TABLET -> WEB_MOBILE_SMALL_TABLET;
      case OTT_FIRE_TV, OTT_APPLE_TV, OTT_CHROMECAST, OTT_ANDROID_TV -> OTT;
      case OTT_APPLE_TV_4K -> OTT_APPLE_TV;
      case OTT_FIRE_TV_4K -> OTT_FIRE_TV;
      default -> null;
    };
  }

  /**
   * Check if Platform hierarchy chain is MobileNative, or ever falls back to MobileNative.
   *
   * @param p the current Platform
   * @return if chain falls back to MobileNative (Platform.MOBILE)
   */
  @SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
  public static boolean hasNativeFallback(final Platform p) {
    if (p == null || p.getFallback() == null) {
      return false;
    }
    if (p == Platform.MOBILE) {
      return true;
    }
    if (p.getFallback() == Platform.MOBILE) {
      return true;
    }
    return hasNativeFallback(p.getFallback());
  }

  @Override
  public String toString() {
    return friendlyName;
  }
}
