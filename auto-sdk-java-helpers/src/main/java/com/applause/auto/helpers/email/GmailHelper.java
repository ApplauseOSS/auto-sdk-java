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
package com.applause.auto.helpers.email;

import com.applause.auto.helpers.util.ThreadHelper;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMultipart;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

/** A concrete email client implementation for Gmail. */
public class GmailHelper extends CommonMailClient {

  private static final Logger logger = LogManager.getLogger(GmailHelper.class);
  private static final int TEN_MINUTES_TIMEOUT_WAIT_MILLI = 600000;
  private static final int FIVE_SECOND_WAIT_MILLI = 5000;

  /**
   * Default constructor with params.
   *
   * <p>PLEASE NOTE:
   *
   * <p>To make this work you should use a Gmail app password.
   *
   * <p>For more information on how to set it, please check
   * https://support.google.com/accounts/answer/185833?hl=en
   *
   * <p>Example: username = some@email.com password = xxxx
   *
   * @param username The Gmail username (email address). Must not be null.
   * @param appPassword The Gmail app password.
   */
  public GmailHelper(final String username, final String appPassword) throws MessagingException {
    super(username.trim(), appPassword, Protocol.IMAP, "imap.gmail.com");
  }

  public GmailHelper(
      final String username, final String appPassword, final Protocol protocol, final String host)
      throws MessagingException {
    super(username, appPassword, protocol, host);
  }

  /** Clears all read emails from the inbox folder of the email. */
  public void emptyEmailBox() {
    Folder inboxFolder = markAllEmailsAsRead();
    emptyAllReadEmailsFromInbox(inboxFolder);
    closeConnection();
  }

  /**
   * Waits for a specific email to arrive.
   *
   * @param driver The currently active selenium WebDriver instance. It is kept alive as long as the
   *     email didn't arrive yet and the timeout value isn't reached.
   * @param criteria The search criteria to use when searching for the email.
   * @param checkOnlyUnreadEmails If true, then we would get fetch the unread emails. If false, * we
   *     would check all the emails in the folder (can be slow).
   * @param markEmailAsSeen If true, the email would be marked as seen after checking it.
   * @return A message array that contains all the emails that match the mentioned criteria.
   */
  public List<Message> waitForEmailToArrive(
      @NonNull final WebDriver driver,
      @NonNull final SearchCriteria criteria,
      final boolean checkOnlyUnreadEmails,
      final boolean markEmailAsSeen) {
    return waitForEmailToArrive(
        driver, criteria, checkOnlyUnreadEmails, markEmailAsSeen, TEN_MINUTES_TIMEOUT_WAIT_MILLI);
  }

