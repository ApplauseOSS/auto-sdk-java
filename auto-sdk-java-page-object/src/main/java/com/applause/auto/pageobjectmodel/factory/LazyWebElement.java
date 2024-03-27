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

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.data.enums.SwipeDirection;
import com.applause.auto.pageobjectmodel.base.LocatedBy;
import com.applause.auto.pageobjectmodel.base.UIElement;
import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;
import com.applause.auto.pageobjectmodel.elements.ContainerElement;
import io.appium.java_client.AppiumDriver;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;

/**
 * Lazy-loading proxy class for Selenium's WebElement. Proxies WebElement method calls to an
 * underlying WebElement. All WebElement method calls are wrapped to lazily load the underlying
 * WebElement and to protect the user from StaleElementReferenceExceptions.
 *
 * @see com.applause.auto.pageobjectmodel.base.BaseElement
 * @see Locator
 * @see LazyList
 * @see PageObjectBuilder
 */
public class LazyWebElement implements WebElement, UIElement {
  private static final Logger logger = LogManager.getLogger();

  @Getter private final LazyWebElement parent;
  @Getter private final IPageObjectContext context;

  /** -- GETTER -- Retrieves the Locator for this element. */
  @Getter final Locator locator;

  private WebElement underlying;
  @Getter private Object[] formatArgs;
  private Duration timeout;
  private Duration pollingInterval;

  private SearchContext shadowRoot;

  /**
   * Basic constructor for a LazyWebElement with no parent.
   *
   * @param locator a Locator pointing at an element in the DOM
   * @param context the underlying context to use
   */
  @SuppressWarnings("PMD.NullAssignment")
  public LazyWebElement(final Locator locator, final IPageObjectContext context) {
    this.locator = locator;
    this.context = context;
    this.parent = null;
  }

  /**
   * Basic constructor for a LazyWebElement with a parent.
   *
   * @param locator a Locator pointing at an element in the DOM
   * @param parent this element's parent element
   * @param context the underlying context to use
   */
  public LazyWebElement(
      final Locator locator, final LazyWebElement parent, final IPageObjectContext context) {
    this.locator = locator;
    this.parent = parent;
    this.context = context;
  }

  /**
   * Constructor for a "pre-initialized" LazyWebElement. Used by LazyList to avoid having to re-find
   * individual elements after finding a whole List of them.
   *
   * @param underlying the underlying WebElement
   * @param locator the Locate annotation for the element
   * @param parent the parent LazyWebElement
   * @param formatArgs String.format() arguments for a dynamic Locator
   * @param context the underlying context to use
   */
  public LazyWebElement(
      final WebElement underlying,
      final Locator locator,
      final LazyWebElement parent,
      final Object[] formatArgs,
      final IPageObjectContext context) {
    this.underlying = underlying;
    this.parent = parent;
    this.locator = locator;
    this.formatArgs = formatArgs == null ? new Object[] {} : formatArgs.clone();
    this.context = context;
  }

  /**
   * Gets the appropriate {@code Wait<WebDriver>} depending on whether the user has set a custom
   * timeout.
   *
   * @return a WebDriver wait, either using RunUtil's default timeouts or the user's custom ones
   */
  private Wait<WebDriver> getWebDriverWait() {
    if (timeout == null) {
      return this.context.getWait();
    }
    return this.context.getWait(timeout, pollingInterval);
  }

  @Override
  public void initialize() {
    if (locator == null || locator.getBy(formatArgs) == null) {
      throw new IllegalArgumentException(
          String.format(
              "Could not initialize element - it doesn't have a "
                  + "locator matching the current Platform. Platform [%s].",
              this.context.getPlatform()));
    }
    WebElement element;
    List<String> bys;
    final var locatorChain = new LocatorChain(this);
    // First of all, check if the last locator in the chain is JavaScript. If it is, we can't do
    // relative search.
    By lastBy = locator.getBy(formatArgs);
    if (lastBy instanceof LocatedBy.ByJavaScript) {
      if (locatorChain.size() > 1) {
        logger.warn(
            "Element with JavaScript locator [{}] has a parent element. Relative search cannot be performed with a JavaScript locator. Attempting to locate from the top of the page.",
            lastBy);
      }
      element = locator.synchronizeAndReturnWebElement(getWebDriverWait(), formatArgs);
      bys = Collections.singletonList("[" + lastBy + "]");
    }
    // JQuery, on the other hand, requires us to concatenate the string locators together.
    else if (lastBy instanceof LocatedBy.ByJQuery) {
      final var jayQueryBy = locatorChain.getJQueryBy();
      element =
          new Locator(jayQueryBy).synchronizeAndReturnWebElement(getWebDriverWait(), formatArgs);
      bys = Collections.singletonList("[" + jayQueryBy + "]");
    }
    // Otherwise, loop through the locator chain, performing a series of relative searches.
    else {
      element = locatorChain.findElementInChain();
      bys = locatorChain.getByChain();
    }
    underlying = element;

    String debugMsg =
        String.format(
            "Initialized element. Locator %s. Platform [%s].",
            String.join(" -> ", bys), locator.getPlatform());

    if (this.locator.isShadowRoot()) {
      // this makes the selenium element.getShadowRoot() call
      shadowRoot = element.getShadowRoot();
      debugMsg += " shadowRootHost [true].";
    }
    logger.debug(debugMsg);
  }

