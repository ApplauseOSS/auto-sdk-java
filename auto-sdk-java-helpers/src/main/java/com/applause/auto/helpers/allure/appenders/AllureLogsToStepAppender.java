/*
 *
 * Copyright © 2025 Applause App Quality, Inc.
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
package com.applause.auto.helpers.allure.appenders;

import io.qameta.allure.Allure;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

/** Log4j2 logs to Allure step definition appender */
@Plugin(
    name = "AllureLogsToStepAppender",
    category = Core.CATEGORY_NAME,
    elementType = Appender.ELEMENT_TYPE)
public class AllureLogsToStepAppender extends AbstractAppender {

  /** storage for filtered packages */
  private static Set<String> filteredPackagesToAppendSet;

  protected AllureLogsToStepAppender(
      String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
    super(name, filter, layout, ignoreExceptions, null);
  }

  /**
   * Create log4j2 AllureLogsToStepAppender appender method
   *
   * @param name appender name
   * @param layout appender layout
   * @param filter appender filter
   * @param filteredPackagesToAppend filter packages to append appender, should be provided as comma
   *     separated list
   * @return
   */
  @PluginFactory
  public static AllureLogsToStepAppender createAppender(
      @PluginAttribute("name") String name,
      @PluginElement("Layout") Layout<? extends Serializable> layout,
      @PluginElement("Filter") final Filter filter,
      @PluginAttribute("filteredPackagesToAppend") String filteredPackagesToAppend) {
    if (Objects.isNull(name)) {
      return null;
    }
    if (Objects.isNull(layout)) {
      layout = PatternLayout.createDefaultLayout();
    }
    if (Objects.nonNull(filteredPackagesToAppend)) {
      filteredPackagesToAppendSet =
          new ConcurrentSkipListSet<>(Arrays.asList(filteredPackagesToAppend.split(",")));
    }
    return new AllureLogsToStepAppender(name, filter, layout, true);
  }

  /**
   * log4j2 append method for Allure step appender
   *
   * @param event logging log4j2 intercepted event
   */
  @Override
  public void append(LogEvent event) {
    if (isSourcePackageValidForAppender(event)) {
      Allure.step(event.getMessage().getFormattedMessage());
    }
  }

  private boolean isSourcePackageValidForAppender(LogEvent event) {
    if (filteredPackagesToAppendSet.isEmpty()) {
      return true;
    }
    String sourceClassName = event.getSource().getClassName();
    return filteredPackagesToAppendSet.stream()
        .filter(
            filteredPackagesToAppendSetItem ->
                sourceClassName.contains(filteredPackagesToAppendSetItem))
        .findAny()
        .isPresent();
  }
}
