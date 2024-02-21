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
package com.applause.auto.pageobjectmodel.base;

import com.applause.auto.data.enums.SwipeDirection;
import com.applause.auto.pageobjectmodel.elements.ContainerElement;
import com.applause.auto.pageobjectmodel.factory.Locator;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Represents a single element somewhere on an HTML page.
 *
 * @see com.applause.auto.pageobjectmodel.base.BaseComponent
 * @see BaseElement
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface UIElement extends Locatable {
  /**
   * Gets a child UIElement of this UIElement. This can be either a subclass of BaseElement or
   * BaseComponent.
   *
   * @param <T> Either a subclass of BaseElement or BaseComponent.
   * @param locator the Locator for the child.
   * @param type the class of element or component to be returned.
   * @return the child element or component
   */
  <T extends UIElement> T getChild(Locator locator, Class<T> type);

  /**
   * Gets a child UIElement of this UIElement. This can be either a subclass of BaseElement or
   * BaseComponent.
   *
   * @param <T> Either a subclass of BaseElement or BaseComponent.
   * @param locator the By locator for the child.
   * @param type the class of element or component to be returned.
   * @return the child element or component
   */
  <T extends UIElement> T getChild(By locator, Class<T> type);

  /**
   * Gets a child UIElement of this UIElement as a generic ContainerElement.
   *
   * @param locator the By locator for the child.
   * @return the child element or component
   */
  ContainerElement getChild(By locator);

  /**
   * Gets a list of UIElement children of this UIElement. They can be either be a subclass of
   * BaseElement or BaseComponent.
   *
   * @param <T> Either a subclass of BaseElement or BaseComponent.
   * @param locator the Locator for the children.
   * @param type the class of element or component to be returned.
   * @return the children elements or components
   */
  <T extends UIElement> List<T> getChildren(Locator locator, Class<T> type);

  /**
   * Gets a list of UIElement children of this UIElement. They can be either be a subclass of
   * BaseElement or BaseComponent.
   *
   * @param <T> Either a subclass of BaseElement or BaseComponent.
   * @param locator the By locator for the children.
   * @param type the class of element or component to be returned.
   * @return the children elements or components
   */
  <T extends UIElement> List<T> getChildren(By locator, Class<T> type);

  /**
   * Gets a list of UIElement children of this UIElement as the generic ContainerElement.
   *
   * @param locator the By locator for the children.
   * @return the children elements or components
   */
  List<ContainerElement> getChildren(By locator);

  /**
   * Checks if the underlying WebElement exists. Initializes first, if necessary. (Not to be
   * confused with isInitialized())
   *
   * @return a boolean indicating whether the underlying WebElement exists in the page
   */
  boolean exists();

  /**
   * Checks if the underlying WebElement is displayed.
   *
   * @return a boolean indicating if the underlying WebElement is displayed
   */
  boolean isDisplayed();

  /**
   * Checks if the underlying WebElement is clickable.
   *
   * @return a boolean indicating if the underlying WebElement is clickable
   */
  boolean isClickable();

  /**
   * Checks if the underlying WebElement is enabled.
   *
   * @return a boolean indicating if the underlying WebElement is enabled
   */
  boolean isEnabled();

  /**
   * Scrolls to this element in the page. Only works in contexts that allow execution of JavaScript,
   * like web browsers.
   */
  void scrollToElement();

  /**
   * A "sensible default" for swipeToElementIfNotExists - swipes up (to scroll down) 3 times to find
   * this element.
   */
  default void swipeToElementIfNotExists() {
    swipeToElementIfNotExists(SwipeDirection.UP);
  }

  /**
   * A "sensible default" for swipeToElementIfNotExists - swipes in the given direction 3 times to
   * find this element.
   *
   * @param direction the direction to swipe
   */
  default void swipeToElementIfNotExists(final SwipeDirection direction) {
    swipeToElementIfNotExists(direction, 3);
  }

  /**
   * Swipes to this element in the page in a user-specified direction. This will repeatedly perform
   * a swipe-and-check as many times as the user specifies. Note that the SwipeDirection parameter
   * is a vector corresponding to the direction that will be swiped across the screen - in other
   * words, to scroll down on the page, you need to swipe up. Swipes are made across the middle 70%
   * of the screen. On certain devices, it is possible that a scroll of length 70% will not produce
   * the intended scroll effect, depending on the size and position of the app viewport relative to
   * other UI elements, like the status bar or soft keys. In these cases, a recommended fallback is
   * DeviceControl.swipeAcrossScreenCoordinates(). This variation checks if an element exists before
   * swiping
   *
   * @param direction the direction to swipe
   * @param attempts the number of attempts
   */
  default void swipeToElementIfNotExists(SwipeDirection direction, final int attempts) {
    if (this.exists()) {
      return;
    }
    swipeToElement(direction, attempts);
  }

  /**
   * A "sensible default" for swipeToElement - swipes up (to scroll down) 3 times to find this
   * element.
   */
  default void swipeToElement() {
    swipeToElement(SwipeDirection.UP);
  }

  /**
   * A "sensible default" for swipeToElement - swipes up (to scroll down) 3 times to find this
   * element.
   *
   * @param direction the direction to swipe
   */
  default void swipeToElement(final SwipeDirection direction) {
    swipeToElement(direction, 3);
  }

  /**
   * Swipes to this element in the page in a user-specified direction. This will repeatedly perform
   * a swipe-and-check as many times as the user specifies. Note that the SwipeDirection parameter
   * is a vector corresponding to the direction that will be swiped across the screen - in other
   * words, to scroll down on the page, you need to swipe up. Swipes are made across the middle 70%
   * of the screen. On certain devices, it is possible that a scroll of length 70% will not produce
   * the intended scroll effect, depending on the size and position of the app viewport relative to
   * other UI elements, like the status bar or soft keys. In these cases, a recommended fallback is
   * DeviceControl.swipeAcrossScreenCoordinates().
   *
   * @param direction the direction to swipe
   * @param attempts the number of attempts
   */
  void swipeToElement(SwipeDirection direction, int attempts);

  /**
   * Gets the attribute value for the wrapped element
   *
   * @param attribute The attribute to get the value of
   * @return The string value of the requested attribute
   */
  String getAttribute(String attribute);

  /**
   * Gets the underlying web element
   *
   * @return The underlying WebElement
   */
  WebElement getUnderlyingWebElement();
}
