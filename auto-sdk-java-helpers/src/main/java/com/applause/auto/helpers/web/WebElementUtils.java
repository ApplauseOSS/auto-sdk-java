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
package com.applause.auto.helpers.web;

import com.applause.auto.helpers.util.ThreadHelper;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

/** Common utils for web elements */
public final class WebElementUtils {

  private static final Logger logger = LogManager.getLogger(WebElementUtils.class);

  private WebElementUtils() {
    // utility class
  }

  /**
   * Get element Y offset
   *
   * @param driver - automation driver
   * @param element - web element
   * @return element Y offset value
   */
  public static int getElementYOffset(
      @NonNull final WebDriver driver, @NonNull final WebElement element) {
    int offsetY =
        Integer.parseInt(
            ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].offsetTop", element)
                .toString());
    logger.info("Current element{} offset y  = {}", element, offsetY);
    return offsetY;
  }

  /**
   * Get element text content with js
   *
   * @param driver - automation driver
   * @param webElementWithTextContent - web element
   * @return element text content
   */
  public static String getElementTextContentWithJS(
      @NonNull final WebDriver driver, @NonNull final WebElement webElementWithTextContent) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    try {
      String textContent =
          (String) js.executeScript("return arguments[0].textContent", webElementWithTextContent);
      logger.info("Text content is: {}", textContent);
      return textContent;
    } catch (Exception e) {
      return StringUtils.EMPTY;
    }
  }

  /**
   * Custom sendkeys by symbol with delay
   *
   * @param textBoxElement - web element
   * @param text - text to input
   * @param waitBetweenEachInput - wait between input in millis
   */
  public static void customSendKeysBySymbolWithDelay(
      @NonNull final WebElement textBoxElement,
      @NonNull final String text,
      final int waitBetweenEachInput) {
    for (int symbolIndex = 0; symbolIndex < text.length(); symbolIndex++) {
      textBoxElement.sendKeys(Character.toString(text.charAt(symbolIndex)));
      ThreadHelper.sleep(waitBetweenEachInput);
    }
  }

  /**
   * Clear input field value with backspace with delay
   *
   * @param inputWebElement - web element
   * @param waitBetweenEachDelete - wait between each backspace deletion
   */
  public static void clearFieldValueWithBackspaceWithDelay(
      @NonNull final WebElement inputWebElement, final int waitBetweenEachDelete) {
    int valueCharacters = inputWebElement.getAttribute("value").length();
    for (int i = 0; i < valueCharacters + 1; i++) {
      inputWebElement.sendKeys(Keys.BACK_SPACE);
      ThreadHelper.sleep(waitBetweenEachDelete);
    }
  }

  /**
   * Check whether mobile web execution is being performed
   *
   * @param driver - automation driver
   * @return boolean state whether this kind of driver is mobile one of not
   */
  public static boolean isMobileWebExecutionDriver(@NonNull final WebDriver driver) {
    return driver instanceof AppiumDriver;
  }

  /**
   * Clicks an element at an accurate point on devices, with native tap. This method is to mitigate
   * issues where the different device sizes cause the element locations to differ.
   *
   * @param driver The WebDriver instance to use.
   * @param element The web element to click.
   * @param xOffset The x-offset from the element's center to click.
   * @param yOffset The y-offset from the element's center to click.
   * @param isTablet A boolean indicating whether the device is a tablet. This is used in
   *     calculating the accurate Y coordinate.
   */
  public static void clickElementWithNativeTapWithOffset(
      @NonNull final AppiumDriver driver, // Use AppiumDriver
      @NonNull final WebElement element,
      final int xOffset,
      final int yOffset,
      final boolean isTablet) {

    int x = getAccuratePointX(driver, element) + xOffset;
    int y = getAccuratePointY(driver, element, isTablet) + yOffset;
    logger.info("Clicking element with native tap at ({}, {}).", x, y);

    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    Sequence tap = new Sequence(finger, 1);

    tap.addAction(
        finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), x, y));
    tap.addAction(finger.createPointerDown(0));
    tap.addAction(finger.createPointerUp(0));

    driver.perform(List.of(tap));
  }

  /**
   * Gets the accurate X coordinate of the center of a web element, accounting for window scaling.
   * This method calculates the element's position and width, then adjusts for any difference
   * between the browser's reported window size and the actual rendered viewport width.
   *
   * @param driver The WebDriver instance used to interact with the browser. Must not be null.
   * @param element The WebElement whose X coordinate is to be retrieved. Must not be null.
   * @return The accurate X coordinate of the center of the element, adjusted for window scaling.
   *     Returns an integer representing the pixel value.
   * @throws NullPointerException If either the driver or the element is null.
   */
  public static int getAccuratePointX(
      @NonNull final WebDriver driver, @NonNull final WebElement element) {
    double widthRatio =
        (double) driver.manage().window().getSize().width / getJavascriptWindowWidth(driver);
    return (int) (getLocation(driver, element).getX() * widthRatio)
        + (element.getSize().getWidth() / 2);
  }

  /**
   * Retrieves the width of the browser window as reported by JavaScript.
   *
   * @param driver The WebDriver instance to use for executing JavaScript. Must not be null.
   * @return The width of the browser window in pixels.
   * @throws NullPointerException If the provided WebDriver is null.
   */
  public static int getJavascriptWindowWidth(@NonNull final WebDriver driver) {
    int windowWidth =
        ((Long)
                ((JavascriptExecutor) driver)
                    .executeScript("return window.innerWidth || document.body.clientWidth"))
            .intValue();
    logger.info("Current window width is: {}", windowWidth);
    return windowWidth;
  }

  /**
   * Gets the element location on the screen using JS. This method is currently used as a workaround
   * as the default getLocation is not working in W3C mode.
   *
   * @param driver The WebDriver instance.
   * @param element The element to retrieve the location from.
   * @return A point representing the location of the top-left corner of the element.
   */
  public static Point getLocation(
      @NonNull final WebDriver driver, @NonNull final WebElement element) {
    return getElementRect(driver, element).getPoint();
  }

  /**
   * Gets element rect.
   *
   * @param driver The WebDriver instance.
   * @param element The WebElement to get the rectangle of.
   * @return The Rectangle representing the element's dimensions and position.
   */
  public static Rectangle getElementRect(
      @NonNull final WebDriver driver, @NonNull final WebElement element) {
    Map<?, ?> result =
        (Map<?, ?>)
            ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].getBoundingClientRect()", element);
    logger.info(result.toString());
    return new Rectangle(
        (int) Double.parseDouble(result.get("x").toString()),
        (int) Double.parseDouble(result.get("y").toString()),
        (int) Double.parseDouble(result.get("height").toString()),
        (int) Double.parseDouble(result.get("width").toString()));
  }

  /**
   * Gets accurate Y point for element. This method calculates the accurate Y coordinate of a web
   * element, taking into account differences between Appium and Selenium's reported window heights,
   * and whether the device is a tablet or not.
   *
   * @param driver The WebDriver instance used to interact with the browser.
   * @param element The WebElement for which to calculate the Y coordinate.
   * @param isTablet A boolean indicating whether the device is a tablet (true) or a phone (false).
   * @return The accurate Y coordinate of the element.
   */
  public static int getAccuratePointY(
      @NonNull final WebDriver driver, @NonNull final WebElement element, final boolean isTablet) {
    int windowDiff =
        isTablet
            ? getWindowHeightDiffBetweenAppiumAndSelenium(driver)
            : (int) (getWindowHeightDiffBetweenAppiumAndSelenium(driver) / 1.5);
    int seleniumWindowHeight = driver.manage().window().getSize().getHeight();
    double heightRatio = (double) seleniumWindowHeight / getJavascriptWindowHeight(driver);
    return (int) ((heightRatio * getLocation(driver, element).getY()) + windowDiff)
        + element.getSize().getHeight() / 2;
  }

  /**
   * Calculates the difference in window height between what Appium reports and what Selenium
   * reports. This is often necessary due to differences in how Appium and Selenium handle window
   * sizes, especially on mobile devices.
   *
   * @param driver The WebDriver instance to use for retrieving window dimensions. Must not be null.
   * @return The difference in window height (Appium height - Selenium height). A positive value
   *     indicates that Appium reports a larger height than Selenium. A negative value indicates the
   *     opposite.
   * @throws NullPointerException If the provided {@code driver} is {@code null}.
   */
  public static int getWindowHeightDiffBetweenAppiumAndSelenium(@NonNull final WebDriver driver) {
    int seleniumWindowHeight = driver.manage().window().getSize().getHeight();
    int appiumHeight = getAppiumWindowHeight(driver);
    return appiumHeight - seleniumWindowHeight;
  }

  /**
   * Returns the JavaScript window height.
   *
   * @param driver The WebDriver instance to use for executing JavaScript.
   * @return The height of the JavaScript window in pixels.
   */
  public static int getJavascriptWindowHeight(@NonNull final WebDriver driver) {
    return ((Long)
            ((JavascriptExecutor) driver)
                .executeScript("return window.innerHeight || document.body.clientHeight"))
        .intValue();
  }

  /**
   * Retrieves the height of the Appium window.
   *
   * @param driver The WebDriver instance.
   * @return The height of the Appium window in pixels.
   */
  public static int getAppiumWindowHeight(@NonNull final WebDriver driver) {
    String currentContext = getContext(driver);
    changeContext(driver, "NATIVE_APP");
    int appiumHeight =
        isAndroid(driver)
            ? getAndroidDriver(driver).manage().window().getSize().getHeight()
            : getIOSDriver(driver).manage().window().getSize().getHeight();
    changeContext(driver, currentContext);
    return appiumHeight;
  }

  /**
   * Switches the WebDriver's context to the desired context.
   *
   * @param driver The WebDriver instance.
   * @param desiredContext The desired context to switch to.
   * @return {@code true} if the context was successfully switched, {@code false} otherwise.
   */
  public static boolean changeContext(
      @NonNull final WebDriver driver, @NonNull final String desiredContext) {
    return isAndroid(driver)
        ? changeContextAndroid(driver, desiredContext)
        : changeContextIos(driver, desiredContext);
  }

  private static boolean changeContextAndroid(
      @NonNull final WebDriver driver, @NonNull final String desiredContext) {
    try {
      Set<String> contextNames = getAndroidDriver(driver).getContextHandles();
      Iterator<String> contextNameIterator = contextNames.iterator();

      String contextName;
      do {
        if (!contextNameIterator.hasNext()) {
          return false;
        }

        contextName = contextNameIterator.next();
      } while (!contextName.contains(desiredContext));

      logger.debug("Switching to context [{}].", contextName);
      getAndroidDriver(driver).context(contextName);
      return true;
    } catch (Exception var6) {
      logger.error("Unable to switch to context [{}].", desiredContext, var6);
      return false;
    }
  }

  private static boolean changeContextIos(
      @NonNull final WebDriver driver, @NonNull final String desiredContext) {
    try {
      Set<String> contextNames = getIOSDriver(driver).getContextHandles();
      Iterator<String> contextNameIterator = contextNames.iterator();

      String contextName;
      do {
        if (!contextNameIterator.hasNext()) {
          return false;
        }

        contextName = contextNameIterator.next();
      } while (!contextName.contains(desiredContext));

      logger.debug("Switching to context [{}].", contextName);
      getIOSDriver(driver).context(contextName);
      return true;
    } catch (Exception var6) {
      logger.error("Unable to switch to context [{}].", desiredContext, var6);
      return false;
    }
  }

  /**
   * Retrieves the current context of the WebDriver.
   *
   * @param driver The WebDriver instance.
   * @return The current context as a String.
   * @throws NullPointerException If the driver is null.
   */
  public static String getContext(@NonNull final WebDriver driver) {
    return isAndroid(driver)
        ? getAndroidDriver(driver).getContext()
        : getIOSDriver(driver).getContext();
  }

  private static boolean isAndroid(@NonNull final WebDriver driver) {
    return driver instanceof AndroidDriver;
  }

  private static AndroidDriver getAndroidDriver(@NonNull final WebDriver driver) {
    return (AndroidDriver) driver;
  }

  private static IOSDriver getIOSDriver(@NonNull final WebDriver driver) {
    return (IOSDriver) driver;
  }
}
