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
import java.util.List;
import lombok.NonNull;
import org.openqa.selenium.WebElement;

/** A Builder class for constructing LazyWebElements */
public class LazyWebElementBuilder extends UiElementBuilder<LazyWebElement> {

  /**
   * Sets up a new LazyWebElementBuilder for a given context
   *
   * @param context The underlying PageObjectContext
   */
  public LazyWebElementBuilder(final IPageObjectContext context) {
    super(context);
  }

  @Override
  public LazyWebElement initialize(final @NonNull Locator locator) {
    return new LazyWebElement(locator, parent, context).format(formatArgs);
  }

  @Override
  public LazyWebElement initialize(final WebElement underlying, final @NonNull Locator locator) {
    return new LazyWebElement(underlying, locator, parent, formatArgs, context);
  }

  @Override
  public LazyWebElement initialize(final LazyWebElement underlying) {
    return underlying;
  }

  @Override
  public List<LazyWebElement> initializeList(final @NonNull Locator locator) {
    return new LazyList<>(LazyWebElement.class, locator, parent, context);
  }

  @Override
  public List<LazyWebElement> initializeList(
      final List<WebElement> elements, final @NonNull Locator locator) {
    return new LazyList<>(LazyWebElement.class, locator, parent, elements, context);
  }

  @Override
  public LazyWebElementBuilder withFormat(final Object... newFormatArgs) {
    super.withFormat(newFormatArgs);
    return this;
  }

  @Override
  public LazyWebElementBuilder withParent(final Locator locator) {
    super.withParent(locator);
    return this;
  }

  @Override
  public LazyWebElementBuilder withParent(
      final Locator locator, final LazyWebElement grandParentElement) {
    super.withParent(locator, grandParentElement);
    return this;
  }

  @Override
  public LazyWebElementBuilder withParent(final WebElement parentElement, final Locator locator) {
    super.withParent(parentElement, locator);
    return this;
  }

  @Override
  public LazyWebElementBuilder withParent(
      final WebElement parentElement,
      final LazyWebElement grandParentElement,
      final Locator locator) {
    super.withParent(parentElement, grandParentElement, locator);
    return this;
  }

  @Override
  public LazyWebElementBuilder withParent(final LazyWebElement parentElement) {
    super.withParent(parentElement);
    return this;
  }

  @Override
  public LazyWebElementBuilder withParent(final BaseElement parentElement) {
    super.withParent(parentElement);
    return this;
  }

  @Override
  public LazyWebElementBuilder withParent(final BaseComponent parentElement) {
    super.withParent(parentElement);
    return this;
  }
}
