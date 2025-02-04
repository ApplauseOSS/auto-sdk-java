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
package com.applause.auto.helpers.mobile;

import static com.applause.auto.helpers.util.AwaitilityWaitUtils.waitForCondition;

import io.appium.java_client.remote.SupportsContextSwitching;
import java.util.Set;
import java.util.concurrent.Callable;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Helper class to work with mobile web and native contexts */
public final class MobileContextsUtils {

  private static final Logger logger = LogManager.getLogger(MobileContextsUtils.class);

  private static final String WEB_CONTEXT_PART_NAME = "web";
  private static final String NATIVE_CONTEXT_PART_NAME = "native";

  private MobileContextsUtils() {
    // utility class
  }

  /**
   * Perform actions in a web context, return result and switch back to native context
   *
   * @param driver - automation driver
   * @param actions - callable to actions to perform inside switched context
   * @param waitForContextTimeout - wait timeout to wait for context to appear
   * @param waitPollingInterval - polling interval timeout for wait for context to appear
   * @param <A> Generic class that is used.
   * @return An object instance from callable actions performed in context
   */
  @SneakyThrows
  public static <A> A performActionsInWebView(
      @NonNull final SupportsContextSwitching driver,
      @NonNull final Callable<A> actions,
      final int waitForContextTimeout,
      final int waitPollingInterval) {
    switchToFirstAvailableWebContext(driver, waitForContextTimeout, waitPollingInterval);
    try {
      return actions.call();
    } finally {
      switchToFirstAvailableNativeContext(driver, waitForContextTimeout, waitPollingInterval);
    }
  }

  /**
   * Switching to web context
   *
   * @param driver - automation driver
   * @param waitForContextTimeout - wait timeout to wait for context to appear
   * @param waitPollingInterval - polling interval timeout for wait for context to appear
   */
  public static void switchToFirstAvailableWebContext(
      @NonNull final SupportsContextSwitching driver,
      final int waitForContextTimeout,
      final int waitPollingInterval) {
    waitForContextAndSwitchToIt(
        driver, WEB_CONTEXT_PART_NAME, waitForContextTimeout, waitPollingInterval);
  }

  /**
   * Switching to native context
   *
   * @param driver - automation driver
   * @param waitForContextTimeout - wait timeout to wait for context to appear
   * @param waitPollingInterval - polling interval timeout for wait for context to appear
   */
  public static void switchToFirstAvailableNativeContext(
      @NonNull final SupportsContextSwitching driver,
      final int waitForContextTimeout,
      final int waitPollingInterval) {
    waitForContextAndSwitchToIt(
        driver, NATIVE_CONTEXT_PART_NAME, waitForContextTimeout, waitPollingInterval);
  }

  /**
   * Wait for mobile app context by string part and switch to it
   *
   * @param driver - automation driver
   * @param contextStringPart - part of expected context name on mobile view
   * @param waitForContextTimeout - wait timeout to wait for context to appear
   * @param waitPollingInterval - polling interval timeout for wait for context to appear
   */
  public static void waitForContextAndSwitchToIt(
      @NonNull final SupportsContextSwitching driver,
      @NonNull final String contextStringPart,
      final int waitForContextTimeout,
      final int waitPollingInterval) {
    logger.info("Looking for context containing: " + contextStringPart);
    waitForCondition(
        () -> {
          Set<String> contexts = driver.getContextHandles();
          logger.info("Available contexts: " + contexts);
          return contexts.stream()
              .anyMatch(context -> StringUtils.containsIgnoreCase(context, contextStringPart));
        },
        waitForContextTimeout,
        waitPollingInterval,
        "Waiting for context string part: " + contextStringPart);
    Set<String> contexts = driver.getContextHandles();
    String neededContext =
        contexts.stream()
            .filter(contextItem -> StringUtils.containsIgnoreCase(contextItem, contextStringPart))
            .findFirst()
            .get();
    logger.info("Switching to context with name: " + neededContext);
    driver.context(neededContext);
  }
}
