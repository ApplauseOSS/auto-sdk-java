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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.openqa.selenium.UnsupportedCommandException;

/**
 * BaseElement subclass representing a TextBox.
 *
 * @see com.applause.auto.pageobjectmodel.base.BaseElement
 */
public class TextBox extends BaseElement {
  /**
   * Basic constructor for a TextBox. TextBoxes generally should be generated by PageObjectBuilder.
   *
   * @param element the LazyWebElement underlying this TextBox
   * @param context the underlying context
   */
  public TextBox(final LazyWebElement element, final IPageObjectContext context) {
    super(element, context);
  }

  /**
   * Sends a string of keys into the TextBox.
   *
   * @param keys the string to send
   */
  public void sendKeys(final String keys) {
    this.underlying.sendKeys(keys);
  }

  /** Clears the current text from the TextBox. */
  public void clearText() {
    this.underlying.clear();
  }

  /**
   * Gets the current text from the TextBox using DOM property. Not compatible with Android 7 or
   * below.
   *
   * @return current text as a string
   */
  @SuppressFBWarnings("DCN_NULLPOINTER_EXCEPTION")
  public String getCurrentText() {
    String response;
    try {
      // selenium8/W3C style
      // this sometimes fails on IOS - catch exceptions.
      response = this.underlying.getDomProperty("value");
    } catch (UnsupportedCommandException | NullPointerException ex) {
      // olderSelenium/JWP style
      // can be nondeterministic
      response = this.underlying.getAttribute("value");
    }
    return response;
  }

  /**
   * Gets the current text from the TextBox using getAttribute.
   *
   * @return current text as a string
   */
  public String getCurrentTextOldAndroid() {
    return this.underlying.getAttribute("value");
  }
}
