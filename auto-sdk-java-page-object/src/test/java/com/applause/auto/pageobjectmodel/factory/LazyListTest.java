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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import com.applause.auto.context.PageObjectContext;
import com.applause.auto.data.enums.DriverType;
import com.applause.auto.data.enums.Platform;
import com.applause.auto.pageobjectmodel.base.LocatedBy;
import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;
import com.applause.auto.pageobjectmodel.elements.ContainerElement;
import com.applause.auto.pageobjectmodel.testobjects.TestComponentFakeElementSub;
import com.applause.auto.pageobjectmodel.testobjects.TestComponentWithFakeElements;
import java.time.Duration;
import java.util.Arrays;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LazyListTest {
  private final Logger logger = LogManager.getLogger();
  private TestComponentWithFakeElements testView;
  private PageObjectContext context;

  private PageObjectContext getMockDriver() {
    final var mockDriver = mock(FirefoxDriver.class);
    when(mockDriver.getCurrentUrl())
        .thenReturn("https://admin.stage.automation.applause.com/sdktestpage.html");

    final var mockElement = mock(WebElement.class);
    when(mockElement.findElement(any())).thenReturn(mockElement);
    when(mockElement.findElements(any()))
        .thenReturn(Arrays.asList(mockElement, mockElement, mockElement, mockElement));
    when(mockDriver.findElement(any())).thenReturn(mockElement);
    when(mockDriver.findElements(any()))
        .thenReturn(Arrays.asList(mockElement, mockElement, mockElement, mockElement));
    return new PageObjectContext(
        mockDriver,
        DriverType.FIREFOX,
        Duration.ofSeconds(1),
        Duration.ofMillis(100),
        Platform.WEB_DESKTOP_FIREFOX);
  }

  @BeforeMethod(alwaysRun = true)
  public void setupDriver() {
    context = getMockDriver();
    context.getDriver().get("https://admin.integration.automation.applause.com/sdktestpage.html");
    this.testView =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize();
  }

  @AfterClass(alwaysRun = true)
  public void quitDriver() {
    context.getDriver().quit();
  }

  @Test
  public void testElementsInitialize() {
    logger.info("STEP 1: Create Locator for the List.");
    Locator locator = new Locator(LocatedBy.className("col2"), this.context.getPlatform());

    logger.info("STEP 2: Create the list.");
    val list = new LazyList<>(ContainerElement.class, locator, null, this.context);

    logger.info("STEP 3: Initialize the list.");
    list.initialize();
  }

  @Test
  public void testElementsInitializeWithParent() {
    logger.info("STEP 1: Create Locators for the parent and child.");
    Locator parentLocator = new Locator(LocatedBy.tagName("tbody"), this.context.getPlatform());
    Locator childLocator = new Locator(LocatedBy.className("col2"), this.context.getPlatform());

    logger.info("STEP 2: Create the parent LazyWebElement.");
    LazyWebElement parent = new LazyWebElement(parentLocator, this.context);

    logger.info("STEP 3: Create the child list.");
    val list = new LazyList<>(ContainerElement.class, childLocator, parent, this.context);

    logger.info("STEP 4: Initialize the list.");
    list.initialize();
  }

  @Test
  public void testElementsInitializeWithParents() {
    logger.info("STEP 1: Create Locators for the grandparent, parent, and child.");
    Locator grandparentLocator =
        new Locator(LocatedBy.tagName("tbody"), this.context.getPlatform());
    Locator parentLocator =
        new Locator(LocatedBy.tagName("tr"), this.context.getPlatform()).withIndex(2);
    Locator childLocator = new Locator(LocatedBy.className("col2"), this.context.getPlatform());

    logger.info("STEP 2: Create the grandparent LazyWebElement.");
    LazyWebElement grandparent = new LazyWebElement(grandparentLocator, this.context);

    logger.info("STEP 3: Create the parent LazyWebElement.");
    LazyWebElement parent = new LazyWebElement(parentLocator, grandparent, this.context);

    logger.info("STEP 4: Create the child list.");
    val list = new LazyList<>(ContainerElement.class, childLocator, parent, this.context);

    logger.info("STEP 5: Initialize the list.");
    list.initialize();
  }

  @Test
  public void testElementsInitializeWithJQueryParent() {
    logger.info("STEP 1: Create Locators for the parent and child.");
    Locator parentLocator =
        new Locator(LocatedBy.jQuery("#text__tables > table > tbody"), this.context.getPlatform());
    Locator childLocator =
        new Locator(LocatedBy.jQuery("tr:nth-child(1) > td.col2"), this.context.getPlatform());

    logger.info("STEP 2: Create the parent LazyWebElement.");
    LazyWebElement parent = new LazyWebElement(parentLocator, this.context);

    logger.info("STEP 3: Create the child list.");
    val list = new LazyList<>(ContainerElement.class, childLocator, parent, this.context);

    logger.info("STEP 4: Initialize the list.");
    list.initialize();
  }

  @Test
  public void testElementsInitializeWithJQueryParents() {
    logger.info("STEP 1: Create Locators for the grandparent, parent, and child.");
    Locator grandparentLocator =
        new Locator(LocatedBy.jQuery("#text__tables > table > tbody"), this.context.getPlatform());
    Locator parentLocator =
        new Locator(LocatedBy.jQuery("tr"), this.context.getPlatform()).withIndex(2);
    Locator childLocator = new Locator(LocatedBy.jQuery("td.col2"), this.context.getPlatform());

    logger.info("STEP 2: Create the grandparent LazyWebElement.");
    val grandparent = new LazyWebElement(grandparentLocator, this.context);

    logger.info("STEP 3: Create the parent LazyWebElement.");
    val parent = new LazyWebElement(parentLocator, grandparent, this.context);

    logger.info("STEP 4: Create the child list.");
    val list = new LazyList<>(ContainerElement.class, childLocator, parent, this.context);

    logger.info("STEP 5: Initialize the list.");
    list.initialize();
  }

  @Test
  public void testElementsInitializeWithJQueryAndBadParent() {
    logger.info("STEP 1: Create Locators for the parent and child.");
    val parentLocator = new Locator(LocatedBy.tagName("tbody"), this.context.getPlatform());
    val childLocator =
        new Locator(LocatedBy.jQuery("tr:nth-child(1) > td.col2"), this.context.getPlatform());

    logger.info("STEP 2: Create the parent LazyWebElement.");
    val parent = new LazyWebElement(parentLocator, this.context);

    logger.info("STEP 3: Create the child list.");
    val list = new LazyList<>(ContainerElement.class, childLocator, parent, this.context);

    logger.info("STEP 4: Initialize the list. Catch InvalidArgumentException.");
    try {
      list.initialize();
      fail("Shouldn't have been able to initialize this list.");
    } catch (InvalidArgumentException expected) {
      logger.info("Caught the exception!");
    }
  }

  @Test
  public void testElementsInitializeWithJavaScript() {
    logger.info("STEP 1: Create Locator for the List.");
    Locator locator =
        new Locator(
            LocatedBy.javaScript("return document.querySelectorAll('tr')"),
            this.context.getPlatform());

    logger.info("STEP 2: Create the list.");
    val list = new LazyList<>(ContainerElement.class, locator, null, this.context);

    logger.info("STEP 3: Initialize the list.");
    list.initialize();
  }

  @Test
  public void testElementsInitializeWithFormat() {
    logger.info("STEP 1: Initialize the wildcard element with parameter.");
    ((LazyList<?>) testView.wildcardList).format("checkbox").initialize();
  }

  @Test
  public void testElementsIsInitialized() {
    logger.info("STEP 1: Create Locator for the List.");
    Locator locator = new Locator(LocatedBy.className("col2"), this.context.getPlatform());

    logger.info("STEP 2: Create the list.");
    val list = new LazyList<>(ContainerElement.class, locator, null, this.context);

    logger.info("STEP 3: Assert that the list is uninitialized.");
    assertFalse(list.isInitialized());

    logger.info("STEP 4: Initialize the list.");
    list.initialize();

    logger.info("STEP 5: Assert that the list is initialized.");
    assertTrue(list.isInitialized());
  }

  @Test
  public void testElementsStale() {
    logger.info("STEP 1: Create Locator for the List.");
    Locator locator = new Locator(LocatedBy.className("col2"), this.context.getPlatform());

    logger.info("STEP 2: Create the list.");
    val list = new LazyList<>(ContainerElement.class, locator, null, this.context);

    logger.info("STEP 3: Initialize the list.");
    list.initialize();

    logger.info("STEP 4: Reload the page, causing the list to go stale.");
    context.getDriver().get(context.getDriver().getCurrentUrl());

    logger.info("STEP 5: Initialize the list again.");
    list.initialize();
  }

  @Test
  public void testElementsStaleWithFormat() {
    logger.info("STEP 1: Initialize the wildcard element with parameter.");
    ((LazyList<?>) testView.wildcardList).format("checkbox");

    logger.info("STEP 2: Reload the page, causing the list to go stale.");
    context.getDriver().get(context.getDriver().getCurrentUrl());

    logger.info("STEP 3: Reinitialize the list without re-specifying the parameter.");
    ((LazyList<?>) testView.wildcardList).initialize();
  }

  @Test
  public void testElementsStaleWithParent() {
    logger.info("STEP 1: Create Locators for the parent and child.");
    Locator parentLocator = new Locator(LocatedBy.tagName("tbody"), this.context.getPlatform());
    Locator childLocator = new Locator(LocatedBy.className("col2"), this.context.getPlatform());

    logger.info("STEP 2: Create the parent LazyWebElement.");
    LazyWebElement parent = new LazyWebElement(parentLocator, this.context);

    logger.info("STEP 3: Create the child list.");
    val list = new LazyList<>(ContainerElement.class, childLocator, parent, this.context);

    logger.info("STEP 4: Initialize the list.");
    list.initialize();

    logger.info("STEP 5: Reload the page, causing the list to go stale.");
    context.getDriver().get(context.getDriver().getCurrentUrl());

    logger.info("STEP 6: Initialize the list again.");
    list.initialize();
  }

  @Test
  public void testComponentsInitialize() {
    logger.info("STEP 1: Create Locator for the List.");
    Locator locator =
        new Locator(LocatedBy.css("#text__tables > table > tbody"), this.context.getPlatform());

    logger.info("STEP 2: Create the list.");
    val list = new LazyList<>(TestComponentFakeElementSub.class, locator, null, this.context);

    logger.info("STEP 3: Initialize the list.");
    list.initialize();
  }

  @Test
  public void testComponentsInitializeWithParent() {
    logger.info("STEP 1: Create Locators for the List.");
    Locator parentLocator =
        new Locator(LocatedBy.css("#text__tables > table > tbody"), this.context.getPlatform());
    Locator childLocator = new Locator(LocatedBy.css("tr"), this.context.getPlatform());

    logger.info("STEP 2: Create the parent.");
    LazyWebElement parent = new LazyWebElement(parentLocator, this.context);

    logger.info("STEP 3: Create the list.");
    val list =
        new LazyList<>(TestComponentFakeElementSub.class, childLocator, parent, this.context);

    logger.info("STEP 4: Initialize the list.");
    list.initialize();
  }

  @Test
  public void testComponentsInitializeWithParents() {
    logger.info("STEP 1: Create Locators for the List.");
    Locator grandparentLocator =
        new Locator(LocatedBy.css("#text__tables > table > tbody"), this.context.getPlatform());
    Locator parentLocator = new Locator(LocatedBy.css("tr"), this.context.getPlatform());
    Locator childLocator = new Locator(LocatedBy.css("td"), this.context.getPlatform());

    logger.info("STEP 2: Create the grandparent.");
    LazyWebElement grandparent = new LazyWebElement(grandparentLocator, this.context);

    logger.info("STEP 3: Create the parent.");
    LazyWebElement parent = new LazyWebElement(parentLocator, grandparent, this.context);

    logger.info("STEP 4: Create the list.");
    val list =
        new LazyList<>(TestComponentFakeElementSub.class, childLocator, parent, this.context);

    logger.info("STEP 5: Initialize the list.");
    list.initialize();
  }

  @Test
  public void testComponentsInitializeWithJQueryParent() {
    logger.info("STEP 1: Create Locators for the List.");
    Locator parentLocator =
        new Locator(LocatedBy.jQuery("#text__tables > table > tbody"), this.context.getPlatform());
    Locator childLocator = new Locator(LocatedBy.jQuery("tr"), this.context.getPlatform());

    logger.info("STEP 2: Create the parent.");
    LazyWebElement parent = new LazyWebElement(parentLocator, this.context);

    logger.info("STEP 3: Create the list.");
    val list =
        new LazyList<>(TestComponentFakeElementSub.class, childLocator, parent, this.context);

    logger.info("STEP 4: Initialize the list.");
    list.initialize();
  }

  @Test
  public void testComponentsInitializeWithJQueryParents() {
    logger.info("STEP 1: Create Locators for the List.");
    Locator grandparentLocator =
        new Locator(LocatedBy.jQuery("#text__tables > table > tbody"), this.context.getPlatform());
    Locator parentLocator = new Locator(LocatedBy.jQuery("tr"), this.context.getPlatform());
    Locator childLocator = new Locator(LocatedBy.jQuery("td"), this.context.getPlatform());

    logger.info("STEP 2: Create the grandparent.");
    LazyWebElement grandparent = new LazyWebElement(grandparentLocator, this.context);

    logger.info("STEP 3: Create the parent.");
    LazyWebElement parent = new LazyWebElement(parentLocator, grandparent, this.context);

    logger.info("STEP 4: Create the list.");
    val list =
        new LazyList<>(TestComponentFakeElementSub.class, childLocator, parent, this.context);

    logger.info("STEP 5: Initialize the list.");
    list.initialize();
  }

  @Test
  public void testComponentsInitializeWithJQueryAndBadParent() {
    logger.info("STEP 1: Create Locators for the List.");
    Locator parentLocator = new Locator(LocatedBy.tagName("tbody"), this.context.getPlatform());
    Locator childLocator = new Locator(LocatedBy.jQuery("tr"), this.context.getPlatform());

    logger.info("STEP 2: Create the parent.");
    LazyWebElement parent = new LazyWebElement(parentLocator, this.context);

    logger.info("STEP 3: Create the list.");
    val list =
        new LazyList<>(TestComponentFakeElementSub.class, childLocator, parent, this.context);

    logger.info("STEP 4: Initialize the list. Catch InvalidArgumentException.");
    try {
      list.initialize();
      fail("Shouldn't have been able to initialize this list.");
    } catch (InvalidArgumentException expected) {
      logger.info("Caught the exception!");
    }
  }

  @Test
  public void testComponentsInitializeWithJavaScript() {
    logger.info("STEP 1: Create Locator for the List.");
    Locator locator =
        new Locator(
            LocatedBy.javaScript("return document.querySelectorAll('tr')"),
            this.context.getPlatform());

    logger.info("STEP 2: Create the list.");
    val list = new LazyList<>(TestComponentFakeElementSub.class, locator, null, this.context);

    logger.info("STEP 3: Initialize the list.");
    list.initialize();
  }

  @Test
  public void testComponentsInitializeWithFormat() {
    logger.info("STEP 1: Initialize the wildcard list with parameter.");
    ((LazyList<TestComponentFakeElementSub>) testView.wildcardComponents).format("tr").initialize();
  }

  @Test
  public void testComponentsIsInitialized() {
    logger.info("STEP 1: Create Locator for the List.");
    Locator locator =
        new Locator(LocatedBy.css("#text__tables > table > tbody"), this.context.getPlatform());

    logger.info("STEP 2: Create the list.");
    val list = new LazyList<>(TestComponentFakeElementSub.class, locator, null, this.context);

    logger.info("STEP 3: Assert that the list is uninitialized.");
    assertFalse(list.isInitialized());

    logger.info("STEP 4: Initialize the list.");
    list.initialize();

    logger.info("STEP 5: Assert that the list is initialized.");
    assertTrue(list.isInitialized());
  }

  @Test
  public void testComponentsStale() {
    logger.info("STEP 1: Create Locator for the List.");
    Locator locator = new Locator(LocatedBy.tagName("tr"), this.context.getPlatform());

    logger.info("STEP 2: Create the list.");
    val list = new LazyList<>(TestComponentFakeElementSub.class, locator, null, this.context);

    logger.info("STEP 3: Initialize the list.");
    list.initialize();

    logger.info("STEP 4: Reload the page, causing the list to go stale.");
    context.getDriver().get(context.getDriver().getCurrentUrl());

    logger.info("STEP 5: Initialize the list again.");
    list.initialize();
  }

  @Test
  public void testComponentsStaleWithFormat() {
    logger.info("STEP 1: Initialize the wildcard list with parameter.");
    ((LazyList<TestComponentFakeElementSub>) testView.wildcardComponents).format("tr");

    logger.info("STEP 2: Reload the page, making the list stale.");
    context.getDriver().get(context.getDriver().getCurrentUrl());

    logger.info("STEP 3: Reinitialize the list without parameter.");
    ((LazyList<?>) testView.wildcardComponents).initialize();
  }

  @Test
  public void testComponentsStaleWithParent() {
    logger.info("STEP 1: Create Locators for the parent and child.");
    Locator parentLocator = new Locator(LocatedBy.tagName("tbody"), this.context.getPlatform());
    Locator childLocator = new Locator(LocatedBy.className("col2"), this.context.getPlatform());

    logger.info("STEP 2: Create the parent LazyWebElement.");
    LazyWebElement parent = new LazyWebElement(parentLocator, this.context);

    logger.info("STEP 3: Create the child list.");
    val list =
        new LazyList<>(TestComponentFakeElementSub.class, childLocator, parent, this.context);

    logger.info("STEP 4: Initialize the list.");
    list.initialize();

    logger.info("STEP 5: Reload the page, causing the list to go stale.");
    context.getDriver().get(context.getDriver().getCurrentUrl());

    logger.info("STEP 6: Initialize the list again.");
    list.initialize();
  }
}
