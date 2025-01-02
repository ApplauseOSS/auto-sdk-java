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
package com.applause.auto.helpers.control;

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.context.IPageObjectExtension;
import com.applause.auto.data.enums.SwipeDirection;
import com.applause.auto.pageobjectmodel.base.LocatedBy;
import com.applause.auto.pageobjectmodel.base.UIElement;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.ios.IOSDriver;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Utility class to facilitate performing a variety of device actions, such as pressing hardware
 * buttons, touching particular points on the screen, and keyboard interaction.
 */
@SuppressWarnings({
  "PMD.GodClass",
  "PMD.CyclomaticComplexity",
  "checkstyle:AbbreviationAsWordInName",
  "checkstyle:MultipleStringLiterals",
  "checkstyle:LocalVariableName"
})
@AllArgsConstructor
public class DeviceControl implements IPageObjectExtension {
  private static final Logger logger = LogManager.getLogger();
  private final IPageObjectContext context;

  /**
   * Checks if the current driver is an Android driver.
   *
   * @return boolean indicating whether the current driver is an Android driver
   */
  private boolean isAndroid() {
    return context.getDriver() instanceof AndroidDriver;
  }

  /**
   * Returns the current driver cast as an Android driver.
   *
   * @return an AndroidDriver
   */
  private AndroidDriver getAndroidDriver() {
    return (AndroidDriver) this.context.getDriver();
  }

  /**
   * Returns the current driver cast as an iOS driver.
   *
   * @return an IOSDriver
   */
  private IOSDriver getIOSDriver() {
    return (IOSDriver) this.context.getDriver();
  }

  /**
   * Returns the current driver cast as a mobile driver.
   *
   * @return an AppiumDriver
   */
  private AppiumDriver getAppiumDriver() {
    return (AppiumDriver) this.context.getDriver();
  }

  /**
   * Presses a specified hardware key on Android. Notably, no equivalent pressKey() method currently
   * exists on iOS.
   *
   * @param key the key to press.
   * @throws UnsupportedOperationException if you try to use an iOS driver.
   */
  public void pressAndroidKey(final AndroidKey key) {
    if (isAndroid()) {
      logger.debug(String.format("Pressing key [%s].", key.name()));
      getAndroidDriver().pressKey(new KeyEvent(key));
    } else {
      throw new UnsupportedOperationException(
          "Cannot press hardware key on iOS - not supported by Appium.");
    }
  }

  /**
   * Press the Home key on Android.
   *
   * @throws UnsupportedOperationException if you try to use an iOS driver.
   */
  public void pressAndroidKeyHome() {
    pressAndroidKey(AndroidKey.HOME);
  }

  /**
   * Press the Back key on Android.
   *
   * @throws UnsupportedOperationException if you try to use an iOS driver.
   */
  public void pressAndroidKeyBack() {
    pressAndroidKey(AndroidKey.BACK);
  }

  /**
   * Press the App Switch key on Android.
   *
   * @throws UnsupportedOperationException if you try to use an iOS driver.
   */
  public void pressAndroidKeyAppSwitch() {
    pressAndroidKey(AndroidKey.APP_SWITCH);
  }

  /**
   * Press the Search key on Android.
   *
   * @throws UnsupportedOperationException if you try to use an iOS driver.
   */
  public void pressAndroidKeySearch() {
    pressAndroidKey(AndroidKey.SEARCH);
  }

  /**
   * Press the Menu key on Android.
   *
   * @throws UnsupportedOperationException if you try to use an iOS driver.
   */
  public void pressAndroidKeyMenu() {
    pressAndroidKey(AndroidKey.MENU);
  }

  /**
   * Checks if the keyboard is currently visible.
   *
   * @return boolean indicating whether the keyboard is presently shown on the screen.
   */
  public boolean isKeyboardShown() {
    if (isAndroid()) {
      return getAndroidDriver().isKeyboardShown();
    }
    return getIOSDriver().isKeyboardShown();
  }

