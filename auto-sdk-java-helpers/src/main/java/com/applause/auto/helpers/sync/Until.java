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
package com.applause.auto.helpers.sync;

import com.applause.auto.helpers.sync.all.AllMatchConditionBuilder;
import com.applause.auto.helpers.sync.one.UiElementConditionBuilder;
import com.applause.auto.helpers.sync.oneofn.FirstMatchConditionBuilder;
import com.applause.auto.pageobjectmodel.base.UIElement;
import java.util.Arrays;
import java.util.List;

/**
 * Start point for a fluent interface to generate wait conditions. Intended to be used with the
 * SyncHelper.wait() method. Example usage: {@code
 * SyncHelper.wait(Until.uiElement(myElement).visible())}*
 */
public final class Until {
  private Until() {}

  /**
   * Creates a condition builder for a single element or component. The resulting condition will
   * repeatedly check for some property to be true on that element, then return it.
   *
   * @param <E> a type extending one of the abstract types BaseElement or BaseComponent
   * @param element either an element or component from the page object framework. if a component is
   *     specified, conditions will be applied against its parent element.
   * @return a UiElementConditionBuilder corresponding to the element
   */
  public static <E extends UIElement> UiElementConditionBuilder<E> uiElement(final E element) {
    return new UiElementConditionBuilder<>(element);
  }

  /**
   * Creates a condition builder for a List of elements or components. The resulting condition will
   * repeatedly check for some property to be true on all the list items, then return the same List.
   *
   * @param <E> a type extending one of the abstract types BaseElement or BaseComponent
   * @param elements a list of elements or components from the page object framework. if components
   *     are specified, conditions will be applied against their parent elements.
   * @return a UiElementConditionBuilder corresponding to the elements/components
   */
  public static <E extends UIElement> AllMatchConditionBuilder<E> allOf(final List<E> elements) {
    return new AllMatchConditionBuilder<>(elements);
  }

  /**
   * Creates a condition builder for an array or variable length list of elements or components. The
   * resulting condition will repeatedly check for some property to be true on all the list items,
   * the
   *
   * @param <E> a type extending one of the abstract types BaseElement or BaseComponent
   * @param elements one or more elements or components from the page object framework. if
   *     components are specified, conditions will be applied against their parent elements.
   * @return a UiElementConditionBuilder corresponding to the elements/components
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public static <E extends UIElement> AllMatchConditionBuilder<E> allOf(final E... elements) {
    return new AllMatchConditionBuilder<>(Arrays.asList(elements));
  }

  /**
   * Creates a condition builder for a List of elements or components. The resulting condition will
   * repeatedly check for some property to be true on one of the list items, then return that item.
   *
   * @param <E> a type extending one of the abstract types BaseElement or BaseComponent
   * @param elements a list of elements or components from the page object framework. if components
   *     are specified, conditions will be applied against their parent elements.
   * @return a UiElementConditionBuilder corresponding to the elements/components
   */
  public static <E extends UIElement> FirstMatchConditionBuilder<E> oneOf(final List<E> elements) {
    return new FirstMatchConditionBuilder<>(elements);
  }

  /**
   * Creates a condition builder for an array or variable length list of elements or components. The
   * resulting condition will repeatedly check for some property to be true on one of the list
   * items, then return that item.
   *
   * @param <E> a type extending one of the abstract types BaseElement or BaseComponent
   * @param elements one or more elements or components from the page object framework. if
   *     components are specified, conditions will be applied against their parent elements.
   * @return a UiElementConditionBuilder corresponding to the elements/components
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public static <E extends UIElement> FirstMatchConditionBuilder<E> oneOf(final E... elements) {
    return new FirstMatchConditionBuilder<>(Arrays.asList(elements));
  }
}
