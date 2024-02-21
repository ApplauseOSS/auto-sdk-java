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
package com.applause.auto.framework.testobjects;

import com.applause.auto.data.enums.Platform;
import com.applause.auto.pageobjectmodel.annotation.Implementation;
import com.applause.auto.pageobjectmodel.annotation.Locate;
import com.applause.auto.pageobjectmodel.annotation.SubComponent;
import com.applause.auto.pageobjectmodel.base.BaseComponent;
import com.applause.auto.pageobjectmodel.elements.ContainerElement;
import com.applause.auto.pageobjectmodel.enums.Strategy;
import java.util.List;

@Implementation(is = TestComponentWithFakeElements.class, on = Platform.DEFAULT)
@Implementation(is = TestComponentWithFakeElementsChrome.class, on = Platform.WEB_DESKTOP_FIREFOX)
public class TestComponentWithFakeElements extends BaseComponent {
  @Override
  public void afterInit() {
    afterInitElement.initialize();
  }

  // Elements: ---------------------------------------------

  @Locate(using = Strategy.ID, value = "checkbox1")
  public ContainerElement afterInitElement;

  @Locate(using = Strategy.ID, value = "hide-button")
  public FakeElement button;

  @Locate(using = Strategy.ID, value = "checkbox1")
  public FakeElement checkbox1;

  @Locate(using = Strategy.ID, value = "checkbox2")
  public FakeElement checkbox2;

  @Locate(using = Strategy.ID, value = "top")
  public ContainerElement containerElement;

  @Locate(using = Strategy.ID, value = "checkbox1", on = Platform.DEFAULT)
  @Locate(using = Strategy.ID, value = "checkbox2", on = Platform.WEB)
  @Locate(using = Strategy.ID, value = "checkbox3", on = Platform.WEB_DESKTOP_FIREFOX)
  public ContainerElement fallbackElement;

  @Locate(using = Strategy.ID, value = "checkbox3", on = Platform.WEB_DESKTOP_FIREFOX)
  public ContainerElement noDefault;

  @Locate(using = Strategy.TAGNAME, value = "table")
  public ContainerElement table;

  @Locate(using = Strategy.CSS, value = "#text__paragraphs > div:nth-child(2) > p")
  public ContainerElement paragraph;

  @Locate(using = Strategy.ID, value = "%s")
  public ContainerElement wildcard;

  @Locate(using = Strategy.JQUERY, value = "%s")
  public ContainerElement wildcardJQuery;

  @Locate(using = Strategy.CSS, value = "#embedded__images > div > figure:nth-child(6) > img")
  public FakeElement kittyInFigureWithCaption;

  @Locate(using = Strategy.CSS, value = "%s")
  public ContainerElement cssWildcard;

  @Locate(using = Strategy.LINKTEXT, value = "@test")
  public FakeElement twitterLink;

  @Locate(using = Strategy.ID, value = "select")
  public FakeElement selectList;

  @Locate(using = Strategy.ID, value = "will-hide")
  public FakeElement text;

  @Locate(using = Strategy.ID, value = "input__text")
  public FakeElement textBox;

  // Components: -------------------------------------------

  @Locate(using = Strategy.CSS, value = "#text__tables > table > thead > tr", on = Platform.DEFAULT)
  @Locate(using = Strategy.CSS, value = "#text__tables > table > tfoot > tr", on = Platform.WEB)
  @Locate(
      using = Strategy.CSS,
      value = "#text__tables > table > tbody > tr:nth-child(1)",
      on = Platform.WEB_DESKTOP_FIREFOX)
  public TestComponentFakeElementSub fallbackSubComponent;

  @Locate(
      using = Strategy.CSS,
      value = "#text__tables > table > tbody > tr:nth-child(1)",
      on = Platform.WEB_DESKTOP_FIREFOX)
  public TestComponentFakeElementSub componentNoDefault;

  @SubComponent public TestComponentFakeElementSub noParamsComponent;

  // Lists: ------------------------------------------------

  @Locate(using = Strategy.NAME, value = "checkbox")
  public List<FakeElement> checkboxList;

  @Locate(using = Strategy.ID, value = "bad-locator")
  public FakeElement waitForNotPresent;

  @Locate(using = Strategy.ID, value = "text-with-an-id")
  public FakeElement textWithId;

  @Locate(using = Strategy.NAME, value = "%s")
  public List<ContainerElement> wildcardList;

  @Locate(using = Strategy.NAME, value = "radio")
  public FakeElement radioButton;

  @Locate(using = Strategy.ID, value = "radio1")
  public FakeElement radio1;

  @Locate(using = Strategy.CLASSNAME, value = "radio")
  public List<FakeElement> radioButtons;

  @Locate(
      using = Strategy.CSS,
      value = "#text__tables > table > thead > tr > th",
      on = Platform.DEFAULT)
  @Locate(
      using = Strategy.CSS,
      value = "#text__tables > table > tfoot > tr > th",
      on = Platform.WEB)
  @Locate(
      using = Strategy.CSS,
      value = "#text__tables > table > tbody > tr:nth-child(1) > td",
      on = Platform.WEB_DESKTOP_FIREFOX)
  public List<FakeElement> fallbackElementList;

  @Locate(
      using = Strategy.CSS,
      value = "#text__tables > table > tbody > tr:nth-child(1) > td",
      on = Platform.WEB_DESKTOP_FIREFOX)
  public List<FakeElement> fallbackElementListNoDefault;

  @Locate(using = Strategy.CSS, value = "#text__tables > table > thead > tr", on = Platform.DEFAULT)
  @Locate(using = Strategy.CSS, value = "#text__tables > table > tfoot > tr", on = Platform.WEB)
  @Locate(
      using = Strategy.CSS,
      value = "#text__tables > table > tbody > tr",
      on = Platform.WEB_DESKTOP_FIREFOX)
  public List<TestComponentFakeElementSub> fallbackComponentList;

  @Locate(using = Strategy.TAGNAME, value = "tr", on = Platform.WEB_DESKTOP_FIREFOX)
  public List<TestComponentFakeElementSub> fallbackComponentListNoDefault;

  @Locate(using = Strategy.CSS, value = "%s")
  public List<TestComponentFakeElementSub> wildcardComponents;

  // Helper methods: ---------------------------------------

  public void clickButton() {
    logger.info("Clicking button.");
    button.click();
  }
}

class TestComponentWithFakeElementsChrome extends TestComponentWithFakeElements {
  @Override
  public void clickButton() {
    logger.info("Clicking button, but on Chrome only.");
    button.click();
  }
}
