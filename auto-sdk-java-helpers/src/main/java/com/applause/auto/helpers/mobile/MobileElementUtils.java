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

import static com.applause.auto.helpers.mobile.MobileUtils.getMobileDriver;

import io.appium.java_client.AppiumDriver;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

/** Helper for mobile elements on native mobile */
public final class MobileElementUtils {

  private static final Logger logger = LogManager.getLogger(MobileElementUtils.class);

  private MobileElementUtils() {
    // utility class
  }

  /**
   * Tap element center
   *
   * @param driver - automation driver
   * @param element - mobile element for tap on
   */
  public static void tapElementCenter(
      @NonNull final AppiumDriver driver, @NonNull final WebElement element) {
    Point centerOfElement = getCenter(element);
    tapElementByCoordinates(driver, centerOfElement);
  }

  /**
   * Tap element by coordinates
   *
   * @param driver - automation driver
   * @param elementCoordinates - coordinates point for tap
   */
  public static void tapElementByCoordinates(
      @NonNull final AppiumDriver driver, @NonNull final Point elementCoordinates) {
    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    Sequence tap = new Sequence(finger, 1);
    tap.addAction(
        finger.createPointerMove(
            Duration.ofMillis(500),
            PointerInput.Origin.viewport(),
            elementCoordinates.getX(),
            elementCoordinates.getY()));
    tap.addAction(finger.createPointerDown(0)); // 0 represents the left mouse button
    tap.addAction(finger.createPointerUp(0));
    driver.perform(List.of(tap));
  }

  /**
   * Long press on element center (Updated for Appium v8)
   *
   * @param driver - automation driver
   * @param element - mobile element for long press on
   */
  public static void longPressOnElementCenter(
      @NonNull final AppiumDriver driver, @NonNull final WebElement element) {
    Point centerOfElement = getCenter(element);

    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    Sequence longPress = new Sequence(finger, 1);

    longPress.addAction(
        finger.createPointerMove(
            Duration.ofMillis(500),
            PointerInput.Origin.viewport(),
            centerOfElement.getX(),
            centerOfElement.getY()));
    longPress.addAction(finger.createPointerDown(0)); // Press down
    longPress.addAction(
        finger.createPointerMove(
            Duration.ofMillis(1000),
            PointerInput.Origin.viewport(),
            centerOfElement.getX(),
            centerOfElement.getY())); // Hold for duration. Appium longpress is default 1000 ms
    longPress.addAction(finger.createPointerUp(0)); // Release

    driver.perform(List.of(longPress));
  }

  /**
   * Click element with execute script, this is new Appium mobile approach of elements and device
   * interactions through .executeScript(...)
   *
   * @param driver - automation driver
   * @param element - mobile element for click on
   * @param tapCount - tap count
   * @param touchCount - tap count
   * @param duration - duration for tap
   */
  public static void clickElementWithExecuteScript(
      @NonNull final WebDriver driver,
      @NonNull final WebElement element,
      final int tapCount,
      final int touchCount,
      final int duration) {
    Point elementCenter = getCenter(element);
    logger.info("Element coords: " + elementCenter);
    Map<String, Double> tap = new HashMap<>();
    tap.put("tapCount", (double) tapCount);
    tap.put("touchCount", (double) touchCount);
    tap.put("duration", (double) duration);
    tap.put("x", (double) elementCenter.getX());
    tap.put("y", (double) elementCenter.getY());
    getMobileDriver(driver).executeScript("mobile: tap", tap);
  }

  public static Point getCenter(@NonNull final WebElement element) {
    Dimension size = element.getSize();
    Point location = element.getLocation();
    int centerX = location.getX() + size.getWidth() / 2;
    int centerY = location.getY() + size.getHeight() / 2;
    return new Point(centerX, centerY);
  }
}
