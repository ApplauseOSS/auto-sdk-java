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
import com.applause.auto.pageobjectmodel.base.LocatedBy;
import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;
import com.applause.auto.pageobjectmodel.factory.LazyWebElement;
import com.applause.auto.pageobjectmodel.factory.Locator;
import java.util.List;

/**
 * BaseElement subclass representing a RadioButton.
 *
 * @see com.applause.auto.pageobjectmodel.base.BaseElement
 */
public class RadioButton extends BaseElement {
  /**
   * Basic constructor for a RadioButton. RadioButtons generally should be generated by
   * PageObjectBuilder.
   *
   * @param element the LazyWebElement underlying this RadioButton
   * @param context the web driver
   */
  public RadioButton(final LazyWebElement element, final IPageObjectContext context) {
    super(element, context);
  }

  /**
   * Get the selected RadioButton from the group. Not compatible with Android 7 or below.
   *
   * @return RadioButton object corresponding to the currently selected button.
   */
  public RadioButton getSelected() {
    String name = this.getDomPropertyValue("name");
    return getSelectedHavingName(name);
  }

  /**
   * Get the selected RadioButton from the group for old Android OS less than 8
   *
   * @return RadioButton object corresponding to the currently selected button.
   */
  public RadioButton getSelectedOldAndroid() {
    String name = this.getAttribute("name");
    return getSelectedHavingName(name);
  }

  private RadioButton getSelectedHavingName(final String locatorName) {
    Locator locator = new Locator(LocatedBy.name(locatorName), this.context.getPlatform());
    List<RadioButton> allButtons =
        PageObjectBuilder.withContext(context)
            .forBaseElement(RadioButton.class)
            .initializeList(locator);
    for (RadioButton button : allButtons) {
      if (button.isSelected()) {
        return button;
      }
    }
    logger.error("No RadioButton with name [" + locatorName + "] is currently selected.");
    return null;
  }

  /**
   * Checks if the RadioButton is selected.
   *
   * @return true if selected, otherwise, false
   */
  public boolean isSelected() {
    return this.underlying.isSelected();
  }
}
