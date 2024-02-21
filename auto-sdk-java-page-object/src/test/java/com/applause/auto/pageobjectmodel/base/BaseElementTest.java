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

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import com.applause.auto.context.PageObjectContext;
import com.applause.auto.data.enums.DriverType;
import com.applause.auto.data.enums.Platform;
import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;
import com.applause.auto.pageobjectmodel.elements.ContainerElement;
import com.applause.auto.pageobjectmodel.testobjects.TestComponentWithFakeElements;
import java.time.Duration;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BaseElementTest {
  private final Logger logger = LogManager.getLogger(BaseElementTest.class);
  private BaseElement baseElement;
  private BaseElement wildcardBaseElement;
  private PageObjectContext context;

  private PageObjectContext getMockDriver() {
    final var mockDriver = mock(FirefoxDriver.class);
    when(mockDriver.getCurrentUrl())
        .thenReturn("https://admin.stage.automation.applause.com/sdktestpage.html");

    // Mock other elements
    final var defaultMock = mock(WebElement.class);
    when(defaultMock.isEnabled()).thenReturn(true);
    when(defaultMock.isDisplayed()).thenReturn(true);

    final var topMock = mock(WebElement.class);
    when(topMock.isEnabled()).thenReturn(true);
    when(topMock.isDisplayed()).thenReturn(true);
    when(topMock.getText()).thenReturn("Top Element");
    when(topMock.getAttribute("id")).thenReturn("top");
    when(topMock.getRect()).thenReturn(new Rectangle(5, 5, 10, 10));

    final var childMock = mock(WebElement.class);
    when(childMock.getText()).thenReturn("Child Header");

    final var grandChildMock = mock(WebElement.class);
    when(grandChildMock.getText()).thenReturn("Grand Child Header");

    when(mockDriver.findElement(any())).thenReturn(defaultMock);
    when(mockDriver.findElements(any())).thenReturn(List.of(defaultMock));

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

    return spy(
        new PageObjectContext(
            mockDriver,
            DriverType.FIREFOX,
            Duration.ofSeconds(1),
            Duration.ofMillis(100),
            Platform.WEB_DESKTOP_FIREFOX));
  }

  @BeforeMethod(alwaysRun = true)
  public void beforeMethod(final ITestResult tr) {
    logger.info("Starting: " + tr.getMethod().getMethodName());
    context = getMockDriver();
    context.getDriver().get("https://admin.integration.automation.applause.com/sdktestpage.html");
    TestComponentWithFakeElements testView =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize();
    baseElement = testView.containerElement;
    wildcardBaseElement = testView.wildcard;
  }

  @AfterMethod(alwaysRun = true)
  public void quitDriver() {
    context.getDriver().quit();
  }

  @Test
  public void testInitialize() {
    logger.info("STEP 1: Verify that BaseElement hasn't yet been initialized.");
    assertFalse(baseElement.isInitialized());

    logger.info("STEP 2: Initialize the BaseElement by running an arbitrary method.");
    assertTrue(baseElement.isEnabled());
  }

  @Test
  public void testInitializeWithFormat() {
    logger.info("STEP 1: Remove the wait time from the wildcard BaseElement.");
    wildcardBaseElement.noWait();

    logger.info("STEP 2: Verify that the BaseElement hasn't yet been initialized.");
    assertFalse(wildcardBaseElement.isInitialized());

    logger.info("STEP 3: Attempt to initialize the element without format. This should fail.");
    try {
      wildcardBaseElement.initialize();
      fail("Shouldn't have been able to find the element.");
    } catch (NoSuchElementException expected) {
      logger.info("Caught the NoSuchElementException.");
    }

    logger.info("STEP 4: Verify that the BaseElement still hasn't yet been initialized.");
    assertFalse(wildcardBaseElement.isInitialized());

    logger.info("STEP 5: Initialize the BaseElement with formatting.");
    baseElement.format("will-hide").initialize();

    logger.info("STEP 6: Verify that the BaseElement is initialized.");
    assertTrue(baseElement.isInitialized());
  }

  @Test
  public void testIsInitialized() {
    logger.info("STEP 1: Verify that the BaseElement is uninitialized.");
    assertFalse(baseElement.isInitialized());

    logger.info("STEP 2: Initialize the BaseElement.");
    baseElement.initialize();

    logger.info("STEP 3: Verify that the BaseElement is initialized.");
    assertTrue(baseElement.isInitialized());
  }

  @Test
  public void testGetWebElement() {
    logger.info("STEP 1: Getting WebElement from BaseElement.");
    WebElement webElement = baseElement.getUnderlyingWebElement();

    logger.info("STEP 2: Verifying that the returned WebElement is non-null.");
    assertNotNull(webElement);

    logger.info("STEP 3: Verifying that the returned WebElement is a WebElement.");
    assertTrue(WebElement.class.isAssignableFrom(webElement.getClass()));
  }

  @Test
  public void testClick() {
    logger.info("STEP 1: Click on the BaseElement.");
    baseElement.click();
  }

  @Test
  public void testSetWait() {
    logger.info(
        "STEP 1: Set the default SyncHelper timeout to 2 seconds and the element timeout to 1 second.");
    this.context.setTimeout(Duration.ofSeconds(2));
    wildcardBaseElement.setWait(1, 1);

    logger.info(
        "STEP 2: Attempt to initialize an element with a bad locator. This should time out in 1 second.");
    long end = Long.MAX_VALUE;
    long start = System.nanoTime();
    try {
      wildcardBaseElement.initialize();
      fail("Element should have failed to initialize due to bad locator.");
    } catch (NoSuchElementException expected) {
      end = System.nanoTime();
      logger.info("Caught the NoSuchElementException!");
    }

    logger.info(
        "STEP 3: Assert that the NoSuchElementException was caught in less than 2 seconds.");
    logger.info(String.format("NoSuchElementException caught in [%d] nanoseconds.", end - start));
    assertTrue((end - start) < 2000000000);
  }

  @Test
  public void testNoWait() {
    logger.info(
        "STEP 1: Set the default SyncHelper timeout to 1 second and remove the element timeout.");
    this.context.setTimeout(Duration.ofSeconds(1));
    wildcardBaseElement.noWait();

    logger.info(
        "STEP 2: Attempt to initialize an element with a bad locator. This should throw "
            + "NoSuchElementException immediately.");
    long end = Long.MAX_VALUE;
    long start = System.nanoTime();
    try {
      wildcardBaseElement.initialize();
      fail("Element should have failed to initialize due to bad locator.");
    } catch (NoSuchElementException expected) {
      end = System.nanoTime();
      logger.info("Caught the NoSuchElementException!");
    }

    logger.info(
        "STEP 3: Assert that the NoSuchElementException was caught in less than 0.75 second.");
    assertTrue((end - start) < 750000000);
    logger.info(String.format("NoSuchElementException caught in [%d] nanoseconds.", end - start));
  }

  @Test
  public void testRestoreDefaultWait() {
    logger.info(
        "STEP 1: Set the default SyncHelper timeout to 2 seconds and the element timeout to 1 second.");
    when(this.context.getTimeout()).thenReturn(Duration.ofSeconds(2));
    wildcardBaseElement.setWait(1, 1);

    logger.info(
        "STEP 2: Attempt to initialize an element with a bad locator. This should time out in 1 second.");
    long end = Long.MAX_VALUE;
    long start = System.nanoTime();
    try {
      wildcardBaseElement.initialize();
      fail("Element should have failed to initialize due to bad locator.");
    } catch (NoSuchElementException expected) {
      end = System.nanoTime();
      logger.info("Caught the NoSuchElementException!");
    }

    logger.info(
        "STEP 3: Assert that the NoSuchElementException was caught in less than 2 seconds.");
    assertTrue((end - start) < 2000000000);
    logger.info(String.format("NoSuchElementException caught in [%d] nanoseconds.", end - start));

    logger.info("STEP 4: Restore the default wait.");
    wildcardBaseElement.restoreDefaultWait();

    logger.info(
        "STEP 5: Attempt to initialize an element with a bad locator. This should time out in 2seconds.");
    end = 0;
    start = System.nanoTime();
    try {
      wildcardBaseElement.initialize();
      fail("Element should have failed to initialize due to bad locator.");
    } catch (NoSuchElementException expected) {
      end = System.nanoTime();
      logger.info("Caught the NoSuchElementException!");
    }

    logger.info(
        "STEP 6: Assert that the NoSuchElementException was caught in greater than 2 seconds.");
    logger.info(String.format("NoSuchElementException caught in [%d] nanoseconds.", end - start));
    assertTrue((end - start) > 2_000_000_000);
  }

  @Test
  public void testGetAttributeValue() {
    logger.info("STEP 1: Assert that the attribute \"id\" = \"top\".");
    assertEquals(baseElement.getAttribute("id"), "top");
  }

  @Test
  public void testGetDimension() {
    logger.info("STEP 1: Retrieve the Dimension of the BaseElement.");
    Dimension dimension = baseElement.getDimension();

    logger.info("STEP 2: Assert that the Dimension isn't null.");
    assertNotNull(dimension);

    logger.info("STEP 3: Assert that the Dimension's width is greater than zero.");
    assertTrue(dimension.getWidth() > 0);
  }

  @Test
  public void testGetLocation() {
    logger.info("STEP 1: Retrieve the location of the BaseElement.");
    Point point = baseElement.getLocation();

    logger.info("STEP 2: Assert that the Point isn't null.");
    assertNotNull(point);

    logger.info("STEP 3: Assert that the Point's X coordinate is greater than zero.");
    assertTrue(point.getX() > 0);
  }

  @Test
  public void testExists() {
    logger.info("STEP 1: Assert that the BaseElement exists on the current page.");
    assertTrue(baseElement.exists());
    // We should get the same result even with a "no wait"
    baseElement.noWait();
    assertTrue(baseElement.exists());
    // restore the wait
    baseElement.restoreDefaultWait();
    logger.info("STEP 2: Go to a different page.");
  }

  @Test
  public void testIsDisplayed() {
    logger.info("STEP 1: Assert that the BaseElement is displayed.");
    assertTrue(baseElement.isDisplayed());
  }

  @Test
  public void testIsEnabled() {
    logger.info("STEP 1: Assert that the BaseElement is enabled.");
    assertTrue(baseElement.isEnabled());
  }

  @Test
  public void testIsClickable() {
    logger.info("STEP 1: Assert that the BaseElement is clickable.");
    assertTrue(baseElement.isClickable());
  }

  @Test
  public void testGetChild() {
    logger.info("STEP 1: Get the child of the base element.");
    ContainerElement child = baseElement.getChild(LocatedBy.id("top-child"));

    logger.info("STEP 2: Assert that the child's text = \"Child Header\".");
    assertEquals(child.getText(), "Child Header");
  }

  @Test
  public void testGetChildren() {
    logger.info("STEP 1: Get the children of the BaseElement.");
    List<ContainerElement> children = baseElement.getChildren(LocatedBy.id("top-child"));

    logger.info("STEP 2: Assert that the first child's text = \"Child Header\".");
    assertEquals(children.get(0).getText(), "Child Header");
  }

  @Test
  public void testGetChildOfChild() {
    logger.info("STEP 1: From the BaseElement, get child and then child.");
    ContainerElement child =
        baseElement.getChild(LocatedBy.id("top-child")).getChild(LocatedBy.id("top-grand-child"));

    logger.info("STEP 2: Assert that the child's text = \"Grand Child Header\".");
    assertEquals(child.getText(), "Grand Child Header");
  }

  @Test
  public void testGetChildrenOfChild() {
    logger.info("STEP 1: From the BaseElement, get child and then child.");
    List<ContainerElement> children =
        baseElement
            .getChild(LocatedBy.id("top-child"))
            .getChildren(LocatedBy.id("top-grand-child"));

    logger.info("STEP 2: Assert that the first child's text = \"Grand Child Header\".");
    assertEquals(children.get(0).getText(), "Grand Child Header");
  }

  @Test
  public void testGetChildOfChildren() {
    logger.info("STEP 1: From the BaseElement, get children and then child.");
    ContainerElement child =
        baseElement
            .getChildren(LocatedBy.id("top-child"))
            .get(0)
            .getChild(LocatedBy.id("top-grand-child"));

    logger.info("STEP 2: Assert that the child's text = \"Grand Child Header\".");
    assertEquals(child.getText(), "Grand Child Header");
  }

  @Test
  public void testGetChildrenOfChildren() {
    logger.info("STEP 1: From the BaseElement, get children <tr> and then children <td>.");
    List<ContainerElement> children =
        baseElement
            .getChildren(LocatedBy.id("top-child"))
            .get(0)
            .getChildren(LocatedBy.id("top-grand-child"));

    logger.info("STEP 2: Assert that the first child's text = \"Grand Child Header\".");
    assertEquals(children.get(0).getText(), "Grand Child Header");
  }
}