  @Override
  public boolean isInitialized() {
    return underlying != null;
  }

  @Override
  public LazyWebElement format(final Object... theFormatArgs) {
    this.formatArgs = theFormatArgs.clone();
    return this;
  }

  @Override
  public void setWait(final Duration theTimeout, final Duration thePollingInterval) {
    this.timeout = theTimeout;
    this.pollingInterval = thePollingInterval;
  }

  @Override
  public WebElement getUnderlyingWebElement() {
    return runLazily(() -> underlying);
  }

  /**
   * Provides access to the underlying WebElement's ShadowRoot.
   *
   * @return This instance's underlying WebElement ShadowRoot.
   */
  public SearchContext getRawElementShadowRoot() {
    return this.shadowRoot;
  }

  /**
   * Certain WebElement methods, such as click() and sendKeys(), require the element to be both
   * present and visible. This is called by both signatures of runLazily() in the event that a
   * method throws an ElementNotVisibleException - we wait for the visibility of the element, then
   * try again.
   *
   * @param e rethrows this exception if it doesn't match visibility exception
   * @throws ElementNotInteractableException if element isn't visible per calculations
   */
  private void tryWaitingForVisibility(final WebDriverException e) {
    String notInteractable = "not pointer or keyboard interactable";
    if (!(e instanceof ElementNotInteractableException)
        && !e.getMessage().contains(notInteractable)) {
      throw e;
    }
    try {
      logger.debug(
          "Element method requires visibility, but the element isn't visible. "
              + "Waiting for visibility. Locator {}.",
          locator.getBy(formatArgs));
      getWebDriverWait().until(ExpectedConditions.visibilityOf(this));
    } catch (TimeoutException timeoutException) {
      throw new ElementNotInteractableException(
          String.format(
              "Element not visible. Locator %s. Platform [%s].",
              locator.getBy(formatArgs), locator.getPlatform()),
          timeoutException);
    }
  }

  /**
   * Runs an arbitrary WebElement lambda expression with return type void. If the element is null or
   * stale, attempts to find the element, then re-runs the lambda expression. If the element isn't
   * visible, and it needs to be, waits for visibility, then tries again.
   *
   * @param runnable a lambda function with return type void
   */
  private void runLazily(final Runnable runnable) {
    if (!isInitialized()) {
      initialize();
    }
    try {
      try {
        runnable.run();
      } catch (StaleElementReferenceException e) {
        logger.debug(
            String.format(
                "Element is stale - attempting to reinitialize. Locator %s. Platform [%s].",
                locator.getBy(formatArgs), locator.getPlatform()));
        initialize();
        runnable.run();
      }
    } catch (WebDriverException e) {
      tryWaitingForVisibility(e);
      runnable.run();
    }
  }

  /**
   * Runs an arbitrary WebElement lambda expression with non-void return type. If the element is
   * null or stale, attempts to find the element, then re-runs the lambda expression. If the element
   * isn't visible, and it needs to be, waits for visibility, then tries again.
   *
   * @param supplier a lambda function with non-void return type
   * @param <T> returned original type if it doesn't throw
   * @return returned type from callable
   */
  @SneakyThrows
  private <T> T runLazily(final Callable<T> supplier) {
    if (!isInitialized()) {
      initialize();
    }
    try {
      try {
        return supplier.call();
      } catch (StaleElementReferenceException e) {
        logger.debug(
            String.format(
                "Element is stale - attempting to reinitialize. Locator %s. Platform [%s].",
                locator.getBy(formatArgs), locator.getPlatform()));
        initialize();
        return supplier.call();
      }
    } catch (WebDriverException e) {
      tryWaitingForVisibility(e);
      return supplier.call();
    }
  }

