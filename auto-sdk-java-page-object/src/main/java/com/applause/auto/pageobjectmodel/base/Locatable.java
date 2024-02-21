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

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.pageobjectmodel.factory.LazyList;
import com.applause.auto.pageobjectmodel.factory.LazyWebElement;
import com.applause.auto.pageobjectmodel.factory.Locator;
import java.time.Duration;
import javax.annotation.Nullable;

/**
 * An interface defining a locatable object within the Applause PageObject model
 *
 * @see UIElement
 * @see LazyWebElement
 * @see LazyList
 * @see BaseComponent
 * @see BaseElement
 */
public interface Locatable {

  /**
   * Gets the page object model context
   *
   * @return The page object model context
   */
  IPageObjectContext getContext();

  /**
   * Gets the locator for this locatable element
   *
   * @return The Locator
   */
  Locator getLocator();

  /**
   * Gets the format args for this element's locator
   *
   * @return The locator format arguments
   */
  Object[] getFormatArgs();

  /**
   * Gets the parent of a UIElement
   *
   * @return The UIElement parent
   */
  LazyWebElement getParent();

  /**
   * Initializes the UIElement. In Applause Automation framework, UIElements are lazily-loaded, so
   * they aren't found in the DOM until they're needed. This method finds the element in the DOM and
   * doesn't perform any other action.
   */
  void initialize();

  /**
   * Check if the UIElement has already been initialized.
   *
   * @return boolean indicating whether the UIElement has been initialized
   */
  boolean isInitialized();

  /**
   * Sets up the UIElement with a formatted locator string. UIElements can then be initialized with
   * formatted strings - like "%s" - and filled in later using this method. Syntax is identical to
   * Java's String.format().
   *
   * @param formatArgs the format args. Will be filled into the string replacing format placeholders
   *     in order
   * @return {@link UIElement} The element that was formatted
   */
  Locatable format(Object... formatArgs);

  /**
   * Set a custom Wait timeout and polling interval for finding an underlying WebElement. This Wait
   * time is used to find the underlying WebElement whenever this element is initialized or
   * re-initialized.
   *
   * @param timeout maximum wait before failing
   * @param pollingInterval re-check poll interval
   */
  void setWait(@Nullable Duration timeout, @Nullable Duration pollingInterval);

  /**
   * Set a custom Wait timeout and polling interval in integer seconds, rather than using Duration
   * objects.
   *
   * @param timeoutSeconds maximum wait before failing
   * @param pollingIntervalSeconds re-check poll interval
   */
  default void setWait(int timeoutSeconds, int pollingIntervalSeconds) {
    setWait(Duration.ofSeconds(timeoutSeconds), Duration.ofSeconds(pollingIntervalSeconds));
  }

  /** Remove the wait time entirely when lazily finding the underlying element. */
  default void noWait() {
    setWait(Duration.ZERO, Duration.ZERO);
  }

  /**
   * Gets the current timeout.
   *
   * @return a Duration object representing the current timeout
   */
  Duration getWaitTimeout();

  /**
   * Gets the current polling interval.
   *
   * @return a Duration object representing the current polling interval
   */
  Duration getWaitPollingInterval();

  /**
   * Restore the default Wait behavior for lazily finding the underlying element. This will cause
   * subsequent Waits to use the timeout and interval set in the SyncHelper (by default, 10 seconds
   * and 1 seconds, respectively).
   */
  default void restoreDefaultWait() {
    setWait(null, null);
  }
}
