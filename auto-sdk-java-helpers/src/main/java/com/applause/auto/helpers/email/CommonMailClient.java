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

import jakarta.mail.*;
import jakarta.mail.Flags.Flag;
import jakarta.mail.search.FlagTerm;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Provides some common methods between most email clients. */
public class CommonMailClient {
  private String host;
  private final Properties props = new Properties();
  private final Store store;
  private String protocol;
  private String userName;
  private String password;
  private static final Logger logger = LogManager.getLogger(CommonMailClient.class);

  /**
   * Constructs a new CommonMailClient with the specified username, password, protocol, and host.
   * Example: username = some@email.com, password = xxxx, protocol = imaps, host = imap.gmail.com
   *
   * @param userName The username for the mail account.
   * @param password The password for the mail account.
   * @param protocol The protocol to use for connecting to the mail server.
   * @param host The hostname of the mail server.
   * @throws MessagingException If an error occurs during the setup of mail credentials.
   */
  public CommonMailClient(
      final String userName, final String password, final Protocol protocol, final String host)
      throws MessagingException {
    this.userName = userName.trim();
    this.password = password;
    this.protocol = protocol.getValue();
    this.host = host.trim();
    store = setMailCredentials();
  }

  /**
   * Extracts from a String using Regular expressions
   *
   * @param text The text to extract from
   * @param regex The regular expression to use
   * @return The extracted text after applying the regex
   */
  public String extractWithRegexFromString(final String text, @NonNull final String regex) {
    if (text.isEmpty()) {
      logger.info("Text is empty");
      return null;
    }
    logger.info("Extracting with regex = [{}...", regex);
    Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
    Matcher matcher = pattern.matcher(text);

    if (matcher.find()) {
      logger.info("Extracted text is:: {}", matcher.group(1).trim());
      return matcher.group(1).trim();
    }
    throw new RuntimeException("No matches were found in the text.");
  }

  /**
   * input stream with properties for email client, protocol, username, password, host example of
   * property file content: mail.store.protocol = imaps username = some@email.com password = xxxx
   * host = imap.gmail.com (for example).
   *
   * @param resourceFile an InputStream that represents a .properties files that includes all the
   *     above-mentioned properties.
   * @throws MessagingException If an error occurs during mail session setup.
   */
  public CommonMailClient(@NonNull final InputStream resourceFile) throws MessagingException {
    store = setMailCredentials(resourceFile);
  }

  /**
   * Deletes all the read emails from the mentioned folder of the email account.
   *
   * @param emailFolder The email folder to use.
   */
  public void emptyAllReadEmailsFromInbox(@NonNull final Folder emailFolder) {
    deleteReadEmails(emailFolder);
  }

  /**
   * Marks all emails on the email account as read (it only does that for the 'Inbox' folder).
   *
   * @return A reference to the InboxFolder after marking all the emails in it as read.
   */
  public Folder markAllEmailsAsRead() {
    Folder inboxFolder = null;
    try {
      inboxFolder = openSelectedFolderWithRights("Inbox", Folder.READ_WRITE);
      Message[] emails = getUnreadEmails(inboxFolder);
      for (Message email : emails) {
        email.setFlag(Flag.SEEN, true);
      }
    } catch (Exception ex) {
      logger.info("An exception was thrown. Message = " + ex.getMessage());
    }

    return inboxFolder;
  }

  /**
   * Checks whether a certain email matches the provided search criteria or not.
   *
   * @param email The email object to check.
   * @param criteria The search criteria to check against.
   * @return True if the email is a match, false otherwise.
   * @throws MessagingException Thrown in case an error happens when getting any details from the
   *     email.
   */
  protected boolean doesEmailMatchCriteria(
      @NonNull final Message email, @NonNull final SearchCriteria criteria)
      throws MessagingException {
    if (criteria.emailSubject() != null && !criteria.emailSubject().isEmpty()) {
      // check email subject
      boolean subjectMatch = email.getSubject().matches(criteria.emailSubject());
      // return if false, otherwise continue
      if (!subjectMatch) {
        return false;
      }
    }

    if (criteria.sentFrom() != null && !criteria.sentFrom().isEmpty()) {
      // check sentFrom
      boolean sentFromMatch =
          Arrays.stream(email.getFrom())
              .sequential()
              .anyMatch(
                  from ->
                      from.toString().contains(criteria.sentFrom().toLowerCase(Locale.ENGLISH)));
      // return if false, otherwise continue
      if (!sentFromMatch) {
        return false;
      }
    }

    if (criteria.sentTo() != null && !criteria.sentTo().isEmpty()) {
      // check sentTo
      // return if false, otherwise continue
      return Arrays.stream(email.getAllRecipients())
          .sequential()
          .anyMatch(to -> to.toString().contains(criteria.sentTo().toLowerCase(Locale.ENGLISH)));
    }

    // email matches the whole criteria
    return true;
  }

