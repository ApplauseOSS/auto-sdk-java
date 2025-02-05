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
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

/** Appender for logging to Allure steps. */
@Plugin(
    name = "AllureLogsToStepAppender",
    category = Node.CATEGORY,
    elementType = Appender.ELEMENT_TYPE)
public final class AllureLogsToStepAppender extends AbstractAppender {

  private static final String COMMA = ",";
  private static final Set<String> FILTERED_PACKAGES_TO_APPEND_SET = new ConcurrentSkipListSet<>();

  private AllureLogsToStepAppender(
      final String name,
      final Filter filter,
      final Layout<? extends Serializable> layout,
      final boolean ignoreExceptions) {
    super(name, filter, layout, ignoreExceptions, null);
  }

  /**
   * Creates an AllureLogsToStepAppender.
   *
   * @param name The name of the appender.
   * @param layout The layout to use for the appender.
   * @param filter The filter to use for the appender.
   * @param filteredPackagesToAppend A comma-separated list of packages to filter.
   * @return A new AllureLogsToStepAppender instance, or null if the name is null.
   */
  @PluginFactory
  public static AllureLogsToStepAppender createAppender(
      @PluginAttribute("name") final String name,
      @PluginElement("Layout") final Layout<? extends Serializable> layout,
      @PluginElement("Filter") final Filter filter,
      @PluginAttribute("filteredPackagesToAppend") final String filteredPackagesToAppend) {

    if (name == null) {
      return null;
    }

    final var usedLayout = Objects.requireNonNullElse(layout, PatternLayout.createDefaultLayout());

    if (filteredPackagesToAppend != null) {
      FILTERED_PACKAGES_TO_APPEND_SET.addAll(Arrays.asList(filteredPackagesToAppend.split(COMMA)));
    }

    return new AllureLogsToStepAppender(name, filter, usedLayout, true);
  }

  /**
   * Appends a log event to Allure step.
   *
   * @param event The log event to append.
   */
  @Override
  public void append(final LogEvent event) {
    if (isSourcePackageValidForAppender(event)) {
      Allure.step(event.getMessage().getFormattedMessage());
    }
  }

  private boolean isSourcePackageValidForAppender(final LogEvent event) {
    if (FILTERED_PACKAGES_TO_APPEND_SET.isEmpty()) {
      return true;
    }

    final var sourceClassName = event.getSource().getClassName();

    return FILTERED_PACKAGES_TO_APPEND_SET.stream().anyMatch(sourceClassName::contains);
  }
}
