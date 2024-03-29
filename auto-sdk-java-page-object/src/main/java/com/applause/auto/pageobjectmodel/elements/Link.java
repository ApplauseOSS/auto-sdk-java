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
package com.applause.auto.pageobjectmodel.elements;

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.pageobjectmodel.base.BaseElement;
import com.applause.auto.pageobjectmodel.factory.LazyWebElement;
import org.openqa.selenium.UnsupportedCommandException;

/**
 * BaseElement subclass representing a Link.
 *
 * @see com.applause.auto.pageobjectmodel.base.BaseElement
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class Link extends BaseElement {
  /**
   * Basic constructor for a Link. Links generally should be generated by PageObjectBuilder.
   *
   * @param element the LazyWebElement underlying this Link
   * @param context the web driver
   */
  public Link(final LazyWebElement element, final IPageObjectContext context) {
    super(element, context);
  }

  /**
   * Gets the URL of this Link using DOM property. Not compatible with Android 7 or below.
   *
   * @return the String url for the link
   */
  public String getLinkURL() {
    String result;
    try {
      // selenium8/W3C style
      // this sometimes fails on IOS - catch exception.
      result = this.underlying.getDomProperty("href");
    } catch (UnsupportedCommandException uce) {
      // olderSelenium/JWP style
      // result is somewhere between between getProperty & getAttribute
      // This can be non-deterministic.  eg: 'http://baseUrl.com/something' vs 'something'
      result = this.underlying.getAttribute("href");
    }
    return result;
  }

  /**
   * Gets the URL of this Link using getAttribute for Android OS less than 8
   *
   * @return the String url for the link
   */
  public String getLinkURLOldAndroid() {
    return this.underlying.getAttribute("href");
  }

  /**
   * Navigates the current driver to the URL of this Link using DOM property. Not compatible with
   * Android 7 or below.
   */
  public void goToURL() {
    this.context.getDriver().get(getLinkURL());
  }

  /**
   * Navigates the current driver to the URL of this Link using getAttribute for Android OS less
   * than 8
   */
  public void goToURLOldAndroid() {
    this.context.getDriver().get(getLinkURLOldAndroid());
  }

  /**
   * Gets the text value of this Link using DOM property. Not compatible with Android 7 or below.
   *
   * @return the text value of the Link
   */
  public String getText() {
    return this.underlying.getDomProperty("innerHTML");
  }

  /**
   * Gets the text value of this Link using getAttribute for Android OS less than 8
   *
   * @return the text value of the Link
   */
  public String getTextOldAndroid() {
    return this.underlying.getAttribute("innerHTML");
  }
}
