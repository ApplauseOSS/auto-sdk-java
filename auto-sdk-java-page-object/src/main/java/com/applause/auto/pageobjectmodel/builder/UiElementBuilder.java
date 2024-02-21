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
package com.applause.auto.pageobjectmodel.builder;

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.pageobjectmodel.base.BaseComponent;
import com.applause.auto.pageobjectmodel.base.BaseElement;
import com.applause.auto.pageobjectmodel.base.UIElement;
import com.applause.auto.pageobjectmodel.factory.LazyWebElement;
import com.applause.auto.pageobjectmodel.factory.Locator;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.openqa.selenium.WebElement;

/**
 * An abstract class defining that ways to initialize different types of UIElements when
 * constructing a page object model
 *
 * @param <T> The type of UIElement being constructed
 */
public abstract class UiElementBuilder<T extends UIElement> {
  /** The Page Object Context */
  protected final IPageObjectContext context;

  /** The Parent Element */
  protected LazyWebElement parent;

  /** The Locator Format Arguments */
  protected Object[] formatArgs = new Object[0];

  /**
   * Initializes a UIElement using a locator
   *
   * @param locator The locator used for fetching the element
   * @return The initialized list
   */
  public abstract T initialize(@NonNull Locator locator);

  /**
   * Initializes a UIElement with a preloaded element
   *
   * @param underlying An underlying element
   * @param locator The locator used for refreshing the element if it becomes stale
   * @return The initialized list
   */
  public abstract T initialize(WebElement underlying, @NonNull Locator locator);

  /**
   * Initializes a lazy list of UIElements using a lazy web elements
   *
   * @param underlying The underlying lazy web element
   * @return The initialized list
   */
  public abstract T initialize(LazyWebElement underlying);

  /**
   * Initializes a lazy list of UIElements using a locator
   *
   * @param locator The locator used for locating the elements in the list
   * @return The initialized list
   */
  public abstract List<T> initializeList(@NonNull Locator locator);

  /**
   * Initializes a list of UIElements with a set of preloaded elements
   *
   * @param elements A list of underlying elements
   * @param locator The locator used for refreshing the list if it becomes stale
   * @return The initialized list
   */
  public abstract List<T> initializeList(List<WebElement> elements, @NonNull Locator locator);

  /**
   * Sets up the common context for all elements created by this initializer
   *
   * @param context The Page Object Context
   */
  protected UiElementBuilder(final IPageObjectContext context) {
    this.context = context;
  }

  /**
   * Provide format arguments to any locators used in creating the given element
   *
   * @param newFormatArgs The format arguments
   * @return The UI Element Initializer
   */
  public UiElementBuilder<T> withFormat(final Object... newFormatArgs) {
    this.formatArgs = newFormatArgs.clone();
    return this;
  }

  /**
   * Provides a parent locator for any elements created by this initializer
   *
   * @param locator The Locator of the parent element
   * @return The UI Element Initializer
   */
  public UiElementBuilder<T> withParent(final Locator locator) {
    this.parent = PageObjectBuilder.withContext(context).forLazyWebElement().initialize(locator);
    return this;
  }

  /**
   * Provides a parent locator and a grandparent for any elements created by this initializer
   *
   * @param locator The Locator of the parent element
   * @param grandParentElement The grandparent LazyWebElement
   * @return The UI Element Initializer
   */
  public UiElementBuilder<T> withParent(
      final Locator locator, final LazyWebElement grandParentElement) {
    this.parent =
        PageObjectBuilder.withContext(context)
            .forLazyWebElement()
            .withParent(grandParentElement)
            .initialize(locator);
    return this;
  }

  /**
   * Provides a parent WebElement along with the locator used to initially fetch that element
   *
   * @param parentElement The parent WebElement
   * @param locator The Locator used to locate the parent element
   * @return The UI Element Initializer
   */
  public UiElementBuilder<T> withParent(final WebElement parentElement, final Locator locator) {
    this.parent =
        PageObjectBuilder.withContext(context)
            .forLazyWebElement()
            .initialize(parentElement, locator);
    return this;
  }

  /**
   * Provides a parent WebElement, a GrandParent element and a parent locator for any elements
   * created by this initializer
   *
   * @param parentElement The parent WebElement
   * @param grandParentElement The grandparent LazyWebElement
   * @param locator The Locator used to locate the parent element
   * @return The UI Element Initializer
   */
  public UiElementBuilder<T> withParent(
      final WebElement parentElement,
      final LazyWebElement grandParentElement,
      final Locator locator) {
    this.parent =
        PageObjectBuilder.withContext(context)
            .forLazyWebElement()
            .withParent(grandParentElement)
            .initialize(parentElement, locator);
    return this;
  }

  /**
   * Provides a LazyWebElement parent for any elements created by this initializer
   *
   * @param parentElement The parent LazyWebElement
   * @return The UI Element Initializer
   */
  public UiElementBuilder<T> withParent(final LazyWebElement parentElement) {
    this.parent = parentElement;
    return this;
  }

  /**
   * Provides a parent BaseElement for any elements created by this initializer
   *
   * @param parentElement The parent BaseElement
   * @return The UI Element Initializer
   */
  public UiElementBuilder<T> withParent(final BaseElement parentElement) {
    this.parent = Optional.ofNullable(parentElement).map(BaseElement::getUnderlying).orElse(null);
    return this;
  }

  /**
   * Provides a parent BaseComponent for any elements created by this initializer
   *
   * @param parentElement The parent of the elements created by this initializer
   * @return The UI Element Initializer
   */
  public UiElementBuilder<T> withParent(final BaseComponent parentElement) {
    this.parent = Optional.ofNullable(parentElement).map(BaseComponent::getUnderlying).orElse(null);
    return this;
  }
}
