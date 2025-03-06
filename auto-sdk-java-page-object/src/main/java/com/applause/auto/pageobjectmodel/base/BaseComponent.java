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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Duration;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

/**
 * An abstract collection of BaseElements and BaseComponents representing a logical grouping of HTML
 * elements in the DOM. Classes extending BaseComponent can be used for collections of elements
 * large and small, from a complete page object to a small group of Buttons. Components are
 * instantiated with a factory pattern by calling {@link PageObjectBuilder} with the class as a
 * parameter, whereupon a new object of the class will be created and all subcomponents and elements
 * will be populated. BaseComponents can optionally be created with a parent element using {@link
 * PageObjectBuilder}, in which case all subcomponents and elements contained within the
 * BaseComponent will be found in the scope of the parent element.
 *
 * @see BaseElement
 * @see com.applause.auto.pageobjectmodel.annotation.Implementation
 * @see PageObjectBuilder
 */
@SuppressWarnings("PMD.LoggerIsNotStaticFinal") // code gen interferes with logger naming
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public abstract class BaseComponent implements UIElement {
  /** A Logger that looks up the classname with any ByteBuddy naming removed */
  protected final Logger logger =
      getClass().getSimpleName().contains("$ByteBuddy")
          ? LogManager.getLogger(
              getClass()
                  .getSimpleName()
                  .substring(0, getClass().getSimpleName().indexOf("$ByteBuddy")))
          : LogManager.getLogger();

  /** -- GETTER -- Gets the underlying element of this component. */
  @Getter @Setter private @Nullable LazyWebElement underlying;

  @Getter @Setter private @NonNull IPageObjectContext context;

  /**
   * Post-creation lifecycle method for the BaseComponent. afterInit() is called by
   * PageObjectBuilder at component creation, immediately after elements are populated.
   */
  @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
  public void afterInit() {
    // Do nothing. Override this method if you need to.
  }

  @Override
  public final Locator getLocator() {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot get locator for component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    return this.underlying.getLocator();
  }

  @Override
  public final Object[] getFormatArgs() {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot get format arguments for component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    return this.underlying.getFormatArgs();
  }

  @Override
  public void initialize() {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot initialize component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    this.underlying.initialize();
  }

  @Override
  public boolean isInitialized() {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot initialize component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    return this.underlying.isInitialized();
  }

  @Override
  public BaseComponent format(final Object... formatArgs) {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot format component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    this.underlying.format(formatArgs);
    return this;
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
    return getChild(new Locator(locator, context.getPlatform()), type);
  }

  @Override
  public ContainerElement getChild(final By locator) {
    return this.getChild(locator, ContainerElement.class);
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
    return getChildren(new Locator(locator, context.getPlatform()), type);
  }

  /**
   * Gets a List of child elements or components of the underlying element using a By locator.
   * Returns a generic ContainerElement.
   *
   * @param locator the By locator for the child.
   * @return the child element or component
   */
  @Override
  public List<ContainerElement> getChildren(final By locator) {
    return this.getChildren(locator, ContainerElement.class);
  }

  /**
   * Links the configured shadowRoot host to a shadowDom with the current page (BaseComponent)
   *
   * @param <T> the child BaseComponent
   * @return the child BaseComponent linked to the underlying element
   */
  @SuppressWarnings({"unchecked"})
  public <T extends BaseComponent> T linkShadowRoot() {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot link shadow root for component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    if (!this.underlying.getLocator().isShadowRoot()) {
      logger.warn(
          "Underlying element Locator [{}] is not configured as ShadowRoot host.",
          this.underlying.getLocator().getVariableName());
    }
    return (T)
        PageObjectBuilder.withContext(context)
            .forBaseComponent(this.getClass())
            .initialize(this.underlying);
  }

  @Override
  public LazyWebElement getParent() {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot get parent for component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    return this.underlying.getParent();
  }

  @Override
  public LazyWebElement getLazyWebElement() {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot get underlying element for component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    return this.underlying;
  }

  @Override
  public WebElement getUnderlyingWebElement() {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot get underlying element for component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    return this.underlying.getUnderlyingWebElement();
  }

  @Override
  public Duration getWaitPollingInterval() {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot get polling interval for component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    return this.underlying.getWaitPollingInterval();
  }

  @Override
  public Duration getWaitTimeout() {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot get wait timeout for component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }

    return this.underlying.getWaitTimeout();
  }

  @Override
  public void setWait(final Duration timeout, final Duration pollingInterval) {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot set wait for component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    this.underlying.setWait(timeout, pollingInterval);
  }

  /**
   * Returns the value of the supplied attribute from the underlying WebElement.
   *
   * @param attribute the attribute to get
   * @return the value of that attribute in the WebElement
   */
  @Override
  public String getAttribute(final String attribute) {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot get attribute for component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    try {
      return this.underlying.getAttribute(attribute);
    } catch (NoSuchElementException | TimeoutException | StaleElementReferenceException e) {
      logger.error("{} could not be initialized!", this.getClass().getSimpleName());
      throw e;
    }
  }

  @Override
  public boolean exists() {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot check existence of component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
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
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot check displayed status for component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
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
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot check clickable state for component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
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
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot check enabled status for component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
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
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot scroll to component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    this.underlying.scrollToElement();
  }

  @Override
  public void swipeToElement(final SwipeDirection direction, final int attempts) {
    if (this.underlying == null) {
      throw new UnsupportedOperationException(
          "Cannot swipe to component [%s] with no underlying element"
              .formatted(this.getClass().getSimpleName()));
    }
    this.underlying.swipeToElement(direction, attempts);
  }
}
