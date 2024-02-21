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
import com.applause.auto.pageobjectmodel.annotation.Locate;
import com.applause.auto.pageobjectmodel.base.LocatedBy;
import com.applause.auto.pageobjectmodel.testobjects.TestComponentWithFakeElements;
import java.time.Duration;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LocatorTest {
  private Locator locator;
  private static final Logger logger = LogManager.getLogger(LocatorTest.class);

  private PageObjectContext context;

  private PageObjectContext getMockDriver() {
    final var mockDriver = mock(FirefoxDriver.class);
    when(mockDriver.getCurrentUrl())
        .thenReturn("https://admin.stage.automation.applause.com/sdktestpage.html");

    when(mockDriver.findElement(any())).thenReturn(mock(WebElement.class));
    when(mockDriver.findElements(any()))
        .thenReturn(Collections.singletonList(mock(WebElement.class)));

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

  @BeforeMethod(alwaysRun = true)
  public void beforeMethod() {
    context.getDriver().get("https://admin.integration.automation.applause.com/sdktestpage.html");
    this.locator = new Locator(LocatedBy.id("test"), Platform.DEFAULT);
  }

  @AfterClass(alwaysRun = true)
  public void quitDriver() {
    context.getDriver().quit();
  }

  @Test
  public void testGetPlatform() throws NoSuchFieldException {
    logger.info("STEP 1: Assert that the Locator's Platform is DEFAULT.");
    assertEquals(locator.getPlatform(), Platform.DEFAULT);

    logger.info("STEP 2: Create a new Locator from a WEB_DESKTOP_FIREFOX annotation.");
    Locate annotation =
        TestComponentWithFakeElements.class
            .getDeclaredField("noDefault")
            .getAnnotation(Locate.class);
    locator = new Locator(annotation);

    logger.info("STEP 3: Assert that the Locator's Platform is WEB_DESKTOP_FIREFOX.");
    assertEquals(locator.getPlatform(), Platform.WEB_DESKTOP_FIREFOX);
  }

  @Test
  public void testGetIndex() {
    logger.info("STEP 1: Assert that the Locator's index is null.");
    assertNull(locator.getIndex());

    logger.info("STEP 2: Get a copy of the Locator with index 4.");
    locator = locator.withIndex(4);

    logger.info("STEP 3: Assert that the Locator's index is 4.");
    assertEquals(locator.getIndex(), (Integer) 4);
  }

  @Test
  public void testGetLocators() {
    logger.info("STEP 1: Create a singleton List of By with By.id: test.");
    By singleLocator = LocatedBy.id("test");

    logger.info("STEP 2: Assert that it matches the result of getBy()");
    assertEquals(locator.getBy(), singleLocator);
  }

  @Test
  public void testGetLocatorsWithFormat() throws NoSuchFieldException {
    logger.info("STEP 1: Create a new Locator with dynamic By.id");
    Locate annotation =
        TestComponentWithFakeElements.class
            .getDeclaredField("wildcard")
            .getAnnotation(Locate.class);
    locator = new Locator(annotation);

    logger.info("STEP 2: Create a singleton List of By with By.id: %s.");
    By nullLocator = LocatedBy.id("%s");

    logger.info("STEP 3: Assert that it matches the result of getBy()");
    assertEquals(locator.getBy(), nullLocator);

    logger.info("STEP 4: Create a singleton List of By with By.id: test.");
    By filledInLocator = LocatedBy.id("test");

    logger.info("STEP 5: Assert that it matches the result of getBy(\"test\")");
    assertEquals(locator.getBy("test"), filledInLocator);
  }

  @Test
  public void testGetJQueryStringFromSingleton() {
    logger.info("STEP 1: Create the Locator with a singleton By.");
    Locator locator = new Locator(LocatedBy.jQuery("test"), this.context.getPlatform());

    logger.info("STEP 2: Verify that it creates the correct JQuery string.");
    assertEquals(locator.withIndex(3).getJQueryString(), "test:nth(3)");
  }

  @Test
  public void testGetJQueryStringFromPairs() throws Exception {
    logger.info("STEP 1: Create the Locator with a list of pairs from an annotation.");
    Locate annotation =
        TestComponentWithFakeElements.class
            .getDeclaredField("paragraph")
            .getAnnotation(Locate.class);
    Locator locator = new Locator(annotation);

    logger.info("STEP 2: Verify that it creates the correct JQuery string.");
    assertEquals(locator.getJQueryString(), "#text__paragraphs > div:nth-child(2) > p");
  }

  @Test
  public void testShadowRootLocator() throws Exception {
    logger.info("STEP 1: Create the Locators with setting and default.");
    Locate annotationShadowHostDefault =
        TestComponentWithFakeElements.class
            .getDeclaredField("paragraph")
            .getAnnotation(Locate.class);
    Locator locatorShadowRootHostDefault = new Locator(annotationShadowHostDefault);

    Locate annotationShadowHostTrue =
        TestComponentWithFakeElements.class
            .getDeclaredField("shadowDomHost")
            .getAnnotation(Locate.class);
    Locator locatorShadowRootHostTrue = new Locator(annotationShadowHostTrue);

    logger.info("STEP 2: Verify the locators have shadowRoot set correctly.");
    assertFalse(locatorShadowRootHostDefault.isShadowRoot());
    assertTrue(locatorShadowRootHostTrue.isShadowRoot());
  }
}
