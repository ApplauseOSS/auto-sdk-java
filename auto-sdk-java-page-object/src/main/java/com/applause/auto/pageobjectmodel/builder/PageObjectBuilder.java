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
import com.google.common.reflect.TypeToken;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/** Builder Class used for initializing different PageObject elements and components */
public final class PageObjectBuilder {

  private PageObjectBuilder() {}

  /**
   * Sets up the builder with a given context
   *
   * @param context The underlying PageObjectContext
   * @return The PAgeObjectBuilder WithContext
   */
  public static WithContext withContext(final @NonNull IPageObjectContext context) {
    if (Objects.isNull(context.getDriver())) {
      throw new RuntimeException(
          "Invalid PageObjectContext detected: No WebDriver tied to context");
    }
    return new WithContext(context);
  }

  /** Extension of the PageObjectBuilder that contains an underlying PageObjectContext */
  @AllArgsConstructor(access = AccessLevel.PACKAGE)
  public static class WithContext {
    private final IPageObjectContext context;

    /**
     * Sets up a builder for the given BaseComponent class
     *
     * @param <T> The BaseComponent implementation
     * @param clazz The BaseComponent class
     * @return A Builder for that component type
     */
    public <T extends BaseComponent> BaseComponentBuilder<T> forBaseComponent(
        final @NonNull Class<T> clazz) {
      return this.forBaseComponent(TypeToken.of(clazz));
    }

    /**
     * Sets up a builder for the given BaseComponent class
     *
     * @param <T> The BaseComponent implementation
     * @param type A TypeToken describing the BaseComponent implementation
     * @return A Builder for that component type
     */
    public <T extends BaseComponent> BaseComponentBuilder<T> forBaseComponent(
        final @NonNull TypeToken<T> type) {
      return new BaseComponentBuilder<>(type, context);
    }

    /**
     * Sets up a builder for the given BaseElement class
     *
     * @param <T> The BaseElement type
     * @param clazz The BaseElement Class
     * @return A Builder for that element type
     */
    public <T extends BaseElement> BaseElementBuilder<T> forBaseElement(
        final @NonNull Class<T> clazz) {
      return new BaseElementBuilder<>(clazz, context);
    }

    /**
     * Sets up a builder for initializing LazyWebElements
     *
     * @return A LazyWebElementBuilder
     */
    public LazyWebElementBuilder forLazyWebElement() {
      return new LazyWebElementBuilder(context);
    }

    /**
     * Sets up a builder for a generic UIElement, which could be a BaseElement, BaseComponent, or
     * LazyWebElement.
     *
     * @param <T> The type of UIElement to initialize
     * @param clazz The UIElement class
     * @return A Builder for the UIElement
     */
    public <T extends UIElement> UiElementBuilder<T> forUiElement(final @NonNull Class<T> clazz) {
      return forUiElement(TypeToken.of(clazz));
    }

    /**
     * Sets up a builder for a generic UIElement, which could be a BaseElement, BaseComponent, or
     * LazyWebElement.
     *
     * @param <T> The type of UIElement to initialize
     * @param type A type token for this UIElement
     * @return A Builder for the UIElement
     */
    @SuppressWarnings("unchecked")
    public <T extends UIElement> UiElementBuilder<T> forUiElement(
        final @NonNull TypeToken<T> type) {
      if (BaseElement.class.isAssignableFrom(type.getRawType())) {
        return (UiElementBuilder<T>) forBaseElement((Class<BaseElement>) type.getRawType());
      }
      if (BaseComponent.class.isAssignableFrom(type.getRawType())) {
        return (UiElementBuilder<T>) forBaseComponent((TypeToken<? extends BaseComponent>) type);
      }
      if (LazyWebElement.class.isAssignableFrom(type.getRawType())) {
        return (UiElementBuilder<T>) forLazyWebElement();
      }
      throw new UnsupportedOperationException(
          "Cannot initialize UIElement type: %s".formatted(type.getRawType().getSimpleName()));
    }
  }
}
