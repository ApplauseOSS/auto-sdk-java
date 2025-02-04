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

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/** Web common utils */
public class WebUtils {

  /**
   * JS scroll down
   *
   * @param driver - automation driver
   * @param yValue - y value scroll to
   */
  public static void jsScrollDown(WebDriver driver, final int yValue) {
    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, " + yValue + ");");
  }

  /**
   * Get page Y position
   *
   * @param driver - automation driver
   * @return Y page position value
   */
  public static int getPagePositionY(WebDriver driver) {
    String javascript = "return window.scrollY;";
    return (int)
        Float.parseFloat(
            String.valueOf(((JavascriptExecutor) driver).executeScript("return window.scrollY;")));
  }
}
