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

import lombok.NonNull;

/**
 * Builds WebDriverWait Conditions. Supply a ConditionBuilder with some UIElement or List of
 * UIElements, then call any of the methods in here to create a Condition that can be run against
 * it/them.
 *
 * @param <I> the input type, which is supplied to the condition
 * @param <O> the output type, which is returned when the condition is run. Sometimes the same as
 *     the input type.
 */
public interface ConditionBuilder<I, O> {
  /**
   * Produces a Condition describing whether the input is present
   *
   * @return a Condition describing whether the input is present
   */
  Condition<I, O> present();

  /**
   * Produces a Condition describing whether the input is not present
   *
   * @return a Condition describing whether the input is not present
   */
  Condition<I, O> notPresent();

  /**
   * Produces a Condition describing whether the input is visible
   *
   * @return a Condition describing whether the input is visible
   */
  Condition<I, O> visible();

  /**
   * Produces a Condition describing whether the input is not visible
   *
   * @return a Condition describing whether the input is not visible
   */
  Condition<I, O> notVisible();

  /**
   * Produces a Condition describing whether the input is clickable
   *
   * @return a Condition describing whether the input is clickable
   */
  Condition<I, O> clickable();

  /**
   * Produces a Condition describing whether the input is not clickable
   *
   * @return a Condition describing whether the input is not clickable
   */
  Condition<I, O> notClickable();

  /**
   * Produces a Condition describing whether the input has a given attribute
   *
   * @param attribute The attribute to check the existence of
   * @return a Condition describing whether the input has a given attribute
   */
  Condition<I, O> attributeExists(@NonNull String attribute);

  /**
   * Produces a Condition describing whether the input's attribute matches the expected value
   *
   * @param attribute The attribute to check against
   * @param value The expected value of the attribute
   * @return a Condition describing whether the input's attribute matches the expected value
   */
  Condition<I, O> attributeEquals(@NonNull String attribute, @NonNull String value);

  /**
   * Produces a Condition describing whether the input's attribute contains the provided substring
   *
   * @param attribute The attribute to check against
   * @param substring The substring to check for
   * @return a Condition describing whether the input's attribute contains the provided substring
   */
  Condition<I, O> attributeContains(@NonNull String attribute, @NonNull String substring);

  /**
   * Produces a Condition describing whether the input's text matches the expected value
   *
   * @param text The expected value of input
   * @return a Condition describing whether the input's text matches the expected value
   */
  Condition<I, O> textEquals(@NonNull String text);

  /**
   * Produces a Condition describing whether the input's text contains the provided substring
   *
   * @param substring The substring to check for
   * @return a Condition describing whether the input's text contains the provided substring
   */
  Condition<I, O> textContains(@NonNull String substring);
}
