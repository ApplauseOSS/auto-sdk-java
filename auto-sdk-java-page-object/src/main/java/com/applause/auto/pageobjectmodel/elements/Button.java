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
import java.util.Optional;

/**
 * BaseElement subclass representing a Button.
 *
 * @see com.applause.auto.pageobjectmodel.base.BaseElement
 */
public class Button extends BaseElement {
  /**
   * Basic constructor for a Button. Buttons generally should be generated by PageObjectBuilder.
   *
   * @param element the LazyWebElement underlying this Button
   * @param context the web driver
   */
  public Button(final LazyWebElement element, final IPageObjectContext context) {
    super(element, context);
  }

  /**
   * Gets the text value of the Button.
   *
   * @return String value of Button, or an empty string in the case that isn't set.
   */
  public String getText() {
    return Optional.ofNullable(this.underlying.getText()).orElse("");
  }
}
