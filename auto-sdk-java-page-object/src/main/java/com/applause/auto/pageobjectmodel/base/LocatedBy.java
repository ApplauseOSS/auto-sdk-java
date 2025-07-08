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
package com.applause.auto.pageobjectmodel.base;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.appium.java_client.AppiumBy;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

// The FindsByCssSelector interface has been deprecated in Selenium's 3.14* library.
// They still use it (so we still need to use it).  This probably goes away with Selenium 4
// import org.openqa.selenium.internal.FindsByCssSelector

/**
 * A complete collection of locator strategies usable in the Applause Automation framework. Includes
 * a standard set of Selenium By strategies, most of the Appium MobileBy strategies, and Applause's
 * custom JQuery and JavaScript strategies.
 *
 * @see com.applause.auto.pageobjectmodel.factory.Locator
 * @see com.applause.auto.pageobjectmodel.factory.LazyWebElement
 * @see com.applause.auto.pageobjectmodel.factory.LazyList
 */
// ignore caps on a few method names, so they match the Selenium
// call (convenience)
public enum LocatedBy {
  ;
  static final Logger logger = LogManager.getLogger();

  /**
   * Returns a By locator for specified ID.
   *
   * @param selector the ID, corresponding to an element's "id" attribute in the DOM
   * @return the By locator corresponding to that selector
   */
  public static By id(final String selector) {
    return By.id(selector);
  }

  /**
   * Returns a By locator with a CSS selector.
   *
   * @param selector the CSS selector pointing at a particular element or elements in the DOM
   * @return the By locator corresponding to that selector
   */
  public static By css(final String selector) {
    return By.cssSelector(selector);
  }

  /**
   * Returns a By locator with an XPath selector.
   *
   * @param selector the XPath selector pointing at a particular element or elements in the DOM
   * @return the By locator corresponding to that selector
   */
  public static By xpath(final String selector) {
    return By.xpath(selector);
  }

  /**
   * Returns a By locator for specified class.
   *
   * @param selector the class name, corresponding to one or several elements' "class" attribute in
   *     the DOM
   * @return the By locator corresponding to that selector
   */
  public static By className(final String selector) {
    return By.className(selector);
  }

  /**
   * Returns a By locator for specified name.
   *
   * @param selector the name, corresponding to one or several elements' "name" attribute in the DOM
   * @return the By locator corresponding to that selector
   */
  public static By name(final String selector) {
    return By.name(selector);
  }

  /**
   * Returns a By locator for specified HTML tag
   *
   * @param selector the name, corresponding to one or several elements' tag in the DOM
   * @return the By locator corresponding to that selector
   */
  public static By tagName(final String selector) {
    return By.tagName(selector);
  }

  /**
   * Returns a By locator for specified link text.
   *
   * @param selector the name, corresponding to the value of one or several elements' link text
   * @return the By locator corresponding to that selector
   */
  public static By linkText(final String selector) {
    return By.linkText(selector);
  }

  /**
   * Returns a By locator for a partial link text.
   *
   * @param selector the name, corresponding to the value of one or several elements' link text
   * @return the By locator corresponding to that selector
   */
  public static By partialLinkText(final String selector) {
    return By.partialLinkText(selector);
  }

  /**
   * Returns a By locator for specified accessibility ID. Mobile-only.
   *
   * @param selector the accessibility ID for the element or elements in the DOM. Implementation
   *     varies for iOS and Android
   * @return the By locator corresponding to that selector
   */
  public static By accessibilityId(final String selector) {
    return AppiumBy.accessibilityId(selector);
  }

  /**
   * Returns a By locator with a UIAutomator selector. Android-only.
   *
   * @param selector the UIAutomator selector pointing at an element or elements in the DOM
   * @return the By locator corresponding to that selector
   */
  public static By androidUIAutomator(final String selector) {
    return AppiumBy.androidUIAutomator(selector);
  }

  /**
   * Returns a By locator with a class chain selector. iOS-only.
   *
   * @param selector the class chain selector pointing at an element or elements in the DOM
   * @return the By locator corresponding to that selector
   */
  @SuppressWarnings("checkstyle:MethodName")
  public static By iOSClassChain(final String selector) {
    return AppiumBy.iOSClassChain(selector);
  }

