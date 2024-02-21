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
package com.applause.auto.helpers.sync.oneofn;

import com.applause.auto.helpers.sync.ConditionBuilder;
import com.applause.auto.helpers.sync.UiConditions;
import com.applause.auto.pageobjectmodel.base.UIElement;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Builder for a UIElement List Condition. These Conditions return a matching UIElement when a
 * specified Function returns true for one of the List items (unless timeout is reached).
 *
 * @param <E> a type extending UIElement. a List of that type is the input, and a single object of
 *     that type is the output
 */
@AllArgsConstructor
public class FirstMatchConditionBuilder<E extends UIElement>
    implements ConditionBuilder<List<E>, E> {
  private final List<E> elements;

  /**
   * Check if the List items meet a custom condition.
   *
   * @param function a Function returning Boolean to be applied against the UIElement
   * @param failureMessage custom assertion failure message
   * @return a Condition with the List and the custom Function
   */
  public FirstMatchCondition<E> meetsCustomCondition(
      final Function<E, Boolean> function, @NonNull final String failureMessage) {
    return new FirstMatchCondition<>(elements, function, failureMessage);
  }

  /**
   * Check if the List items are present.
   *
   * @return a Condition that checks if the List is present
   */
  @Override
  public FirstMatchCondition<E> present() {
    return new FirstMatchCondition<>(elements, UiConditions.present(), "is present");
  }

  /**
   * Check if the List items are not present.
   *
   * @return a Condition that checks if the List is not present
   */
  @Override
  public FirstMatchCondition<E> notPresent() {
    return new FirstMatchCondition<>(elements, UiConditions.notPresent(), "is not present");
  }

  /**
   * Check if the List items are visible.
   *
   * @return a Condition that checks if the List is visible
   */
  @Override
  public FirstMatchCondition<E> visible() {
    return new FirstMatchCondition<>(elements, UiConditions.visible(), "is visible");
  }

  /**
   * Check if the List items are not visible.
   *
   * @return a Condition that checks if the List is not visible
   */
  @Override
  public FirstMatchCondition<E> notVisible() {
    return new FirstMatchCondition<>(elements, UiConditions.notVisible(), "is not visible");
  }

  /**
   * Check if the List items are clickable.
   *
   * @return a Condition that checks if the List is clickable
   */
  @Override
  public FirstMatchCondition<E> clickable() {
    return new FirstMatchCondition<>(elements, UiConditions.clickable(), "is clickable");
  }

  /**
   * Check if the List items are not clickable.
   *
   * @return a Condition that checks if the List is not clickable
   */
  @Override
  public FirstMatchCondition<E> notClickable() {
    return new FirstMatchCondition<>(elements, UiConditions.notClickable(), "is not clickable");
  }

  /**
   * Check if the List items have a particular attribute.
   *
   * @param attribute the attribute to check for
   * @return a Condition that checks if the List has a particular attribute equalling the value
   */
  @Override
  public FirstMatchCondition<E> attributeExists(final @NonNull String attribute) {
    return new FirstMatchCondition<>(
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
  public FirstMatchCondition<E> attributeEquals(
      final @NonNull String attribute, final @NonNull String value) {
    return new FirstMatchCondition<>(
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
  public FirstMatchCondition<E> attributeContains(
      final @NonNull String attribute, final @NonNull String substring) {
    return new FirstMatchCondition<>(
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
  public FirstMatchCondition<E> textEquals(final @NonNull String text) {
    return new FirstMatchCondition<>(
        elements, UiConditions.textEquals(text), String.format("text equals [%s]", text));
  }

  /**
   * Check if the List's text values contain a specified value.
   *
   * @param substring the substring to check against
   * @return a Condition that checks if the UIElement's text contains the value
   */
  @Override
  public FirstMatchCondition<E> textContains(final @NonNull String substring) {
    return new FirstMatchCondition<>(
        elements,
        UiConditions.textContains(substring),
        String.format("text contains [%s]", substring));
  }
}
