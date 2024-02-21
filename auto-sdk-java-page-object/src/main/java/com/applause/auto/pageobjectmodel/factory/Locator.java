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
package com.applause.auto.pageobjectmodel.factory;

import com.applause.auto.data.enums.Platform;
import com.applause.auto.pageobjectmodel.annotation.Locate;
import com.applause.auto.pageobjectmodel.enums.Strategy;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;

/**
 * Pointer to an HTML element in the DOM. Used by LazyWebElement and LazyList to find or re-find
 * WebElements just before they're needed for use.
 *
 * @see LazyWebElement
 * @see LazyList
 */
@Getter
public class Locator {
  private final @NonNull Strategy strategy;
  private final @NonNull String locatorString;

  /**
   * -- GETTER -- Gets the Platform of this Locator. Mainly used for logging in LazyWebElement and
   * LazyList.
   */
  private final @NonNull Platform platform;

  @Setter private @Nullable String variableName;
  private @Nullable Integer index;
  private final boolean shadowRoot;

  /**
   * Instantiates a new Locator from a @Locate annotation.
   *
   * @param annotation the @Locate annotation
   */
  public Locator(final @NonNull Locate annotation) {
    this.strategy = annotation.using();
    this.locatorString = annotation.value();
    this.platform = annotation.on();
    this.shadowRoot = annotation.shadowRoot();
  }

  /**
   * Instantiates a new Locator from a @Locate annotation.
   *
   * @param annotation the @Locate annotation
   * @param variableName the name of the variable annotated with @Locate
   */
  public Locator(final Locate annotation, final @Nullable String variableName) {
    this(annotation);
    this.variableName = variableName;
  }

  /**
   * Instantiates a new Locator from a Selenium By locator. Please note that Locators created with a
   * By cannot support formatted string arguments.
   *
   * @param by a Selenium By locator
   * @param platform the platform to search by
   * @param shadowRoot is element a shadowRoot
   */
  public Locator(final @NonNull By by, final @NonNull Platform platform, final boolean shadowRoot) {
    // We parse the By.toString() because By doesn't support giving you your locator string back.
    this.strategy = Strategy.forBy(by);
    this.locatorString = this.strategy.parseLocatorString(by);
    this.platform = platform;
    this.shadowRoot = shadowRoot;
  }

  /**
   * Instantiates a new Locator from a Selenium By locator. Please note that Locators created with a
   * By cannot support formatted string arguments.
   *
   * @param by a Selenium By locator
   */
  public Locator(final By by) {
    this(by, Platform.DEFAULT);
  }

  /**
   * Instantiates a new Locator from a Selenium By locator. Please note that Locators created with a
   * By cannot support formatted string arguments.
   *
   * @param by a Selenium By locator
   * @param platform the platform to search by
   */
  public Locator(final By by, final Platform platform) {
    this(by, platform, false);
  }

  /**
   * Instantiates a new Locator from a Selenium By locator. Please note that Locators created with a
   * By cannot support formatted string arguments.
   *
   * @param by a Selenium By locator
   * @param platform the platform to search by
   * @param variableName the name of the variable annotated with @Locate
   */
  public Locator(final By by, final Platform platform, final @Nullable String variableName) {
    this(by, platform);
    this.variableName = variableName;
  }

  /**
   * Instantiates a new Locator with all the fields filled in, including an index. Used to create
   * Lists of elements.
   *
   * @param strategy The locator strategy
   * @param locatorString The by string
   * @param platform the Platform to which the Locator corresponds
   * @param index the index of the Locator, in case the Locator needs to point to the nth matching
   *     element
   */
  private Locator(
      final @NonNull Strategy strategy,
      final @NonNull String locatorString,
      final @NonNull Platform platform,
      final @Nullable Integer index) {
    this.strategy = strategy;
    this.locatorString = locatorString;
    this.platform = platform;
    this.index = index;
    this.shadowRoot = false;
  }

  /**
   * Gets a List of By locators based on the contents of this Locator. If one has already been
   * created and no format arguments are specified, returns the previously created List to preserve
   * formatting.
   *
   * @param formatArgs the format arguments to be filled into each By locator
   * @return the List of By locators corresponding to this Locator
   */
  public By getBy(final Object... formatArgs) {
    if (formatArgs != null && formatArgs.length > 0) {
      return this.strategy.getByConstructor().apply(this.locatorString.formatted(formatArgs));
    } else {
      return this.strategy.getByConstructor().apply(this.locatorString);
    }
  }

  /**
   * Returns a copy of this Locator with specified index. Used in LazyWebElement and LazyList to
   * build chains of Locators to find a particular element.
   *
   * @param theIndex the desired index of the new Locator
   * @return a copy of this Locator with that index
   */
  public Locator withIndex(int theIndex) {
    return new Locator(this.strategy, this.locatorString, this.platform, theIndex);
  }

  /**
   * When, in the course of testing events, it becomes necessary for one locator strategy to
   * dissolve the relative finds which have connected them with another and to assume a single
   * concatenated locator... (Elements can't perform a relative search with a JQuery locator because
   * it requires executing a piece of JavaScript. In this case, we attempt to concatenate the JQuery
   * locators together.)
   *
   * @param formatArgs the optional format arguments
   * @return a locator string. JQuery, if possible, or CSS as a fallback
   */
  String getJQueryString(final Object... formatArgs) {
    String jayQueryString = null;
    String byString = getBy(formatArgs).toString();
    if (byString.startsWith("By.JQuery: ")) {
      jayQueryString = byString.replace("By.JQuery: ", "");
    } else if (byString.startsWith("By.cssSelector: ")) {
      jayQueryString = byString.replace("By.cssSelector: ", "");
    }
    // If this locator has an index, append :nth(index)
    if (jayQueryString != null && index != null) {
      jayQueryString = jayQueryString + ":nth(" + index + ")";
    }
    return jayQueryString;
  }

  /**
   * Waits until an element is present in the page, then returns it.
   *
   * @param wait The WebDriver wait used to wait for element presence
   * @param formatArgs selects the nth element with a particular locator
   * @return the found WebElement
   */
  public WebElement synchronizeAndReturnWebElement(
      final Wait<WebDriver> wait, final Object... formatArgs) {
    final var by = getBy(formatArgs);
    if (index == null) {
      return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }
    final var allElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
    // Verify that there is an element at the given index
    if (index >= allElements.size()) {
      throw new NoSuchElementException("Could not find element at index [] using by []");
    }
    return allElements.get(index);
  }
}
