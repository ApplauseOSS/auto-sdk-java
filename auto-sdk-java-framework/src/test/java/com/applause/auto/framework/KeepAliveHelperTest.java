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
package com.applause.auto.framework;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.applause.auto.framework.keepalive.KeepAlive;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KeepAliveHelperTest {

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
    new KeepAlive()
        .forDrivers(mockDriver)
        .pollingEvery(Duration.ofSeconds(1))
        .executeWhile(
            () -> {
              try {
                Thread.sleep(5000);
              } catch (InterruptedException ignored) {
              }
              mockDriver.getTitle();
            });
    verify(mockDriver, times(6)).getCurrentUrl();
    verify(mockDriver, times(1)).getTitle();
    mockDriver.quit();
  }

  @Test(
      expectedExceptions = RuntimeException.class,
      expectedExceptionsMessageRegExp = "This Should Propagate as a Runtime Exception")
  public void testKeepAliveErrorPropagation() throws InterruptedException, ExecutionException {
    final WebDriver mockDriver = setupDriver();
    new KeepAlive()
        .forDrivers(mockDriver)
        .pollingEvery(Duration.ofSeconds(1))
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
    new KeepAlive()
        .forDrivers(mockDriver)
        .pollingEvery(Duration.ofSeconds(1))
        .executeWhile(() -> Assert.fail("This Should Propagate to the main thread"));
  }

  @Test
  public void testMultiDriverKeepAlive() throws InterruptedException, ExecutionException {
    final WebDriver mockDriver = setupDriver();
    final WebDriver mockDriver2 = setupDriver();
    new KeepAlive()
        .forDrivers(mockDriver, mockDriver2)
        .pollingEvery(Duration.ofSeconds(1))
        .executeWhile(
            () -> {
              try {
                Thread.sleep(5100);
                mockDriver.getTitle();
              } catch (InterruptedException ignored) {
              }
            });
    verify(mockDriver, times(6)).getCurrentUrl();
    verify(mockDriver2, times(6)).getCurrentUrl();
    verify(mockDriver, times(1)).getTitle();
    mockDriver.quit();
  }

  @Test
  public void testCustomKeepAlive() throws InterruptedException, ExecutionException {
    final WebDriver mockDriver = setupDriver();
    new KeepAlive()
        .forDrivers(mockDriver)
        .pollingEvery(Duration.ofSeconds(1))
        .usingKeepAlive(WebDriver::getTitle)
        .executeWhile(
            () -> {
              try {
                Thread.sleep(5100);
              } catch (InterruptedException ignored) {
              }
            });
    verify(mockDriver, times(0)).getCurrentUrl();
    verify(mockDriver, times(6)).getTitle();
    mockDriver.quit();
  }

  @Test
  public void testKeepAliveSupplier() throws InterruptedException, ExecutionException {
    final WebDriver mockDriver = setupDriver();
    final String result =
        new KeepAlive()
            .forDrivers(mockDriver)
            .pollingEvery(Duration.ofSeconds(1))
            .usingKeepAlive(WebDriver::getTitle)
            .executeWhile(() -> "Done!");
    Assert.assertEquals(result, "Done!");
    mockDriver.quit();
  }
}
