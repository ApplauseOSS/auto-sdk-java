/*
 *
 * Copyright © 2025 Applause App Quality, Inc.
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
package com.applause.auto.helpers.mobile.deeplinks;

import com.applause.auto.helpers.mobile.MobileUtils;
import com.applause.auto.helpers.util.ThreadHelper;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HidesKeyboard;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

/** Provides some common mobile deeplinks navigation methods */
public final class MobileDeepLinksUtils {

  private static final Logger logger = LogManager.getLogger(MobileDeepLinksUtils.class);

  private MobileDeepLinksUtils() {
    // utility class
  }

  /**
   * Open deeplink on Android
   *
   * @param driver - mobile driver
   * @param androidDeepLink - Andrdoid deeplink DTO object
   */
  public static void openDeepLinkOnAndroid(
      @NonNull final AppiumDriver driver,
      @NonNull final NativeMobileAppCommonDeeplink.AndroidDeepLink androidDeepLink) {
    logger.info("Opening a Android deeplink: " + androidDeepLink.deepLinkUrl());
    Map<String, String> parameters = new HashMap<>();
    parameters.put("url", androidDeepLink.deepLinkUrl());
    parameters.put("package", androidDeepLink.deepLinkPackage());
    driver.executeScript("mobile:deepLink", parameters);
    // another possible approach
    // getMobileDriver().get(androidDeepLink.getDeepLinkUrl());
  }

  /**
   * Open deeplink on iOS
   *
   * @param driver - mobile driver
   * @param iosDeepLink - iOS deeplink DTO object
   * @param waitForSafariURLBarTimeoutInSec - wait for Safari URL bar to appear during opening a
   *     deeplink
   * @param waitForSafariURLBarPollingInSec - polling timeout for Safari URL bar to appear during
   *     opening a deeplink
   * @param <T> the driver
   */
  public static <T extends AppiumDriver & HidesKeyboard> void openDeepLinkOniOS(
      @NonNull final T driver,
      @NonNull final NativeMobileAppCommonDeeplink.IOSDeepLink iosDeepLink,
      final int waitForSafariURLBarTimeoutInSec,
      final int waitForSafariURLBarPollingInSec) {
    logger.info("Launch Safari and enter the deep link in the address bar");
    Map<String, String> parameters = new HashMap<>();
    parameters.put("bundleId", "com.apple.mobilesafari");
    driver.executeScript("mobile:launchApp", parameters);

    By urlButtonSelector =
        AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeButton' && name CONTAINS 'URL'");

    // Wait for the url button to appear and click on it so the text field will appear
    // iOS 13 now has the keyboard open by default because the URL field has focus when opening
    // the Safari browser
    final var wait =
        new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(waitForSafariURLBarTimeoutInSec))
            .pollingEvery(Duration.ofSeconds(waitForSafariURLBarPollingInSec))
            .ignoring(Exception.class);

    wait.until(ExpectedConditions.presenceOfElementLocated(urlButtonSelector));

    MobileUtils.hideKeyboard(driver);

    driver.findElement(urlButtonSelector).click();
    // URL bar is 'jumping' here on the left side, custom wait
    ThreadHelper.sleep(1000);

    By urlFieldSelector =
        AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeTextField' && name CONTAINS 'URL'");
    driver.findElement(urlFieldSelector).sendKeys(iosDeepLink.deepLinkUrl());
    By goSelector =
        AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeButton' && label CONTAINS 'go'");
    try {
      wait.until(ExpectedConditions.presenceOfElementLocated(goSelector));
      driver.findElement(goSelector).click();
    } catch (Exception e) {
      logger.error("iOS deeplink navigation confirmation button click issue");
    }
    By openSelector =
        AppiumBy.iOSNsPredicateString(
            "type == 'XCUIElementTypeButton' && (name CONTAINS 'Open' OR name == 'Go')");
    wait.until(ExpectedConditions.presenceOfElementLocated(openSelector));
    driver.findElement(openSelector).click();
  }
}
