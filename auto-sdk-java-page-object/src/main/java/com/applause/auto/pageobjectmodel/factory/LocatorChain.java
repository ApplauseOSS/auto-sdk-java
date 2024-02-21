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

import com.applause.auto.pageobjectmodel.base.Locatable;
import com.applause.auto.pageobjectmodel.base.LocatedBy;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;

/**
 * Class representing a chain of locatable elements. Allows for initializing of sub elements by
 * following the locator chain down to the Locatable element
 *
 * @see Locatable
 * @see LazyWebElement
 * @see LazyList
 */
@Log4j2
public class LocatorChain {

  private final Locatable underlying;
  @Getter private final Deque<LazyWebElement> chain;

  /**
   * Initializes the LocatorChain for a Locatable element
   *
   * @param element The Locatable Element
   */
  public LocatorChain(final Locatable element) {
    this.underlying = element;
    this.chain = new ArrayDeque<>();
    var chainLink = element.getParent();
    while (chainLink != null) {
      // We only need to look at the lazy web elements that have an underlying by
      if (chainLink.getLocator() != null) {
        this.chain.addFirst(chainLink);
      }
      // Move up the parent chain
      chainLink = chainLink.getParent();
    }
  }

  /**
   * Get the size of the locatable parent chain
   *
   * @return The number of locatable parents
   */
  public int size() {
    return this.chain.size();
  }

  /**
   * Creates a concatenated JQuery locator string from an entire chain of Locators. Since JQuery
   * requires the execution of JavaScript, the By.JQuery strategy in LocatedBy cannot support
   * relative search. Instead, we can perform an identical behavior by concatenating JQuery (or CSS)
   * locator strings from the topmost parent.
   *
   * @return a By.JQuery with a concatenated locator string
   * @throws InvalidArgumentException when wrong parent type
   */
  public By getJQueryBy() {
    List<String> jayQueryStrings = new ArrayList<>();
    for (var chainLink : this.chain) {
      String jayQueryString = chainLink.getLocator().getJQueryString(chainLink.getFormatArgs());
      if (jayQueryString == null) {
        throw new InvalidArgumentException(
            "JQuery elements require either JQuery or CSS parents. Element [%s]. Parent [%s]."
                .formatted(
                    this.underlying.getLocator().getBy(this.underlying.getFormatArgs()),
                    chainLink.getLocator().getBy(chainLink.getFormatArgs())));
      }
      jayQueryStrings.add(jayQueryString);
    }
    jayQueryStrings.add(underlying.getLocator().getJQueryString(underlying.getFormatArgs()));
    String selector = String.join(" ", jayQueryStrings);
    return LocatedBy.jQuery(selector);
  }

  /**
   * Finds the given Locatable element within the chain
   *
   * @return The located Element with the byChain used for locating it
   */
  public WebElement findElementInChain() {
    // If we have a parent
    var parent = this.chain.isEmpty() ? null : this.chain.getLast();
    if (parent != null) {
      if (!parent.isInitialized()) {
        parent.initialize();
      }
      var searchContext =
          parent.getLocator().isShadowRoot()
              ? parent.getShadowRoot()
              : parent.getUnderlyingWebElement();
      return this.findElementInContext(
          searchContext, this.underlying.getLocator(), this.underlying.getFormatArgs());
    }
    return this.findElementInContext(
        this.underlying.getContext().getDriver(),
        this.underlying.getLocator(),
        this.underlying.getFormatArgs());
  }

  /**
   * Finds a list of Locatable elements within the chain
   *
   * @return The located Elements with the byChain used for locating them
   */
  public List<WebElement> findElementsInChain() {
    // If we have a parent
    var parent = this.chain.isEmpty() ? null : this.chain.getLast();
    if (parent != null) {
      if (!parent.isInitialized()) {
        parent.initialize();
      }
      var searchContext =
          parent.getLocator().isShadowRoot()
              ? parent.getShadowRoot()
              : parent.getUnderlyingWebElement();
      return this.findElementsInContext(
          searchContext, this.underlying.getLocator(), this.underlying.getFormatArgs());
    }
    return this.findElementsInContext(
        this.underlying.getContext().getDriver(),
        this.underlying.getLocator(),
        this.underlying.getFormatArgs());
  }