  // ============= WebDriver methods =============

  /** Proxy to the underlying WebElement.click() with lazy-loading and stale element protection. */
  @Override
  public void click() {
    runLazily(() -> underlying.click());
  }

  /** Proxy to the underlying WebElement.submit() with lazy-loading and stale element protection. */
  @Override
  public void submit() {
    runLazily(() -> underlying.submit());
  }

  /**
   * Proxy to the underlying WebElement.sendKeys() with lazy-loading and stale element protection.
   */
  @Override
  public void sendKeys(final CharSequence... charSequences) {
    runLazily(() -> underlying.sendKeys(charSequences));
  }

  /** Proxy to the underlying WebElement.clear() with lazy-loading and stale element protection. */
  @Override
  public void clear() {
    runLazily(() -> underlying.clear());
  }

  /**
   * Proxy to the underlying WebElement.getTagName() with lazy-loading and stale element protection.
   */
  @Override
  public String getTagName() {
    return runLazily(() -> underlying.getTagName());
  }

  /**
   * Proxy to the underlying WebElement.getAttribute() with lazy-loading and stale element
   * protection.
   */
  @Override
  public String getAttribute(final String s) {
    return runLazily(() -> underlying.getAttribute(s));
  }

  @Override
  public String getDomAttribute(final String s) {
    return runLazily(() -> underlying.getDomAttribute(s));
  }

  @Override
  public String getDomProperty(final String s) {
    return runLazily(() -> underlying.getDomProperty(s));
  }

  /**
   * Proxy to the underlying WebElement.isSelected() with lazy-loading and stale element protection.
   */
  @Override
  public boolean isSelected() {
    return runLazily(() -> underlying.isSelected());
  }

  /**
   * Proxy to the underlying WebElement.isEnabled() with lazy-loading and stale element protection.
   */
  @Override
  public boolean isEnabled() {
    return runLazily(() -> underlying.isEnabled());
  }

  /**
   * Proxy to the underlying WebElement.getText() with lazy-loading and stale element protection.
   */
  @Override
  public String getText() {
    return runLazily(() -> underlying.getText());
  }

  /**
   * Proxy to the underlying WebElement.findElements() with lazy-loading and stale element
   * protection.
   */
  @Override
  public List<WebElement> findElements(final By by) {
    return runLazily(() -> underlying.findElements(by));
  }

  /**
   * Proxy to the underlying WebElement.findElement() with lazy-loading and stale element
   * protection.
   */
  @Override
  public WebElement findElement(final By by) {
    return runLazily(() -> underlying.findElement(by));
  }

  /**
   * Proxy to the underlying WebElement.isDisplayed() with lazy-loading and stale element
   * protection.
   */
  @Override
  public boolean isDisplayed() {
    return runLazily(() -> underlying.isDisplayed());
  }

  /**
   * Proxy to the underlying WebElement.getLocation() with lazy-loading and stale element
   * protection.
   */
  @Override
  public Point getLocation() {
    return runLazily(() -> underlying.getLocation());
  }

  /**
   * Proxy to the underlying WebElement.getSize() with lazy-loading and stale element protection.
   */
  @Override
  public Dimension getSize() {
    return runLazily(() -> underlying.getSize());
  }

  /**
   * Proxy to the underlying WebElement.getRect() with lazy-loading and stale element protection.
   */
  @Override
  public Rectangle getRect() {
    return runLazily(() -> underlying.getRect());
  }

  /**
   * Proxy to the underlying WebElement.getCssValue() with lazy-loading and stale element
   * protection.
   */
  @Override
  public String getCssValue(final String s) {
    return runLazily(() -> underlying.getCssValue(s));
  }

  /**
   * Proxy to the underlying WebElement.getScreenshotAs() with lazy-loading and stale element
   * protection.
   */
  @Override
  public <X> X getScreenshotAs(final OutputType<X> target) throws WebDriverException {
    return runLazily(() -> underlying.getScreenshotAs(target));
  }

  @Override
  public SearchContext getShadowRoot() {
    return runLazily(() -> underlying.getShadowRoot());
  }

  @Override
  public <T extends UIElement> T getChild(final Locator childLocator, final Class<T> type) {
    return PageObjectBuilder.withContext(context)
        .forUiElement(type)
        .withParent(this)
        .initialize(childLocator);
  }

  @Override
  public <T extends UIElement> T getChild(final By childLocator, final Class<T> type) {
    return PageObjectBuilder.withContext(context)
        .forUiElement(type)
        .withParent(this)
        .initialize(new Locator(childLocator));
  }

