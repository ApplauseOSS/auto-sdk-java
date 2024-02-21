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

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.helpers.sync.Condition;
import com.applause.auto.pageobjectmodel.base.UIElement;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;

/**
 * Condition for a single UIElement. Returns a specified UIElement when a specified Function returns
 * true (unless timeout is reached).
 *
 * @param <E> a type extending UIElement, which serves as both the input and output types of this
 *     Condition
 */
@RequiredArgsConstructor
public class UiElementCondition<E extends UIElement> implements Condition<E, E> {
  private static final Logger logger = LogManager.getLogger();

  private final E element;
  private final Function<E, Boolean> function;
  private final String description;
  @Setter private Duration timeout;
  @Setter private Duration pollingInterval;

  /**
   * Run the Condition.
   *
   * @param context The current context to execute under
   * @return the UIElement specified at creation of this UiElementCondition
   */
  @Override
  public E waitThenReturn(final IPageObjectContext context) {
    return waitThenReturn(
        context,
        Optional.ofNullable(this.timeout).orElse(context.getTimeout()),
        Optional.ofNullable(this.pollingInterval).orElse(context.getPollingInterval()));
  }

  /**
   * Run the Condition.
   *
   * @param context The current context to execute under
   * @param newTimeout the duration to wait before timing out
   * @param newPollingInterval the duration between polls
   * @return the UIElement specified at creation of this UiElementCondition
   */
  @Override
  public E waitThenReturn(
      final IPageObjectContext context,
      final Duration newTimeout,
      final Duration newPollingInterval) {
    logger.debug("Waiting until {} {}.", element.getClass().getSimpleName(), description);
    Wait<WebDriver> wait = context.getWait(newTimeout, newPollingInterval);
    try {
      wait.until(ignored -> function.apply(element));
    } catch (TimeoutException e) {
      throw new TimeoutException(
          String.format(
              "Timed out waiting until %s %s. Waited %ds with polling interval %ds.",
              element.getClass().getSimpleName(),
              description,
              newTimeout.getSeconds(),
              newPollingInterval.getSeconds()),
          e);
    }
    return element;
  }
}