  /**
   * input stream with properties for email client, protocol, username, password, host example of
   * property file content: mail.store.protocol = imaps username = some@email.com password = xxxx
   * host = imap.gmail.com (for example).
   *
   * @param resourceStream an InputStream that represents a .properties files that includes all the
   *     above-mentioned properties.
   * @return mailProperties
   */
  private Properties setProperties(@NonNull final InputStream resourceStream) {
    try {
      props.load(resourceStream);
      this.userName = props.getProperty("username");
      this.password = props.getProperty("password");
      this.protocol = props.getProperty("mail.store.protocol");
      host = props.getProperty("host");
    } catch (FileNotFoundException e) {
      logger.error("FileNotFoundException on method setProperties\nMessage: {}", e.getMessage());
    } catch (IOException e1) {
      logger.error("IOException on method setProperties");
    }
    return props;
  }

  /**
   * Opens a specified mail folder with the given access rights.
   *
   * @param folderName The name of the mail folder to open (e.g., "INBOX").
   * @param accessMode The access mode to use when opening the folder. See {@link
   *     jakarta.mail.Folder} for available modes.
   * @return The opened {@link jakarta.mail.Folder} object, or null if an error occurred.
   * @throws MessagingException If an error occurs while opening the folder.
   */
  public Folder openSelectedFolderWithRights(@NonNull final String folderName, final int accessMode)
      throws MessagingException {
    Folder inbox = null;
    try {
      inbox = store.getFolder(folderName);
      inbox.open(accessMode); // Open the folder immediately after retrieving it

    } catch (MessagingException e1) {
      logger.error(e1);
      throw e1; // Re-throw the exception after logging it. This is crucial for proper error
      // handling.
    }
    return inbox;
  }

  /**
   * Initialize properties for current mailbox. Username, password, protocol, and host should be set
   * in advance.
   *
   * @return The initialized Store object.
   */
  private Store setMailCredentials() throws MessagingException {
    Session session = Session.getDefaultInstance(getProperties(), null);
    return getStore(session);
  }

  /**
   * Initialize properties for current mailbox example setMailCredentials(new
   * InputStream("/xxx/xxx/propertyFile"))
   *
   * @param resourceStream an InputStream that represents a .properties files that includes all the
   *     above-mentioned properties.
   * @return mail credential store
   */
  private Store setMailCredentials(@NonNull final InputStream resourceStream)
      throws MessagingException {
    if (props.isEmpty()) {
      setProperties(resourceStream);
    }
    Session session = Session.getDefaultInstance(props, null);
    return getStore(session);
  }

  private Store getStore(@NonNull final Session session) throws MessagingException {
    Store storeLocal = null;
    try {
      storeLocal = session.getStore(protocol);
    } catch (NoSuchProviderException e1) {
      logger.error(e1);
      throw e1;
    }
    try {
      storeLocal.connect(host, userName, password);
    } catch (MessagingException e1) {
      logger.error(e1);
      throw e1;
    }
    return storeLocal;
  }

  /**
   * Creates a Properties instance with pre-set username, password, protocol, and host. Username,
   * password, protocol, and host should be set in advance.
   *
   * @return A Properties instance containing the username, password, protocol, and host.
   */
  private Properties getProperties() {
    Properties properties = new Properties();

    properties.put("username", userName);
    properties.put("password", password);
    properties.put("mail.store.protocol", protocol);
    properties.put("host", host);

    return properties;
  }

  /**
   * Get array with unread emails.
   *
   * @param folder The email folder to check.
   * @return unread emails for selected folder
   */
  public Message[] getUnreadEmails(@NonNull final Folder folder) {
    Message[] emails = new Message[0];
    try {
      Flags seen = new Flags(Flags.Flag.SEEN);
      FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
      emails = folder.search(unseenFlagTerm);
    } catch (MessagingException e1) {
      logger.error(e1);
    }

    return emails;
  }

  /**
   * Delete all read emails form selected folder.
   *
   * @param folder The email folder to check.
   */
  public void deleteReadEmails(@NonNull final Folder folder) {
    Message[] emails;
    try {
      Flags seen = new Flags(Flags.Flag.SEEN);
      FlagTerm seenFlagTerm = new FlagTerm(seen, true);
      emails = folder.search(seenFlagTerm);
      folder.setFlags(emails, new Flags(Flags.Flag.DELETED), true);
    } catch (MessagingException e1) {
      logger.error(e1);
    }
  }

  /** Close the current connection store. */
  public void closeConnection() {
    try {
      store.close();
    } catch (MessagingException e) {
      logger.error("Error occurred when closing connection.");
      logger.error(e);
    }
  }
}
