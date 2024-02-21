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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import com.applause.auto.context.PageObjectContext;
import com.applause.auto.data.enums.DriverType;
import com.applause.auto.data.enums.Platform;
import com.applause.auto.pageobjectmodel.annotation.Locate;
import com.applause.auto.pageobjectmodel.base.BaseElement;
import com.applause.auto.pageobjectmodel.base.LocatedBy;
import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;
import com.applause.auto.pageobjectmodel.testobjects.TestComponentWithFakeElements;
import java.time.Duration;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LazyWebElementTest {
  private static final Logger logger = LogManager.getLogger(LazyWebElementTest.class);
  private BaseElement randomElement;
  private TestComponentWithFakeElements testView;
  private PageObjectContext context;

  private PageObjectContext getMockDriver() {
    final var mockDriver = mock(FirefoxDriver.class);
    when(mockDriver.getCurrentUrl())
        .thenReturn("https://admin.stage.automation.applause.com/sdktestpage.html");

    final var defaultMock = mock(WebElement.class);
    when(defaultMock.getAttribute("id")).thenReturn("fake-id");
    when(defaultMock.isEnabled()).thenReturn(true);
    when(defaultMock.isDisplayed()).thenReturn(true);

    final var topMock = mock(WebElement.class);
    when(topMock.isEnabled()).thenReturn(true);
    when(topMock.isDisplayed()).thenReturn(true);
    when(topMock.getAttribute("id")).thenReturn("top");
    when(topMock.getRect()).thenReturn(new Rectangle(5, 5, 10, 10));

    final var childMock = mock(WebElement.class);
    when(childMock.getText()).thenReturn("Child Header");

    final var grandChildMock = mock(WebElement.class);
    when(grandChildMock.getText()).thenReturn("Grand Child Header");
    when(mockDriver.findElement(any())).thenReturn(defaultMock);
    when(mockDriver.findElements(any())).thenReturn(List.of(defaultMock));

    final var shadowRootElement = mock(WebElement.class);
    when(mockDriver.findElement(By.id("shadow-host-open"))).thenReturn(shadowRootElement);
    when(shadowRootElement.getText()).thenReturn("Shadow Cat");
    when(shadowRootElement.getShadowRoot()).thenReturn(mock(SearchContext.class));

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

    final var jQueryMock = mock(WebElement.class);
    when(jQueryMock.getAttribute("id")).thenReturn("fake-id");
    when(jQueryMock.isEnabled()).thenReturn(true);
    when(jQueryMock.isDisplayed()).thenReturn(true);
    when(mockDriver.findElement(LocatedBy.jQuery(anyString()))).thenReturn(jQueryMock);
    when(mockDriver.findElements(LocatedBy.jQuery(anyString()))).thenReturn(List.of(jQueryMock));

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
    randomElement = this.testView.textBox;
  }

  @AfterClass(alwaysRun = true)
  public void quitDriver() {
    context.getDriver().quit();
  }

  @Test
  public void testInitialize() {
    logger.info("STEP 1: Create a Locator for the page element.");
    Locator locator =
        new Locator(LocatedBy.css("article:nth-child(5)"), this.context.getPlatform());

    logger.info("STEP 2: Create a new LazyWebElement with the Locator.");
    LazyWebElement lazy = new LazyWebElement(locator, this.context);

    logger.info("STEP 3: Call a method on the LazyWebElement, initializing it.");
    assertEquals(lazy.getAttribute("id"), "fake-id");

    logger.info("STEP 4: ShadowRoot was NOT automatically populated.");
    assertNull(lazy.getRawElementShadowRoot());
  }

  @Test
  public void testInitializeShadowRootHost() {
    try {
      logger.info("STEP 1: Create a Locator for the page element that is shadowRoot host.");
      Locator locatorShadowRootHost =
          new Locator(LocatedBy.id("shadow-host-open"), this.context.getPlatform(), true);

      logger.info("STEP 2: Verify shadowRoot-host is configured via locator.");
      assertTrue(locatorShadowRootHost.isShadowRoot());

      logger.info("STEP 3: Create a new LazyWebElement with the Locator.");
      LazyWebElement lazy = new LazyWebElement(locatorShadowRootHost, this.context);

      logger.info("STEP 4: Verify ShadowRoot is null before initialized.");
      assertNull(lazy.getRawElementShadowRoot());

      logger.info("STEP 5: Call a method on the LazyWebElement, initializing it.");
      assertTrue(lazy.getText().contains("Shadow Cat"));

      logger.info("STEP 6: Verify ShadowRoot IS populated after initialization.");
      assertNotNull(lazy.getRawElementShadowRoot());
    } catch (NoSuchElementException
        | UnsupportedCommandException
        | NoSuchShadowRootException exception) {
      throw new SkipException("Unsupported browser version.");
    }
  }

  @Test
  public void testInitializeWithParent() {
    logger.info("STEP 1: Create a Locator for the parent element.");
    Locator parent = new Locator(LocatedBy.id("top"), this.context.getPlatform());

    logger.info("STEP 2: Create a Locator for the child element.");
    Locator child = new Locator(LocatedBy.id("top-child"), this.context.getPlatform());

    logger.info("STEP 3: Create the parent element.");
    LazyWebElement parentElement = new LazyWebElement(parent, this.context);

    logger.info("STEP 4: Create the child element.");
    LazyWebElement childElement = new LazyWebElement(child, parentElement, this.context);

    logger.info("STEP 5: Assert that the child's text starts with \"Child Header\".");
    assertTrue(childElement.getText().startsWith("Child Header"));
  }

  @Test
  public void testInitializeWithParents() {
    logger.info("STEP 1: Create a Locator for the grandparent element.");
    Locator grandparent = new Locator(LocatedBy.id("top"), this.context.getPlatform());

    logger.info("STEP 2: Create a Locator for the parent element.");
    Locator parent = new Locator(LocatedBy.id("top-child"), this.context.getPlatform());

    logger.info("STEP 3: Create a Locator for the child element.");
    Locator child = new Locator(LocatedBy.id("top-grand-child"), this.context.getPlatform());

    logger.info("STEP 4: Create the grandparent element.");
    LazyWebElement grandparentElement = new LazyWebElement(grandparent, this.context);

    logger.info("STEP 5: Create the parent element.");
    LazyWebElement parentElement = new LazyWebElement(parent, grandparentElement, this.context);

    logger.info("STEP 6: Create the child element.");
    LazyWebElement childElement = new LazyWebElement(child, parentElement, this.context);

    logger.info("STEP 7: Assert that the child's text is \"Grand Child Header\".");
    assertEquals(childElement.getText(), "Grand Child Header");
  }

  @Test
  public void testIsInitialized() {
    logger.info("STEP 1: Assert that isInitialized() is false on a new element.");
    assertFalse(randomElement.isInitialized());

    logger.info("STEP 2: Call initialize().");
    randomElement.initialize();

    logger.info("STEP 3: Assert that isInitialized() is now true.");
    assertTrue(randomElement.isInitialized());
  }

  @Test
  public void testInitializeWithFormat() {
    logger.info("STEP 1: Initialize the wildcard element with parameter.");
    testView.wildcard.format("will-hide");

    logger.info("STEP 2: Get the underlying WebElement.");
    WebElement webElement = testView.wildcard.getUnderlyingWebElement();

    logger.info("STEP 3: Assert that the underlying WebElement is not null.");
    assertNotNull(webElement);

    logger.info("STEP 4: Assert that we got a WebElement by id \"will-hide\".");
    assertEquals(webElement.getAttribute("id"), "fake-id");
  }

  @Test
  public void testInitializeWithFormatAndParent() throws NoSuchFieldException {
    logger.info("STEP 1: Create a Locator for the parent element.");
    Locator parent = new Locator(LocatedBy.id("top"), this.context.getPlatform());

    logger.info("STEP 2: Create a Locator for the child element.");
    Locate annotation =
        TestComponentWithFakeElements.class
            .getDeclaredField("idWildcard")
            .getAnnotation(Locate.class);
    Locator child = new Locator(annotation);

    logger.info("STEP 3: Create the parent element.");
    LazyWebElement parentElement = new LazyWebElement(parent, this.context);

    logger.info("STEP 4: Create the child element.");
    LazyWebElement childElement = new LazyWebElement(child, parentElement, this.context);

    logger.info("STEP 5: Initialize the child element with locator \"top-child\".");
    childElement.format("top-child");

    logger.info("STEP 6: Assert that the child's text starts with \"Child Header\".");
    assertTrue(childElement.getText().startsWith("Child Header"));
  }

  @Test
  public void testInitializeWithFormatAndParents() throws NoSuchFieldException {
    logger.info("STEP 1: Create a Locator for the grandparent element.");
    Locator grandparent = new Locator(LocatedBy.id("top"), this.context.getPlatform());

    logger.info("STEP 2: Create a Locator for the parent element.");
    Locator parent = new Locator(LocatedBy.id("top-child"), this.context.getPlatform());

    logger.info("STEP 3: Create a Locator for the child element.");
    Locate annotation =
        TestComponentWithFakeElements.class
            .getDeclaredField("idWildcard")
            .getAnnotation(Locate.class);
    Locator child = new Locator(annotation);

    logger.info("STEP 4: Create the grandparent element.");
    LazyWebElement grandparentElement = new LazyWebElement(grandparent, this.context);

    logger.info("STEP 5: Create the parent element.");
    LazyWebElement parentElement = new LazyWebElement(parent, grandparentElement, this.context);

    logger.info("STEP 6: Create the child element.");
    LazyWebElement childElement = new LazyWebElement(child, parentElement, this.context);

    logger.info("STEP 7: Initialize the child element with locator \"top-grand-child\".");
    childElement.format("top-grand-child");

    logger.info("STEP 8: Assert that the child's text is \"Grand Child Header\".");
    assertEquals(childElement.getText(), "Grand Child Header");
  }

  @Test
  public void testInitializeWithFormattedParent() throws NoSuchFieldException {
    logger.info("STEP 1: Create a Locator for the parent element.");
    Locate annotation =
        TestComponentWithFakeElements.class
            .getDeclaredField("idWildcard")
            .getAnnotation(Locate.class);
    Locator parent = new Locator(annotation);

    logger.info("STEP 2: Create a Locator for the child element.");
    Locator child = new Locator(LocatedBy.id("top-child"), this.context.getPlatform());

    logger.info("STEP 3: Create the parent element.");
    LazyWebElement parentElement = new LazyWebElement(parent, this.context);

    logger.info("STEP 4: Create the child element.");
    LazyWebElement childElement = new LazyWebElement(child, parentElement, this.context);

    logger.info("STEP 5: Initialize the parent element with locator \"top\".");
    parentElement.format("top").initialize();

    logger.info("STEP 6: Assert that the child's text starts with \"Child Header\".");
    assertTrue(childElement.getText().startsWith("Child Header"));
  }

  @Test
  public void testStale() {
    logger.info("STEP 1: Run an arbitrary element method to ensure that it isn't stale.");
    randomElement.click();

    logger.info("STEP 2: Reload the page to make the element stale.");
    context.getDriver().get(context.getDriver().getCurrentUrl());

    logger.info("STEP 3: Run the arbitrary element method again to reinitialize the element.");
    randomElement.click();
  }

  @Test
  public void testStaleWithFormat() {
    logger.info("STEP 1: Initialize the wildcard element with parameter.");
    testView.wildcard.format("will-hide");

    logger.info(
        "STEP 2: Run an arbitrary element method on the wildcard to ensure that it isn't stale.");
    testView.wildcard.click();

    logger.info("STEP 3: Reload the page to make the element stale.");
    context.getDriver().get(context.getDriver().getCurrentUrl());

    logger.info("STEP 4: Run the arbitrary element method again to reinitialize the element.");
    testView.wildcard.click();
  }

  @Test
  public void testStaleWithParent() {
    logger.info("STEP 1: Create a Locator for the parent element.");
    Locator parent = new Locator(LocatedBy.id("top"), this.context.getPlatform());

    logger.info("STEP 2: Create a Locator for the child element.");
    Locator child = new Locator(LocatedBy.id("top-child"), this.context.getPlatform());

    logger.info("STEP 3: Create the parent element.");
    LazyWebElement parentElement = new LazyWebElement(parent, this.context);

    logger.info("STEP 4: Create the child element.");
    LazyWebElement childElement = new LazyWebElement(child, parentElement, this.context);

    logger.info("STEP 5: Initialize the child element.");
    childElement.initialize();

    logger.info("STEP 6: Reload the page.");
    context.getDriver().get("https://admin.integration.automation.applause.com/sdktestpage.html");

    logger.info("STEP 7: Assert that the child's text starts with \"Child Header list\".");
    assertTrue(childElement.getText().startsWith("Child Header"));
  }

  @Test
  public void testElementWithJQuery() {
    logger.info("STEP 1: Create a Locator for the element.");
    Locator locator =
        new Locator(
            LocatedBy.jQuery("#embedded__images > div > figure:nth-child(6) > img"),
            this.context.getPlatform());

    logger.info("STEP 2: Create the element.");
    LazyWebElement element = new LazyWebElement(locator, this.context);

    logger.info("STEP 3: Get an attribute from the element, finding it on the page.");
    assertEquals(element.getAttribute("id"), "fake-id");
  }

  @Test
  public void testElementWithJQueryAndWildcard() {
    logger.info("STEP 1: Initialize the wildcard element with parameter.");
    testView.wildcardJQuery.format("#will-hide");

    logger.info(
        "STEP 2: Run an arbitrary element method on the wildcard to ensure that it isn't stale.");
    testView.wildcardJQuery.click();

    logger.info("STEP 3: Reload the page to make the element stale.");
    context.getDriver().get(context.getDriver().getCurrentUrl());

    logger.info("STEP 4: Run the arbitrary element method again to reinitialize the element.");
    testView.wildcardJQuery.click();
  }

  @Test
  public void testElementWithJQueryAndParent() {
    logger.info("STEP 1: Create a Locator for the parent.");
    Locator parentLocator =
        new Locator(
            LocatedBy.jQuery("#embedded__images > div > figure:nth-child(6)"),
            this.context.getPlatform());

    logger.info("STEP 2: Create a Locator for the child.");
    Locator childLocator = new Locator(LocatedBy.jQuery("img"), this.context.getPlatform());

    logger.info("STEP 3: Create the parent.");
    LazyWebElement parent = new LazyWebElement(parentLocator, this.context);

    logger.info("STEP 4: Create the child.");
    LazyWebElement child = new LazyWebElement(childLocator, parent, this.context);

    logger.info("STEP 5: Get an attribute from the element, finding it on the page.");
    assertEquals(child.getAttribute("id"), "fake-id");
  }

  @Test
  public void testElementWithJQueryAndBadParent() {
    logger.info("STEP 1: Create a Locator for the parent.");
    Locator parentLocator =
        new Locator(
            LocatedBy.javaScript(
                "document.querySelector('#embedded__images > div > " + "figure:nth-child(6)')"),
            this.context.getPlatform());

    logger.info("STEP 2: Create a Locator for the child.");
    Locator childLocator = new Locator(LocatedBy.jQuery("img"), this.context.getPlatform());

    logger.info("STEP 3: Create the parent.");
    LazyWebElement parent = new LazyWebElement(parentLocator, this.context);

    logger.info("STEP 4: Create the child.");
    LazyWebElement child = new LazyWebElement(childLocator, parent, this.context);

    logger.info(
        "STEP 5: Try to initialize the element with a non-JQuery parent, throwing an exception.");
    try {
      child.initialize();
      fail("Should have caught a InvalidArgumentException!");
    } catch (InvalidArgumentException expected) {
      logger.info("Caught the InvalidArgumentException!");
    }
  }

  @Test
  public void testElementWithJavaScript() {
    logger.info("STEP 1: Create a Locator for the element.");
    Locator locator =
        new Locator(
            LocatedBy.jQuery("#embedded__images > div > figure:nth-child(6) > img"),
            this.context.getPlatform());

    logger.info("STEP 2: Create the element.");
    LazyWebElement element = new LazyWebElement(locator, this.context);

    logger.info("STEP 3: Get an attribute from the element, finding it on the page.");
    assertEquals(element.getAttribute("id"), "fake-id");
  }
}
