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
package com.applause.auto.helpers;

import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.context.FrameworkContext;
import com.applause.auto.helpers.email.Inbox;
import com.applause.auto.util.autoapi.AutoApi;
import com.applause.auto.util.autoapi.AutoApiClient;
import com.applause.auto.util.autoapi.EmailAddressResponse;
import java.net.Proxy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

/** Helper class to provision addresses for email testing. Dispenses Inbox objects. */
public final class EmailHelper {
  private static final Proxy httpProxy = ApplauseConfigHelper.getHttpProxy();
  private static final AutoApi autoApi =
      AutoApiClient.getClient(
          ApplauseEnvironmentConfigurationManager.INSTANCE.get().autoApiUrl(),
          ApplauseEnvironmentConfigurationManager.INSTANCE.get().apiKey(),
          httpProxy);

  // Private constructor for utility class
  private EmailHelper() {}

  /**
   * Gets an Inbox object for the email address (prefix).
   *
   * @param context The context
   * @param emailPrefix a String prefix to be placed at the front of the email address
   * @return an Inbox object corresponding to the email address
   */
  public static Inbox getInbox(final FrameworkContext context, final String emailPrefix) {
    if (!ApplauseEnvironmentConfigurationManager.INSTANCE.get().reportingEnabled()) {
      throw new RuntimeException("Cannot retrieve Inbox when `reportingEnabled==false` .");
    }
    EmailAddressResponse response;
    try {
      response = autoApi.getEmailAddress(emailPrefix).get().body();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("The request to provision an email address was interrupted.", e);
    }
    if (response == null || response.getEmailAddress() == null) {
      throw new RuntimeException(
          "AutoApi returned a null response when provisioning the email address.");
    }
    return new Inbox(context, response.getEmailAddress());
  }

  /**
   * Gets an Inbox object for a new email address using the current date and time.
   *
   * @param context The context
   * @return an Inbox object corresponding to the new email address
   */
  public static Inbox getInbox(final FrameworkContext context) {
    return getInbox(
        context, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")));
  }
}
