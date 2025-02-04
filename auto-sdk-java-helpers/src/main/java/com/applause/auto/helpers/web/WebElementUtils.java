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
import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.touch.offset.PointOption;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;

/** Common utils for web elements */
public class WebElementUtils {

  private static final Logger logger = LogManager.getLogger(WebElementUtils.class);

  /**
   * Get element Y offset
   *
   * @param driver - automation driver
   * @param element - web element
   * @return element Y offset value
   */
  public static int getElementYOffset(WebDriver driver, WebElement element) {
    int offsetY =
        Integer.parseInt(
            ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].offsetTop", element)
                .toString());
    logger.info("Current element" + element + " offset y  = " + offsetY);
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
      WebDriver driver, WebElement webElementWithTextContent) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    try {
      String textContent =
          (String) js.executeScript("return arguments[0].textContent", webElementWithTextContent);
      logger.info("Text content is: " + textContent);
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
      WebElement textBoxElement, String text, int waitBetweenEachInput) {
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
      WebElement inputWebElement, int waitBetweenEachDelete) {
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
  public static boolean isMobileWebExecutionDriver(WebDriver driver) {
    return driver instanceof AppiumDriver;
  }

  /**
   * Clicks an element at an accurate point on devices, with native tap. This method is to mitigate
   * issues where the different device sizes cause the element locations to differ.
   *
   * @param element
   */
  public static void clickElementWithNativeTapWithOffset(
      WebDriver driver, WebElement element, int xOffset, int yOffset, boolean isTablet) {
    int x = getAccuratePointX(driver, element) + xOffset;
    int y = getAccuratePointY(driver, element, isTablet) + yOffset;
    logger.info("Clicking element with native tap at (" + x + ", " + y + ").");
    new TouchAction((PerformsTouchActions) driver).tap(PointOption.point(x, y)).release().perform();
  }

  /**
   * Gets accurate X point for element
   *
   * @param element
   * @return Accurate X point for element
   */
  public static int getAccuratePointX(WebDriver driver, WebElement element) {
    double widthRatio =
        (double) driver.manage().window().getSize().width
            / (double) getJavascriptWindowWidth(driver);
    return (int) (getLocation(driver, element).getX() * widthRatio)
        + (element.getSize().getWidth() / 2);
  }

  /**
   * @return JavaScript window width
   */
  public static int getJavascriptWindowWidth(WebDriver driver) {
    int windowWidth =
        ((Long)
                ((JavascriptExecutor) driver)
                    .executeScript("return window.innerWidth || document.body.clientWidth"))
            .intValue();
    logger.info("Current window width is: " + windowWidth);
    return windowWidth;
  }

  /**
   * Gets the element location on the screen using JS. TODO: We are currently using this as a
   * workaround as the default getLocation is not working in W3C mode
   *
   * @param element The element to retrieve the location from.
   * @return A point representing the location
   */
  public static Point getLocation(WebDriver driver, WebElement element) {
    return getElementRect(driver, element).getPoint();
  }

  /**
   * Gets element rect. TODO: remove this when getLocation & getDimension work again in W3C mode.
   *
   * @param element the element
   * @return the element rect
   */
  public static Rectangle getElementRect(WebDriver driver, WebElement element) {
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
   * Gets accurate Y point for element
   *
   * @param element
   * @param isTablet -
   * @return Accurate Y point for element
   */
  public static int getAccuratePointY(WebDriver driver, WebElement element, boolean isTablet) {
    int windowDiff =
        isTablet
            ? getWindowHeightDiffBetweenAppiumAndSelenium(driver)
            : (int) (getWindowHeightDiffBetweenAppiumAndSelenium(driver) / 1.5);
    int seleniumWindowHeight = driver.manage().window().getSize().getHeight();
    double heightRatio = ((double) seleniumWindowHeight / getJavascriptWindowHeight(driver));
    return (int) ((heightRatio * getLocation(driver, element).getY()) + windowDiff)
        + element.getSize().getHeight() / 2;
  }

  /**
   * @return the window height difference between Appium and Selenium
   */
  public static int getWindowHeightDiffBetweenAppiumAndSelenium(WebDriver driver) {
    int seleniumWindowHeight = driver.manage().window().getSize().getHeight();
    int appiumHeight = getAppiumWindowHeight(driver);
    return appiumHeight - seleniumWindowHeight;
  }

  /**
   * @return JavaScript window height
   */
  public static int getJavascriptWindowHeight(WebDriver driver) {
    return ((Long)
            ((JavascriptExecutor) driver)
                .executeScript("return window.innerHeight || document.body.clientHeight"))
        .intValue();
  }

  public static int getAppiumWindowHeight(WebDriver driver) {
    String currentContext = getContext(driver);
    changeContext(driver, "NATIVE_APP");
    int appiumHeight =
        isAndroid(driver)
            ? getAndroidDriver(driver).manage().window().getSize().getHeight()
            : getIOSDriver(driver).manage().window().getSize().getHeight();
    changeContext(driver, currentContext);
    return appiumHeight;
  }

  public static boolean changeContext(WebDriver driver, String desiredContext) {
    return isAndroid(driver)
        ? changeContextAndroid(driver, desiredContext)
        : changeContextIos(driver, desiredContext);
  }

  private static boolean changeContextAndroid(WebDriver driver, String desiredContext) {
    try {
      Set<String> contextNames = getAndroidDriver(driver).getContextHandles();
      Iterator contextNameIterator = contextNames.iterator();

      String contextName;
      do {
        if (!contextNameIterator.hasNext()) {
          return false;
        }

        contextName = (String) contextNameIterator.next();
      } while (!contextName.contains(desiredContext));

      logger.debug(String.format("Switching to context [%s].", contextName));
      getAndroidDriver(driver).context(contextName);
      return true;
    } catch (Exception var6) {
      logger.error(String.format("Unable to switch to context [%s].", desiredContext), var6);
      return false;
    }
  }

  private static boolean changeContextIos(WebDriver driver, String desiredContext) {
    try {
      Set<String> contextNames = getIOSDriver(driver).getContextHandles();
      Iterator contextNameIterator = contextNames.iterator();

      String contextName;
      do {
        if (!contextNameIterator.hasNext()) {
          return false;
        }

        contextName = (String) contextNameIterator.next();
      } while (!contextName.contains(desiredContext));

      logger.debug(String.format("Switching to context [%s].", contextName));
      getIOSDriver(driver).context(contextName);
      return true;
    } catch (Exception var6) {
      logger.error(String.format("Unable to switch to context [%s].", desiredContext), var6);
      return false;
    }
  }

  public static String getContext(WebDriver driver) {
    return isAndroid(driver)
        ? getAndroidDriver(driver).getContext()
        : getIOSDriver(driver).getContext();
  }

  private static boolean isAndroid(WebDriver driver) {
    return driver instanceof AndroidDriver;
  }

  private static <T extends WebElement> AndroidDriver getAndroidDriver(WebDriver driver) {
    return (AndroidDriver) driver;
  }

  private static <T extends WebElement> IOSDriver getIOSDriver(WebDriver driver) {
    return (IOSDriver) driver;
  }
}
