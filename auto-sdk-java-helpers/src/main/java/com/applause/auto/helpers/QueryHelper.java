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

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.context.IPageObjectExtension;
import com.applause.auto.pageobjectmodel.base.BaseElement;
import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;
import com.applause.auto.pageobjectmodel.elements.ContainerElement;
import com.applause.auto.pageobjectmodel.factory.Locator;
import java.util.List;
import lombok.AllArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Helper class to facilitate finding and enumerating BaseElements. Useful in the case that you need
 * to find an element outside the typical page object pattern.
 */
@AllArgsConstructor
public class QueryHelper implements IPageObjectExtension {
  private IPageObjectContext context;

  /**
   * Finds the first element matching a particular Locator. Returns the element as a specified
   * subclass of BaseElement.
   *
   * @param <T> a subclass of BaseElement, such as Button or Checkbox
   * @param locator the Locator pointing at an element or elements in the DOM
   * @param type the type to return the element as
   * @return the first element found by the Locator, returned as type {@code <T> }
   */
  public <T extends BaseElement> T findElement(final Locator locator, final Class<T> type) {
    By by = locator.getBy();
    WebElement underlying = this.context.getDriver().findElement(by);
    return PageObjectBuilder.withContext(context)
        .forUiElement(type)
        .initialize(underlying, locator);
  }

  /**
   * Finds the first element matching a particular Locator. Returns the element as a default
   * ContainerElement.
   *
   * @param locator the Locator pointing at an element or elements in the DOM
   * @return the first element found by the Locator, returned as ContainerElement
   */
  public ContainerElement findElement(final Locator locator) {
    return findElement(locator, ContainerElement.class);
  }

  /**
   * Finds the first element matching a particular Selenium By. Returns the element as a specified
   * subclass of BaseElement.
   *
   * @param <T> a subclass of BaseElement, such as Button or Checkbox
   * @param by the Selenium By pointing at an element or elements in the DOM
   * @param type the type to return the element as
   * @return the first element found by the Selenium By, returned as type {@code <T> }
   */
  public <T extends BaseElement> T findElement(final By by, final Class<T> type) {
    return findElement(new Locator(by, this.context.getPlatform()), type);
  }

  /**
   * Finds the first element matching a particular Selenium By. Returns the element as a default
   * ContainerElement.
   *
   * @param by the Selenium By pointing at an element or elements in the DOM
   * @return the first element found by the Selenium By, returned as ContainerElement
   */
  public ContainerElement findElement(final By by) {
    return findElement(new Locator(by, this.context.getPlatform()), ContainerElement.class);
  }

  /**
   * Finds all elements matching a particular Locator. Returns the element as a List of a specified
   * subclass of BaseElement.
   *
   * @param <T> a subclass of BaseElement, such as Button or Checkbox
   * @param locator the Locator pointing at some elements in the DOM
   * @param type the type to return the elements as
   * @return a List of elements found by the Locator, returned as type {@code <T> }
   */
  public <T extends BaseElement> List<T> findElements(final Locator locator, final Class<T> type) {
    return PageObjectBuilder.withContext(context).forUiElement(type).initializeList(locator);
  }

  /**
   * Finds all elements matching a particular Locator. Returns the element as a List of default
   * ContainerElements.
   *
   * @param locator the Locator pointing at some elements in the DOM
   * @return a List of elements found by the Locator, returned as ContainerElements
   */
  public List<ContainerElement> findElements(final Locator locator) {
    return findElements(locator, ContainerElement.class);
  }

  /**
   * Finds all elements matching a particular Selenium By. Returns the element as a List of a
   * specified subclass of BaseElement.
   *
   * @param <T> a subclass of BaseElement, such as Button or Checkbox
   * @param by the Selenium By pointing at some elements in the DOM
   * @param type the type to return the elements as
   * @return a List of elements found by the Selenium By, returned as type {@code <T> }
   */
  public <T extends BaseElement> List<T> findElements(final By by, final Class<T> type) {
    return findElements(new Locator(by, this.context.getPlatform()), type);
  }

  /**
   * Finds all elements matching a particular Selenium By. Returns the element as a List of default
   * ContainerElements.
   *
   * @param by the Selenium By pointing at some elements in the DOM
   * @return a List of elements found by the Selenium By, returned as ContainerElements
   */
  public List<ContainerElement> findElements(final By by) {
    return findElements(new Locator(by, this.context.getPlatform()), ContainerElement.class);
  }

  /**
   * Finds the count of matching a particular Locator.
   *
   * @param locator the Locator pointing at some elements in the DOM
   * @return an integer indicating how many elements were found
   */
  public int elementCount(final Locator locator) {
    return findElements(locator).size();
  }

  /**
   * Finds the count of matching a particular Selenium By.
   *
   * @param by the Selenium By pointing at some elements in the DOM
   * @return an integer indicating how many elements were found
   */
  public int elementCount(final By by) {
    return findElements(by).size();
  }
}
