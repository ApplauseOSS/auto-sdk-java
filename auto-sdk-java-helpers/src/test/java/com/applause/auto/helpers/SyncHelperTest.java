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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applause.auto.context.PageObjectContext;
import com.applause.auto.data.enums.DriverType;
import com.applause.auto.data.enums.Platform;
import com.applause.auto.helpers.sync.Until;
import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;
import com.applause.auto.testobjects.TestComponentWithFakeElements;
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SyncHelperTest {
  private final Logger logger = LogManager.getLogger();
  private TestComponentWithFakeElements testView;
  private PageObjectContext context;
  private SyncHelper syncHelper;

  private PageObjectContext getMockDriver() {
    final var mockDriver = mock(FirefoxDriver.class);
    when(mockDriver.getCurrentUrl())
        .thenReturn("https://admin.stage.automation.applause.com/sdktestpage.html");
    return new PageObjectContext(
        mockDriver,
        DriverType.FIREFOX,
        Duration.ofSeconds(1),
        Duration.ofMillis(100),
        Platform.WEB_DESKTOP_FIREFOX);
  }

  private WebElement mockElement() {
    final var fakeElement = mock(WebElement.class);
    when(this.context.getDriver().findElement(any())).thenReturn(fakeElement);
    return fakeElement;
  }

  @BeforeMethod(alwaysRun = true)
  public void setupDriver() {
    context = getMockDriver();
    this.syncHelper = new SyncHelper(context);
  }

  @Test
  public void testWaitUntilElementPresent() {
    final var el = mockElement();
    when(el.isEnabled()).thenReturn(false).thenReturn(false).thenReturn(true);
    this.testView =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize();
    logger.info("STEP 1: Wait until an element is present.");
    this.syncHelper.wait(Until.uiElement(this.testView.button).present());
    Assert.assertTrue(
        this.syncHelper.matchesCondition(Until.uiElement(this.testView.button).present()));
  }

  @Test
  public void testWaitUntilElementsPresent() {
    final var el = mockElement();
    when(el.isDisplayed()).thenReturn(false).thenReturn(false).thenReturn(true);
    this.testView =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize();
    logger.info("STEP 1: Wait until an element is present.");
    this.syncHelper.wait(Until.uiElement(this.testView.radioButton).present());
    Assert.assertTrue(
        this.syncHelper.matchesCondition(Until.uiElement(this.testView.radioButton).present()));
  }

  @Test
  public void testWaitUntilElementVisible() {
    final var el = mockElement();
    when(el.isDisplayed()).thenReturn(false).thenReturn(false).thenReturn(true);
    this.testView =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize();
    logger.info("STEP 1: Wait until an element is visible.");
    this.syncHelper.wait(Until.uiElement(this.testView.button).visible());
    Assert.assertTrue(
        this.syncHelper.matchesCondition(Until.uiElement(this.testView.button).visible()));
  }

  @Test
  public void testWaitUntilOneOfTwoElementsAppears() {
    final var el = mockElement();
    when(el.isDisplayed()).thenReturn(false).thenReturn(false).thenReturn(true);
    this.testView =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize();
    logger.info("STEP 1: Wait until one of two elements appears.");
    this.syncHelper.wait(
        Until.oneOf(this.testView.waitForNotPresent, this.testView.button).present());
    Assert.assertTrue(
        this.syncHelper.matchesCondition(
            Until.oneOf(this.testView.waitForNotPresent, this.testView.button).present()));
  }

  @Test
  public void testWaitUntilElementAttributeAppears() {
    final var el = mockElement();
    when(el.getAttribute("id")).thenReturn(null).thenReturn(null).thenReturn("val");
    this.testView =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize();
    logger.info("STEP 1: Wait until element attribute appears.");

    this.syncHelper.wait(Until.uiElement(this.testView.radio1).attributeExists("id"));
    Assert.assertTrue(
        this.syncHelper.matchesCondition(
            Until.uiElement(this.testView.radio1).attributeExists("id")));
  }

  @Test
  public void testWaitUntilElementAttributeValueEquals() {
    final var el = mockElement();
    when(el.getAttribute("id"))
        .thenReturn(null)
        .thenReturn(null)
        .thenReturn("val")
        .thenReturn("radio1");
    this.testView =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize();
    logger.info("STEP 1: Wait until element attribute equals value.");
    this.syncHelper.wait(Until.uiElement(this.testView.radio1).attributeEquals("id", "radio1"));
    Assert.assertTrue(
        this.syncHelper.matchesCondition(
            Until.uiElement(this.testView.radio1).attributeEquals("id", "radio1")));
  }

  @Test
  public void testWaitUntilElementAttributeValueContains() {
    final var el = mockElement();
    when(el.getAttribute("id")).thenReturn("").thenReturn("").thenReturn("radio12");
    this.testView =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize();
    logger.info("STEP 1: Wait until element attribute contains value.");
    this.syncHelper.wait(Until.uiElement(this.testView.radio1).attributeContains("id", "radio1"));
    Assert.assertTrue(
        this.syncHelper.matchesCondition(
            Until.uiElement(this.testView.radio1).attributeContains("id", "radio1")));
  }

  @Test
  public void testWaitUntilElementTextEquals() {
    final var el = mockElement();
    when(el.getText()).thenReturn(null).thenReturn(null).thenReturn("I am text with an ID.");
    this.testView =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize();
    logger.info("STEP 1: Wait until element text equals value.");
    this.syncHelper.wait(
        Until.uiElement(this.testView.textWithId).textEquals("I am text with an ID."));
    Assert.assertTrue(
        this.syncHelper.matchesCondition(
            Until.uiElement(this.testView.textWithId).textEquals("I am text with an ID.")));
  }

  @Test
  public void testWaitUntilElementTextContains() {
    final var el = mockElement();
    when(el.getText()).thenReturn(null).thenReturn(null).thenReturn("I am text with an ID. 2");
    this.testView =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize();
    logger.info("STEP 1: Wait until element text contains value.");
    this.syncHelper.wait(
        Until.uiElement(this.testView.textWithId).textContains("am text with an ID."));
    Assert.assertTrue(
        this.syncHelper.matchesCondition(
            Until.uiElement(this.testView.textWithId).textContains("am text with an ID.")));
  }

  @Test
  public void testWaitUntilElementClickable() {
    final var el = mockElement();
    when(el.isDisplayed()).thenReturn(false).thenReturn(false).thenReturn(true);
    when(el.isEnabled()).thenReturn(false).thenReturn(true).thenReturn(true);
    this.testView =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentWithFakeElements.class)
            .initialize();
    logger.info("STEP 1: Wait until an element is clickable.");
    this.syncHelper.wait(Until.uiElement(this.testView.button).clickable());
    Assert.assertTrue(
        this.syncHelper.matchesCondition(Until.uiElement(this.testView.button).clickable()));
  }
}
