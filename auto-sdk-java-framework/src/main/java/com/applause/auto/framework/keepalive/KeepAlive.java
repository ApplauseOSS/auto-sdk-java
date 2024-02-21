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
package com.applause.auto.framework.keepalive;

import com.applause.auto.framework.ContextManager;
import com.applause.auto.logging.LogOutputSingleton;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebDriver;

/** Helper class for keeping alive one or more WebDrivers while performing a long-running process */
@Log4j2
@NoArgsConstructor
public class KeepAlive {
  private static final int MAX_DRIVERS = 10;

  private List<WebDriver> keepAliveDrivers = List.of();
  private Consumer<WebDriver> using = WebDriver::getCurrentUrl;
  private Duration every = Duration.ofSeconds(5);
  private Duration maxWait = Duration.ofMinutes(20);

  /**
   * Sets the drivers. Accepts a maximum of 10 drivers
   *
   * @param drivers a collection of drivers to keep alive
   * @return The keep alive helper
   */
  public KeepAlive forDrivers(final WebDriver... drivers) {
    if (drivers.length > MAX_DRIVERS) {
      throw new IllegalArgumentException(
          "Keep Alive only able to keep alive " + MAX_DRIVERS + " drivers at a time");
    }
    this.keepAliveDrivers = Arrays.asList(drivers);
    return this;
  }

  /**
   * Sets the function used to keep the driver alive. By default, the keep alive will grab the page
   * title
   *
   * @param usingKeepAlive The override function
   * @return The keep alive
   */
  public KeepAlive usingKeepAlive(final @NonNull Consumer<WebDriver> usingKeepAlive) {
    this.using = usingKeepAlive;
    return this;
  }

  /**
   * Sets the polling duration
   *
   * @param everyDuration The duration between polls
   * @return The keep alive
   */
  public KeepAlive pollingEvery(final @NonNull Duration everyDuration) {
    this.every = everyDuration;
    return this;
  }

  /**
   * Sets the max wait time
   *
   * @param maxWaitDuration The maximum time to keep the drivers alive
   * @return The keep alive
   */
  public KeepAlive withMaxWait(final @NonNull Duration maxWaitDuration) {
    this.maxWait = maxWaitDuration;
    return this;
  }

  /**
   * Polls the keep alive while the provided supplier executes.
   *
   * @param <T> the return type of the supplier
   * @param whileFunction The execution function
   * @return The result of the supplier function
   * @throws ExecutionException If the execution throws an exception
   * @throws InterruptedException If the execution is interrupted
   */
  public <T> T executeWhile(final @NonNull Supplier<T> whileFunction)
      throws ExecutionException, InterruptedException {
    // Since we shut this down every time it is called, set up a new one each time we call this
    // function
    final ScheduledExecutorService keepAliveExecutors =
        new ScheduledThreadPoolExecutor(Math.min(this.keepAliveDrivers.size(), MAX_DRIVERS));

    // Start the keep alive threads
    this.keepAliveDrivers.forEach(driver -> this.setupKeepAlive(driver, keepAliveExecutors));

    // Run the execution function
    final var executionFuture = this.setupExecutorThread(whileFunction);
    try {
      // Wait for the execution to finish with a max wait of x milliseconds
      return executionFuture.get(maxWait.toMillis(), TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      // If the function takes too long, then we should inform the user and cancel keep alive
      // threads
      // This will keep executing the main execution, but stop the keep alive. We can accomplish
      // this
      // by shutting down the keepAliveExecutors
      log.warn(
          "While function timed out after {} milliseconds. Ending keep alive threads",
          maxWait.toMillis());
      keepAliveExecutors.shutdown();
      return executionFuture.get();
    } catch (ExecutionException e) {
      // If an exception or error happens during execution, the CompletableFuture throws an
      // ExecutionException. We should check the cause of it to determine if we need to pass
      // it back to the main thread. This ensures certain exceptions (such as AssertionExceptions)
      // make it back to the main thread
      final var cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      if (cause instanceof Error) {
        throw (Error) cause;
      }
      throw e;
    } finally {
      // This is a no-op if we already shut it down
      keepAliveExecutors.shutdown();
    }
  }

  /**
   * Polls the keep alive while the provided runnable executes.
   *
   * @param whileFunction The execution function
   * @throws ExecutionException If the execution throws an exception
   * @throws InterruptedException If the execution is interrupted
   */
  public void executeWhile(final @NonNull Runnable whileFunction)
      throws InterruptedException, ExecutionException {
    executeWhile(
        () -> {
          whileFunction.run();
          return "Done";
        });
  }

  private void setupKeepAlive(
      final @NonNull WebDriver driver, final @NonNull ScheduledExecutorService keepAliveExecutors) {
    // Get the context and log output queue for the main thread
    final var currentThreadContext = ContextManager.INSTANCE.getCurrentContext();
    final var currentThreadLogsQueue = LogOutputSingleton.getLogsQueue();
    keepAliveExecutors.scheduleAtFixedRate(
        () -> {
          // detach the existing context if one is present. This prevents the context override
          // warning
          ContextManager.INSTANCE.detachContext();
          // Flush the logs from this thread in case any are lingering
          LogOutputSingleton.flush();
          // Pipe the log output of this thread into the main thread
          LogOutputSingleton.pipeTo(currentThreadLogsQueue);
          // If a context is set up already, copy it over to the new thread
          currentThreadContext.ifPresent(ContextManager.INSTANCE::overrideContext);
          try {
            this.using.accept(driver);
          } finally {
            // Flush the logs from this thread
            LogOutputSingleton.flush();
          }
        },
        0L,
        this.every.toMillis(),
        TimeUnit.MILLISECONDS);
  }

  private <T> CompletableFuture<T> setupExecutorThread(final @NonNull Supplier<T> whileFunction) {
    // Get the context and log output queue for the main thread
    final var currentThreadContext = ContextManager.INSTANCE.getCurrentContext();
    final var currentThreadLogsQueue = LogOutputSingleton.getLogsQueue();
    return CompletableFuture.supplyAsync(
        () -> {
          // detach the existing context if one is present. This prevents the context override
          // warning
          ContextManager.INSTANCE.detachContext();
          // Pipe the log output of this thread into the main thread
          LogOutputSingleton.pipeTo(currentThreadLogsQueue);
          // If a context is set up already, copy it over to the new thread
          currentThreadContext.ifPresent(ContextManager.INSTANCE::overrideContext);
          try {
            return whileFunction.get();
          } finally {
            // Flush the logs from this thread
            LogOutputSingleton.flush();
          }
        });
  }
}
