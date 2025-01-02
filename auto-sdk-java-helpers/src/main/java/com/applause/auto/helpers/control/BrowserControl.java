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
package com.applause.auto.helpers.control;

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.context.IPageObjectExtension;
import com.applause.auto.pageobjectmodel.base.UIElement;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;

/**
 * Utility class to facilitate performing a variety of browser actions, such as clicks, drags, and
 * mouse-downs.
 */
@SuppressWarnings({"checkstyle:ParameterName", "checkstyle:AbbreviationAsWordInName"})
@AllArgsConstructor
public class BrowserControl implements IPageObjectExtension {
  private static final Logger logger = LogManager.getLogger();
  private final IPageObjectContext context;

  /**
   * Gets the current driver as a JavascriptExecutor.
   *
   * @return a JavascriptExecutor to perform Javascript actions against
   */
  private JavascriptExecutor getExecutor() {
    return (JavascriptExecutor) context.getDriver();
  }

  /**
   * Gets the browser action script from the resources' directory.
   *
   * @return the script as a string
   */
  private String getBrowserActionsScript() {
    URL fileURL = Resources.getResource("nativeBrowserActions.js");

    if (fileURL == null) {
      throw new RuntimeException(
          "BrowserControl's required file \"nativeBrowserAction.js\" could not be found.");
    }

    try {
      return Resources.toString(fileURL, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Simulates a mouse down action on an element.
   *
   * @param element the element on which the mouse down will be performed
   */
  private void mouseDown(final UIElement element) {
    getExecutor()
        .executeScript(
            getBrowserActionsScript() + "dispatchDownEvent(arguments[0]);",
            element.getUnderlyingWebElement());
  }

  /**
   * Simulates a mouse up action on an element with an offset.
   *
   * @param element the element on which the mouse up will be performed
   * @param xOffset the x offset
   * @param yOffset the y offset
   */
  private void mouseUp(final UIElement element, final int xOffset, final int yOffset) {
    getExecutor()
        .executeScript(
            getBrowserActionsScript()
                + "dispatchUpEvent(arguments[0], arguments[1], "
                + "arguments[2]);",
            element.getUnderlyingWebElement(),
            xOffset,
            yOffset);
  }

  /**
   * Simulates a mouse up action on an element with no offset.
   *
   * @param element the element on which the mouse up will be performed
   */
  private void mouseUp(final UIElement element) {
    mouseUp(element, 0, 0);
  }

  /**
   * Simulates a mouse move action on an element with an offset. The mouse will be moved from the
   * element to the offset point.
   *
   * @param element the element on which the mouse up will be performed
   * @param xOffset the x offset
   * @param yOffset the y offset
   */
  private void mouseMove(final UIElement element, final int xOffset, final int yOffset) {
    getExecutor()
        .executeScript(
            getBrowserActionsScript()
                + "dispatchMoveEvent(arguments[0], arguments[1], "
                + "arguments[2]);",
            element.getUnderlyingWebElement(),
            xOffset,
            yOffset);
  }

  /**
   * Invokes the native click function of the web element.
   *
   * @param element the target element to perform the action on.
   */
  public void jsClick(final UIElement element) {
    logger.debug("Performing a native click.");
    getExecutor().executeScript("arguments[0].click();", element.getUnderlyingWebElement());
  }

  /**
   * Simulates a click (or one-finger tap for touch-enabled devices) action.
   *
   * @param element the target element to perform the action on.
   */
  public void click(final UIElement element) {
    mouseDown(element);
    mouseUp(element);
  }

  /**
   * Simulates a click-and-hold (or press for touch-enabled devices) action.
   *
   * @param element the target element to perform the action on.
   * @param millis the time (in milliseconds) to simulate the hold for.
   */
  public void clickAndHold(final UIElement element, final long millis) {
    mouseDown(element);
    sleep(millis);
    mouseUp(element);
  }

  /**
   * Simulate a mouse hover element (or one finger hover for touch-enabled devices) action.
   *
   * @param element the target element to perform the action on.
   */
  public void hoverOverElement(final UIElement element) {
    Dimension size = element.getUnderlyingWebElement().getSize();
    mouseMove(element, size.getWidth() / 2, size.getHeight() / 2);
  }

  /**
   * Simulate a drag-and-drop action.
   *
   * @param element the target element to perform the action on.
   * @param xOffset the x-coordinate offset to move the element to.
   * @param yOffset the y-coordinate offset to move the element to.
   */
  public void dragAndDrop(final UIElement element, final int xOffset, final int yOffset) {
    mouseDown(element);
    mouseMove(element, xOffset, yOffset);
    mouseUp(element, xOffset, yOffset);
  }

  /**
   * Sleep the current thread for specified number of milliseconds.
   *
   * @param millis number of milliseconds to sleep the thread.
   */
  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      logger.error("Sleep failed.", e);
    }
  }
}
