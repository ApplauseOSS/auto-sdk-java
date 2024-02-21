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
package com.applause.auto.pageobjectmodel.builder;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.context.PageObjectContext;
import com.applause.auto.data.enums.DriverType;
import com.applause.auto.pageobjectmodel.base.LocatedBy;
import com.applause.auto.pageobjectmodel.factory.LazyList;
import com.applause.auto.pageobjectmodel.factory.LazyWebElement;
import com.applause.auto.pageobjectmodel.factory.Locator;
import com.applause.auto.pageobjectmodel.testobjects.FakeElement;
import com.applause.auto.pageobjectmodel.testobjects.TestComponentFakeElementSub;
import com.google.common.reflect.TypeToken;
import java.util.List;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PageObjectBuilderTest {
  private IPageObjectContext context;

  @BeforeMethod(alwaysRun = true)
  public void setup() {
    final var mockDriver = mock(FirefoxDriver.class);
    when(mockDriver.getCurrentUrl())
        .thenReturn("https://admin.stage.automation.applause.com/sdktestpage.html");

    // Mock other elements
    final var defaultMock = mock(WebElement.class);
    when(defaultMock.isEnabled()).thenReturn(true);
    when(defaultMock.isDisplayed()).thenReturn(true);

    when(mockDriver.findElement(any())).thenReturn(defaultMock);
    when(mockDriver.findElements(any())).thenReturn(List.of(defaultMock));

    this.context = new PageObjectContext(mockDriver, DriverType.FIREFOX);
  }

  @Test
  public void testPageObjectBuilderUIElementSelectionLazyWebElement() {
    var selectedBuilder =
        PageObjectBuilder.withContext(this.context).forUiElement(LazyWebElement.class);
    assertEquals(selectedBuilder.getClass(), LazyWebElementBuilder.class);
  }

  @Test
  public void testPageObjectBuilderUIElementSelectionBaseComponentExtension() {
    var selectedBuilder =
        PageObjectBuilder.withContext(this.context).forUiElement(TestComponentFakeElementSub.class);
    assertEquals(selectedBuilder.getClass(), BaseComponentBuilder.class);
  }

  @Test
  public void testPageObjectBuilderUIElementSelectionBaseComponentUsingTypeToken() {
    var selectedBuilder =
        PageObjectBuilder.withContext(this.context)
            .forUiElement(TypeToken.of(TestComponentFakeElementSub.class));
    assertEquals(selectedBuilder.getClass(), BaseComponentBuilder.class);
  }

  @Test
  public void testPageObjectBuilderUIElementSelectionBaseElementUsingTypeToken() {
    var selectedBuilder =
        PageObjectBuilder.withContext(this.context).forUiElement(TypeToken.of(FakeElement.class));
    assertEquals(selectedBuilder.getClass(), BaseElementBuilder.class);
  }

  @Test
  public void testPageObjectBuilderUIElementSelectionLazyWebElementUsingTypeToken() {
    var selectedBuilder =
        PageObjectBuilder.withContext(this.context)
            .forUiElement(TypeToken.of(LazyWebElement.class));
    assertEquals(selectedBuilder.getClass(), LazyWebElementBuilder.class);
  }

  @Test
  public void testPageObjectBuilderUIElementSelectionBaseComponentExtensionUsingTypeToken() {
    var selectedBuilder =
        PageObjectBuilder.withContext(this.context)
            .forUiElement(TypeToken.of(TestComponentFakeElementSub.class));
    assertEquals(selectedBuilder.getClass(), BaseComponentBuilder.class);
  }

  @Test
  public void testPageObjectBuilderUIElementSelectionBaseElementExtensionUsingTypeToken() {
    var selectedBuilder =
        PageObjectBuilder.withContext(this.context).forUiElement(TypeToken.of(FakeElement.class));
    assertEquals(selectedBuilder.getClass(), BaseElementBuilder.class);
  }

  @Test
  public void testPageObjectBuilderBaseComponentInitialization() {
    var component =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentFakeElementSub.class)
            .initialize();
    assertNotNull(component);
    assertNotNull(component.headers);
    assertNotNull(component.cells);
    assertEquals(component.getClass(), TestComponentFakeElementSub.class);
    assertNull(component.getUnderlying());
  }

  @Test
  public void testPageObjectBuilderBaseComponentInitializationWithUnderlyingLocatorElement() {
    var component =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentFakeElementSub.class)
            .initialize(new Locator(LocatedBy.id("id")));
    assertNotNull(component);
    assertNotNull(component.headers);
    assertNotNull(component.cells);
    assertEquals(component.getClass(), TestComponentFakeElementSub.class);
    assertNotNull(component.getUnderlying());
    assertNull(component.getParent());
    assertFalse(component.isInitialized());
    assertEquals(
        component.getUnderlying().getLocator().getBy(), new Locator(LocatedBy.id("id")).getBy());
  }

  @Test
  public void
      testPageObjectBuilderBaseComponentInitializationWithUnderlyingLocatorElementAndLocatorParent() {
    var component =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentFakeElementSub.class)
            .withParent(new Locator(LocatedBy.css("css")))
            .initialize(new Locator(LocatedBy.id("id")));
    assertNotNull(component);
    assertNotNull(component.headers);
    assertNotNull(component.cells);
    assertEquals(component.getClass(), TestComponentFakeElementSub.class);
    assertNotNull(component.getUnderlying());
    assertNotNull(component.getParent());
    assertNull(component.getParent().getParent());
    assertFalse(component.isInitialized());
    assertEquals(
        component.getUnderlying().getLocator().getBy(), new Locator(LocatedBy.id("id")).getBy());
    assertEquals(
        component.getParent().getLocator().getBy(), new Locator(LocatedBy.css("css")).getBy());
  }

  @Test
  public void testPageObjectBuilderBaseComponentInitializationWithUnderlyingWebElement() {
    var mockUnderlyingWebElement = mock(WebElement.class);
    var component =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentFakeElementSub.class)
            .initialize(mockUnderlyingWebElement, new Locator(LocatedBy.id("id")));
    assertNotNull(component);
    assertNotNull(component.headers);
    assertNotNull(component.cells);
    assertEquals(component.getClass(), TestComponentFakeElementSub.class);
    assertNotNull(component.getUnderlying());
    assertNull(component.getParent());
    // The component is considered initialized because there is an underlying, pre-located element
    assertTrue(component.isInitialized());
    assertEquals(
        component.getUnderlying().getLocator().getBy(), new Locator(LocatedBy.id("id")).getBy());
  }

  @Test
  public void testPageObjectBuilderBaseComponentListInitializationUsingLocator() {
    var componentList =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentFakeElementSub.class)
            .initializeList(new Locator(LocatedBy.id("id")));
    assertNotNull(componentList);
    assertEquals(componentList.getClass(), LazyList.class);
    assertEquals(((LazyList<?>) componentList).getType(), TestComponentFakeElementSub.class);
    assertNull(((LazyList<?>) componentList).getParent());
    assertEquals(
        ((LazyList<?>) componentList).getLocator().getBy(),
        new Locator(LocatedBy.id("id")).getBy());
  }

  @Test
  public void testPageObjectBuilderBaseComponentListInitializationUsingLocatorWithParent() {
    var componentList =
        PageObjectBuilder.withContext(context)
            .forBaseComponent(TestComponentFakeElementSub.class)
            .withParent(new Locator(LocatedBy.css("css")))
            .initializeList(new Locator(LocatedBy.id("id")));
    assertNotNull(componentList);
    assertEquals(componentList.getClass(), LazyList.class);
    assertEquals(((LazyList<?>) componentList).getType(), TestComponentFakeElementSub.class);
    assertNotNull(((LazyList<?>) componentList).getParent());
    assertNull(((LazyList<?>) componentList).getParent().getParent());
    assertEquals(
        ((LazyList<?>) componentList).getLocator().getBy(),
        new Locator(LocatedBy.id("id")).getBy());
    assertEquals(
        ((LazyList<?>) componentList).getParent().getLocator().getBy(),
        new Locator(LocatedBy.css("css")).getBy());
  }

  @Test
  public void testPageObjectBuilderBaseElementInitializationWithUnderlyingLocatorElement() {
    var element =
        PageObjectBuilder.withContext(context)
            .forBaseElement(FakeElement.class)
            .initialize(new Locator(LocatedBy.id("id")));
    assertNotNull(element);
    assertEquals(element.getClass(), FakeElement.class);
    assertNotNull(element.getUnderlying());
    assertNull(element.getParent());
    assertFalse(element.isInitialized());
    assertEquals(
        element.getUnderlying().getLocator().getBy(), new Locator(LocatedBy.id("id")).getBy());
  }

  @Test
  public void
      testPageObjectBuilderBaseElementInitializationWithUnderlyingLocatorElementAndLocatorParent() {
    var element =
        PageObjectBuilder.withContext(context)
            .forBaseElement(FakeElement.class)
            .withParent(new Locator(LocatedBy.css("css")))
            .initialize(new Locator(LocatedBy.id("id")));
    assertNotNull(element);
    assertEquals(element.getClass(), FakeElement.class);
    assertNotNull(element.getUnderlying());
    assertNotNull(element.getParent());
    assertNull(element.getParent().getParent());
    assertFalse(element.isInitialized());
    assertEquals(
        element.getUnderlying().getLocator().getBy(), new Locator(LocatedBy.id("id")).getBy());
    assertEquals(
        element.getParent().getLocator().getBy(), new Locator(LocatedBy.css("css")).getBy());
  }

  @Test
  public void testPageObjectBuilderBaseElementInitializationWithUnderlyingWebElement() {
    var mockUnderlyingWebElement = mock(WebElement.class);
    var element =
        PageObjectBuilder.withContext(context)
            .forBaseElement(FakeElement.class)
            .initialize(mockUnderlyingWebElement, new Locator(LocatedBy.id("id")));
    assertNotNull(element);
    assertEquals(element.getClass(), FakeElement.class);
    assertNotNull(element.getUnderlying());
    assertNull(element.getParent());
    // The component is considered initialized because there is an underlying, pre-located element
    assertTrue(element.isInitialized());
    assertEquals(
        element.getUnderlying().getLocator().getBy(), new Locator(LocatedBy.id("id")).getBy());
  }

  @Test
  public void testPageObjectBuilderBaseElementListInitializationUsingLocator() {
    var elementList =
        PageObjectBuilder.withContext(context)
            .forBaseElement(FakeElement.class)
            .initializeList(new Locator(LocatedBy.id("id")));
    assertNotNull(elementList);
    assertEquals(elementList.getClass(), LazyList.class);
    assertEquals(((LazyList<?>) elementList).getType(), FakeElement.class);
    assertNull(((LazyList<?>) elementList).getParent());
    assertEquals(
        ((LazyList<?>) elementList).getLocator().getBy(), new Locator(LocatedBy.id("id")).getBy());
  }

  @Test
  public void testPageObjectBuilderBaseElementListInitializationUsingLocatorWithParent() {
    var elementList =
        PageObjectBuilder.withContext(context)
            .forBaseElement(FakeElement.class)
            .withParent(new Locator(LocatedBy.css("css")))
            .initializeList(new Locator(LocatedBy.id("id")));
    assertNotNull(elementList);
    assertEquals(elementList.getClass(), LazyList.class);
    assertEquals(((LazyList<?>) elementList).getType(), FakeElement.class);
    assertNotNull(((LazyList<?>) elementList).getParent());
    assertNull(((LazyList<?>) elementList).getParent().getParent());
    assertEquals(
        ((LazyList<?>) elementList).getLocator().getBy(), new Locator(LocatedBy.id("id")).getBy());
    assertEquals(
        ((LazyList<?>) elementList).getParent().getLocator().getBy(),
        new Locator(LocatedBy.css("css")).getBy());
  }

  @Test
  public void
      testPageObjectBuilderLazyWebElementElementInitializationWithUnderlyingLocatorElement() {
    var element =
        PageObjectBuilder.withContext(context)
            .forLazyWebElement()
            .initialize(new Locator(LocatedBy.id("id")));
    assertNotNull(element);
    assertEquals(element.getClass(), LazyWebElement.class);
    assertNull(element.getParent());
    assertFalse(element.isInitialized());
    assertEquals(element.getLocator().getBy(), new Locator(LocatedBy.id("id")).getBy());
  }

  @Test
  public void
      testPageObjectBuilderLazyWebElementInitializationWithUnderlyingLocatorElementAndLocatorParent() {
    var element =
        PageObjectBuilder.withContext(context)
            .forLazyWebElement()
            .withParent(new Locator(LocatedBy.css("css")))
            .initialize(new Locator(LocatedBy.id("id")));
    assertNotNull(element);
    assertEquals(element.getClass(), LazyWebElement.class);
    assertNotNull(element.getParent());
    assertNull(element.getParent().getParent());
    assertFalse(element.isInitialized());
    assertEquals(element.getLocator().getBy(), new Locator(LocatedBy.id("id")).getBy());
    assertEquals(
        element.getParent().getLocator().getBy(), new Locator(LocatedBy.css("css")).getBy());
  }

  @Test
  public void testPageObjectBuilderLazyWebElementInitializationWithUnderlyingWebElement() {
    var mockUnderlyingWebElement = mock(WebElement.class);
    var element =
        PageObjectBuilder.withContext(context)
            .forLazyWebElement()
            .initialize(mockUnderlyingWebElement, new Locator(LocatedBy.id("id")));
    assertNotNull(element);
    assertEquals(element.getClass(), LazyWebElement.class);
    assertNull(element.getParent());
    // The component is considered initialized because there is an underlying, pre-located element
    assertTrue(element.isInitialized());
    assertEquals(element.getLocator().getBy(), new Locator(LocatedBy.id("id")).getBy());
  }

  @Test
  public void testPageObjectBuilderLazyWebElementListInitializationUsingLocator() {
    var elementList =
        PageObjectBuilder.withContext(context)
            .forLazyWebElement()
            .initializeList(new Locator(LocatedBy.id("id")));
    assertNotNull(elementList);
    assertEquals(elementList.getClass(), LazyList.class);
    assertEquals(((LazyList<?>) elementList).getType(), LazyWebElement.class);
    assertNull(((LazyList<?>) elementList).getParent());
    assertEquals(
        ((LazyList<?>) elementList).getLocator().getBy(), new Locator(LocatedBy.id("id")).getBy());
  }

  @Test
  public void testPageObjectBuilderLazyWebElementListInitializationUsingLocatorWithParent() {
    var elementList =
        PageObjectBuilder.withContext(context)
            .forLazyWebElement()
            .withParent(new Locator(LocatedBy.css("css")))
            .initializeList(new Locator(LocatedBy.id("id")));
    assertNotNull(elementList);
    assertEquals(elementList.getClass(), LazyList.class);
    assertEquals(((LazyList<?>) elementList).getType(), LazyWebElement.class);
    assertNotNull(((LazyList<?>) elementList).getParent());
    assertNull(((LazyList<?>) elementList).getParent().getParent());
    assertEquals(
        ((LazyList<?>) elementList).getLocator().getBy(), new Locator(LocatedBy.id("id")).getBy());
    assertEquals(
        ((LazyList<?>) elementList).getParent().getLocator().getBy(),
        new Locator(LocatedBy.css("css")).getBy());
  }
}
