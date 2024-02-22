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
import com.applause.auto.templates.TemplateManager;
import com.applause.auto.templates.TemplateManager.TemplateGenerationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** A helper class for loading a capabilities file into a DriverConfigTemplate */
public final class DriverConfigTemplateHelper {
  private static final Logger logger = LogManager.getLogger(DriverConfigTemplateHelper.class);

  private DriverConfigTemplateHelper() {
    // Just static methods
  }

  /**
   * Process the capabilities file and return
   *
   * @param capabilitiesFilePath The file used to generate that capabilities
   * @return An instance of EnhancedCapabilities. null on error
   * @throws BadJsonFormatException if can't make valid configuration
   * @throws TemplateGenerationException If template could not be generated
   * @throws IOException If the file cannot be found or is a directory
   */
  public static DriverConfigTemplate makeCapabilities(final @NonNull String capabilitiesFilePath)
      throws BadJsonFormatException, TemplateGenerationException, IOException {
    return makeCapabilities(Path.of(capabilitiesFilePath));
  }

  /**
   * Process the capabilities file and return
   *
   * @param capabilitiesFilePath The file used to generate that capabilities
   * @return An instance of EnhancedCapabilities. null on error
   * @throws BadJsonFormatException if can't make valid configuration
   * @throws TemplateGenerationException If template could not be generated
   * @throws IOException If the file cannot be found or is a directory
   */
  public static DriverConfigTemplate makeCapabilities(final @NonNull Path capabilitiesFilePath)
      throws BadJsonFormatException, TemplateGenerationException, IOException {
    return makeCapabilities(capabilitiesFilePath.toFile());
  }

  /**
   * Process the capabilities file and return
   *
   * @param capabilitiesFile The file used to generate that capabilities
   * @return An instance of EnhancedCapabilities. null on error
   * @throws BadJsonFormatException if can't make valid configuration
   * @throws TemplateGenerationException If template could not be generated
   * @throws IOException If the file cannot be found or is a directory
   */
  public static DriverConfigTemplate makeCapabilities(final @NonNull File capabilitiesFile)
      throws BadJsonFormatException, TemplateGenerationException, IOException {
    logger.debug("Beginning JSON parse: capsFile={}", capabilitiesFile.getName());
    final var fileContents = FileUtils.readFileToString(capabilitiesFile, Charset.defaultCharset());
    if (fileContents == null) {
      throw new BadJsonFormatException(
          String.format(
              "Unable to load file '%s' from file system and jar file",
              capabilitiesFile.getName()));
    }

    return new DriverConfigTemplate(
        TemplateManager.generateTemplate(capabilitiesFile.getName(), fileContents));
  }
}
