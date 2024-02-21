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
package com.applause.auto.pageobjectmodel.factory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.applause.auto.context.PageObjectContext;
import com.applause.auto.data.enums.DriverType;
import com.applause.auto.data.enums.Platform;
import com.applause.auto.pageobjectmodel.base.LocatedBy;
import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.Test;

public class LocatorChainTest {
  private PageObjectContext getMockDriver() {
    final var mockDriver = mock(FirefoxDriver.class);
    when(mockDriver.getCurrentUrl())
        .thenReturn("https://admin.stage.automation.applause.com/sdktestpage.html");

    final var defaultMock = mock(WebElement.class);
    when(defaultMock.getAttribute("id")).thenReturn("fake-id");
    when(defaultMock.isEnabled()).thenReturn(true);
    when(defaultMock.isDisplayed()).thenReturn(true);

    final var topMock = mock(WebElement.class);
    final var childMock = mock(WebElement.class);
    final var grandChildMock = mock(WebElement.class);

    when(topMock.findElement(By.id("top-child"))).thenReturn(childMock);
    when(topMock.findElements(By.id("top-child"))).thenReturn(List.of(childMock));
    when(topMock.findElement(By.id("top-grand-child"))).thenReturn(grandChildMock);
    when(topMock.findElements(By.id("top-grand-child"))).thenReturn(List.of(grandChildMock));
    when(childMock.findElement(By.id("top-grand-child"))).thenReturn(grandChildMock);
    when(childMock.findElements(By.id("top-grand-child"))).thenReturn(List.of(grandChildMock));

    when(mockDriver.findElement(By.id("top"))).thenReturn(topMock);
    when(mockDriver.findElements(By.id("top"))).thenReturn(List.of(topMock));
    when(mockDriver.findElement(By.id("top-child"))).thenReturn(childMock);
    when(mockDriver.findElements(By.id("top-child"))).thenReturn(List.of(childMock));
    when(mockDriver.findElement(By.id("top-grand-child"))).thenReturn(grandChildMock);
    when(mockDriver.findElements(By.id("top-grand-child"))).thenReturn(List.of(grandChildMock));
    when(mockDriver.findElement(By.id("%s"))).thenThrow(new NoSuchElementException(""));
    when(mockDriver.findElements(By.id("%s"))).thenThrow(new NoSuchElementException(""));

    return new PageObjectContext(
        mockDriver,
        DriverType.FIREFOX,
        Duration.ofSeconds(1),
        Duration.ofMillis(100),
        Platform.WEB_DESKTOP_FIREFOX);
  }

  @Test
  public void testLocatorChainOrdering() {
    final var builder = PageObjectBuilder.withContext(getMockDriver());
    final var greatGrandParent =
        builder.forLazyWebElement().initialize(new Locator(LocatedBy.id("top")));
    final var grandParent =
        builder
            .forLazyWebElement()
            .withParent(greatGrandParent)
            .initialize(new Locator(LocatedBy.id("top-child")));
    final var parent =
        builder
            .forLazyWebElement()
            .withParent(grandParent)
            .initialize(new Locator(LocatedBy.id("top-grand-child")));
    final var child =
        builder
            .forLazyWebElement()
            .withParent(parent)
            .initialize(new Locator(LocatedBy.id("top-great-grand-child")));
    final var lc = new LocatorChain(child);
    assertEquals(lc.getChain().getFirst(), greatGrandParent);
    assertEquals(lc.getChain().getLast(), parent);
    assertEquals(
        lc.getByChain(),
        List.of(
            "By.id: top",
            "By.id: top-child",
            "By.id: top-grand-child",
            "By.id: top-great-grand-child"));
  }
}
