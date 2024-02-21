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

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.helpers.sync.Condition;
import com.applause.auto.pageobjectmodel.base.UIElement;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;

/**
 * Condition for a List of UIElements. When a specified Function returns true for one of the items
 * in the List (unless timeout is reached), returns that item.
 *
 * @param <E> a type extending UIElement. a List of that type is the input, and a single object of
 *     that type is the output
 */
@RequiredArgsConstructor
public class FirstMatchCondition<E extends UIElement> implements Condition<List<E>, E> {
  private static final Logger logger = LogManager.getLogger();

  private final List<E> elements;
  private final Function<E, Boolean> function;
  private final String description;
  @Setter private Duration timeout;
  @Setter private Duration pollingInterval;

  /**
   * Run the Condition against the whole List, returning when any one UIElement returns true
   *
   * @param context The current context to execute under
   * @return the UIElement that matched true
   */
  @Override
  public E waitThenReturn(final IPageObjectContext context) {
    return waitThenReturn(
        context,
        Optional.ofNullable(this.timeout).orElse(context.getTimeout()),
        Optional.ofNullable(this.pollingInterval).orElse(context.getPollingInterval()));
  }

  /**
   * Run the Condition against the whole List, returning when any one UIElement returns true
   *
   * @param context The current context to execute under
   * @param newTimeout the duration to wait before timing out
   * @param newPollingInterval the duration between polls
   * @return the UIElement that matched true
   */
  @Override
  public E waitThenReturn(
      final IPageObjectContext context,
      final Duration newTimeout,
      final Duration newPollingInterval) {
    logger.debug("Waiting until an item in List {}.", description);
    Wait<WebDriver> wait = context.getWait(newTimeout, newPollingInterval);
    AtomicReference<E> returnable = new AtomicReference<>();
    try {
      wait.until(
          ignored ->
              elements.stream()
                  .anyMatch(
                      element -> {
                        returnable.set(element);
                        return function.apply(element);
                      }));
    } catch (TimeoutException e) {
      throw new TimeoutException(
          String.format(
              "Timed out waiting until an item in List %s. Waited %ds with polling interval %ds.",
              description, newTimeout.getSeconds(), newPollingInterval.getSeconds()),
          e);
    }
    return returnable.get();
  }
}
