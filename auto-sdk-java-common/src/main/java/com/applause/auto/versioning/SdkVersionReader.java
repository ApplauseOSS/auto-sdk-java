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
package com.applause.auto.versioning;

import com.google.common.base.Suppliers;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Util class to read the SDK version from the VERSION.txt file that we package inside the pom */
public final class SdkVersionReader {

  private static final String SDK_VERSION_FILE_NAME = "VERSION.txt";
  private static final Logger logger = LogManager.getLogger();

  /** Supplies the raw SDK Version from the file */
  private static final Supplier<String> rawVersionSupplier =
      Suppliers.memoize(SdkVersionReader::loadSdkVersionFromFile);

  private SdkVersionReader() { // utility class
  }

  /**
   * Gets the raw SDK Version from the packaged file without performing any network validation.
   *
   * @return The raw SDK Version String, or null if it cannot be read.
   */
  public static String getSdkVersion() {
    return rawVersionSupplier.get();
  }

  /**
   * Performs the file I/O to read the version from the classpath.
   *
   * @return The version string from the file.
   */
  private static String loadSdkVersionFromFile() {
    try (InputStream fileUrl =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(SDK_VERSION_FILE_NAME)) {
      if (fileUrl == null) {
        logger.error(
            "Could not read current Applause Automation SDK version from classpath. Version file not found");
        throw new RuntimeException(
            "Could not read current Applause Automation SDK version from classpath. Version file not found");
      }
      return IOUtils.toString(fileUrl, StandardCharsets.UTF_8).trim();
    } catch (IOException e) {
      logger.fatal("Could not read current Applause Automation SDK version from classpath", e);
      throw new RuntimeException(
          "Could not read current Applause Automation SDK version from classpath", e);
    }
  }
}
