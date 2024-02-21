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
package com.applause.auto.helpers.email;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import lombok.Getter;

/** Represents an email's file attachment, containing the filename and byte content. */
public class Attachment {
  @Getter private final String filename;
  private final byte[] content;

  Attachment(final String filename, final byte[] content) {
    this.filename = filename;
    this.content = content.clone();
  }

  /**
   * Gets a copy of the content of the attachment.
   *
   * @return a copied byte[] with the content of the attachment
   */
  public byte[] getContent() {
    return content.clone();
  }

  /**
   * Writes the content of the attachment to an absolute file destination.
   *
   * @param destination the absolute filepath where the file will be stored, including filename
   */
  public void writeToFile(final String destination) {
    try {
      Files.write(content, new File(destination));
    } catch (IOException e) {
      throw new RuntimeException(
          String.format(
              "Unable to write attachment [%s] to destination [%s].", this.filename, destination),
          e);
    }
  }
}
