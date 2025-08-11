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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.mockito.ArgumentCaptor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KeepAliveTest {

  public WebDriver setupDriver() {
    final var mockDriver = mock(FirefoxDriver.class);
    when(mockDriver.getCurrentUrl())
        .thenReturn("https://admin.stage.automation.applause.com/sdktestpage.html");
    when(mockDriver.getTitle()).thenReturn("Mock Page");
    return mockDriver;
  }

  @Test
  public void testKeepAlive() throws InterruptedException, ExecutionException {
    final WebDriver mockDriver = setupDriver();
    final var mockExecutor = mock(ScheduledExecutorService.class);
    final var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

    // Use the new public API to inject the mock executor
    new KeepAlive()
        .forDrivers(mockDriver)
        .pollingEvery(Duration.ofSeconds(1))
        .withExecutor(mockExecutor)
        .executeWhile(mockDriver::getTitle);

    // Verify that the keep-alive task was scheduled
    verify(mockExecutor)
        .scheduleAtFixedRate(
            runnableCaptor.capture(), eq(0L), eq(1000L), eq(TimeUnit.MILLISECONDS));

    // Manually run the captured keep-alive task to simulate execution over time
    final Runnable keepAliveTask = runnableCaptor.getValue();
    for (int i = 0; i < 6; i++) {
      keepAliveTask.run();
    }

    // Verify the results
    verify(mockDriver, times(6)).getCurrentUrl();
    verify(mockDriver, times(1)).getTitle();
    // Verify the injected executor was NOT shut down
    verify(mockExecutor, never()).shutdown();
    mockDriver.quit();
  }

  @Test(
      expectedExceptions = RuntimeException.class,
      expectedExceptionsMessageRegExp = "This Should Propagate as a Runtime Exception")
  public void testKeepAliveErrorPropagation() throws InterruptedException, ExecutionException {
    final WebDriver mockDriver = setupDriver();
    final var mockExecutor = mock(ScheduledExecutorService.class);
    new KeepAlive()
        .forDrivers(mockDriver)
        .pollingEvery(Duration.ofSeconds(1))
        .withExecutor(mockExecutor)
        .executeWhile(
            () -> {
              throw new RuntimeException("This Should Propagate as a Runtime Exception");
            });
    Assert.fail("Should have thrown a RuntimeException");
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testKeepAliveAssertionFailurePropagation()
      throws InterruptedException, ExecutionException {
    final WebDriver mockDriver = setupDriver();
    final var mockExecutor = mock(ScheduledExecutorService.class);
    new KeepAlive()
        .forDrivers(mockDriver)
        .pollingEvery(Duration.ofSeconds(1))
        .withExecutor(mockExecutor)
        .executeWhile(() -> Assert.fail("This Should Propagate to the main thread"));
  }

  @Test
  public void testMultiDriverKeepAlive() throws InterruptedException, ExecutionException {
    final WebDriver mockDriver = setupDriver();
    final WebDriver mockDriver2 = setupDriver();
    final var mockExecutor = mock(ScheduledExecutorService.class);
    final var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

    new KeepAlive()
        .forDrivers(mockDriver, mockDriver2)
        .pollingEvery(Duration.ofSeconds(1))
        .withExecutor(mockExecutor)
        .executeWhile(mockDriver::getTitle);

    // Verify that keep-alive tasks were scheduled for both drivers
    verify(mockExecutor, times(2))
        .scheduleAtFixedRate(
            runnableCaptor.capture(), eq(0L), eq(1000L), eq(TimeUnit.MILLISECONDS));

    // Manually run the captured tasks
    final List<Runnable> keepAliveTasks = runnableCaptor.getAllValues();
    for (final Runnable task : keepAliveTasks) {
      for (int i = 0; i < 6; i++) {
        task.run();
      }
    }

    // Verify results for both drivers
    verify(mockDriver, times(6)).getCurrentUrl();
    verify(mockDriver2, times(6)).getCurrentUrl();
    verify(mockDriver, times(1)).getTitle();
    // Verify the injected executor was NOT shut down
    verify(mockExecutor, never()).shutdown();
    mockDriver.quit();
  }

  @Test
  public void testCustomKeepAlive() throws InterruptedException, ExecutionException {
    final WebDriver mockDriver = setupDriver();
    final var mockExecutor = mock(ScheduledExecutorService.class);
    final var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

    new KeepAlive()
        .forDrivers(mockDriver)
        .pollingEvery(Duration.ofSeconds(1))
        .usingKeepAlive(WebDriver::getTitle)
        .withExecutor(mockExecutor)
        .executeWhile(() -> {}); // Empty runnable

    // Verify that the custom keep-alive task was scheduled
    verify(mockExecutor)
        .scheduleAtFixedRate(
            runnableCaptor.capture(), eq(0L), eq(1000L), eq(TimeUnit.MILLISECONDS));

    // Manually run the captured keep-alive task
    final Runnable keepAliveTask = runnableCaptor.getValue();
    for (int i = 0; i < 6; i++) {
      keepAliveTask.run();
    }

    // Verify the results
    verify(mockDriver, times(0)).getCurrentUrl();
    verify(mockDriver, times(6)).getTitle();
    // Verify the injected executor was NOT shut down
    verify(mockExecutor, never()).shutdown();
    mockDriver.quit();
  }

  @Test
  public void testKeepAliveSupplier() throws InterruptedException, ExecutionException {
    final WebDriver mockDriver = setupDriver();
    final var mockExecutor = mock(ScheduledExecutorService.class);
    final String result =
        new KeepAlive()
            .forDrivers(mockDriver)
            .pollingEvery(Duration.ofSeconds(1))
            .usingKeepAlive(WebDriver::getTitle)
            .withExecutor(mockExecutor)
            .executeWhile(() -> "Done!");

    Assert.assertEquals(result, "Done!");
    // Verify a keep-alive task was scheduled
    verify(mockExecutor).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any());
    // Verify the injected executor was NOT shut down
    verify(mockExecutor, never()).shutdown();
    mockDriver.quit();
  }
}
