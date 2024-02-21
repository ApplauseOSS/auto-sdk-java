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
import com.applause.auto.pageobjectmodel.factory.LazyList;
import com.applause.auto.pageobjectmodel.factory.LazyWebElement;
import com.applause.auto.pageobjectmodel.factory.Locator;
import java.lang.reflect.Modifier;
import java.util.List;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.openqa.selenium.WebElement;

/**
 * A Builder class for constructing different BaseElement implementations
 *
 * @param <T> The type of Element being constructed
 */
public class BaseElementBuilder<T extends BaseElement> extends UiElementBuilder<T> {
  private final Class<T> clazz;

  /**
   * Sets up a new builder for the given element type and context
   *
   * @param clazz The Class that should be initialized
   * @param context The underlying PageObjectContext
   */
  public BaseElementBuilder(final Class<T> clazz, final IPageObjectContext context) {
    super(context);
    if (Modifier.isAbstract(clazz.getModifiers())) {
      throw new UnsupportedOperationException(
          String.format(
              "BaseElementBuilder cannot create an instance of an abstract BaseElement. Class [%s].",
              clazz.getSimpleName()));
    }
    this.clazz = clazz;
  }

  @Override
  public T initialize(final @NonNull Locator locator) {
    final var underlying =
        PageObjectBuilder.withContext(context)
            .forLazyWebElement()
            .withParent(parent)
            .initialize(locator);
    return this.initialize(underlying);
  }

  @Override
  public T initialize(final @Nullable WebElement element, final @NonNull Locator locator) {
    final LazyWebElement underlying =
        PageObjectBuilder.withContext(context)
            .forLazyWebElement()
            .withParent(parent)
            .initialize(element, locator);
    return this.initialize(underlying);
  }

  @Override
  public T initialize(final LazyWebElement element) {
    try {
      return clazz
          .getConstructor(LazyWebElement.class, IPageObjectContext.class)
          .newInstance(element, context);
    } catch (final ReflectiveOperationException e) {
      throw new UnsupportedOperationException(
          String.format("Cannot find element with type [%s].", clazz.getSimpleName()), e);
    }
  }

  @Override
  public List<T> initializeList(final @NonNull Locator locator) {
    return new LazyList<>(clazz, locator, parent, context);
  }

  @Override
  public List<T> initializeList(final List<WebElement> elements, final @NonNull Locator locator) {
    return new LazyList<>(clazz, locator, parent, elements, context);
  }

  @Override
  public BaseElementBuilder<T> withFormat(final Object... newFormatArgs) {
    super.withFormat(newFormatArgs);
    return this;
  }

  @Override
  public BaseElementBuilder<T> withParent(final Locator locator) {
    super.withParent(locator);
    return this;
  }

  @Override
  public BaseElementBuilder<T> withParent(
      final @NonNull Locator locator, final LazyWebElement grandParentElement) {
    super.withParent(locator, grandParentElement);
    return this;
  }

  @Override
  public BaseElementBuilder<T> withParent(
      final WebElement parentElement, final @NonNull Locator locator) {
    super.withParent(parentElement, locator);
    return this;
  }

  @Override
  public BaseElementBuilder<T> withParent(
      final WebElement parentElement,
      final LazyWebElement grandParentElement,
      final @NonNull Locator locator) {
    super.withParent(parentElement, grandParentElement, locator);
    return this;
  }

  @Override
  public BaseElementBuilder<T> withParent(final LazyWebElement parentElement) {
    super.withParent(parentElement);
    return this;
  }

  @Override
  public BaseElementBuilder<T> withParent(final BaseElement parentElement) {
    super.withParent(parentElement);
    return this;
  }

  @Override
  public BaseElementBuilder<T> withParent(final BaseComponent parentElement) {
    super.withParent(parentElement);
    return this;
  }
}
