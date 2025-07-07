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

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.data.enums.SwipeDirection;
import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;
import com.applause.auto.pageobjectmodel.elements.ContainerElement;
import com.applause.auto.pageobjectmodel.factory.LazyWebElement;
import com.applause.auto.pageobjectmodel.factory.Locator;
import java.time.Duration;
import java.util.List;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;

/**
 * BaseElement is the extendable building block of page objects in the Applause Automation
 * framework. Elements correspond one-to-one with elements on a particular web page or mobile
 * application, against which they can perform a variety of actions, like clicks and attribute
 * checks.
 *
 * @see LazyWebElement
 */
public abstract class BaseElement implements UIElement {
  /** The Logger */
  protected final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

  /** The underlying LazyWebElement */
  @Getter protected final LazyWebElement underlying;

  /** The underlying PageObjectContext */
  @Getter protected final IPageObjectContext context;

  /**
   * Constructs the BaseElement with a LazyWebElement. Under most circumstances, you shouldn't need
   * to create BaseElement objects yourself - the PageObjectBuilder will create them for you.
   *
   * @param underlying a lazy loaded web element
   * @param context the underlying context
   */
  public BaseElement(final LazyWebElement underlying, final IPageObjectContext context) {
    this.underlying = underlying;
    this.context = context;
  }

  @Override
  public LazyWebElement getParent() {
    return this.underlying.getParent();
  }

  @Override
  public void initialize() {
    this.underlying.initialize();
  }

  @Override
  public boolean isInitialized() {
    return this.underlying.isInitialized();
  }

  @Override
  public BaseElement format(final Object... formatArgs) {
    this.underlying.format(formatArgs);
    return this;
  }

  @Override
  public Object[] getFormatArgs() {
    return this.underlying.getFormatArgs();
  }

  @Override
  public void setWait(final Duration timeout, final Duration pollingInterval) {
    this.underlying.setWait(timeout, pollingInterval);
  }

  @Override
  public Duration getWaitTimeout() {
    return this.underlying.getWaitTimeout();
  }

  @Override
  public Duration getWaitPollingInterval() {
    return this.underlying.getWaitPollingInterval();
  }

  @Override
  public <T extends UIElement> T getChild(final Locator locator, final Class<T> type) {
    return PageObjectBuilder.withContext(context)
        .forUiElement(type)
        .withParent(this)
        .initialize(locator);
  }

  @Override
  public <T extends UIElement> T getChild(final By locator, final Class<T> type) {
    return getChild(new Locator(locator, this.context.getPlatform()), type);
  }

  @Override
  public ContainerElement getChild(final By locator) {
    return getChild(locator, ContainerElement.class);
  }

  @Override
  public <T extends UIElement> List<T> getChildren(final Locator locator, final Class<T> type) {
    return PageObjectBuilder.withContext(context)
        .forUiElement(type)
        .withParent(this)
        .initializeList(locator);
  }

  @Override
  public <T extends UIElement> List<T> getChildren(final By locator, final Class<T> type) {
    return getChildren(new Locator(locator, this.context.getPlatform()), type);
  }

  @Override
  public List<ContainerElement> getChildren(final By locator) {
    return getChildren(locator, ContainerElement.class);
  }

  @Override
  public Locator getLocator() {
    return this.underlying.getLocator();
  }

  @Override
  public LazyWebElement getLazyWebElement() {
    return this.underlying;
  }

  @Override
  public WebElement getUnderlyingWebElement() {
    return this.underlying.getUnderlyingWebElement();
  }

  /** Clicks on the underlying WebElement. */
  public void click() {
    logger.debug(String.format("Clicking on %s.", this.getClass().getSimpleName()));
    this.underlying.click();
  }

  @Override
  public String getAttribute(final String attribute) {
    String value;
    try {
      value = this.underlying.getAttribute(attribute);
    } catch (NoSuchElementException | TimeoutException | StaleElementReferenceException e) {
      logger.error("{} could not be initialized!", this.getClass().getSimpleName());
      throw e;
    }
    extraAttributeOrPropertyLogging(attribute, value);
    return value;
  }

  /**
   * Returns the value of the supplied attribute from the underlying WebElement.
   *
   * @param attribute the attribute to get
   * @return the value of that attribute in the WebElement
   */
  public String getDomAttributeValue(final String attribute) {
    String value;
    try {
      value = this.underlying.getDomAttribute(attribute);
    } catch (NoSuchElementException | TimeoutException | StaleElementReferenceException e) {
      logger.error("{} could not be initialized!", this.getClass().getSimpleName());
      throw e;
    }
    extraAttributeOrPropertyLogging(attribute, value);
    return value;
  }

