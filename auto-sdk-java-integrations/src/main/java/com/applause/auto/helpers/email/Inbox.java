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

import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.context.FrameworkContext;
import com.applause.auto.context.IContextConnector;
import com.applause.auto.framework.SdkHelper;
import com.applause.auto.helpers.ApplauseConfigHelper;
import com.applause.auto.util.autoapi.AutoApi;
import com.applause.auto.util.autoapi.AutoApiClient;
import com.applause.auto.util.autoapi.EmailClearInboxRequest;
import com.applause.auto.util.autoapi.EmailRetrievalRequest;
import com.applause.auto.util.autoapi.EmailRetrievalResponse;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.StopWatch;
import retrofit2.Response;

/**
 * Represents a single, Applause-generated email address. Exposes methods to control the remote
 * inbox associated with that address.
 */
@Getter
@AllArgsConstructor
@Log4j2
public class Inbox {
  private static final AutoApi autoApi =
      AutoApiClient.getClient(
          ApplauseEnvironmentConfigurationManager.INSTANCE.get().autoApiUrl(),
          ApplauseEnvironmentConfigurationManager.INSTANCE.get().apiKey(),
          ApplauseConfigHelper.getHttpProxy());
  private final FrameworkContext context;
  private final String emailAddress;

  /**
   * Checks the remote inbox associated with this address to receive an email.
   *
   * @return an Optional containing the email object, if present
   */
  public Optional<Email> getEmail() {
    final var resultId =
        Optional.of(context.getConnector()).map(IContextConnector::getResultId).orElse(null);
    if (resultId == null) {
      throw new RuntimeException("Could not get an email - no result id tied to context.");
    }
    URL emlFileUrl;
    try {
      EmailRetrievalRequest request =
          new EmailRetrievalRequest().setEmailAddress(this.emailAddress).setTestResultId(resultId);
      Response<EmailRetrievalResponse> response = autoApi.awaitEmail(request).get();
      // If the email was not found, return an empty optional
      if (response.code() == 404) {
        return Optional.empty();
      }
      // For other error statuses, throw an exception
      if (!response.isSuccessful()) {
        throw new RuntimeException(
            "Failed to retrieve email - received status code: " + response.code());
      }
      // If we got a successful status with no response body, throw an exception
      if (response.body() == null) {
        throw new RuntimeException(
            "AutoApi returned a null response when attempting to retrieve the .eml file URL.");
      }
      emlFileUrl = response.body().getEmailAssetUrl();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(
          "Interrupted while retrieving URL pointing at .eml file from AutoApi.", e);
    }

    return Optional.of(fetchEmailAsset(emlFileUrl));
  }

  /**
   * Waits for the remote inbox associated with this address to receive an email. This is a blocking
   * operation.
   *
   * @param timeout The minimum amount of time to wait for the email
   * @return an Email object representing the received email
   * @throws TimeoutException If we timed out while waiting for the email
   */
  public Email waitForEmail(final Duration timeout) throws TimeoutException {
    URL emlFileUrl;
    try {
      EmailRetrievalRequest request =
          new EmailRetrievalRequest()
              .setEmailAddress(this.emailAddress)
              .setTestResultId(SdkHelper.getTestResultId());

      // Note: the call we make in auto-api is a blocking call that has a configurable timeout.
      // Since the SDK doesn't know the timeout, we just keep track of the time we are waiting,
      // so we will guarantee that we wait at least the provided timeout.
      final var stopWatch = StopWatch.createStarted();
      Response<EmailRetrievalResponse> response = null;

      // Poll the email fetch endpoint until we get a successful response or
      // reach the timeout.
      while (stopWatch.getTime(TimeUnit.SECONDS) < timeout.getSeconds()) {
        response = autoApi.awaitEmail(request).get();
        if (response.isSuccessful()) {
          stopWatch.stop();
          break;
        }
      }

      // If the email was not found during the timeout, throw a checked timeout exception so the
      // caller can handle it
      if (response == null || response.code() == 404) {
        log.debug("No email found a this time");
        throw new TimeoutException("Timed out waiting for an email.");
      }
      // For other error statuses, throw an unchecked exception
      if (!response.isSuccessful()) {
        throw new RuntimeException(
            "Failed to retrieve email - Email Server returned status code: " + response.code());
      }
      // If we got a successful status with no response body, throw an exception
      if (response.body() == null) {
        throw new RuntimeException(
            "AutoApi returned a null response when attempting to retrieve the .eml file URL.");
      }
      emlFileUrl = response.body().getEmailAssetUrl();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(
          "Interrupted while retrieving URL pointing at .eml file from AutoApi.", e);
    }

    return fetchEmailAsset(emlFileUrl);
  }

  /** Clears the remote inbox associated with this address. */
  public void clear() {
    EmailClearInboxRequest request = new EmailClearInboxRequest(this.emailAddress);
    autoApi.clearInbox(request);
  }

  private Email fetchEmailAsset(final URL emlFileUrl) {
    Message msg;
    try (InputStream stream = emlFileUrl.openStream()) {
      msg = new MimeMessage(null, stream);
    } catch (IOException e) {
      throw new RuntimeException("Unable to download .eml file.", e);
    } catch (MessagingException e) {
      throw new RuntimeException("Unable to parse .eml file.", e);
    }

    return new Email(msg);
  }
}
