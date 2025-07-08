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
package com.applause.auto.logging;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

/** Custom log4J2 appender plugin used to capture test log output */
@Plugin(name = "ServerSide", category = "core", elementType = "appender", printObject = true)
public final class ServerSideRemoteLogAppender extends AbstractAppender {
  private final Layout<?> layout;

  private ServerSideRemoteLogAppender(
      final String name, final Filter filter, final Layout<? extends Serializable> layout) {
    super(name, filter, layout, true, null);
    this.layout = layout;
  }

  /**
   * Log4J uses this method to initialize our appender
   *
   * @param name name of appender from config file
   * @param ignoreExceptions ignored for our appender, here to match interface
   * @param layout layout from config file, should be set to match console layout
   * @param filter filters from config file, ignored for our handler
   * @return appender instance
   */
  @PluginFactory
  @SuppressWarnings({
    "PMD.AvoidReassigningParameters",
    "checkstyle:ParameterAssignment",
    "checkstyle:FinalParameters"
  })
  public static ServerSideRemoteLogAppender createAppender(
      @PluginAttribute("name") final String name,
      @PluginAttribute("ignoreExceptions") final boolean ignoreExceptions,
      @PluginElement("Layout") Layout<? extends Serializable> layout,
      @PluginElement("Filters") final Filter filter) {

    if (name == null) {
      LOGGER.error("No name provided for serverside");
      return null;
    }
    if (layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }
    return new ServerSideRemoteLogAppender(name, filter, layout);
  }

  /**
   * Called whenever the user or framework makes a call using a log statement
   *
   * @param logEvent metadata about log statement
   */
  @Override
  public void append(final LogEvent logEvent) {
    // code inspired by Log4J source to behave like their other appender
    LogOutputSingleton.put(new String(layout.toByteArray(logEvent), StandardCharsets.UTF_8));
  }
}
