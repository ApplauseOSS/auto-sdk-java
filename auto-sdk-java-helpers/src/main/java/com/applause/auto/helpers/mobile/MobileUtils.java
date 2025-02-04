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
package com.applause.auto.helpers.mobile;

import com.applause.auto.helpers.util.ThreadHelper;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HidesKeyboard;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.android.StartsActivity;
import io.appium.java_client.ios.IOSDriver;
import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebDriver;

/** Common mobile native utils */
public class MobileUtils {

  private static final Logger logger = LogManager.getLogger(MobileUtils.class);

  private static final String CF_BUNDLE_IDENTIFIER = "CFBundleIdentifier";

  /**
   * Scroll down scroll view X times
   *
   * @param driver - automation driver
   * @param pStartXCoef - start x coef. ( pStartXCoef * x screen width)
   * @param pStartYCoef - start y coef. ( pStartYCoef * Y screen height)
   * @param pEndYCoef - end y coef. ( pEndYCoef * Y screen height)
   * @param waitOption1 - wait action after first press
   * @param waitOption2 - wait action after move to
   * @param scrollTimes - how many times to scroll
   */
  public static void scrollVerticalSeveralTimes(
      AppiumDriver driver,
      double pStartXCoef,
      double pStartYCoef,
      double pEndYCoef,
      int waitOption1,
      int waitOption2,
      int scrollTimes) {
    IntStream.range(0, scrollTimes)
        .forEach(
            action ->
                scrollVertical(
                    driver, pStartXCoef, pStartYCoef, pEndYCoef, waitOption1, waitOption2));
  }

  /**
   * Scroll down scroll view
   *
   * @param driver - automation driver
   * @param pStartXCoef - start x coef. ( pStartXCoef * x screen width)
   * @param pStartYCoef - start y coef. ( pStartYCoef * Y screen height)
   * @param pEndYCoef - end y coef. ( pEndYCoef * Y screen height)
   * @param waitOption1 - wait action after first press
   * @param waitOption2 - wait action after move to
   */
  public static void scrollVertical(
      AppiumDriver driver,
      double pStartXCoef,
      double pStartYCoef,
      double pEndYCoef,
      int waitOption1,
      int waitOption2) {

    Dimension size = driver.manage().window().getSize();

    int startY = (int) (size.getHeight() * pStartYCoef);
    int endY = (int) (size.getHeight() * pEndYCoef);
    int startX = (int) (size.getWidth() * pStartXCoef); // Make sure startX is an int

    logger.info(
        "Swiping Down: [startX]: " + startX + " , [startY]: " + startY + " , [endY]: " + endY);

    try {
      PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
      Sequence scroll = new Sequence(finger, 1);

      scroll.addAction(
          finger.createPointerMove(
              Duration.ofMillis(0), PointerInput.Origin.viewport(), startX, startY));
      scroll.addAction(finger.createPointerDown(0));
      scroll.addAction(
          finger.createPointerMove(
              Duration.ofMillis(waitOption1), PointerInput.Origin.viewport(), startX, startY));
      scroll.addAction(
          finger.createPointerMove(
              Duration.ofMillis(waitOption2), PointerInput.Origin.viewport(), startX, endY));
      scroll.addAction(finger.createPointerUp(0));

      driver.perform(List.of(scroll));

    } catch (Exception wex) {
      logger.warn(
          "Swipe cause error, probably nothing to swipe or driver issue: " + wex.getMessage());
    }
  }

  /** Clear chrome cache locally through ADB shell */
  public static void clearAndroidChromeCacheForLocalExecution() {
    logger.info("Clearing android chrome cache");
    new ProcessBuilder().command("db", "shell", "pm", "clear", "com.android.chrome");
  }

  /**
   * Hide mobile keyboard
   *
   * @param driver - automation driver
   */
  public static void hideKeyboard(HidesKeyboard driver) {
    try {
      driver.hideKeyboard();
    } catch (Exception e) {
      logger.error("Error occured while hiding a keyboard");
    }
  }

  /**
   * Press screen by coordinates
   *
   * @param driver - automation driver
   * @param x - x coordinate press
   * @param y - y coordinate press
   * @param waitOption - wait action option value in millis
   */
  public static void pressByCoordinates(AppiumDriver driver, int x, int y, long waitOption) {
    logger.info("Pressing natively by coordinates: " + x + " " + y);

    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    Sequence press = new Sequence(finger, 1);

    press.addAction(
        finger.createPointerMove(
            Duration.ofMillis(0), PointerInput.Origin.viewport(), x, y)); // Move to coordinates
    press.addAction(finger.createPointerDown(0)); // Press down
    if (waitOption > 0) { // Add wait if specified
      press.addAction(
          finger.createPointerMove(
              Duration.ofMillis(waitOption), PointerInput.Origin.viewport(), x, y)); // Hold
    }
    press.addAction(finger.createPointerUp(0)); // Release

    driver.perform(List.of(press));
  }

  /**
   * Refresh page source (for mobile UIs with big page layout xml)
   *
   * @param driver - automation driver
   * @param waitBeforePageSourceRefresh - wait before page source refresh in millis
   * @param waitAfterPageSourceRefresh - wait after page source refresh in millis
   */
  public static void refreshPageSource(
      WebDriver driver, int waitBeforePageSourceRefresh, int waitAfterPageSourceRefresh) {
    logger.info("Refresh page source");
    ThreadHelper.sleep(waitBeforePageSourceRefresh);
    getMobileDriver(driver).getPageSource();
    ThreadHelper.sleep(waitAfterPageSourceRefresh);
  }

  /**
   * Get mobile appium driver
   *
   * @param driver - automation driver
   * @return casted mobile driver or exception if driver casting to mobile one is not supported
   */
  public static AppiumDriver getMobileDriver(WebDriver driver) {
    if (driver instanceof AppiumDriver) {
      return (AppiumDriver) driver;
    } else {
      throw new IllegalStateException("No mobile driver found");
    }
  }

  /**
   * Get App Bundle Identifier for android device
   *
   * @return String
   */
  public static String getBundleIdentifierAndroid(StartsActivity driver) {
    String bundleId = driver.getCurrentPackage();
    logger.info(String.format("App bundle ID is [%s]", bundleId));
    return bundleId;
  }

  /**
   * Get App Bundle Identifier iOS
   *
   * @return String
   */
  public static String getBundleIdentifierIos(RemoteWebDriver driver) {
    String bundleId =
        getMobileDriver(driver).getCapabilities().getCapability(CF_BUNDLE_IDENTIFIER).toString();
    logger.info(String.format("App bundle ID is [%s]", bundleId));
    return bundleId;
  }

  /**
   * Get App Bundle Identifier
   *
   * @return String
   */
  public static String getBundleIdentifier(RemoteWebDriver driver) {
    return driver instanceof IOSDriver
        ? getBundleIdentifierIos(driver)
        : getBundleIdentifierAndroid((StartsActivity) driver);
  }

  /** Move App In Background */
  public static void moveAppToBackground(InteractsWithApps driver) {
    logger.info("Move App To Background");
    driver.runAppInBackground(Duration.ofSeconds(-1));
  }

  /**
   * Activate App using bundleId and Return to AUT
   *
   * @param bundleId Bundle ID
   */
  public static void activateApp(InteractsWithApps driver, String bundleId) {
    logger.info("Return to AUT.");
    driver.activateApp(bundleId);
  }
}
