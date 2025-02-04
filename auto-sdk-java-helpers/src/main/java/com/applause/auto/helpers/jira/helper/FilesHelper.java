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
package com.applause.auto.helpers.jira.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FilesHelper {
  private static final Logger logger = LogManager.getLogger(FilesHelper.class);

  private FilesHelper() {
    // utility class
  }

  /**
   * Encodes a file to Base64.
   *
   * @param filePath The path to the file to encode.
   * @return The Base64 encoded string of the file.
   * @throws IOException If an I/O error occurs while reading the file.
   * @throws NullPointerException If the provided filePath is null.
   */
  public static String encodeBase64File(@NonNull final String filePath) throws IOException {
    logger.info("Encoding file: {} to Base64", filePath);
    byte[] fileContent = Files.readAllBytes(getFile(filePath).toPath());
    return Base64.getEncoder().encodeToString(fileContent);
  }

  /**
   * Gets the file name from a file path.
   *
   * @param filePath The path to the file.
   * @return The name of the file.
   * @throws NullPointerException If the provided filePath is null.
   */
  public static String getFileNameFromPath(@NonNull final String filePath) {
    return getFile(filePath).getName();
  }

  /**
   * Gets the file type from a file path.
   *
   * @param filePath The path to the file.
   * @return The type of the file, or null if it cannot be determined.
   * @throws IOException If an I/O error occurs while determining the file type.
   * @throws NullPointerException If the provided filePath is null.
   */
  public static String getFileType(@NonNull final String filePath) throws IOException {
    return Files.probeContentType(getFile(filePath).toPath());
  }

  @SneakyThrows
  private static File getFile(@NonNull final String filePath) {
    logger.info("Returning file from path: {}", filePath);
    return new File(filePath);
  }
}