  @Override
  public ContainerElement getChild(final By childLocator) {
    return PageObjectBuilder.withContext(context)
        .forUiElement(ContainerElement.class)
        .withParent(this)
        .initialize(new Locator(childLocator));
  }

  @Override
  public <T extends UIElement> List<T> getChildren(
      final Locator childLocator, final Class<T> type) {
    return PageObjectBuilder.withContext(context)
        .forUiElement(type)
        .withParent(this)
        .initializeList(childLocator);
  }

  @Override
  public <T extends UIElement> List<T> getChildren(final By childLocator, final Class<T> type) {
    return PageObjectBuilder.withContext(context)
        .forUiElement(type)
        .withParent(this)
        .initializeList(new Locator(childLocator));
  }

  @Override
  public List<ContainerElement> getChildren(final By childLocator) {
    return PageObjectBuilder.withContext(context)
        .forUiElement(ContainerElement.class)
        .withParent(this)
        .initializeList(new Locator(childLocator));
  }

  @Override
  public Duration getWaitTimeout() {
    return this.timeout;
  }

  @Override
  public Duration getWaitPollingInterval() {
    return this.pollingInterval;
  }

  @Override
  public boolean exists() {
    return runLazily(() -> this.underlying.isEnabled());
  }

  @Override
  public boolean isClickable() {
    return runLazily(() -> this.underlying.isDisplayed() && this.underlying.isEnabled());
  }

  @Override
  public void scrollToElement() {
    logger.debug(String.format("Scrolling to %s.", this.getClass().getSimpleName()));
    JavascriptExecutor js = (JavascriptExecutor) this.context.getDriver();
    if (this.context.getDriver() instanceof EdgeDriver) {
      // Edge has a hard time scrolling up. let's scroll to the top of the page first
      logger.debug("Special case for Edge, scrolling to the top before searching for an element");
      js.executeScript("window.scrollTo(0, 0)");
    }

    // For certain versions of Safari on macOS, calls to scrollToElement and getLocation
    // will result in a NullPointer Exception. The JavaScript selenium uses doesn't return
    // all the information selenium expects. This is a know defect in Selenium/Sauce
    // https://github.com/SeleniumHQ/selenium/issues/6637
    // We need to look at the capabilities of the webDriver and determine if the browser
    // can support the "scrollTo" operation.
    if (this.context.getDriver() instanceof RemoteWebDriver) {
      Capabilities caps = ((RemoteWebDriver) context.getDriver()).getCapabilities();
      if ("Safari".equals(caps.getBrowserName()) && "12.0".equals(caps.getBrowserVersion())) {
        logger.debug(
            "Special case for Safari 12, scrolling to the top before searching for an element");
        js.executeScript("arguments[0].scrollIntoView(true)", this.getUnderlyingWebElement());
        return;
      }
    }
    js.executeScript("javascript:window.scrollTo(0, arguments[0])", this.getLocation().y);
  }

  @Override
  public void swipeToElement(final SwipeDirection direction, final int attempts) {
    if (!(context.getDriver() instanceof AppiumDriver)) {
      throw new UnsupportedOperationException(
          "Cannot swipe to element if not using a mobile driver. Try scrollToElement()");
    }

    Dimension size = this.context.getDriver().manage().window().getSize();
    Pair<Point, Point> swipeVector = direction.getSwipeVector(size.width, size.height);
    Point start = swipeVector.getKey();
    Point end = swipeVector.getValue();

    for (int i = 0; i < attempts; i++) {
      final var fingerSwipe = new PointerInput(PointerInput.Kind.TOUCH, "finger");
      final var sequence = new Sequence(fingerSwipe, 0);
      // move to start point
      sequence.addAction(
          fingerSwipe.createPointerMove(
              Duration.ZERO, PointerInput.Origin.viewport(), start.x, start.y));
      // press down
      sequence.addAction(fingerSwipe.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
      // swipe to end location over 2 seconds
      sequence.addAction(
          fingerSwipe.createPointerMove(
              Duration.ofMillis(2000), PointerInput.Origin.viewport(), end.x, end.y));
      // release press
      sequence.addAction(fingerSwipe.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
      // do the thing
      ((AppiumDriver) this.context.getDriver()).perform(List.of(sequence));
      // can we see element now?
      if (this.exists()) {
        return;
      }
    }

    throw new NoSuchElementException(
        String.format("Could not find element after [%d] swipes [%s].", attempts, direction));
  }
}
