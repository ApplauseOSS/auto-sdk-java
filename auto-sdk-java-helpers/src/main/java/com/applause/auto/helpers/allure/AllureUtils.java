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

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AllureUtils {

  private static final Logger logger = LogManager.getLogger(AllureUtils.class);

  /**
   * Copy Allure categorisation .json file to report directory
   * https://docs.qameta.io/allure/#_categories_2
   *
   * @throws IOException
   */
  public static void addAllureDefectsCategoriesConfiguration(
      String allureReportPath, String allureDefectsCategorisationFilePath) {
    try {
      logger.info("Copy defects configs to categories.json file for Allure");
      File categoriesAllureFile = new File(allureReportPath + "/categories.json");
      if (!categoriesAllureFile.exists()) {
        categoriesAllureFile.createNewFile();
      }
      categoriesAllureFile.setReadable(true);
      categoriesAllureFile.setWritable(true);
      FileUtils.copyFile(new File(allureDefectsCategorisationFilePath), categoriesAllureFile);

    } catch (Exception e) {
      logger.error("Error creating allure categories.json file " + e.getMessage());
    }
  }
}
