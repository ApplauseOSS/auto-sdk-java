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

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.helpers.sync.Condition;
import com.applause.auto.pageobjectmodel.base.UIElement;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;

/**
 * Condition for a List of UIElements. Returns the List when a specified Function returns true for
 * all items in the List (unless timeout is reached).
 *
 * @param <E> a type extending UIElement, a List of which serves as both the input and output types
 *     of this Condition
 */
public class AllMatchCondition<E extends UIElement> implements Condition<List<E>, List<E>> {
  private static final Logger logger = LogManager.getLogger();

  private final List<E> elements;
  private final Function<E, Boolean> function;
  private final String description;
  @Setter private Duration timeout;
  @Setter private Duration pollingInterval;

  /**
   * Constructor for AllMatchCondition.
   *
   * @param elements The list of elements to check.
   * @param function The function to apply to each element.
   * @param description The description of the condition.
   */
  public AllMatchCondition(
      final List<E> elements, final Function<E, Boolean> function, final String description) {
    this.elements = elements;
    this.function = function;
    this.description = description;
  }

  /**
   * Run the Condition against the whole List.
   *
   * @param context The current context to execute under
   * @return the List of UIElements specified at creation of this AllMatchCondition
   */
  @Override
  public List<E> waitThenReturn(final IPageObjectContext context) {
    return waitThenReturn(
        context,
        Optional.ofNullable(this.timeout).orElse(context.getTimeout()),
        Optional.ofNullable(this.pollingInterval).orElse(context.getPollingInterval()));
  }

  /**
   * Run the Condition against the whole List.
   *
   * @param context The current context to execute under
   * @param newTimeout the duration to wait before timing out
   * @param newPollingInterval the duration between polls
   * @return the List of UIElements specified at creation of this AllMatchCondition
   */
  @Override
  public List<E> waitThenReturn(
      final IPageObjectContext context,
      final Duration newTimeout,
      final Duration newPollingInterval) {
    logger.debug("Waiting until List {}.", description);
    Wait<WebDriver> wait = context.getWait(newTimeout, newPollingInterval);
    try {
      wait.until(ignored -> elements.stream().allMatch(function::apply));
    } catch (TimeoutException e) {
      throw new TimeoutException(
          String.format(
              "Timed out waiting until entire List %s. Waited %ds with polling interval %ds.",
              description, newTimeout.getSeconds(), newPollingInterval.getSeconds()),
          e);
    }
    return elements;
  }
}
