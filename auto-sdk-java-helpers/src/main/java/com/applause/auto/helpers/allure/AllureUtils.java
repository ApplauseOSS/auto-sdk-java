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
package com.applause.auto.helpers.allure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Utility class for Allure reporting. */
public final class AllureUtils {

  private static final Logger logger = LogManager.getLogger(AllureUtils.class);
  private static final String CATEGORIES_JSON = "categories.json";

  private AllureUtils() {}

  /**
   * Copies the Allure defects categorization JSON file to the Allure report directory.
   *
   * @param allureReportPath The path to the Allure report directory.
   * @param allureDefectsCategorisationFilePath The path to the Allure defects categorization JSON
   *     file.
   * @throws IOException If an I/O error occurs.
   */
  public static void addAllureDefectsCategoriesConfiguration(
      @NonNull final String allureReportPath,
      @NonNull final String allureDefectsCategorisationFilePath)
      throws IOException {

    logger.info("Copying defects configs to {} file for Allure", CATEGORIES_JSON);

    final Path categoriesAllureFile = Path.of(allureReportPath, CATEGORIES_JSON);

    if (!Files.exists(categoriesAllureFile)) {
      Files.createFile(categoriesAllureFile);
    }

    Files.copy(Path.of(allureDefectsCategorisationFilePath), categoriesAllureFile);
  }
}
