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
import com.applause.auto.pageobjectmodel.factory.LazyWebElement;
import com.applause.auto.pageobjectmodel.factory.Locator;
import io.appium.java_client.AppiumBy;
import java.time.Duration;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LocatedByTest {

  private final Logger logger = LogManager.getLogger();
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

  @Test
  public void testId() {
    logger.info("STEP 1: Create a new By with id \"test\".");
    By by = LocatedBy.id("test");

    logger.info("STEP 2: Assert that we got a ById.");
    assertTrue(by instanceof By.ById);

    logger.info("STEP 3: Assert that it has selector \"test\".");
    assertEquals(by.toString(), "By.id: test");
  }

  @Test
  public void testCss() {
    logger.info("STEP 1: Create a new By with CSS selector \"test\".");
    By by = LocatedBy.css("test");

    logger.info("STEP 2: Assert that we got a ByCssSelector.");
    assertTrue(by instanceof By.ByCssSelector);

    logger.info("STEP 3: Assert that it has selector \"test\".");
    assertEquals(by.toString(), "By.cssSelector: test");
  }

  @Test
  public void testXpath() {
    logger.info("STEP 1: Create a new By with XPath selector \"test\".");
    By by = LocatedBy.xpath("test");

    logger.info("STEP 2: Assert that we got a ByXPath.");
    assertTrue(by instanceof By.ByXPath);

    logger.info("STEP 3: Assert that it has selector \"test\".");
    assertEquals(by.toString(), "By.xpath: test");
  }

  @Test
  public void testClassName() {
    logger.info("STEP 1: Create a new By with class selector \"test\".");
    By by = LocatedBy.className("test");

    logger.info("STEP 2: Assert that we got a ByXPath.");
    assertTrue(by instanceof By.ByClassName);

    logger.info("STEP 3: Assert that it has selector \"test\".");
    assertEquals(by.toString(), "By.className: test");
  }

  @Test
  public void testName() {
    logger.info("STEP 1: Create a new By with name selector \"test\".");
    By by = LocatedBy.name("test");

    logger.info("STEP 2: Assert that we got a ByName.");
    assertTrue(by instanceof By.ByName);

    logger.info("STEP 3: Assert that it has selector \"test\".");
    assertEquals(by.toString(), "By.name: test");
  }

  @Test
  public void testTagName() {
    logger.info("STEP 1: Create a new By with name selector \"test\".");
    By by = LocatedBy.tagName("test");

    logger.info("STEP 2: Assert that we got a ByTagName.");
    assertTrue(by instanceof By.ByTagName);

    logger.info("STEP 3: Assert that it has selector \"test\".");
    assertEquals(by.toString(), "By.tagName: test");
  }

  @Test
  public void testLinkText() {
    logger.info("STEP 1: Create a new By with link text selector \"test\".");
    By by = LocatedBy.linkText("test");

    logger.info("STEP 2: Assert that we got a ByLinkText.");
    assertTrue(by instanceof By.ByLinkText);

    logger.info("STEP 3: Assert that it has selector \"test\".");
    assertEquals(by.toString(), "By.linkText: test");
  }

  @Test
  public void testPartialLinkText() {
    logger.info("STEP 1: Create a new By with partial link text selector \"est\".");
    By by = LocatedBy.partialLinkText("est");

    logger.info("STEP 2: Assert that we got a ByPartialLinkText.");
    assertTrue(by instanceof By.ByPartialLinkText);

    logger.info("STEP 3: Assert that it has selector \"est\".");
    assertEquals(by.toString(), "By.partialLinkText: est");
  }

  @Test
  public void testAccessibilityId() {
    logger.info("STEP 1: Create a new By with accessibility ID selector \"test\".");
    By by = LocatedBy.accessibilityId("test");

    logger.info("STEP 2: Assert that we got a ByAccessibilityId.");
    assertTrue(by instanceof AppiumBy.ByAccessibilityId);

    logger.info("STEP 3: Assert that it has selector \"test\".");
    assertEquals(by.toString(), "AppiumBy.accessibilityId: test");
  }

  @Test
  public void testAndroidUIAutomator() {
    logger.info("STEP 1: Create a new By with UIAutomator selector \"test\".");
    By by = LocatedBy.androidUIAutomator("test");

    logger.info("STEP 2: Assert that we got a ByAndroidUIAutomator.");
    assertTrue(by instanceof AppiumBy.ByAndroidUIAutomator);

    logger.info("STEP 3: Assert that it has selector \"test\".");
    assertEquals(by.toString(), "AppiumBy.androidUIAutomator: test");
  }

  @Test
  public void testIOSClassChain() {
    logger.info("STEP 1: Create a new By with class chain selector \"test\".");
    By by = LocatedBy.iOSClassChain("test");

    logger.info("STEP 2: Assert that we got a ByIosClassChain.");
    assertTrue(by instanceof AppiumBy.ByIosClassChain);

    logger.info("STEP 3: Assert that it has selector \"test\".");
    assertEquals(by.toString(), "AppiumBy.iOSClassChain: test");
  }

  @Test
  public void testIOSNsPredicate() {
    logger.info("STEP 1: Create a new By with NsPredicate selector \"test\".");
    By by = LocatedBy.iOSNsPredicate("test");

    logger.info("STEP 2: Assert that we got a ByIosNsPredicate.");
    assertTrue(by instanceof AppiumBy.ByIosNsPredicate);

    logger.info("STEP 3: Assert that it has selector \"test\".");
    assertEquals(by.toString(), "AppiumBy.iOSNsPredicate: test");
  }

  @Test
  public void testJQuery() {
    logger.info(
        "STEP 1: Create a new By with JQuery selector \"#text__tables > table > thead > tr > th.col1\".");
    By by = LocatedBy.jQuery("#text__tables > table > thead > tr > th.col1");

    logger.info("STEP 2: Assert that we got a ByJQuery.");
    assertTrue(by instanceof LocatedBy.ByJQuery);

    logger.info(
        "STEP 3: Assert that it has selector \"#text__tables > table > thead > tr > th.col1\".");
    assertEquals(by.toString(), "By.JQuery: #text__tables > table > thead > tr > th.col1");

    logger.info("STEP 4: Create a new Locator with that selector.");
    Locator locator = new Locator(by, this.context.getPlatform());

    logger.info("STEP 5: Create a new LazyWebElement with that Locator.");
    LazyWebElement lazy = new LazyWebElement(locator, this.context);

    logger.info("STEP 6: Initialize the LazyWebElement, finding the Locator in the page.");
    lazy.initialize();
  }

  @Test
  public void testJavaScript() {
    logger.info(
        "STEP 1: Create a new By with JavaScript selector \"return document.querySelector('#text> header"
            + " > h1')\".");
    By by = LocatedBy.javaScript("return document.querySelector('#text > header > h1')");

    logger.info("STEP 2: Assert that we got a ByJavaScript.");
    assertTrue(by instanceof LocatedBy.ByJavaScript);

    logger.info(
        "STEP 3: Assert that it has selector \"return document.querySelector('#text > header >h1')\".");
    assertEquals(
        by.toString(), "By.JavaScript: return document.querySelector('#text > header > h1')");

    logger.info("STEP 4: Create a new Locator with that selector.");
    Locator locator = new Locator(by, this.context.getPlatform());

    logger.info("STEP 5: Create a new LazyWebElement with that Locator.");
    LazyWebElement lazy = new LazyWebElement(locator, this.context);

    logger.info("STEP 6: Initialize the LazyWebElement, finding the Locator in the page.");
    lazy.initialize();
  }
}
