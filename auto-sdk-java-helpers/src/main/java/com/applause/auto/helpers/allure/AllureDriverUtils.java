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
package com.applause.auto.helpers.allure;

import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/** Provides utility methods for attaching information to Allure reports. */
public final class AllureDriverUtils {

  private static final Logger LOGGER = LogManager.getLogger(AllureDriverUtils.class);
  private static final String SCREENSHOT_ATTACHMENT = "Screenshot attachment";
  private static final String IMAGE_PNG = "image/png";
  private static final String CURRENT_URL = "Current URL";
  private static final String TEXT_PLAIN = "text/plain";
  private static final String CURRENT_PAGE_SOURCE = "Current page source";
  private static final String LOG_EXTENSION = ".log";

  private AllureDriverUtils() {
    // Utility class - no public constructor
  }

  /**
   * Attaches a screenshot to the Allure report.
   *
   * @param driver The WebDriver instance to capture the screenshot from.
   */
  public static void attachScreenshot(@NonNull final WebDriver driver) {
    LOGGER.info("Taking screenshot on test failure");
    try {
      var screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
      Allure.addAttachment(
          SCREENSHOT_ATTACHMENT, IMAGE_PNG, new ByteArrayInputStream(screenshot), "png");
    } catch (Exception e) {
      LOGGER.error("Error taking screenshot: {}", e.getMessage());
    }
  }

  /**
   * Attaches the current URL to the Allure report.
   *
   * @param driver The WebDriver instance to get the current URL from.
   */
  public static void attachCurrentURL(@NonNull final WebDriver driver) {
    LOGGER.info("Attaching current URL");
    try {
      Allure.addAttachment(CURRENT_URL, TEXT_PLAIN, driver.getCurrentUrl(), LOG_EXTENSION);
    } catch (Exception e) {
      LOGGER.error("Error taking current URL: {}", e.getMessage());
    }
  }

  /**
   * Attaches the current page source to the Allure report.
   *
   * @param driver The WebDriver instance to get the page source from.
   */
  public static void attachCurrentPageSourceOnFailure(@NonNull final WebDriver driver) {
    LOGGER.info("Attaching page source");
    try {
      Allure.addAttachment(CURRENT_PAGE_SOURCE, TEXT_PLAIN, driver.getPageSource(), LOG_EXTENSION);
    } catch (Exception e) {
      LOGGER.error("Error taking current page source: {}", e.getMessage());
    }
  }
}
