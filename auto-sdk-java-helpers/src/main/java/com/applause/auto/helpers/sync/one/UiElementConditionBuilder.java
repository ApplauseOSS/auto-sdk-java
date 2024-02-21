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
package com.applause.auto.helpers.sync.one;

import com.applause.auto.helpers.sync.ConditionBuilder;
import com.applause.auto.helpers.sync.UiConditions;
import com.applause.auto.pageobjectmodel.base.UIElement;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Builder for a single UIElement Condition. These Conditions return a specified UIElement when a
 * specified Function returns true (unless timeout is reached).
 *
 * @param <E> a type extending UIElement, which serves as both the input and output types of this
 *     Condition
 */
@AllArgsConstructor
public class UiElementConditionBuilder<E extends UIElement> implements ConditionBuilder<E, E> {
  private final E element;

  /**
   * Check if the UIElement meets a custom condition.
   *
   * @param function a Function returning Boolean to be applied against the UIElement
   * @return a Condition with the UIElement and the custom Function
   */
  public UiElementCondition<E> meetsCustomCondition(final Function<E, Boolean> function) {
    return new UiElementCondition<>(element, function, "meets custom condition");
  }

  /**
   * Check if the UIElement meets a custom condition.
   *
   * @param function a Function returning Boolean to be applied against the UIElement
   * @param failureMessage custom assertion failure message
   * @return a Condition with the UIElement and the custom Function
   */
  public UiElementCondition<E> meetsCustomCondition(
      final Function<E, Boolean> function, @NonNull final String failureMessage) {
    return new UiElementCondition<>(element, function, failureMessage);
  }

  /**
   * Check if the UIElement is present.
   *
   * @return a Condition that checks if the UIElement is present
   */
  @Override
  public UiElementCondition<E> present() {
    return new UiElementCondition<>(element, UiConditions.present(), "is present");
  }

  /**
   * Check if the UIElement is not present.
   *
   * @return a Condition that checks if the UIElement is not present
   */
  @Override
  public UiElementCondition<E> notPresent() {
    return new UiElementCondition<>(element, UiConditions.notPresent(), "is not present");
  }

  /**
   * Check if the UIElement is visible.
   *
   * @return a Condition that checks if the UIElement is visible
   */
  @Override
  public UiElementCondition<E> visible() {
    return new UiElementCondition<>(element, UiConditions.visible(), "is visible");
  }

  /**
   * Check if the UIElement is not visible.
   *
   * @return a Condition that checks if the UIElement is not visible
   */
  @Override
  public UiElementCondition<E> notVisible() {
    return new UiElementCondition<>(element, UiConditions.notVisible(), "is not visible");
  }

  /**
   * Check if the UIElement is clickable.
   *
   * @return a Condition that checks if the UIElement is clickable
   */
  @Override
  public UiElementCondition<E> clickable() {
    return new UiElementCondition<>(element, UiConditions.clickable(), "is clickable");
  }

  /**
   * Check if the UIElement is not clickable.
   *
   * @return a Condition that checks if the UIElement is not clickable
   */
  @Override
  public UiElementCondition<E> notClickable() {
    return new UiElementCondition<>(element, UiConditions.notClickable(), "is not clickable");
  }

  /**
   * Check if the UIElement has a particular attribute.
   *
   * @param attribute the attribute to check for
   * @return a Condition that checks if the UIElement has a particular attribute
   */
  @Override
  public UiElementCondition<E> attributeExists(final @NonNull String attribute) {
    return new UiElementCondition<>(
        element,
        UiConditions.attributeExists(attribute),
        String.format("has attribute [%s]", attribute));
  }

  /**
   * Check if the UIElement has a particular attribute with a specified value.
   *
   * @param attribute the attribute to check for
   * @param value the attribute value to check for
   * @return a Condition that checks if the UIElement has a particular attribute equalling the value
   */
  @Override
  public UiElementCondition<E> attributeEquals(
      final @NonNull String attribute, final @NonNull String value) {
    return new UiElementCondition<>(
        element,
        UiConditions.attributeEquals(attribute, value),
        String.format("attribute [%s] equals [%s]", attribute, value));
  }

  /**
   * Check if the UIElement has a particular attribute with a specified substring.
   *
   * @param attribute the attribute to check for
   * @param substring the substring to check for
   * @return a Condition that checks if the UIElement has a particular attribute containing the
   *     substring
   */
  @Override
  public UiElementCondition<E> attributeContains(
      final @NonNull String attribute, final @NonNull String substring) {
    return new UiElementCondition<>(
        element,
        UiConditions.attributeContains(attribute, substring),
        String.format("attribute [%s] contains [%s]", attribute, substring));
  }

  /**
   * Check if the UIElement's text value is equal to a specified value.
   *
   * @param text the text to check against
   * @return a Condition that checks if the UIElement's text equals the value
   */
  @Override
  public UiElementCondition<E> textEquals(final @NonNull String text) {
    return new UiElementCondition<>(
        element, UiConditions.textEquals(text), String.format("text equals [%s]", text));
  }

  /**
   * Check if the UIElement's text value contains a specified value.
   *
   * @param substring the substring to check against
   * @return a Condition that checks if the UIElement's text contains the value
   */
  @Override
  public UiElementCondition<E> textContains(final @NonNull String substring) {
    return new UiElementCondition<>(
        element,
        UiConditions.textContains(substring),
        String.format("text contains [%s]", substring));
  }
}
