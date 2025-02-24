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
package com.applause.auto.framework.selenium.apppush;

import com.applause.auto.config.AppPushConfig;
import com.applause.auto.config.AppPushConfigImpl;
import com.applause.auto.framework.selenium.apppush.providers.BrowserstackPusher;
import com.applause.auto.framework.selenium.apppush.providers.SauceLabsPusher;
import com.applause.auto.util.seleniumproxy.AppPushProxyDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.HttpMethod;
import lombok.NonNull;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

/** Basic Application Push functional that does not require Applause Services. */
@SuppressWarnings("PMD.GodClass")
public final class AppPushHelper {
  private static final Logger logger = LogManager.getLogger(AppPushHelper.class);
  private static final long MAX_PUSH_WRITE_MINUTES = 10;
  private static final long MAX_PUSH_READ_MINUTES = 10;
  private static final RetryPolicy<Object> pushRetry =
      RetryPolicy.builder()
          .handle(AppPushRetryableException.class)
          .withDelay(Duration.ofMillis(30_000))
          .withMaxRetries(3)
          .build();

  private static final OkHttpClient client =
      new OkHttpClient.Builder()
          .connectTimeout(MAX_PUSH_WRITE_MINUTES / 2, TimeUnit.MINUTES)
          .writeTimeout(MAX_PUSH_WRITE_MINUTES, TimeUnit.MINUTES)
          .readTimeout(MAX_PUSH_READ_MINUTES, TimeUnit.MINUTES)
          .build();

  private AppPushHelper() {
    // Just static methods
  }

  /**
   * find the best class that implements that AppPushTarget interface
   *
   * @param appPushCfg current configuration
   * @return the best implementation, if available. throws a RuntimeException if not available.
   */
  public static AppPushTarget getTarget(final AppPushConfig appPushCfg) {
    String provider = appPushCfg.appPushProvider();
    String providerClass = appPushCfg.appPushClass();
    if (StringUtils.isBlank(providerClass)) {
      providerClass = pushProviderToPushAppClass(provider);
      if (StringUtils.isBlank(providerClass)) {
        throw new RuntimeException(
            "Application Push.  Provider="
                + provider
                + ", but no appPushClass provided and lookup by provider failed");
      }
    }
    Object rawImpl;

    try {
      rawImpl = Class.forName(providerClass).getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException e) {
      throw new RuntimeException("Unable to load appPushClass='" + providerClass + "'", e);
    }
    if (!(rawImpl instanceof AppPushTarget)) {
      throw new RuntimeException(
          "appPushClass='" + providerClass + "' does not implement the AppPushTarget interface");
    }
    return (AppPushTarget) rawImpl;
  }

  /**
   * Main entry point into the operation
   *
   * @param sdkInput The input from the command-line/env
   * @return Optional.empty if the feature is not active. Otherwise, the value that should be placed
   *     in the Appium "app" field
   * @throws RuntimeException on error
   */
  public static Optional<String> pushApplication(@NonNull final AppPushConfig sdkInput) {
    // Step 1:  Get the provider
    final String provider = sdkInput.appPushProvider();
    if (StringUtils.isBlank(provider)) {
      return Optional.empty(); // Feature is not active
    }
    AppPushTarget target = getTarget(sdkInput);
    return pushApplication(sdkInput, target);
  }

  /**
   * Push the application and parse the results
   *
   * @param sdkInput The configuration input by the user
   * @param targetProvider An instance of the AppPushTarget interface
   * @return The application string to use
   */
  public static Optional<String> pushApplication(
      @NonNull final AppPushConfig sdkInput, @NonNull final AppPushTarget targetProvider) {
    if (!sdkInput.isComplete()) {
      // By this point, we need a complete configuration.  Otherwise, we return an error
      throw new RuntimeException(
          "Unable to push application to provide='"
              + sdkInput.appPushProvider()
              + "', missing parameters: "
              + String.join(", ", sdkInput.missingParameters()));
    }
    // This call will throw an exception on error with the source input
    validateSource(sdkInput.appPushSource());
    // We have a complete configuration.
    final String src = sdkInput.appPushSource();
    if (src.startsWith("http://") || src.startsWith("https://")) {
      targetProvider.init(src, sdkInput);
    } else {
      targetProvider.init(new File(src), sdkInput);
    }
    final RequestBody httpBody = targetProvider.getRequestBody();
    return pushDirect(sdkInput, targetProvider, httpBody);
  }

