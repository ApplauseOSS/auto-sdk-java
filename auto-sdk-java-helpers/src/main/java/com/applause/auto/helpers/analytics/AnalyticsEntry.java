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
package com.applause.auto.helpers.analytics;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Optional;
import java.util.logging.Level;
import lombok.Getter;
import org.openqa.selenium.logging.LogEntry;

/**
 * Wrapper around Selenium's LogEntry class, adding easy access to relevant information needed to
 * check an Analytics call, such as getting the method (aka the type of network activity that
 * happened), the webview (to get the GUID of this browser session), and the params (containing a
 * plethora of information regarding the actual underlying HTTP call made).
 */
@Getter
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class AnalyticsEntry extends LogEntry {
  private final JsonObject parsedMessage;

  /**
   * Constructor for AnalyticsEntry.
   *
   * @param level The log level.
   * @param timestamp The timestamp of the log entry.
   * @param message The log message.
   */
  public AnalyticsEntry(final Level level, final long timestamp, final String message) {
    super(level, timestamp, message);
    this.parsedMessage = new Gson().fromJson(message, JsonObject.class);
  }

  /**
   * Gets the webview GUID.
   *
   * @return The webview GUID, or null if not found.
   */
  public String getWebView() {
    return Optional.ofNullable(parsedMessage)
        .map(body -> body.get("webview"))
        .map(JsonElement::getAsString)
        .orElse(null);
  }

  /**
   * Gets the method of the analytics call.
   *
   * @return The method, or null if not found.
   */
  public String getMethod() {
    return Optional.ofNullable(parsedMessage)
        .map(body -> body.getAsJsonObject("message"))
        .map(messageElement -> messageElement.get("method"))
        .map(JsonElement::getAsString)
        .orElse(null);
  }

  /**
   * Gets the parameters of the analytics call.
   *
   * @return The parameters as a JsonObject, or null if not found.
   */
  public JsonObject getParams() {
    return Optional.ofNullable(parsedMessage)
        .map(body -> body.getAsJsonObject("message"))
        .map(messageElement -> messageElement.getAsJsonObject("params"))
        .orElse(null);
  }

  /**
   * Checks to see if the analytics entry is at a given level
   *
   * @param level The level to check against
   * @return True if the entry is at the given level
   */
  public boolean isLevel(final Level level) {
    return getLevel().equals(level);
  }

  /**
   * Checks to see if the given analytics entry is timestamped before the provided time
   *
   * @param timestamp The timestamp to compare to
   * @return True if the entry is timestamped before the provided time
   */
  public boolean isTimestampBefore(final long timestamp) {
    return this.getTimestamp() < timestamp;
  }

  /**
   * Checks to see if the given analytics entry is timestamped after the provided time
   *
   * @param timestamp The timestamp to compare to
   * @return True if the entry is timestamped after the provided time
   */
  public boolean isTimestampAfter(final long timestamp) {
    return this.getTimestamp() >= timestamp;
  }

  /**
   * returns whether this webview matches
   *
   * @param theWebView string to check
   * @return whether matches
   */
  public boolean isWebView(final String theWebView) {
    if (null != getWebView() && null != theWebView) {
      return getWebView().equals(theWebView);
    } else {
      return false;
    }
  }

  /**
   * Checks if method contains partial param
   *
   * @param partialMethod partial string to match against method
   * @return whether match
   */
  public boolean doesMethodContain(final String partialMethod) {
    if (null != getMethod() && null != partialMethod) {
      return getMethod().contains(partialMethod);
    } else {
      return false;
    }
  }

  /**
   * Checks for a full method name match
   *
   * @param theMethod method name
   * @return whether matched
   */
  public boolean isMethod(final String theMethod) {
    if (null != getMethod() && null != theMethod) {
      return getMethod().equals(theMethod);
    } else {
      return false;
    }
  }

  /**
   * Checks to see if the input is a key as part of the `params` object.
   *
   * @param key text to check for (case-sensitive)
   * @return true if the key is an exact match to a *top level* key in the params object Note: this
   *     does not recurse down the params tree, it simply checks the top level keys!
   */
  public boolean hasParam(final String key) {
    if (null != getParams() && null != key) {
      return getParams().has(key);
    } else {
      return false;
    }
  }

  /**
   * Checks to see if a parameter is an exact match to the value.
   *
   * @param key text to check for (case-sensitive)
   * @param value expected value (case-sensitive)
   * @return true if the key and value are both present and an exact match Note: similar to
   *     hasParam(), this ony searches top level keys!
   */
  public boolean paramEquals(final String key, final String value) {
    // calling hasParam ensures null safety
    if (hasParam(key)) {
      return getParams().get(key).toString().replace("\"", "").equals(value);
    } else {
      return false;
    }
  }

  /**
   * A convenience method to search the entire log entry sent across to us as a string
   *
   * @param text text to check for (case-sensitive)
   * @return true if this text is anywhere in the log entry
   */
  public boolean containsText(final String text) {
    if (null != text && null != getMessage()) {
      return this.toString().contains(text);
    } else {
      return false;
    }
  }
}
