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
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Provides some common methods between most email clients. */
public abstract class CommonMailClient {
  private String host;
  private Properties props = new Properties();
  private Store store;
  private String protocol;
  private String userName;
  private String password;
  private static final Logger logger = LogManager.getLogger(CommonMailClient.class);

  /**
   * Default constructor with params Example: username = some@email.com password = xxxx protocol =
   * imaps host = imap.gmail.com
   */
  public CommonMailClient(String userName, String password, Protocol protocol, String host) {
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
  public String extractWithRegexFromString(String text, String regex) {
    if (text.isEmpty()) {
      logger.info("Text is empty");
      return null;
    }
    logger.info("Extracting with regex = [" + regex + "...");
    Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
    Matcher matcher = pattern.matcher(text);

    if (matcher.find()) {
      logger.info("Extracted text is:: " + matcher.group(1).trim());
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
   */
  public CommonMailClient(InputStream resourceFile) {
    store = setMailCredentials(resourceFile);
  }

  /**
   * Initialize properties for current mailbox example setMailCredentials(new
   * InputStream("/xxx/xxx/propertyFile"))
   *
   * @param resourceStream an InputStream that represents a .properties files that includes all the
   *     above-mentioned properties.
   * @return
   */
  private Store setMailCredentials(InputStream resourceStream) {
    if (props.isEmpty()) {
      setProperties(resourceStream);
    }
    Session session = Session.getDefaultInstance(props, null);
    return getStore(session);
  }

  /**
   * Deletes all the read emails from the mentioned folder of the email account.
   *
   * @param emailFolder The email folder to use.
   */
  public void emptyAllReadEmailsFromInbox(Folder emailFolder) {
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
  protected boolean doesEmailMatchCriteria(Message email, SearchCriteria criteria)
      throws MessagingException {
    if (criteria.getEmailSubject() != null && !criteria.getEmailSubject().isEmpty()) {
      // check email subject
      boolean subjectMatch = email.getSubject().matches(criteria.getEmailSubject());
      // return if false, otherwise continue
      if (!subjectMatch) {
        return false;
      }
    }

    if (criteria.getSentFrom() != null && !criteria.getSentFrom().isEmpty()) {
      // check sentFrom
      boolean sentFromMatch =
          Arrays.stream(email.getFrom())
              .sequential()
              .anyMatch(from -> from.toString().contains(criteria.getSentFrom().toLowerCase()));
      // return if false, otherwise continue
      if (!sentFromMatch) {
        return false;
      }
    }

    if (criteria.getSentTo() != null && !criteria.getSentTo().isEmpty()) {
      // check sentTo
      // return if false, otherwise continue
      return Arrays.stream(email.getAllRecipients())
          .sequential()
          .anyMatch(to -> to.toString().contains(criteria.getSentTo().toLowerCase()));
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
  private Properties setProperties(InputStream resourceStream) {
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
   * @param folderName mail folder name example INBOX
   * @param accessMode check Folder.to get which mode exists
   * @return required Folder
   */
  public Folder openSelectedFolderWithRights(String folderName, int accessMode) {
    Folder inbox = null;
    try {
      inbox = store.getFolder(folderName);
    } catch (MessagingException e1) {
      e1.printStackTrace();
    }
    try {
      inbox.open(accessMode);
    } catch (MessagingException e1) {
      e1.printStackTrace();
    }
    return inbox;
  }

  /**
   * Initialize properties for current mailbox. Username, password, protocol, and host should be set
   * in advance
   */
  private Store setMailCredentials() {
    Session session = Session.getDefaultInstance(getProperties(), null);
    return getStore(session);
  }

  private Store getStore(Session session) {
    Store store = null;
    try {
      store = session.getStore(protocol);
    } catch (NoSuchProviderException e1) {
      e1.printStackTrace();
    }
    try {
      store.connect(host, userName, password);
    } catch (MessagingException e1) {
      e1.printStackTrace();
    }
    return store;
  }

  /**
   * Creating properties instance Username, password, protocol, and host should be set in advance
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
  public Message[] getUnreadEmails(Folder folder) {
    Message[] emails = new Message[0];
    try {
      Flags seen = new Flags(Flags.Flag.SEEN);
      FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
      emails = folder.search(unseenFlagTerm);
    } catch (MessagingException e1) {
      e1.printStackTrace();
    }

    return emails;
  }

  /**
   * Delete all read emails form selected folder.
   *
   * @param folder The email folder to check.
   */
  public void deleteReadEmails(Folder folder) {
    Message[] emails;
    try {
      Flags seen = new Flags(Flags.Flag.SEEN);
      FlagTerm seenFlagTerm = new FlagTerm(seen, true);
      emails = folder.search(seenFlagTerm);
      folder.setFlags(emails, new Flags(Flags.Flag.DELETED), true);
    } catch (MessagingException e1) {
      e1.printStackTrace();
    }
  }

  /** Close the current connection store. */
  public void closeConnection() {
    try {
      store.close();
    } catch (MessagingException e) {
      logger.error("Error occurred when closing connection.");
      e.printStackTrace();
    }
  }
}
