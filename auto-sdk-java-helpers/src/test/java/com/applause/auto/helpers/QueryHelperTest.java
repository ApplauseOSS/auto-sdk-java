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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.applause.auto.context.PageObjectContext;
import com.applause.auto.data.enums.DriverType;
import com.applause.auto.data.enums.Platform;
import com.applause.auto.pageobjectmodel.base.LocatedBy;
import com.applause.auto.pageobjectmodel.elements.ContainerElement;
import com.applause.auto.pageobjectmodel.factory.Locator;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class QueryHelperTest {
  protected final Logger logger = LogManager.getLogger(QueryHelperTest.class);
  private PageObjectContext context;

  private PageObjectContext getMockDriver() {
    final var mockDriver = mock(FirefoxDriver.class);
    when(mockDriver.getCurrentUrl())
        .thenReturn("https://admin.stage.automation.applause.com/sdktestpage.html");
    when(mockDriver.findElement(By.id("will-hide"))).thenReturn(mock(WebElement.class));
    when(mockDriver.findElements(By.name("radio")))
        .thenReturn(
            Arrays.asList(mock(WebElement.class), mock(WebElement.class), mock(WebElement.class)));
    return new PageObjectContext(
        mockDriver,
        DriverType.FIREFOX,
        Duration.ofSeconds(1),
        Duration.ofMillis(100),
        Platform.WEB_DESKTOP_FIREFOX);
  }

  @BeforeClass(alwaysRun = true)
  public void setupDriver() {
    context = getMockDriver();
  }

  @Test
  public void testFindElement() {
    logger.info("STEP 1: Find an element in the page and initialize it.");
    new QueryHelper(this.context)
        .findElement(
            new Locator(LocatedBy.id("will-hide"), this.context.getPlatform()),
            ContainerElement.class)
        .initialize();
  }

  @Test
  public void testFindElements() {
    logger.info("STEP 1: Find some elements in the page.");
    List<ContainerElement> elements =
        new QueryHelper(this.context)
            .findElements(new Locator(LocatedBy.name("radio"), this.context.getPlatform()));

    logger.info("STEP 2: Initialize the elements.");
    for (ContainerElement element : elements) {
      element.initialize();
    }
  }

  @Test
  public void testElementCount() {
    logger.info("STEP 1: Find a count of 3 elements in the page.");
    assertEquals(
        new QueryHelper(this.context)
            .elementCount(new Locator(LocatedBy.name("radio"), this.context.getPlatform())),
        3);
  }
}