  static Optional<String> pushDirect(
      @NonNull final AppPushConfig finalConfig,
      @NonNull final AppPushTarget targetImpl,
      @NonNull final RequestBody httpBody) {
    String targetUrl = targetImpl.getTargetUrl();
    logger.info("Pushing app DIRECTLY to '" + targetUrl + "'");
    Request.Builder bldr =
        new Request.Builder()
            .url(targetUrl)
            .addHeader(
                HttpHeaders.AUTHORIZATION,
                Credentials.basic(finalConfig.appPushUser(), finalConfig.appPushPassword()));
    final String httpMethod = targetImpl.httpMethod();
    if (HttpMethod.PUT.equalsIgnoreCase(httpMethod)) {
      bldr.put(httpBody);
    } else if (HttpMethod.POST.equalsIgnoreCase(httpMethod)) {
      bldr.post(httpBody);
    } else {
      // Right now, we do puts and posts
      throw new RuntimeException(
          "Unsupported HTTP method for provider="
              + targetImpl.getClass().getCanonicalName()
              + " = "
              + httpMethod);
    }
    return Failsafe.with(pushRetry)
        .onFailure(
            failure -> logger.error("App Push Failed, OUT OF RETRIES", failure.getException()))
        .get(() -> tryOnce(bldr, finalConfig, targetUrl, targetImpl));
  }

  static Optional<String> tryOnce(
      @NonNull final Request.Builder bldr,
      @NonNull final AppPushConfig finalConfig,
      @NonNull final String targetUrl,
      @NonNull final AppPushTarget targetImpl) {
    Request request = bldr.build();
    Call call = client.newCall(request);
    Response response;
    try {
      response = call.execute();
    } catch (IOException e) {
      throw new RuntimeException("Failed to execute", e);
    }
    verifyHttp200OrThrow(response, finalConfig, targetUrl);
    return Optional.of(targetImpl.getApplicationValue(response));
  }

  static String proxyPushData2JsonStr(@NonNull final AppPushProxyDto data) {
    // Use Jackson (Not GSON) for encoding.  We tried Gson.  We ran into problems
    // When the data included '=', Gson encoded the '=' as unicode.  Jackson is the safer
    // serializer.
    try {
      return new ObjectMapper().writeValueAsString(data);
    } catch (JsonProcessingException jpe) {
      throw new RuntimeException("Unable to encode data for proxy", jpe);
    }
  }

  static String base64EncodePushData(@NonNull final AppPushProxyDto data) {
    final String asStr = proxyPushData2JsonStr(data);
    final byte[] asBytes = asStr.getBytes(StandardCharsets.UTF_8);
    return Base64.getUrlEncoder().encodeToString(asBytes);
  }

  /**
   * For App pushing we should always get an HTTP 200 or 204. Check and throw an exception if we
   * don't
   *
   * @param response The http response received
   * @param cfg the configuration. Used to build an error message
   * @param targetUrl used to build an error message
   */
  public static void verifyHttp200OrThrow(
      @NonNull final Response response,
      @NonNull final AppPushConfig cfg,
      @NonNull final String targetUrl) {
    if (!response.isSuccessful()) {
      throwCorrectException(response, cfg.appPushSource(), targetUrl);
    }
    final int code = response.code();
    // Status 201 (Created) is used by SauceLabs to signal that the upload was successful
    if (code == 200 || code == 201) {
      return;
    }
    if (code == 204) {
      logger.info("HTTP 204 received in response to HTTP Post.  May not be able to parse output");
      return;
    }
    throwCorrectException(response, cfg.appPushSource(), targetUrl);
  }

