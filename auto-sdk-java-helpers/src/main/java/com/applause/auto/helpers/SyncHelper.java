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
package com.applause.auto.helpers;

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.context.IPageObjectExtension;
import com.applause.auto.helpers.sync.Condition;
import com.applause.auto.helpers.sync.Until;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Utility class to handle waiting for elements and locating elements. */
@SuppressWarnings({"PMD.ExcessivePublicCount", "PMD.GodClass"})
@AllArgsConstructor
public class SyncHelper implements IPageObjectExtension {
  private static final Logger logger = LogManager.getLogger(SyncHelper.class);
  private final IPageObjectContext context;

  /**
   * Sleep the current thread for specified number of milliseconds.
   *
   * @param millis number of milliseconds to sleep the thread.
   */
  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      logger.error("Sleep failed.", e);
    }
  }

  /**
   * Waits until a condition is true, then returns its result. Conditions should be generated using
   * the fluent condition builder. Example usage: {@code
   * SyncHelper.wait(Until.uiElement(myElement).visible())}
   *
   * @param condition a generated condition
   * @param <O> an output type, generally extending UIElement or a List of that type
   * @return the output of the condition
   * @see Until
   */
  public <O> O wait(final Condition<?, O> condition) {
    return condition.waitThenReturn(this.context);
  }

  /**
   * Waits to see if the condition will be true, if so this function returns true, otherwise it will
   * return false.
   *
   * @param condition a generated condition
   * @param <O> an output type, generally extending UIElement or a List of that type
   * @return boolean result
   */
  public <O> boolean matchesCondition(final Condition<?, O> condition) {
    return condition.matchesCondition(this.context);
  }

  // --------------------- waitUntilAlertPresent ---------------------

  /**
   * Wait until an alert is present on the page.
   *
   * @return the Alert object
   */
  public Alert waitUntilAlertPresent() {
    logger.info("Waiting for alert to appear.");
    return this.context.getWait().until(ExpectedConditions.alertIsPresent());
  }

  // --------------------- Miscellaneous ---------------------

  /**
   * Wait until an arbitrary {@code ExpectedCondition<Boolean> } becomes true.
   *
   * @param expectedCondition the expected condition
   * @return the boolean indicating whether the condition became true
   */
  public boolean waitUntilExpectedConditionIsTrue(
      final ExpectedCondition<Boolean> expectedCondition) {
    return this.context.getWait().until(expectedCondition);
  }

  /**
   * Waits until an expected condition is true
   *
   * @param expect the expected condition
   * @param <T> type of condition you want to wait on
   * @return item returned after wait
   */
  public <T> T waitUntil(final ExpectedCondition<T> expect) {
    return this.context.getWait().until(expect);
  }

  /**
   * Waits until an expected condition is true
   *
   * @param expect the expected condition
   * @param <T> type of condition you want to wait on
   * @param wait the WebDriver wait
   * @param <F> wait type (extends webdriver)
   * @return item returned after wait
   */
  public <T, F extends WebDriver> T waitUntil(
      final ExpectedCondition<T> expect, final Wait<F> wait) {
    return wait.until(expect);
  }

  /**
   * Waits until an expected condition is true
   *
   * @param expect the expected condition
   * @param <T> type of condition you want to wait on
   * @param pollingInterval the wait polling interval
   * @return item returned after wait
   */
  public <T> T waitUntilPolled(final ExpectedCondition<T> expect, final Duration pollingInterval) {
    return new WebDriverWait(this.context.getDriver(), pollingInterval).until(expect);
  }

  /**
   * Waits until an expected condition is true
   *
   * @param expect the expected condition
   * @param <T> type of condition you want to wait on
   * @param pollingInterval the wait polling interval
   * @param sleep the time to wait between polls
   * @return item returned after wait
   */
  public <T> T waitUntilPolled(
      final ExpectedCondition<T> expect, final Duration pollingInterval, final Duration sleep) {
    return new WebDriverWait(this.context.getDriver(), pollingInterval, sleep).until(expect);
  }
}
