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
package com.applause.auto.pageobjectmodel.base;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import com.applause.auto.context.PageObjectContext;
import com.applause.auto.data.enums.DriverType;
import com.applause.auto.data.enums.Platform;
import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;
import com.applause.auto.pageobjectmodel.elements.ContainerElement;
import com.applause.auto.pageobjectmodel.factory.LazyList;
import com.applause.auto.pageobjectmodel.factory.LazyWebElement;
import com.applause.auto.pageobjectmodel.factory.Locator;
import com.applause.auto.pageobjectmodel.testobjects.TestComponentWithFakeElements;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BaseComponentTest {
  private final Logger logger = LogManager.getLogger();
  private TestComponentWithFakeElements component;
  private PageObjectContext context;

  private PageObjectContext getMockDriver() {
    final var mockDriver = mock(FirefoxDriver.class);
    when(mockDriver.getCurrentUrl())
        .thenReturn("https://admin.stage.automation.applause.com/sdktestpage.html");

    // Mock other elements
    final var defaultElement = mock(WebElement.class);
    final var defaultElementChild = mock(WebElement.class);
    when(mockDriver.findElement(any())).thenReturn(defaultElement);
    when(defaultElement.findElement(any())).thenReturn(defaultElementChild);
    when(defaultElement.findElements(any())).thenReturn(List.of(defaultElementChild));
    // Top Element mock
    final var topElementMock = mock(WebElement.class);
    when(topElementMock.getAttribute("id")).thenReturn("top");
    final var willHideMock = mock(WebElement.class);
    when(topElementMock.getAttribute("will-hide")).thenReturn("will-hide");
    when(mockDriver.findElement(By.id("top"))).thenReturn(topElementMock);
    when(mockDriver.findElement(By.id("will-hide"))).thenReturn(willHideMock);
    when(mockDriver.findElements(By.id("top"))).thenReturn(List.of(topElementMock));
    when(mockDriver.findElements(By.id("will-hide")))
        .thenReturn(Collections.singletonList(willHideMock));

    when(topElementMock.findElements(By.id("will-hide")))
        .thenReturn(Collections.singletonList(willHideMock));
    when(topElementMock.findElement(any())).thenReturn(defaultElement);
    when(topElementMock.findElements(any())).thenReturn(Collections.singletonList(defaultElement));

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
    var parent =
        new LazyWebElement(
            new Locator(LocatedBy.id("top"), this.context.getPlatform()), this.context);
    component =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize(parent);
  }

  @AfterClass(alwaysRun = true)
  public void quitDriver() {
    context.getDriver().quit();
  }

  @Test
  public void testGetParent() {
    logger.info("STEP 1: Assert that the parent is the expected parent element");
    assertEquals(component.getUnderlying().getAttribute("id"), "top");
  }

  @Test
  public void testSetParent() {
    logger.info("STEP 1: Set the parent to something else");
    // Top Element mock
    final var otherParentMock = mock(WebElement.class);
    when(otherParentMock.getAttribute("id")).thenReturn("hide-button");
    when(context.getDriver().findElement(By.id("hide-button"))).thenReturn(otherParentMock);
    component.setUnderlying(
        new LazyWebElement(
            new Locator(LocatedBy.id("hide-button"), this.context.getPlatform()), this.context));

    logger.info("STEP 2: Assert that the parent is the expected parent element");
    assertEquals(component.getUnderlying().getAttribute("id"), "hide-button");
  }

  @Test
  public void testInitialize() {
    logger.info("STEP 1: Initialize the parent, finding it on the page.");
    component.initialize();
  }

  @Test
  public void testIsInitialized() {
    logger.info("STEP 1: Assert that the component isn't initialized.");
    assertFalse(component.componentNoDefault.isInitialized());

    logger.info("STEP 2: Initialize the parent, finding it on the page.");
    component.componentNoDefault.initialize();

    logger.info("STEP 3: Assert that the component is initialized.");
    assertTrue(component.componentNoDefault.isInitialized());
  }

  @Test
  public void testInitializeWithFormat() {
    logger.info("STEP 1: Set the parent to something with a wildcard.");
    var component2 =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize()
            .wildcard
            .getChild(LocatedBy.id("who-cares"), TestComponentWithFakeElements.class);

    logger.info("STEP 2: Initialize with format, dynamically finding the parent on the page.");
    component2.format("will-hide").initialize();
  }

  @Test
  public void testGetChild() {
    logger.info("STEP 1: Create a child.");
    BaseElement element = component.getChild(LocatedBy.id("will-hide"));

    logger.info("STEP 2: Initialize the child.");
    element.initialize();
  }

  @Test
  public void testGetChildren() {
    logger.info("STEP 1: Create some children.");
    List<ContainerElement> elements = component.getChildren(LocatedBy.id("will-hide"));

    logger.info("STEP 2: Initialize the children.");
    ((LazyList<?>) elements).initialize();
  }

  @Test
  public void testAfterInit() {
    logger.info("STEP 1: Create an instance of the component class.");
    TestComponentWithFakeElements co =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize();

    logger.info("STEP 2: Verify that some other element is not initialized.");
    assertFalse(co.containerElement.isInitialized());

    logger.info("STEP 3: Verify that the afterInitElement is initialized by afterInit().");
    assertTrue(co.afterInitElement.isInitialized());
  }
}
