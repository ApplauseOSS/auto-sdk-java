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

import com.google.common.io.ByteStreams;
import jakarta.mail.BodyPart;
import jakarta.mail.Header;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;

/** Represents a single email message, including any headers, content, and attachments. */
@Data
public class Email {
  private final Message rawMessage;
  private final String body;
  private final List<Attachment> attachments = new ArrayList<>();

  Email(final Message msg) {
    this.rawMessage = msg;
    this.body = parseContent();
  }

  private String parseContent() {
    try {
      if (rawMessage.isMimeType("multipart/*")) {
        return parseMultipart((MimeMultipart) rawMessage.getContent());
      }
      return rawMessage.getContent().toString();
    } catch (IOException | MessagingException e) {
      throw new RuntimeException("Unable to parse content of email.", e);
    }
  }

  private String parseMultipart(final MimeMultipart multipart)
      throws MessagingException, IOException {
    StringBuilder result = new StringBuilder();
    int count = multipart.getCount();
    for (int i = 0; i < count; i++) {
      BodyPart bodyPart = multipart.getBodyPart(i);
      Object content = bodyPart.getContent();
      String disposition = bodyPart.getDisposition();
      if (content instanceof MimeMultipart) {
        result.append(parseMultipart((MimeMultipart) content));
      } else if (Part.ATTACHMENT.equals(disposition) || Part.INLINE.equals(disposition)) {
        try (InputStream stream = (InputStream) content) {
          byte[] bytes = ByteStreams.toByteArray(stream);
          attachments.add(new Attachment(bodyPart.getFileName(), bytes));
        }
      } else {
        if (!result.isEmpty()) {
          result.append('\n');
        }
        result.append(content);
      }
    }
    return result.toString();
  }

  /**
   * Gets the headers of this email message.
   *
   * @return a List of Header objects, each containing a name and value
   */
  public List<Header> getHeaders() {
    try {
      return Collections.list(rawMessage.getAllHeaders());
    } catch (MessagingException e) {
      throw new RuntimeException("Could not retrieve headers from email.", e);
    }
  }

  /**
   * Gets the subject of this email message.
   *
   * @return a String containing the subject of the message
   */
  public String getSubject() {
    try {
      return rawMessage.getSubject();
    } catch (MessagingException e) {
      throw new RuntimeException("Could not retrieve subject from email.", e);
    }
  }

  /**
   * Gets the first match of a regex pattern in the email body.
   *
   * @param regex a String pattern to be run against the email body
   * @return a String containing the first match. null if no match.
   */
  public String getFirstMatchInBody(final String regex) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(getBody());
    if (matcher.find()) {
      return matcher.group();
    }
    return null;
  }
}