  /**
   * Returns a By locator with an NsPredicate selector. iOS-only.
   *
   * @param selector the NsPredicate selector pointing at an element or elements in the DOM
   * @return the By locator corresponding to that selector
   */
  @SuppressWarnings("checkstyle:MethodName")
  public static By iOSNsPredicate(final String selector) {
    return AppiumBy.iOSNsPredicateString(selector);
  }

  /**
   * Returns a By locator for the given className.
   *
   * @param selector the AppiumBy className selector string used to find an element in the DOM
   * @return the By locator corresponding to that selector
   */
  public static By appiumClassName(final String selector) {
    return AppiumBy.className(selector);
  }

  /**
   * Creates a By using JQuery's <a href="https://github.com/jquery/sizzle/wiki">Sizzle library</a>
   *
   * @param selector The content of a Sizzle Public API call, pointing at an element in the DOM
   * @return a By which locates elements selected by that Sizzle Public API call
   * @throws IllegalArgumentException if sizzle selector is null
   */
  @SuppressWarnings("checkstyle:MethodName")
  public static By jQuery(final String selector) {
    if (selector == null) {
      throw new IllegalArgumentException("Cannot find elements with a null Sizzle selector.");
    }
    return new ByJQuery(selector);
  }

  /**
   * Creates a By using an arbitrary JavaScript selector
   *
   * @param selector an arbitrary piece of JavaScript that locates a WebElement or WebElements
   * @return a By which locates elements selected by that JavaScript
   * @throws IllegalArgumentException if JS selector is null
   */
  public static By javaScript(final String selector) {
    if (selector == null) {
      throw new IllegalArgumentException("Cannot find elements with a null JavaScript selector.");
    }
    return new ByJavaScript(selector);
  }

  /**
   * Executes an arbitrary piece of JavaScript to retrieve an element or elements from the DOM. Used
   * by both the JQuery and JavaScript locator strategies.
   *
   * @param context The SearchContext (WebDriver or WebElement) to run the script against
   * @param script an arbitrary piece of JavaScript that returns a WebElement or several WebElements
   * @return a List of WebElements found by the script
   */
  @SuppressWarnings("unchecked")
  static List<WebElement> getElementsFromScript(final SearchContext context, final String script) {
    JavascriptExecutor executor = (JavascriptExecutor) context;
    Object result = executor.executeScript(script);
    if (WebElement.class.isAssignableFrom(result.getClass())) {
      return Collections.singletonList((WebElement) result);
    }
    return (List<WebElement>) result;
  }

  /** An extension of By that finds elements with JQuery's Sizzle selection library. */
  public static class ByJQuery extends By implements Serializable {

    /** The String selector for the jQuery by */
    private final String selector;

    /**
     * select by JQuery expression
     *
     * @param selector The content of a Sizzle Public API call
     */
    ByJQuery(final String selector) {
      this.selector = selector;
    }

    /**
     * Returns a single element matching the Sizzle selector.
     *
     * @param context A context to use to find the element
     * @return The WebElement that matches the selector
     * @exception WebDriverException if the context doesn't support finding by CSS
     * @exception NoSuchElementException if no element matching the selector is found
     */
    @Override
    public WebElement findElement(final SearchContext context) {
      return this.findElements(context).get(0);
    }

    /**
     * Returns all elements matching the Sizzle selector.
     *
     * @param context A context to use to find the elements
     * @return A List of WebElements that match the selector
     * @exception WebDriverException if the context doesn't support finding by CSS
     * @exception NoSuchElementException if no element matching the selector is found
     */
    @Override
    public List<WebElement> findElements(final SearchContext context) {
      // org.openqa.selenium.internal.FindsByCssSelector is deprecated, but still used by Selenium
      injectSizzleIfMissing(context);
      String sizzleCall = "return Sizzle(\"" + selector + "\")";
      List<WebElement> elements = getElementsFromScript(context, sizzleCall);
      if (!elements.isEmpty()) {
        return elements;
      }
      throw new NoSuchElementException("Could not find selector '" + selector + "' in the DOM!");
    }

    /**
     * Injects Sizzle into the current context if it's missing.
     *
     * @param context The context to check for Sizzle and inject if needed.
     * @throws RuntimeException if sizzle failed to load
     */
    void injectSizzleIfMissing(final SearchContext context) {
      if (isSizzleLoaded(context)) {
        logger.debug("Sizzle already loaded. Not injecting again.");
        return;
      }
      try {
        logger.debug("Injecting Sizzle into the current page.");
        injectSizzle(context);
        logger.debug("Injected Sizzle into the current page.");
      } catch (Exception unknownException) {
        // Should never get here, but if we do we should see exception info
        logger.error("Unknown error during sizzle injection attempt.", unknownException);
      }
      if (!isSizzleLoaded(context)) {
        throw new RuntimeException("Sizzle loading has failed.");
      }
    }