  /**
   * Returns the value of the supplied property from the underlying WebElement.
   *
   * @param property the property to get
   * @return the value of that property in the WebElement
   */
  public String getDomPropertyValue(final String property) {
    String value;
    try {
      value = this.underlying.getDomProperty(property);
    } catch (NoSuchElementException | TimeoutException | StaleElementReferenceException e) {
      logger.error("{} could not be initialized!", this.getClass().getSimpleName());
      throw e;
    }
    extraAttributeOrPropertyLogging(property, value);
    return value;
  }

  /*
   * Extra logging for getAttribute calls.  This requests/results are different depending on actual strings supplied.
   * See the following docs:
   * https://w3c.github.io/webdriver/#get-element-attribute
   * https://w3c.github.io/webdriver/#get-element-property
   * https://w3c.github.io/webdriver/#dfn-get-element-property
   *
   * Depending on which is used and in what context:
   * true  is usually the actual value of the requested attribute/property key
   * false can be [null, '', 'false']
   *
   * @param attributeOrProperty the request attribute/property
   * @param value the value returned from the related function call
   */
  private void extraAttributeOrPropertyLogging(
      final String attributeOrProperty, final String value) {
    if (value == null) {
      logger.debug(
          "{} attribute [{}] is null.", this.getClass().getSimpleName(), attributeOrProperty);
    } else {
      logger.debug(
          "{} attribute [{}] is {}.",
          this.getClass().getSimpleName(),
          attributeOrProperty,
          value.isEmpty() ? "empty" : "[" + value + "]");
    }
  }

  /**
   * Returns the Dimension of the element.
   *
   * @return a Dimension object representing the width and height of the underlying WebElement
   */
  public Dimension getDimension() {
    logger.debug(String.format("Getting Dimension of %s.", this.getClass().getSimpleName()));
    Dimension response;
    try {
      // selenium8/W3C style
      response = this.underlying.getRect().getDimension();
    } catch (UnsupportedCommandException uce) {
      // olderSelenium/JWP style
      response = this.underlying.getSize();
    }
    return response;
  }

  /**
   * Returns the Point object corresponding to the top-left corner of the element.
   *
   * @return a Point object representing the top-left coordinates of the underlying WebElement
   */
  public Point getLocation() {
    logger.debug(String.format("Getting location of %s.", this.getClass().getSimpleName()));
    try {
      // selenium8/W3C style
      final var rectangle = this.underlying.getRect();
      if (rectangle == null) {
        return this.underlying.getLocation();
      }
      return new Point(rectangle.x, rectangle.y);
    } catch (UnsupportedCommandException ex) {
      // olderSelenium/JWP style
      return this.underlying.getLocation();
    }
  }

  @Override
  public boolean exists() {
    boolean exists = true;
    try {
      this.underlying.isEnabled();
    } catch (NoSuchElementException | TimeoutException | StaleElementReferenceException e) {
      exists = false;
    }

    logger.debug("{} {}.", this.getClass().getSimpleName(), exists ? "exists" : "does not exist");
    return exists;
  }

  @Override
  public boolean isDisplayed() {
    boolean isDisplayed = false;
    try {
      isDisplayed = this.underlying.isDisplayed();
    } catch (NoSuchElementException | TimeoutException | StaleElementReferenceException ignored) {
    }
    logger.debug(
        "{} {} displayed.", this.getClass().getSimpleName(), isDisplayed ? "is" : "is not");
    return isDisplayed;
  }

  @Override
  public boolean isClickable() {
    boolean isClickable = false;
    try {
      isClickable = this.underlying.isDisplayed() && this.underlying.isEnabled();
    } catch (NoSuchElementException | TimeoutException | StaleElementReferenceException ignored) {
    }
    logger.debug(
        "{} {} clickable.", this.getClass().getSimpleName(), isClickable ? "is" : "is not");
    return isClickable;
  }

  @Override
  public boolean isEnabled() {
    boolean isEnabled = false;
    try {
      isEnabled = this.underlying.isEnabled();
    } catch (NoSuchElementException | TimeoutException | StaleElementReferenceException ignored) {
    }
    logger.debug("{} {} enabled.", this.getClass().getSimpleName(), isEnabled ? "is" : "is not");
    return isEnabled;
  }

  @Override
  public void scrollToElement() {
    this.getUnderlying().scrollToElement();
  }

  @Override
  public void swipeToElement(final SwipeDirection direction, final int attempts) {
    this.underlying.swipeToElement(direction, attempts);
  }
}
