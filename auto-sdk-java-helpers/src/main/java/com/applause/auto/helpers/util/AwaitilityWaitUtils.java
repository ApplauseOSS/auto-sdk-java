/*
 *
 * Copyright © 2025 Applause App Quality, Inc.
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
package com.applause.auto.helpers.util;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import lombok.NonNull;
import org.awaitility.Awaitility;

/** Awaitility Wait Utils */
public final class AwaitilityWaitUtils {

  private AwaitilityWaitUtils() {
    // utility class
  }

  /**
   * Wait for callable state with predicate and return it's value
   *
   * @param <T> - callable actions type parameter
   * @param callable - actions to call during wait
   * @param predicate - predicate to match for actions that will be called during wait
   * @param waitInterval - wait interval in seconds
   * @param pollingInterval - polling timeout for wait interval in seconds
   * @param alias - text alias for this wait
   * @return T - object instance that will be returned from callable actions during wait
   */
  public static <T> T waitForCondition(
      @NonNull final Callable<T> callable,
      @NonNull final Predicate<T> predicate,
      final int waitInterval,
      final int pollingInterval,
      final String alias) {
    return Awaitility.with()
        .pollInterval(pollingInterval, TimeUnit.SECONDS)
        .pollInSameThread()
        .atMost(waitInterval, TimeUnit.SECONDS)
        .ignoreExceptions()
        .alias(alias)
        .until(callable, predicate);
  }

  /**
   * Wait for callable state with predicate and return it's value
   *
   * @param callable - boolean callable actions state that will be checked during wait
   * @param waitInterval - wait interval in seconds
   * @param pollingInterval - polling timeout for wait interval in seconds
   * @param alias - text alias for this wait
   */
  public static void waitForCondition(
      @NonNull final Callable<Boolean> callable,
      int waitInterval,
      int pollingInterval,
      final String alias) {
    Awaitility.with()
        .pollInterval(pollingInterval, TimeUnit.SECONDS)
        .pollInSameThread()
        .atMost(waitInterval, TimeUnit.SECONDS)
        .ignoreExceptions()
        .alias(alias)
        .until(callable);
  }
}