    /**
     * Check if the Sizzle library is loaded in a particular context.
     *
     * @param context The context to check for Sizzle.
     * @return true if Sizzle is loaded in the web page
     */
    boolean isSizzleLoaded(final SearchContext context) {
      try {
        String script = "return (typeof window.Sizzle) !== 'undefined';";
        return (boolean) ((JavascriptExecutor) context).executeScript(script);
      } catch (WebDriverException e) {
        return false;
      }
    }

    /**
     * Injects Sizzle into the specified context.
     *
     * @param context The context to inject Sizzle into.
     * @throws IOException when sizzle fails to load
     */
    @SuppressWarnings("PMD.UseProperClassLoader")
    void injectSizzle(final SearchContext context) throws IOException {
      try (InputStream sizzleStream =
          getClass().getClassLoader().getResourceAsStream("sizzle.min.js")) {
        String sizzleString =
            IOUtils.toString(Objects.requireNonNull(sizzleStream), StandardCharsets.UTF_8);

        String documentIsReady = "return document.readyState === 'complete';";
        String sizzleIsLoaded = "return (typeof window.Sizzle) !== 'undefined';";

        Failsafe.with(
                // these retry numbers don't have any hard requirements, but
                // realize jquery lookup can potentially be performed on ALL elements.
                // the failure mechanisms should be fairly quick, performant, and as non-blocking as
                // possible
                // with that said: these retries are expected to be hit only in rare circumstances
                RetryPolicy.builder()
                    .withBackoff(1L, 10L, ChronoUnit.SECONDS)
                    .withMaxRetries(3)
                    .withMaxDuration(Duration.ofMinutes(1L))
                    .onFailedAttempt(
                        failure ->
                            logger.debug(
                                "failed to inject sizzle, retrying", failure.getLastException()))
                    .onFailure(
                        failure ->
                            logger.debug(
                                "failed to inject sizzle, OUT OF RETRIES", failure.getException()))
                    .build())
            .run(
                () -> {
                  new FluentWait<>((WebDriver) context)
                      .withTimeout(Duration.ofSeconds(10))
                      .pollingEvery(Duration.ofSeconds(1))
                      .until(
                          ignored ->
                              (Boolean)
                                  ((JavascriptExecutor) context).executeScript(documentIsReady));

                  ((JavascriptExecutor) context).executeScript(sizzleString);

                  new FluentWait<>((WebDriver) context)
                      .withTimeout(Duration.ofSeconds(10))
                      .pollingEvery(Duration.ofSeconds(1))
                      .until(
                          ignored ->
                              (Boolean)
                                  ((JavascriptExecutor) context).executeScript(sizzleIsLoaded));
                });
      }
    }

    @Override
    public String toString() {
      return "By.JQuery: " + selector;
    }
  }

  /** An extension of By that finds elements with an arbitrary piece of JavaScript. */
  public static class ByJavaScript extends By implements Serializable {

    /** The JavaScript selector */
    private final String selector;

    /**
     * Locates using a javascript selector
     *
     * @param selector Arbitrary JavaScript expression to return a WebElement or WebElements.
     */
    ByJavaScript(final String selector) {
      this.selector = selector;
    }

    /**
     * Returns a single element found by the arbitrary JavaScript.
     *
     * @param context A context to use to find the element
     * @return The WebElement returned by the JavaScript expression.
     * @exception NoSuchElementException if no element is returned
     */
    @Override
    public WebElement findElement(final SearchContext context) {
      return this.findElements(context).get(0);
    }

    /**
     * Returns all elements found by the arbitrary JavaScript.
     *
     * @param context A context to use to find the elements
     * @return List of WebElements returned by the JavaScript expression.
     * @exception NoSuchElementException if no element is returned
     */
    @Override
    public List<WebElement> findElements(final SearchContext context) {
      List<WebElement> elements = getElementsFromScript(context, selector);
      if (!elements.isEmpty()) {
        return elements;
      }
      throw new NoSuchElementException(
          "Could not find any elements using JavaScript expression: '" + selector + "'");
    }

    @Override
    public String toString() {
      return "By.JavaScript: " + selector;
    }
  }
}
