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
package com.applause.auto.helpers.sync.all;

import com.applause.auto.helpers.sync.ConditionBuilder;
import com.applause.auto.helpers.sync.UiConditions;
import com.applause.auto.pageobjectmodel.base.UIElement;
import java.util.List;
import java.util.function.Function;
import lombok.NonNull;

/**
 * Builder for a UIElement List Condition. These Conditions return a List of UIElement when a
 * specified Function returns true for all elements (unless timeout is reached).
 *
 * @param <E> a type extending UIElement, a List of which serves as both the input and output types
 *     of this Condition
 */
public class AllMatchConditionBuilder<E extends UIElement>
    implements ConditionBuilder<List<E>, List<E>> {
  private final List<E> elements;

  /**
   * Constructor for AllMatchConditionBuilder.
   *
   * @param elements The list of UI elements to evaluate.
   */
  public AllMatchConditionBuilder(final List<E> elements) {
    this.elements = elements;
  }

  /**
   * Check if the List items meet a custom condition.
   *
   * @param function a Function returning Boolean to be applied against the UIElement
   * @return a Condition with the List and the custom Function
   */
  public AllMatchCondition<E> meetsCustomCondition(final Function<E, Boolean> function) {
    return meetsCustomCondition(function, "meets custom condition");
  }

  /**
   * Check if the List items meet a custom condition, allowing custom condition messages.
   *
   * @param function a Function returning Boolean to be applied against the UIElement
   * @param failureMessage custom assertion failure message
   * @return a Condition with the List and the custom Function
   */
  public AllMatchCondition<E> meetsCustomCondition(
      final Function<E, Boolean> function, @NonNull final String failureMessage) {
    return new AllMatchCondition<>(elements, function, failureMessage);
  }

  /**
   * Check if the List items are present.
   *
   * @return a Condition that checks if the List is present
   */
  @Override
  public AllMatchCondition<E> present() {
    return new AllMatchCondition<>(elements, UiConditions.present(), "is present");
  }

  /**
   * Check if the List items are not present.
   *
   * @return a Condition that checks if the List is not present
   */
  @Override
  public AllMatchCondition<E> notPresent() {
    return new AllMatchCondition<>(elements, UiConditions.notPresent(), "is not present");
  }

  /**
   * Check if the List items are visible.
   *
   * @return a Condition that checks if the List is visible
   */
  @Override
  public AllMatchCondition<E> visible() {
    return new AllMatchCondition<>(elements, UiConditions.visible(), "is visible");
  }

  /**
   * Check if the List items are not visible.
   *
   * @return a Condition that checks if the List is not visible
   */
  @Override
  public AllMatchCondition<E> notVisible() {
    return new AllMatchCondition<>(elements, UiConditions.notVisible(), "is not visible");
  }

  /**
   * Check if the List items are clickable.
   *
   * @return a Condition that checks if the List is clickable
   */
  @Override
  public AllMatchCondition<E> clickable() {
    return new AllMatchCondition<>(elements, UiConditions.clickable(), "is clickable");
  }

  /**
   * Check if the List items are not clickable.
   *
   * @return a Condition that checks if the List is not clickable
   */
  @Override
  public AllMatchCondition<E> notClickable() {
    return new AllMatchCondition<>(elements, UiConditions.notClickable(), "is not clickable");
  }

  /**
   * Check if the List items have a particular attribute.
   *
   * @param attribute the attribute to check for
   * @return a Condition that checks if the List has a particular attribute equalling the value
   */
  @Override
  public AllMatchCondition<E> attributeExists(final @NonNull String attribute) {
    return new AllMatchCondition<>(
        elements,
        UiConditions.attributeExists(attribute),
        String.format("has attribute [%s]", attribute));
  }

  /**
   * Check if the List items have a particular attribute with a specified value.
   *
   * @param attribute the attribute to check for
   * @param value the attribute value to check for
   * @return a Condition that checks if the List has a particular attribute equalling the value
   */
  @Override
  public AllMatchCondition<E> attributeEquals(
      final @NonNull String attribute, final @NonNull String value) {
    return new AllMatchCondition<>(
        elements,
        UiConditions.attributeEquals(attribute, value),
        String.format("attribute [%s] equals [%s]", attribute, value));
  }

  /**
   * Check if the List items have a particular attribute containing a specified substring.
   *
   * @param attribute the attribute to check for
   * @param substring the substring to check for
   * @return a Condition that checks if the List has a particular attribute containing the substring
   */
  @Override
  public AllMatchCondition<E> attributeContains(
      final @NonNull String attribute, final @NonNull String substring) {
    return new AllMatchCondition<>(
        elements,
        UiConditions.attributeContains(attribute, substring),
        String.format("attribute [%s] contains [%s]", attribute, substring));
  }

  /**
   * Check if the List's text values are equal to a specified value.
   *
   * @param text the text to check against
   * @return a Condition that checks if the List's text equals the value
   */
  @Override
  public AllMatchCondition<E> textEquals(final @NonNull String text) {
    return new AllMatchCondition<>(
        elements, UiConditions.textEquals(text), String.format("text equals [%s]", text));
  }

  /**
   * Check if the List's text values contain a specified value.
   *
   * @param substring the substring to check against
   * @return a Condition that checks if the UIElement's text contains the value
   */
  @Override
  public AllMatchCondition<E> textContains(final @NonNull String substring) {
    return new AllMatchCondition<>(
        elements,
        UiConditions.textContains(substring),
        String.format("text contains [%s]", substring));
  }
}
