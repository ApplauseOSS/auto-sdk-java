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

import com.applause.auto.pageobjectmodel.base.UIElement;
import java.time.Duration;
import java.util.function.Function;
import lombok.NonNull;

/** Library of UIElement conditions, each returned as a Function returning a Boolean. */
public final class UiConditions {
  private UiConditions() {}

  /**
   * We perform all the checks in this file without LazyWebElement's implicit wait. Otherwise, our
   * wait would call another wait, and we might not honor the user's timeout or polling interval
   * settings correctly.
   *
   * @param <T> the type of UI Element
   * @param element the element
   * @param function function to run
   * @return function result after waiting
   */
  private static <T extends UIElement> boolean withNoWait(
      final T element, final Function<T, Boolean> function) {
    Duration timeout = element.getWaitTimeout();
    Duration pollingInterval = element.getWaitPollingInterval();

    element.noWait();
    try {
      return function.apply(element);
    } finally {
      element.setWait(timeout, pollingInterval);
    }
  }

  /**
   * Check if the UIElement is present in the DOM.
   *
   * @param <E> a type extending BaseElement or BaseComponent
   * @return a function to be applied to an element of that type that returns true or false
   */
  public static <E extends UIElement> Function<E, Boolean> present() {
    return uiElement -> withNoWait(uiElement, UIElement::exists);
  }

  /**
   * Check if the UIElement is not present in the DOM.
   *
   * @param <E> a type extending BaseElement or BaseComponent
   * @return a function to be applied to an element of that type that returns true or false
   */
  public static <E extends UIElement> Function<E, Boolean> notPresent() {
    return uiElement -> !withNoWait(uiElement, UIElement::exists);
  }

  /**
   * Check if the UIElement is visible.
   *
   * @param <E> a type extending BaseElement or BaseComponent
   * @return a function to be applied to an element of that type that returns true or false
   */
  public static <E extends UIElement> Function<E, Boolean> visible() {
    return uiElement -> withNoWait(uiElement, UIElement::isDisplayed);
  }

  /**
   * Check if the UIElement is not visible.
   *
   * @param <E> a type extending BaseElement or BaseComponent
   * @return a function to be applied to an element of that type that returns true or false
   */
  public static <E extends UIElement> Function<E, Boolean> notVisible() {
    return uiElement -> !withNoWait(uiElement, UIElement::isDisplayed);
  }

  /**
   * Check if the UIElement is both present and visible.
   *
   * @param <E> a type extending BaseElement or BaseComponent
   * @return a function to be applied to an element of that type that returns true or false
   */
  public static <E extends UIElement> Function<E, Boolean> clickable() {
    return uiElement -> withNoWait(uiElement, UIElement::isClickable);
  }

  /**
   * Check if the UIElement is either not present or not visible.
   *
   * @param <E> a type extending BaseElement or BaseComponent
   * @return a function to be applied to an element of that type that returns true or false
   */
  public static <E extends UIElement> Function<E, Boolean> notClickable() {
    return uiElement -> !withNoWait(uiElement, UIElement::isClickable);
  }

  /**
   * Check if the UIElement is enabled.
   *
   * @param <E> a type extending BaseElement or BaseComponent
   * @return a function to be applied to an element of that type that returns true or false
   */
  public static <E extends UIElement> Function<E, Boolean> enabled() {
    return uiElement -> withNoWait(uiElement, UIElement::isEnabled);
  }

  /**
   * Check if the UIElement is not enabled.
   *
   * @param <E> a type extending BaseElement or BaseComponent
   * @return a function to be applied to an element of that type that returns true or false
   */
  public static <E extends UIElement> Function<E, Boolean> notEnabled() {
    return uiElement -> !withNoWait(uiElement, UIElement::isEnabled);
  }

  /**
   * Check if the UIElement has a particular attribute.
   *
   * @param <E> a type extending BaseElement or BaseComponent
   * @param attribute the attribute to check for
   * @return a function to be applied to an element of that type that returns true or false
   */
  public static <E extends UIElement> Function<E, Boolean> attributeExists(
      final @NonNull String attribute) {
    return uiElement ->
        withNoWait(
            uiElement,
            element -> {
              String attributeValue = element.getAttribute(attribute);
              return attributeValue != null && !attributeValue.isEmpty();
            });
  }

  /**
   * Check if the UIElement has a particular attribute with a specified value.
   *
   * @param <E> a type extending BaseElement or BaseComponent
   * @param attribute the attribute to check for
   * @param value the attribute value to check for
   * @return a function to be applied to an element of that type that returns true or false
   */
  public static <E extends UIElement> Function<E, Boolean> attributeEquals(
      final @NonNull String attribute, final @NonNull String value) {
    return uiElement ->
        withNoWait(
            uiElement,
            element -> {
              String attributeValue = element.getAttribute(attribute);
              return attributeValue != null && attributeValue.equals(value);
            });
  }

  /**
   * Check if the UIElement has a particular attribute with a specified substring.
   *
   * @param <E> a type extending BaseElement or BaseComponent
   * @param attribute the attribute to check for
   * @param substring the substring to check for
   * @return a function to be applied to an element of that type that returns true or false
   */
  public static <E extends UIElement> Function<E, Boolean> attributeContains(
      final @NonNull String attribute, final @NonNull String substring) {
    return uiElement ->
        withNoWait(
            uiElement,
            element -> {
              String attributeValue = element.getAttribute(attribute);
              return attributeValue != null && attributeValue.contains(substring);
            });
  }

  /**
   * Check if the UIElement's text value is equal to a specified value.
   *
   * @param <E> a type extending BaseElement or BaseComponent
   * @param text the text to check against
   * @return a function to be applied to an element of that type that returns true or false
   */
  public static <E extends UIElement> Function<E, Boolean> textEquals(final @NonNull String text) {
    return uiElement ->
        withNoWait(
            uiElement,
            element -> {
              String textValue = element.getLazyWebElement().getText();
              return textValue != null && textValue.equals(text);
            });
  }

  /**
   * Check if the UIElement's text value contains a specified substring.
   *
   * @param <E> a type extending BaseElement or BaseComponent
   * @param substring the substring to check for
   * @return a function to be applied to an element of that type that returns true or false
   */
  public static <E extends UIElement> Function<E, Boolean> textContains(
      final @NonNull String substring) {
    return uiElement ->
        withNoWait(
            uiElement,
            element -> {
              String textValue = element.getLazyWebElement().getText();
              return textValue != null && textValue.contains(substring);
            });
  }
}