  /**
   * Waits for a specific email to arrive.
   *
   * @param driver The currently active selenium WebDriver instance. It is kept alive as long as the
   *     email didn't arrive yet and the timeout value isn't reached.
   * @param criteria The search criteria to use when searching for the email.
   * @param checkOnlyUnreadEmails If true, then we would get fetch the unread emails. If false, * we
   *     would check all the emails in the folder (can be slow).
   * @param markEmailAsSeen If true, the email would be marked as seen after checking it.
   * @param timeOutInMillis The time to wait in milli-seconds before timing out and giving up to
   *     find the email.
   * @return A message array that contains all the emails that match the mentioned criteria.
   */
  public List<Message> waitForEmailToArrive(
      @Nullable final WebDriver driver,
      @NonNull final SearchCriteria criteria,
      final boolean checkOnlyUnreadEmails,
      final boolean markEmailAsSeen,
      final long timeOutInMillis) {
    logger.info("Waiting for email to arrive...");
    int timeElapsed = 0;
    List<Message> foundEmails = null;

    while (timeElapsed < timeOutInMillis) {
      foundEmails = isEmailReceived(criteria, checkOnlyUnreadEmails, markEmailAsSeen);
      if (foundEmails.isEmpty()) {
        logger.info(
            "Email not received after {} seconds. Retrying after 5 seconds...", timeElapsed / 1000);
        ThreadHelper.sleep(FIVE_SECOND_WAIT_MILLI);
        timeElapsed += FIVE_SECOND_WAIT_MILLI;

        /*
         * Get the session ID to prevent remote Cloud provider from closing the connection for being
         * idle (Usually timeouts after 90 seconds).
         */
        if (driver != null) {
          logger.info("Session ID: {}", ((RemoteWebDriver) driver).getSessionId());
        } else {
          logger.info("No driver provided, moving on...");
        }
      } else {
        break;
      }
    }

    /*
     * Shutdown hook to make sure the email server connection is terminated correctly before the
     * application terminates.
     */
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  logger.info("Shutdown hook started. Terminating the GmailHelper email instance");
                  // close the email connection
                  closeConnection();
                }));

    return foundEmails;
  }

  /**
   * Parses the content of the email one part at a time. Sometimes this might not work when the
   * email is not formatted correctly, and you might need to use @parseEmailFromInputStream.
   *
   * @param email The email object to check
   * @return A string representing the content of the concatenated parts of the email object.
   */
  public String parseEmailParts(@NonNull final Message email) {
    String result = "";
    try {
      if (email.isMimeType("text/plain")) {
        result = email.getContent().toString();
      } else if (email.isMimeType("multipart/*")) {
        MimeMultipart mimeMultipart = (MimeMultipart) email.getContent();
        result = getTextFromMimeMultipart(mimeMultipart);
      }
    } catch (Exception ex) {
      logger.info("An exception was thrown. Message = " + ex.getMessage());
    }

    return result;
  }

  /**
   * Parses the email content form its input stream. This method will return an aggregated
   * representation of all the email parts as text. Very useful in case parsing the email as parts
   * fail.
   *
   * @param email The email object to check
   * @return A string representation to the email's input stream.
   */
  public String parseEmailFromInputStream(@NonNull final Message email) {
    try {
      return new String(email.getInputStream().readAllBytes(), Charset.defaultCharset());
    } catch (Exception e) {
      logger.error(e);
    }
    return null;
  }

  /**
   * Extracts the text from multipart emails.
   *
   * @param mimeMultipart The multipart email to get the content from.
   * @return A string representing the email content.
   * @throws MessagingException If a messaging exception occurs.
   * @throws IOException If an I/O exception occurs.
   */
  private String getTextFromMimeMultipart(@NonNull final MimeMultipart mimeMultipart)
      throws MessagingException, IOException {
    StringBuilder result = new StringBuilder();
    int count = mimeMultipart.getCount();
    for (int i = 0; i < count; i++) {
      BodyPart bodyPart = mimeMultipart.getBodyPart(i);
      if (bodyPart.isMimeType("text/plain")) {
        result.append('\n').append(bodyPart.getContent());
        break; // without break same text appears twice in my tests
      } else if (bodyPart.isMimeType("text/html")) {
        String html = (String) bodyPart.getContent();
        result.append('\n').append(org.jsoup.Jsoup.parse(html).text());
      } else if (bodyPart.getContent() instanceof MimeMultipart) {
        result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
      }
    }
    return result.toString();
  }

  /**
   * Checks whether a certain email exists in the inbox or not. It uses the provided criteria when
   * searching. If multiple emails are found, it will return all of them.
   *
   * @param criteria The search criteria to use when searching for the email.
   * @param checkOnlyUnreadEmails If true, then we would get fetch the unread emails. If false, we
   *     would check all the emails in the folder (can be slow).
   * @param markEmailAsSeen If true, the email would be marked as seen after checking it.
   * @return A message array that contains all the emails that match the mentioned criteria.
   */
  public List<Message> isEmailReceived(
      @NonNull final SearchCriteria criteria,
      final boolean checkOnlyUnreadEmails,
      final boolean markEmailAsSeen) {
    final var matchedEmails = new ArrayList<Message>();
    try {
      Message[] emails;
      if (checkOnlyUnreadEmails) {
        emails = getUnreadEmails(openSelectedFolderWithRights("Inbox", Folder.READ_WRITE));
      } else {
        emails = openSelectedFolderWithRights("Inbox", Folder.READ_WRITE).getMessages();
      }

      if (emails.length == 0) {
        logger.info("No emails found, moving on...");
        return List.of();
      }

      logger.info("Found emails: " + emails.length);
      for (int i = 0; i < emails.length; i++) {
        Message email = emails[i];
        logger.info("---------------------------------");
        logger.info("Email Number: [{}]", i + 1);
        logger.info("Subject: [{}]", email.getSubject());
        logger.info("From: [{}]", (Object) email.getFrom());

        // check if the email matches the criteria
        if (doesEmailMatchCriteria(email, criteria)) {
          matchedEmails.add(email);
        }

        if (markEmailAsSeen) {
          email.setFlag(Flag.SEEN, true);
        }
      }
      return matchedEmails;
    } catch (Exception e) {
      logger.info("An exception was thrown.", e);
    }

    return List.of(new Message[0]);
  }
}
