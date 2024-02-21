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
package com.applause.auto.framework.templates;

import com.applause.auto.framework.json.BadJsonFormatException;
import com.applause.auto.framework.selenium.EnhancedCapabilities;
import com.applause.auto.logging.ResultPropertyMap;
import com.applause.auto.templates.TemplateManager;
import com.applause.auto.templates.TemplateManager.TemplateProcessException;
import freemarker.template.Template;

/**
 * A freemarker template definition of a driver config that can be re-evaluated before
 * initialization of the driver
 */
public final class DriverConfigTemplate {
  private final Template template;
  private EnhancedCapabilities caps;
  private boolean isValid;
  private String validationError;

  /**
   * Sets up a new DriverConfigTemplate from a freemarker template
   *
   * @param template The Freemarker template
   */
  public DriverConfigTemplate(final Template template) {
    this.template = template;
    this.reProcess();
  }

  /**
   * Refreshes the template with the current state of the ResultPropertyMap
   *
   * @return The refreshed template
   */
  public DriverConfigTemplate reProcess() {
    final String out;
    this.isValid = true;
    try {
      out = TemplateManager.process(this.template, ResultPropertyMap.getProperties());
      this.caps = EnhancedCapabilities.fromJsonString(out);
    } catch (TemplateProcessException | BadJsonFormatException te) {
      this.isValid = false;
      this.validationError = te.getMessage();
    }
    return this;
  }

  /**
   * Fetches the latest capabilities
   *
   * @return The current capabilities object
   * @throws BadJsonFormatException If the last capabilities resulted in an error
   */
  public EnhancedCapabilities getCurrentCapabilities() throws BadJsonFormatException {
    if (!this.isValid) {
      throw new BadJsonFormatException(validationError);
    }
    return this.caps;
  }
}
