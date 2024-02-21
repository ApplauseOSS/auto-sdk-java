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

import com.applause.auto.context.IPageObjectContext;
import java.time.Duration;
import lombok.NonNull;
import org.openqa.selenium.TimeoutException;

/**
 * Interface for conditions to be applied against a particular UIElement or List of UIElements.
 * Conditions generally do a WebDriverWait until some value is true, either for one UIElement or a
 * whole List of them.
 *
 * @param <I> the input type, which is supplied to the condition
 * @param <O> the output type, which is returned when the condition is run. Sometimes the same as
 *     the input type.
 */
public interface Condition<I, O> {
  /**
   * Sets the timeout for the wait.
   *
   * @param duration the duration of timeout
   * @return this Condition
   */
  Condition<I, O> setTimeout(@NonNull Duration duration);

  /**
   * Sets the polling interval for the wait.
   *
   * @param duration the duration of polling interval
   * @return this Condition
   */
  Condition<I, O> setPollingInterval(@NonNull Duration duration);

  /**
   * Apply the wait, then return an object of the output type.
   *
   * @param context The current context to execute under
   * @return an object of the output type
   */
  O waitThenReturn(IPageObjectContext context);

  /**
   * Apply the wait, then return an object of the output type. Allows for overriding the default
   * wait temporarily
   *
   * @param context The current context to execute under
   * @param timeout the duration to wait before timing out
   * @param pollingInterval the duration between polls
   * @return an object of the output type
   */
  O waitThenReturn(IPageObjectContext context, Duration timeout, Duration pollingInterval);

  /**
   * Apply the wait, then return a boolean.
   *
   * @param context The current context to execute under
   * @return an object of the output type
   */
  default boolean matchesCondition(IPageObjectContext context) {
    boolean value = true;

    try {
      waitThenReturn(context);
    } catch (TimeoutException e) {
      value = false;
    }

    return value;
  }
}