  /**
   * Finds a WebElement in the current SearchContext. A SearchContext can be a WebDriver or another
   * WebElement. In the former case, the new element will be found from the top of the page. In the
   * latter case, it will be found in the scope of the other WebElement. If an index n is specified,
   * this method will return the nth matching element.
   *
   * @param searchContext the WebDriver or WebElement in which to find the new WebElement
   * @param locator the By locator pointing to the new WebElement
   * @param formatArgs the format arguments for the given locator
   * @return a WebElement found with the specified locator at the specified index in the specified
   *     context
   * @throws NoSuchElementException if element find timed out
   */
  public WebElement findElementInContext(
      final @NonNull SearchContext searchContext,
      final Locator locator,
      final Object... formatArgs) {
    if (locator == null) {
      return null;
    }
    By by = locator.getBy(formatArgs);
    var variableString =
        locator.getVariableName() != null ? "[%s].".formatted(locator.getVariableName()) : ".";
    var indexString = locator.getIndex() != null ? "index [%d].".formatted(locator.getIndex()) : "";
    log.debug("Synchronizing element{}{}", variableString, indexString);
    if (searchContext instanceof WebDriver && !Duration.ZERO.equals(underlying.getWaitTimeout())) {
      try {
        return locator.synchronizeAndReturnWebElement(getWebDriverWait(), formatArgs);
      } catch (TimeoutException e) {
        throw new NoSuchElementException(
            Optional.ofNullable(locator.getVariableName())
                .map(name -> String.format("Could not find element [%s]. Locator [%s].", name, by))
                .orElse(String.format("Could not find element. Locator [%s].", by)),
            e);
      }
    }
    Integer index = locator.getIndex();
    if (index == null) {
      return searchContext.findElement(by);
    }
    final var allMatches = searchContext.findElements(by);

    // Verify that there is an element at the given index
    if (allMatches.size() <= index) {
      throw new NoSuchElementException("Failed to locate element");
    }
    return allMatches.get(index);
  }

  /**
   * Finds a {@code List<WebElement> } in the current SearchContext. A SearchContext can be a
   * WebDriver or another WebElement. In the former case, the new elements will be found from the
   * top of the page with a wait to ensure that the page is loaded. In the latter case, the elements
   * will simply be found in the scope of the other WebElement. If an index n is specified, this
   * method will return the nth matching element.
   *
   * @param searchContext the WebDriver or WebElement in which to find the new WebElements
   * @param locator the By locator pointing to the new WebElements
   * @param formatArgs the format arguments for the locator
   * @return a {@code List<WebElement> } found with the specified locator at the specified index in
   *     the specified context
   */
  public List<WebElement> findElementsInContext(
      final SearchContext searchContext, final Locator locator, final Object... formatArgs) {
    By by = locator.getBy(formatArgs);
    if (by == null) {
      return Collections.emptyList();
    }
    Integer index = locator.getIndex();
    if (!WebDriver.class.isAssignableFrom(searchContext.getClass())
        || Objects.equals(this.underlying.getWaitTimeout(), Duration.ZERO)) {
      if (index == null) {
        return searchContext.findElements(by);
      } else {
        // Verify that there is an element at the given index
        final var allElements = searchContext.findElements(by);
        if (allElements.size() <= index) {
          throw new NoSuchElementException("Failed to locate element!");
        }
        return Collections.singletonList(allElements.get(index));
      }
    }
    return synchronizeAndReturnWebElements(locator, formatArgs);
  }

  /**
   * Synchronizes the lazy list by loading all elements matching the provided by at the given
   * (optional) index
   *
   * @param locator The Locator used to locate the web elements
   * @param formatArgs The format arguments for the locator
   * @return A list of elements matching the by and index
   */
  public List<WebElement> synchronizeAndReturnWebElements(
      final Locator locator, final Object... formatArgs) {
    final var by = locator.getBy(formatArgs);
    log.debug("Synchronizing List. Locator [{}] Index [{}].", by, locator.getIndex());
    List<WebElement> elements;
    try {
      elements = getWebDriverWait().until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
    } catch (TimeoutException e) {
      // Check if the PageObject options allows for an empty list
      if (!underlying.getContext().getOptions().isThrowExceptionOnEmptyList()) {
        return Collections.emptyList();
      }
      throw new NoSuchElementException(
          Optional.ofNullable(locator.getVariableName())
              .map(name -> String.format("Could not find elements [%s]. Locator [%s].", name, by))
              .orElse(String.format("Could not find elements. Locator [%s].", by)),
          e);
    }
    if (locator.getIndex() == null) {
      return elements;
    }
    return Collections.singletonList(elements.get(locator.getIndex()));
  }

  /**
   * Gets the by chain from the locator chain
   *
   * @return A list of by strings used to locate the locatable element
   */
  public List<String> getByChain() {
    List<String> res = new ArrayList<>();
    res.addAll(
        this.chain.stream()
            .map(e -> e.getLocator().getBy(e.getFormatArgs()))
            .map(By::toString)
            .toList());
    res.add(this.underlying.getLocator().getBy(this.underlying.getFormatArgs()).toString());
    return res;
  }

  /**
   * Gets the appropriate {@code Wait<WebDriver>} depending on whether the user has set a custom
   * timeout.
   *
   * @return a WebDriver wait, either using RunUtil's default timeouts or the user's custom ones
   */
  private Wait<WebDriver> getWebDriverWait() {
    if (this.underlying.getWaitTimeout() == null) {
      return this.underlying.getContext().getWait();
    }
    return this.underlying
        .getContext()
        .getWait(this.underlying.getWaitTimeout(), this.underlying.getWaitPollingInterval());
  }
}
