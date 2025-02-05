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

import com.applause.auto.helpers.util.AwaitilityWaitUtils;
import java.util.List;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import us.codecraft.xsoup.Xsoup;

/** Common methods for HTML-based content. */
public final class HtmlUtils {

  private static final Logger logger = LogManager.getLogger(HtmlUtils.class);

  private HtmlUtils() {
    // utility class
  }

  /**
   * Wait for page viewport to be not empty
   *
   * @param driver - automation driver
   * @param xpathRootLocator - root Xpath locator for viewport DOM element
   * @param waitInterval - wait interval to wait for viewport
   * @param pollingInterval - polling interval for wait of viewport
   */
  public static void waitForPageViewPortNotEmpty(
      @NonNull final WebDriver driver,
      @NonNull final String xpathRootLocator,
      final int waitInterval,
      final int pollingInterval) {
    AwaitilityWaitUtils.waitForCondition(
        () -> {
          String currentPageSource = driver.getPageSource();
          boolean gsdHtmlInViewportLoaded =
              wasHtmlInViewportLoadedByRootElementXpathLocator(currentPageSource, xpathRootLocator);
          logger.info("Page viewport was loaded correctly: {}", gsdHtmlInViewportLoaded);
          return gsdHtmlInViewportLoaded;
        },
        waitInterval,
        pollingInterval,
        "Wait for html viewport to be loaded");
  }

  private static boolean wasHtmlInViewportLoadedByRootElementXpathLocator(
      @NonNull final String currentHtml, @NonNull final String xpathRootLocator) {
    Document doc = Jsoup.parse(currentHtml);
    List<String> list = Xsoup.compile(xpathRootLocator).evaluate(doc).list();
    return !list.isEmpty();
  }
}
