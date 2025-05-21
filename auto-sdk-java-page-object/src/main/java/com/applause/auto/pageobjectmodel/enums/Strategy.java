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
package com.applause.auto.pageobjectmodel.enums;

import com.applause.auto.pageobjectmodel.base.LocatedBy;
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import org.openqa.selenium.By;

/**
 * Enum representing the various element locator strategies available in the Applause Automation
 * framework. Used primarily by Locator to store key-value pairs of locator strategies and locator
 * strings.
 */
@Getter
public enum Strategy {
  /** Locate by ID */
  ID("By.id", LocatedBy::id),
  /** Locate by CSS */
  CSS("By.cssSelector", LocatedBy::css),
  /** Locate by XPATH */
  XPATH("By.xpath", LocatedBy::xpath),
  /** Locate by Class Name */
  CLASSNAME("By.className", LocatedBy::className),
  /** Locate by Name attribute */
  NAME("By.name", LocatedBy::name),
  /** Locate by HTML Tag Name */
  TAGNAME("By.tagName", LocatedBy::tagName),
  /** Locate by Link Text */
  LINKTEXT("By.linkText", LocatedBy::linkText),
  /** Locate by Parial Link Text */
  PARTIAL_LINKTEXT("By.partialLinkText", LocatedBy::partialLinkText),
  /** Locate by Accessibility ID */
  ACCESSIBILITYID("By.AccessibilityId", LocatedBy::accessibilityId),
  /** Locate using Android UI Automator */
  ANDROID_UIAUTOMATOR("By.AndroidUIAutomator", LocatedBy::androidUIAutomator),
  /** Locate using the IOS Class Chain */
  IOS_CLASSCHAIN("By.IosClassChain", LocatedBy::iOSClassChain),
  /** Locate using the IOS NS Predicate */
  IOS_NSPREDICATE("By.IosNsPredicate", LocatedBy::iOSNsPredicate),
  /** Locate by Class Name (Appium Drivers) */
  APPIUM_CLASSNAME("AppiumBy.className", LocatedBy::appiumClassName),
  /** Locate using jQuery */
  JQUERY("By.JQuery", LocatedBy::jQuery),
  /** Locate using JavaScript */
  JAVASCRIPT("By.JavaScript", LocatedBy::javaScript);

  private final String byPrefix;
  private final Function<String, By> byConstructor;

  Strategy(final String byPrefix, final Function<String, By> byConstructor) {
    this.byPrefix = byPrefix;
    this.byConstructor = byConstructor;
  }

  /**
   * Looks up the locator strategy for a Selenium by
   *
   * @param by The Selenium by
   * @return The matching strategy
   */
  public static Strategy forBy(final @NonNull By by) {
    var byString = by.toString();
    for (var strategy : values()) {
      if (byString.startsWith(strategy.byPrefix)) {
        return strategy;
      }
    }
    throw new RuntimeException("Could not detect strategy for selenium by [" + byString + "]");
  }

  /**
   * Extracts the locator string from a by using the given strategy
   *
   * @param by The Selenium by
   * @return The matching strategy
   */
  public String parseLocatorString(final @NonNull By by) {
    var byString = by.toString();
    if (!byString.startsWith(this.byPrefix)) {
      throw new RuntimeException(
          "Could not parse locator string from selenium by [%s] using strategy [%s]"
              .formatted(byString, this.byPrefix));
    }
    // Remove the by string prefix
    var withoutPrefix = byString.substring(this.byPrefix.length());
    // Strip out the colon
    if (withoutPrefix.startsWith(":")) {
      withoutPrefix = withoutPrefix.substring(1);
    }
    // Finally, remove any leading spaces
    return withoutPrefix.stripLeading();
  }
}