  /**
   * Hides the keyboard, if currently visible. On an iOS device, this performs a workaround by
   * tapping a point just above the top left corner of the keyboard. This is necessary because of
   * Appium/XCUITest bugs affecting the base driver.hideKeyboard() function.
   */
  public void hideKeyboard() {
    if (isKeyboardShown()) {
      if (isAndroid()) {
        logger.debug("Hiding keyboard.");
        getAndroidDriver().hideKeyboard();
      } else {
        logger.debug("Tapping point just above keyboard to hide it.");
        Point keyboardCoords;
        try {
          // selenium8/W3C style
          final var rect =
              getIOSDriver().findElement(AppiumBy.className("XCUIElementTypeKeyboard")).getRect();
          keyboardCoords = new Point(rect.x, rect.y);
        } catch (UnsupportedCommandException uce) {
          // olderSelenium/JWP style
          keyboardCoords =
              getIOSDriver().findElement(By.className("XCUIElementTypeKeyboard")).getLocation();
        }
        tapScreenCoordinates(keyboardCoords.getX() + 2, keyboardCoords.getY() - 2);
        this.context
            .getWait()
            .until(
                ExpectedConditions.invisibilityOfElementLocated(
                    LocatedBy.className("XCUIElementTypeKeyboard")));
      }
    } else {
      logger.error("Keyboard not visible. Nothing to hide.");
    }
  }

  /**
   * Checks if the device is currently locked.
   *
   * @return boolean indicating whether the device is currently locked.
   */
  public boolean isDeviceLocked() {
    if (isAndroid()) {
      return getAndroidDriver().isDeviceLocked();
    }
    return getIOSDriver().isDeviceLocked();
  }

  /** Unlocks the device if it's currently locked. */
  public void unlockDevice() {
    if (isDeviceLocked()) {
      logger.debug("Unlocking device.");
      if (isAndroid()) {
        getAndroidDriver().unlockDevice();
      } else {
        getIOSDriver().unlockDevice();
      }
    } else {
      logger.error("Device is already unlocked. Cannot unlock.");
    }
  }

  /** Locks the device if it's currently unlocked. */
  public void lockDevice() {
    if (!isDeviceLocked()) {
      logger.debug("Locking device.");
      if (isAndroid()) {
        getAndroidDriver().lockDevice();
      } else {
        getIOSDriver().lockDevice();
      }
    } else {
      logger.error("Device is already locked. Cannot lock.");
    }
  }

  /**
   * Returns the current device orientation.
   *
   * @return current orientation
   */
  public ScreenOrientation getOrientation() {
    if (isAndroid()) {
      return getAndroidDriver().getOrientation();
    }
    return getIOSDriver().getOrientation();
  }

  /** Changes the device orientation to landscape. */
  public void rotateLandscape() {
    logger.debug("Changing orientation to landscape");
    if (isAndroid()) {
      getAndroidDriver().rotate(ScreenOrientation.LANDSCAPE);
    } else {
      getIOSDriver().rotate(ScreenOrientation.LANDSCAPE);
    }
  }

  /** Changes the device orientation to portrait. */
  public void rotatePortrait() {
    logger.debug("Changing orientation to portrait");
    if (isAndroid()) {
      getAndroidDriver().rotate(ScreenOrientation.PORTRAIT);
    } else {
      getIOSDriver().rotate(ScreenOrientation.PORTRAIT);
    }
  }

  /**
   * Returns the size of the screen.
   *
   * @return Dimension object representing the dimensions of the screen in pixels
   */
  public Dimension getScreenSize() {
    return getAppiumDriver().manage().window().getSize();
  }

  /**
   * Tap a specific coordinate on the screen.
   *
   * @param x x coordinate of screen position, starting from upper left corner.
   * @param y y coordinate of screen position, starting from upper left corner.
   */
  public void tapScreenCoordinates(final int x, final int y) {
    logger.debug(String.format("Tapping coordinates [%d, %d].", x, y));
    final var specificDriver = isAndroid() ? getAndroidDriver() : getIOSDriver();
    tapAction(specificDriver, new Point(x, y), Duration.ofMillis(100));
  }

