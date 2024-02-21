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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;
import okhttp3.HttpUrl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** extends regular analytics entry with network info */
@SuppressWarnings({
  "PMD.GodClass",
  "checkstyle:CyclomaticComplexity",
  "checkstyle:MultipleStringLiterals"
})
public class NetworkEntry extends AnalyticsEntry {
  private static final Logger logger = LogManager.getLogger(NetworkEntry.class);

  public NetworkEntry(final Level level, final long timestamp, final String message) {
    super(level, timestamp, message);
  }

  public String getRequestId() {
    return Optional.ofNullable(this.getParams())
        .map(params -> params.get("requestId"))
        .map(JsonElement::getAsString)
        .orElse(null);
  }

  public JsonObject getRequest() {
    if (!this.doesMethodContain("requestWillBeSent")) {
      return null;
    }
    return Optional.ofNullable(this.getParams())
        .map(params -> params.getAsJsonObject("request"))
        .orElse(null);
  }

  public JsonObject getResponse() {
    if (!this.doesMethodContain("responseReceived")) {
      return null;
    }
    return Optional.ofNullable(this.getParams())
        .map(params -> params.getAsJsonObject("response"))
        .orElse(null);
  }

  public String getUrl() {
    if (this.doesMethodContain("requestWillBeSent")) {
      return Optional.ofNullable(getRequest())
          .map(req -> req.get("url"))
          .map(JsonElement::getAsString)
          .orElse(null);
    }
    if (this.doesMethodContain("responseReceived")) {
      return Optional.ofNullable(getResponse())
          .map(req -> req.get("url"))
          .map(JsonElement::getAsString)
          .orElse(null);
    }
    return null;
  }

  public JsonObject getHeaders() {
    if (this.doesMethodContain("requestWillBeSent")) {
      return Optional.ofNullable(getRequest())
          .map(req -> req.getAsJsonObject("headers"))
          .orElse(null);
    }
    if (this.doesMethodContain("responseReceived")) {
      return Optional.ofNullable(getResponse())
          .map(req -> req.getAsJsonObject("headers"))
          .orElse(null);
    }
    return null;
  }

  public JsonObject getInitiator() {
    return Optional.ofNullable(this.getParams())
        .map(params -> params.getAsJsonObject("initiator"))
        .orElse(null);
  }

  /**
   * returns true if is network request
   *
   * @return is network request
   */
  public boolean isRequest() {
    return getRequest() != null;
  }

  /**
   * true if response
   *
   * @return true if response
   */
  public boolean isResponse() {
    return getResponse() != null;
  }

  /**
   * returns true if initiator param has key
   *
   * @param key param key
   * @return true/false
   */
  public boolean hasInitiatorParam(final String key) {
    if (getInitiator() == null) {
      return false;
    }
    return getInitiator().has(key);
  }

  /**
   * returns true if initiator param is equal to value
   *
   * @param key param key
   * @param value param value
   * @return true/false
   */
  public boolean initiatorParamEquals(final String key, final String value) {
    if (getInitiator() == null || null == getInitiator().get(key)) {
      return false;
    }
    return getInitiator().get(key).getAsString().equals(value);
  }

  /**
   * returns true if initiator param has key
   *
   * @param key param key
   * @return true/false
   */
  public boolean hasRequestParam(final String key) {
    if (getRequest() == null) {
      return false;
    }
    return getRequest().has(key);
  }

  /**
   * returns true if request param is equal to value
   *
   * @param key param key
   * @param value param value
   * @return true/false
   */
  public boolean requestParamEquals(final String key, final String value) {
    if (getRequest() == null || !hasRequestParam(key)) {
      return false;
    }
    return getRequest().get(key).getAsString().equals(value);
  }

  /**
   * returns true if response param has key
   *
   * @param key param key
   * @return true/false
   */
  public boolean hasResponseParam(final String key) {
    if (getResponse() == null) {
      return false;
    }
    return getResponse().has(key);
  }

  /**
   * returns true if response param is equal to value
   *
   * @param key param key
   * @param value param value
   * @return true/false
   */
  public boolean responseParamEquals(final String key, final String value) {
    if (getResponse() == null || !hasResponseParam(key)) {
      return false;
    }
    return getResponse().get(key).getAsString().equals(value);
  }

  /**
   * returns true if it has header
   *
   * @param header header value
   * @return if matching
   */
  public boolean hasHeader(final String header) {
    if (getHeaders() == null) {
      return false;
    }
    return getHeaders().has(header);
  }

  /**
   * returns true if header contains
   *
   * @param header header key
   * @param value header value partial string
   * @return true/false
   */
  public boolean headerContains(final String header, final String value) {
    if (getHeaders() == null || !hasHeader(header)) {
      return false;
    }
    return getHeaders().get(header).getAsString().contains(value);
  }

  /**
   * return true if request id matches
   *
   * @param theRequestId the id
   * @return true/false
   */
  public boolean isRequestId(final String theRequestId) {
    if (null == this.getRequestId()) {
      return false;
    }
    return this.getRequestId().equals(theRequestId);
  }

  /**
   * Checks if request URL contains a fragment
   *
   * @param urlFragment URL fragment
   * @return does it contain
   */
  public boolean doesUrlContain(final String urlFragment) {
    if (getUrl() == null) {
      return false;
    }
    return getUrl().contains(urlFragment);
  }

  /**
   * Gets the names of query params
   *
   * @return map of query params
   */
  public Map<String, String> getQueryParameters() {
    if (getUrl() == null) {
      return new HashMap<>();
    }
    final var parsedUrl = HttpUrl.parse(getUrl());
    if (parsedUrl == null) {
      return new HashMap<>();
    }
    return parsedUrl.queryParameterNames().stream()
        .collect(
            Collectors.toMap(
                param -> param,
                param -> Optional.ofNullable(parsedUrl.queryParameter(param)).orElse("")));
  }

  /**
   * Checks if request has a query param
   *
   * @param key param name
   * @return true/false
   */
  public boolean hasQueryParam(final String key) {
    return getQueryParameters().containsKey(key);
  }

  /**
   * Gets a query param
   *
   * @param key param name
   * @return queryParameter string for the given key
   */
  public String getQueryParam(final String key) {
    if (hasQueryParam(key)) {
      return getQueryParameters().get(key);
    }
    logger.error(String.format("Couldn't retrieve the query parameter [%s].", key));
    return null;
  }

  /**
   * Checks if a query param name + value match
   *
   * @param key param name
   * @param value param value
   * @return whether there's a match
   */
  public boolean queryParamEquals(final String key, final String value) {
    return this.hasQueryParam(key) && this.getQueryParam(key).equals(value);
  }
}