  static void throwCorrectException(
      @NonNull final Response okHttpRsp,
      @NonNull final String source,
      @NonNull final String targetUrl) {
    // Sometimes shouldn't be retried
    final int httpRspCode = okHttpRsp.code();
    if (httpRspCode == 401 || httpRspCode == 400 || httpRspCode == 403 || httpRspCode == 422) {
      throw new RuntimeException(createErrMsg(okHttpRsp, source, targetUrl));
    }
    throw new AppPushRetryableException(createErrMsg(okHttpRsp, source, targetUrl));
  }

  static String createErrMsg(
      @NonNull final Response okHttpRsp,
      @NonNull final String source,
      @NonNull final String targetUrl) {
    // If there's a body, we want to ready it, so we can include is in the error msg
    final String isSuccess = okHttpRsp.isSuccessful() ? "" : "(success=false)";
    ResponseBody body = okHttpRsp.body();
    String rawBody = null;
    if (body != null) {
      try {
        rawBody = body.string();
        // Check the subtype parsed out by
      } catch (IOException e) {
        rawBody = "Exception while parsing body: " + e.getMessage();
      }
    }
    if (Strings.isEmpty(rawBody)) {
      return String.format(
          "Unable to push application '%s' to '%s'.  status=%d%s, msg=''",
          source, targetUrl, okHttpRsp.code(), isSuccess);
    }
    if (rawBody.chars().filter(c -> c == '\n').count() < 1) {
      return String.format(
          "Unable to push application '%s' to '%s'.  status=%d%s, msg='%s'",
          source, targetUrl, okHttpRsp.code(), isSuccess, rawBody);
    }
    return String.format(
        "Unable to push application '%s' to '%s'.  status=%d%s, msg=%n     %s%n",
        source, targetUrl, okHttpRsp.code(), isSuccess, rawBody);
  }

  /**
   * Map a provider to a canonical class name of a class that can support the push of an application
   * to that provide
   *
   * @param pushProvider the name of the provider
   * @return The canonical class name of a class that implements AppPushTarget
   */
  public static String pushProviderToPushAppClass(@NonNull final String pushProvider) {
    // Eventually we should externalize this
    if (pushProvider.toUpperCase(Locale.getDefault()).contains("BROWSERSTACK")) {
      return BrowserstackPusher.class.getCanonicalName();
    }
    if (pushProvider.toUpperCase(Locale.getDefault()).contains("SAUCELABS")) {
      return SauceLabsPusher.class.getCanonicalName();
    }
    throw new RuntimeException("Unable to map " + pushProvider + " to a concrete class");
  }

  /**
   * Check that the source parameter is accessible
   *
   * @param pushSrc The source parameter
   */
  public static void validateSource(@NonNull final String pushSrc) {
    if (pushSrc.startsWith("http://") || pushSrc.startsWith("https://")) {
      // Looks like a URL.  parse it and make sure
      try {
        new URI(pushSrc).toURL();
        return;
      } catch (MalformedURLException | URISyntaxException e) {
        throw new RuntimeException("Malformed URL: " + e.getMessage(), e);
      }
    }
    // Otherwise, it's a file.
    File srcFile = new File(pushSrc);
    if (!srcFile.exists()) {
      throw new RuntimeException(
          String.format(
              "file %s='%s' does not exit", AppPushConfigImpl.PARAM_APP_PUSH_SOURCE, pushSrc));
    }
    if (!srcFile.canRead()) {
      throw new RuntimeException(
          String.format(
              "file %s='%s' is not readable", AppPushConfigImpl.PARAM_APP_PUSH_SOURCE, pushSrc));
    }
  }
}