  /**
   * Finger-tap a specific point on the viewPort. Down press is help for specified duration.
   *
   * @param appiumDriver the driver to use
   * @param point the point (x,y) to tap
   * @param millis the duration to hold down the tap
   */
  public void tapAction(final AppiumDriver appiumDriver, final Point point, final Duration millis) {
    PointerInput fingerPointer = new PointerInput(PointerInput.Kind.TOUCH, "fingerTap");
    Sequence tap = new Sequence(fingerPointer, 1);
    tap.addAction(
        fingerPointer.createPointerMove(
            Duration.ofMillis(0), PointerInput.Origin.viewport(), point.getX(), point.getY()));
    tap.addAction(fingerPointer.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    tap.addAction(new Pause(fingerPointer, millis));
    tap.addAction(fingerPointer.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    appiumDriver.perform(List.of(tap));
  }

  /**
   * Finger-swipe the across the viewPort. Movement occurs for specified duration.
   *
   * @param appiumDriver the driver to use
   * @param startPoint starting Point(x,y) of swipe
   * @param endPoint ending Point(x,y) of swipe
   * @param millis duration to move between starting and ending Points
   */
  public void swipeAction(
      final AppiumDriver appiumDriver,
      final Point startPoint,
      final Point endPoint,
      final Duration millis) {
    PointerInput fingerPointer = new PointerInput(PointerInput.Kind.TOUCH, "fingerSwipe");
    Sequence swipe = new Sequence(fingerPointer, 1);
    swipe.addAction(
        fingerPointer.createPointerMove(
            Duration.ofMillis(0),
            PointerInput.Origin.viewport(),
            startPoint.getX(),
            startPoint.getY()));
    swipe.addAction(fingerPointer.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    swipe.addAction(
        fingerPointer.createPointerMove(
            millis, PointerInput.Origin.viewport(), endPoint.getX(), endPoint.getY()));
    swipe.addAction(fingerPointer.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    appiumDriver.perform(List.of(swipe));
  }

  /** Taps the center of the screen. */
  public void tapScreenCenter() {
    Dimension size = getScreenSize();
    logger.debug(String.format("Screen size is [%d x %d].", size.width, size.height));
    tapScreenCoordinates(size.width / 2, size.height / 2);
  }

  /**
   * Tap on an element's position, offset from the top left of the element by pixel coordinates.
   *
   * @param element the UIElement to tap on
   * @param x horizontal pixel offset from the top left corner of the element
   * @param y vertical pixel offset from the top left corner of the element
   * @throws InvalidArgumentException if offset X or Y exceeds the size of the element.
   */
  public void tapElementCoordinates(final UIElement element, final int x, final int y) {
    Point anchor = element.getUnderlyingWebElement().getLocation();
    Dimension size = element.getUnderlyingWebElement().getSize();

    logger.debug(
        String.format(
            "Element stretches from [%d, %d] to [%d, %d].",
            anchor.x, anchor.y, anchor.x + size.width, anchor.y + size.height));

    if (x > size.width) {
      throw new InvalidArgumentException(
          String.format("Coordinate x = [%d] exceeds the width of element [%s].", x, element));
    }
    if (y > size.height) {
      throw new InvalidArgumentException(
          String.format("Coordinate y = [%d] exceeds the height of element [%s].", y, element));
    }

    int xPlusOffset = x + anchor.x;
    int yPlusOffset = y + anchor.y;
    tapScreenCoordinates(xPlusOffset, yPlusOffset);
  }

  /**
   * Tap in the center of an element's position.
   *
   * @param element the UIElement to tap on
   */
  public void tapElementCenter(final UIElement element) {
    Dimension size = element.getUnderlyingWebElement().getSize();
    int x = size.width / 2;
    int y = size.height / 2;
    tapElementCoordinates(element, x, y);
  }

  /**
   * Taps a specific coordinate on the screen and holds it for a specified duration.
   *
   * @param x x coordinate of screen position, starting from upper left corner.
   * @param y y coordinate of screen position, starting from upper left corner.
   * @param millis duration to hold the tap, in milliseconds
   */
  public void pressAndHoldScreenCoordinates(final int x, final int y, final long millis) {
    logger.debug(
        String.format(
            "Tapping and holding coordinates [%d, %d] for [%d] milliseconds.", x, y, millis));
    final var specificDriver = isAndroid() ? getAndroidDriver() : getIOSDriver();
    tapAction(specificDriver, new Point(x, y), Duration.ofMillis(millis));
  }

  /**
   * Taps the center of the screen and holds it for a specified duration.
   *
   * @param millis duration to hold the tap, in milliseconds
   */
  public void pressAndHoldScreenCenter(final long millis) {
    Dimension size = getScreenSize();
    logger.debug(String.format("Screen size is [%d x %d].", size.width, size.height));
    pressAndHoldScreenCoordinates(size.width / 2, size.height / 2, millis);
  }

  /**
   * Taps on an element's position, offset from the top left of the element by pixel coordinates,
   * and holds it for a specified duration.
   *
   * @param element the UIElement to tap on
   * @param x horizontal pixel offset from the top left corner of the element
   * @param y vertical pixel offset from the top left corner of the element
   * @param millis duration to hold the tap, in milliseconds
   * @throws InvalidArgumentException if offset X or Y exceeds the size of the element.
   */
  public void pressAndHoldElementCoordinates(
      final UIElement element, final int x, final int y, final long millis) {
    Point anchor = element.getUnderlyingWebElement().getLocation();
    Dimension size = element.getUnderlyingWebElement().getSize();

    logger.debug(
        String.format(
            "Element stretches from [%d, %d] to [%d, %d].",
            anchor.x, anchor.y, anchor.x + size.width, anchor.y + size.height));

    if (x > size.width) {
      throw new InvalidArgumentException(
          String.format("Coordinate x = [%d] exceeds the width of element [%s].", x, element));
    }
    if (y > size.height) {
      throw new InvalidArgumentException(
          String.format("Coordinate y = [%d] exceeds the height of element [%s].", y, element));
    }

    int xPlusOffset = x + anchor.x;
    int yPlusOffset = y + anchor.y;
    pressAndHoldScreenCoordinates(xPlusOffset, yPlusOffset, millis);
  }

  /**
   * Taps in the center of an element and holds it for a specified duration.
   *
   * @param element the UIElement to tap on
   * @param millis duration to hold the tap, in milliseconds
   * @throws InvalidArgumentException if offset X or Y exceeds the size of the element.
   */
  public void pressAndHoldElementCenter(final UIElement element, final long millis) {
    Dimension size = element.getUnderlyingWebElement().getSize();
    int x = size.width / 2;
    int y = size.height / 2;
    pressAndHoldElementCoordinates(element, x, y, millis);
  }

  /**
   * Swipes from one point to another based on two sets of coordinates.
   *
   * @param startX X value for the start point - a horizontal pixel offset from the top left corner
   *     of the screen
   * @param startY Y value for the start point - a vertical pixel offset from the top left corner of
   *     the screen
   * @param endX X value for the end point - a horizontal pixel offset from the top left corner of
   *     the screen
   * @param endY Y value for the end point - a vertical pixel offset from the top left corner of the
   *     screen
   * @param millis duration over which the swipe is performed, in milliseconds
   */
  public void swipeAcrossScreenCoordinates(
      final int startX, final int startY, final int endX, final int endY, final long millis) {
    logger.debug(String.format("Swiping from [%d, %d] to [%d, %d].", startX, startY, endX, endY));
    final var specificDriver = isAndroid() ? getAndroidDriver() : getIOSDriver();
    swipeAction(
        specificDriver,
        new Point(startX, startY),
        new Point(endX, endY),
        Duration.ofMillis(millis));
  }

  /**
   * A convenience function to perform a generic swipe across the screen in a specified direction.
   * Swipes across the middle 70% of the screen.
   *
   * @param direction a SwipeDirection object indicating which direction to swipe.
   */
  public void swipeAcrossScreenWithDirection(final SwipeDirection direction) {
    Dimension size = getScreenSize();
    logger.debug(String.format("Screen size is [%d x %d].", size.width, size.height));
    Pair<Point, Point> vector = direction.getSwipeVector(size.width, size.height);
    Point start = vector.getKey();
    Point end = vector.getValue();
    swipeAcrossScreenCoordinates(start.x, start.y, end.x, end.y, 2000);
  }

  /**
   * Swipes from one point to another based on two sets of coordinates, relative to an element.
   *
   * @param element The element to swipe across
   * @param startX X value for the start point - a horizontal pixel offset from the top left corner
   *     of the element
   * @param startY Y value for the start point - a vertical pixel offset from the top left corner of
   *     the element
   * @param endX X value for the end point - a horizontal pixel offset from the top left corner of
   *     the element
   * @param endY Y value for the end point - a vertical pixel offset from the top left corner of the
   *     element
   * @param millis duration over which the swipe is performed, in milliseconds
   */
  public void swipeAcrossElementCoordinates(
      final UIElement element,
      final int startX,
      final int startY,
      final int endX,
      final int endY,
      final long millis) {
    Point anchor = element.getUnderlyingWebElement().getLocation();
    Dimension size = element.getUnderlyingWebElement().getSize();

    logger.debug(
        String.format(
            "Element stretches from [%d, %d] to [%d, %d].",
            anchor.x, anchor.y, anchor.x + size.width, anchor.y + size.height));

    if (startX > size.width) {
      throw new InvalidArgumentException(
          String.format("Coordinate x = [%d] exceeds the width of element [%s].", startX, element));
    }
    if (startY > size.height) {
      throw new InvalidArgumentException(
          String.format(
              "Coordinate y = [%d] exceeds the height of element [%s].", startY, element));
    }
    if (endX > size.width) {
      throw new InvalidArgumentException(
          String.format("Coordinate x = [%d] exceeds the width of element [%s].", endX, element));
    }
    if (endY > size.height) {
      throw new InvalidArgumentException(
          String.format("Coordinate y = [%d] exceeds the height of element [%s].", endY, element));
    }

    int startXPlusOffset = startX + anchor.x;
    int startYPlusOffset = startY + anchor.y;
    int endXPlusOffset = endX + anchor.x;
    int endYPlusOffset = endY + anchor.y;

    swipeAcrossScreenCoordinates(
        startXPlusOffset, startYPlusOffset, endXPlusOffset, endYPlusOffset, millis);
  }

  /**
   * A convenience function to perform a generic swipe across an element in a specified direction.
   * Swipes across the middle 70% of the element.
   *
   * @param element the element to swipe across
   * @param direction a SwipeDirection object indicating which direction to swipe.
   */
  public void swipeAcrossElementWithDirection(
      final UIElement element, final SwipeDirection direction) {
    Point anchor = element.getUnderlyingWebElement().getLocation();
    Dimension size = element.getUnderlyingWebElement().getSize();

    logger.debug(
        String.format(
            "Element stretches from [%d, %d] to [%d, %d].",
            anchor.x, anchor.y, anchor.x + size.width, anchor.y + size.height));

    Pair<Point, Point> vector = direction.getSwipeVector(size.width, size.height);
    Point start = vector.getKey();
    Point end = vector.getValue();

    swipeAcrossScreenCoordinates(
        anchor.x + start.x, anchor.y + start.y, anchor.x + end.x, anchor.y + end.y, 2000);
  }

  /**
   * Activates the given app if it installed, but not running or if it is running in the background.
   *
   * @param bundleId the bundle identifier (or app id) of the app to activate.
   */
  public void launchApp(final String bundleId) {
    logger.debug("Launching app bundle: {}.", bundleId);
    if (isAndroid()) {
      getAndroidDriver().activateApp(bundleId);
    } else {
      getIOSDriver().activateApp(bundleId);
    }
  }

  /**
   * Put the app provided in the capabilities at session creation into the background for a
   * specified amount of time.
   *
   * @param millis The duration to put the app in the background. Use -1 to put it in the background
   *     indefinitely.
   */
  public void runAppInBackground(final long millis) {
    logger.debug("Putting app under test in the background for [{}] milliseconds.", millis);
    if (isAndroid()) {
      getAndroidDriver().runAppInBackground(Duration.ofMillis(millis));
    } else {
      getIOSDriver().runAppInBackground(Duration.ofMillis(millis));
    }
  }

  /**
   * Terminate the particular application if it is running.
   *
   * @param bundleId the bundle identifier (or app id) of the app to be terminated.
   * @return true if the app was running before and has been successfully stopped.
   */
  public boolean closeApp(final String bundleId) {
    logger.debug("Terminating app bundle {}.", bundleId);
    if (isAndroid()) {
      return getAndroidDriver().terminateApp(bundleId);
    }
    return getIOSDriver().terminateApp(bundleId);
  }

  /**
   * Gets the current context.
   *
   * @return the current context as a string
   */
  public String getContext() {
    if (isAndroid()) {
      return getAndroidDriver().getContext();
    }
    return getIOSDriver().getContext();
  }

  /**
   * Switches to a context best matching input string.
   *
   * @param desiredContext {@code NATIVE_APP } or {@code WEBVIEW_<id> } (iOS) or {@code
   *     WEBVIEW_<package name> } (Android)
   * @return whether the context is now the desired one
   */
  public boolean changeContext(final String desiredContext) {
    if (this.getContext().equals(desiredContext)) {
      return true;
    }

    try {
      final var specificDriver = isAndroid() ? getAndroidDriver() : getIOSDriver();
      Set<String> contextNames = specificDriver.getContextHandles();
      for (String contextName : contextNames) {
        if (contextName.contains(desiredContext)) {
          logger.debug(String.format("Switching to context [%s].", contextName));
          specificDriver.context(contextName);
          this.context
              .getWait()
              .until(ignored -> Objects.equals(specificDriver.getContext(), contextName));
          return true;
        }
      }
    } catch (Exception e) {
      logger.error(String.format("Unable to switch to context [%s].", desiredContext), e);
      return false;
    }
    return false;
  }
}
