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
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/** Some common allure utils (with attachments) */
public class AllureDriverUtils {

  private static final Logger logger = LogManager.getLogger(AllureDriverUtils.class);

  /**
   * Attach driver screenshot
   *
   * @param driver - automation driver
   */
  public static void attachScreenshot(WebDriver driver) {
    if (Objects.nonNull(driver)) {
      logger.info("Taking screenshot on test failure");
      try {
        Allure.addAttachment(
            "Screenshot attachment",
            "image/png",
            new ByteArrayInputStream(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES)),
            "png");
      } catch (Exception e) {
        logger.error("Error taking screenshot: " + e.getMessage());
      }
    }
  }

  /**
   * Attach driver url
   *
   * @param driver - automation driver
   */
  public static void attachCurrentURL(WebDriver driver) {
    if (Objects.nonNull(driver)) {
      logger.info("Taking current URL");
      try {
        Allure.addAttachment("Current URL", "text/plain", driver.getCurrentUrl(), ".log");
      } catch (Exception e) {
        logger.error("Error taking current URL: " + e.getMessage());
      }
    }
  }

  /**
   * Attach driver page source
   *
   * @param driver - automation driver
   */
  public static void attachCurrentPageSourceOnFailure(WebDriver driver) {
    if (Objects.nonNull(driver)) {
      logger.info("Taking page source");
      try {
        Allure.addAttachment("Current page source", "text/plain", driver.getPageSource(), ".log");
      } catch (Exception e) {
        logger.error("Error taking current page source: " + e.getMessage());
      }
    }
  }
}
