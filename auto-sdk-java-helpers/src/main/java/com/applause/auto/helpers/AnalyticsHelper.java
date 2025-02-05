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

import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.context.IPageObjectExtension;
import com.applause.auto.helpers.analytics.AnalyticsCall;
import com.applause.auto.helpers.analytics.AnalyticsEntry;
import com.applause.auto.helpers.analytics.AnalyticsInterceptor;
import com.applause.auto.helpers.analytics.NetworkEntry;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.remote.RemoteWebDriver;

/** Helper class for handling analytics entries provided by a driver */
@SuppressWarnings({
  "PMD.NPathComplexity",
  "PMD.CyclomaticComplexity",
  "PMD.GodClass",
  "checkstyle:AbbreviationAsWordInName"
})
public class AnalyticsHelper implements IPageObjectExtension {
  private static final Logger logger = LogManager.getLogger(AnalyticsHelper.class);
  private final List<AnalyticsEntry> logs = new ArrayList<>();
  private List<NetworkEntry> preMethodLogs = new ArrayList<>();
  private final IPageObjectContext context;
  private final WebDriver webDriver;
  private final Boolean enabled;

  /**
   * Constructs the Analytics Helper
   *
   * @param context The context to use
   */
  public AnalyticsHelper(final IPageObjectContext context) {
    this.context = context;
    this.webDriver = this.context.getDriver();
    if (EnvironmentConfigurationManager.INSTANCE.get().performanceLogging()) {
      if (((RemoteWebDriver) webDriver)
          .getCapabilities()
          .getBrowserName()
          .toLowerCase(Locale.ENGLISH)
          .contains("chrome")) {
        logger.debug(
            "Property [performanceLogging] equals [true] and capability [browserName] equals [chrome]. "
                + "Initializing AnalyticsHelper.");
        enabled = true;
      } else {
        logger.error(
            String.format(
                "Property [performanceLogging] equals [true], but capability [browserName] was "
                    + "[%s]. Expected [chrome].",
                ((RemoteWebDriver) this.webDriver).getCapabilities().getBrowserName()));
        enabled = false;
      }
    } else {
      logger.warn(
          "Property [performanceLogging] not present or equals [false]. Analytics data will not be"
              + " captured; any analytics tests may fail as a result.");
      enabled = false;
    }
  }

  /** Flushes existing performance logs, both from Selenium and from the "logs" buffer. */
  public void flushLogs() {
    getAllLogs();
    logs.clear();
    logger.debug("Cleared performance logs.");
  }

  /**
   * Retrieves all performance logs since the last flush. Each entry is parsed into either the
   * AnalyticsEntry wrapping class or into the more specific extension NetworkEntry, as applicable.
   *
   * @return the performance logs as a list of AnalyticsEntry.
   */
  public List<AnalyticsEntry> getAllLogs() {
    if (!enabled) {
      return new ArrayList<>();
    }

    // Log entries are retrieved from Selenium.
    List<LogEntry> logEntries = this.webDriver.manage().logs().get("performance").getAll();

    // However, Selenium's logs are cleared upon access, so the logs are buffered into this class's
    // "logs" field.
    for (LogEntry entry : logEntries) {
      if (new Gson()
          .fromJson(entry.getMessage(), JsonObject.class)
          .getAsJsonObject("message")
          .get("method")
          .getAsString()
          .startsWith("Network")) {
        logs.add(new NetworkEntry(entry.getLevel(), entry.getTimestamp(), entry.getMessage()));
      } else {
        logs.add(new AnalyticsEntry(entry.getLevel(), entry.getTimestamp(), entry.getMessage()));
      }
    }

    return logs;
  }

  /**
   * Filters a list of analytics entries down to those that use Network methods.
   *
   * @param logList - The list of entries to filter.
   * @param <T> - AnalyticsEntry or NetworkEntry.
   * @return the filtered list as a {@code List<NetworkEntry> }.
   */
  public <T extends AnalyticsEntry> List<NetworkEntry> filterNetworkLogs(final List<T> logList) {
    List<NetworkEntry> networkList = new ArrayList<>();
    for (AnalyticsEntry entry : filterByMethod(logList, "Network")) {
      if (entry instanceof NetworkEntry) {
        networkList.add((NetworkEntry) entry);
      }
    }
    return networkList;
  }

  /**
   * Gets all available logs and filters them down to those that use Network methods.
   *
   * @return the filtered list as a {@code List<NetworkEntry> }.
   */
  public List<NetworkEntry> getAllNetworkLogs() {
    return filterNetworkLogs(getAllLogs());
  }

  /**
   * Filters a list of analytics entries down to those that use Page methods.
   *
   * @param logList - The list of entries to filter.
   * @return the filtered list as a {@code List<AnalyticsEntry> }.
   */
  public List<AnalyticsEntry> filterPageLogs(final List<AnalyticsEntry> logList) {
    return filterByMethod(logList, "Page");
  }

  /**
   * Gets all available logs and filters them down to those that use Page methods.
   *
   * @return the filtered list as a {@code List<AnalyticsEntry> }.
   */
  public List<AnalyticsEntry> getAllPageLogs() {
    return filterPageLogs(getAllLogs());
  }

  /**
   * Filters an arbitrary list of log entries down to requests and responses from
   * google-analytics.com.
   *
   * @param logList list of network entries to filter
   * @param <T> entry type
   * @return the filtered list as a {@code List<NetworkEntry> }.
   */
  public <T extends AnalyticsEntry> List<NetworkEntry> filterGoogleAnalyticsLogs(
      final List<T> logList) {
    return filterByURL(filterNetworkLogs(logList), "google-analytics.com");
  }

  /**
   * Gets all available logs and filters them down to requests and responses from
   * google-analytics.com.
   *
   * @return the filtered list as a {@code List<NetworkEntry> }.
   */
  public List<NetworkEntry> getAllGoogleAnalyticsLogs() {
    return filterGoogleAnalyticsLogs(getAllLogs());
  }

  /**
   * Filters an arbitrary list of log entries down to requests and responses from omtrdc.net.
   *
   * @param logList list of entries to filter
   * @param <T> entry type
   * @return the filtered list as a {@code List<NetworkEntry> }.
   */
  public <T extends AnalyticsEntry> List<NetworkEntry> filterOmnitureLogs(final List<T> logList) {
    return filterByURL(filterNetworkLogs(logList), "omtrdc.net");
  }

  /**
   * Gets all available logs and filters them down to requests and responses from omtrdc.net.
   *
   * @return the filtered list as a {@code List<NetworkEntry> } .
   */
  public List<NetworkEntry> getAllOmnitureLogs() {
    return filterOmnitureLogs(getAllLogs());
  }

  /**
   * Given a request NetworkEntry, waits for a response and returns it. Uses the timeout and polling
   * interval provided in the Framework context.
   *
   * @param request the request to filter
   * @return the response as a NetworkEntry.
   */
  public NetworkEntry waitForResponse(final NetworkEntry request) {
    final var wait = context.getWait();
    wait.until(ignored -> this.getResponse(request) != null);
    return this.getResponse(request);
  }

  /**
   * Given a request NetworkEntry, waits for a response and returns it.
   *
   * @param request the request to filter
   * @param timeout The maximum time to wait
   * @param pollingInterval the polling interval
   * @return the response as a NetworkEntry.
   */
  public NetworkEntry waitForResponse(
      final NetworkEntry request, final Duration timeout, final Duration pollingInterval) {
    final var wait = context.getWait(timeout, pollingInterval);
    wait.until(ignored -> this.getResponse(request) != null);
    return this.getResponse(request);
  }

  /**
   * gets the network entry?
   *
   * @param entry The network entry to get the request for
   * @return network entry
   */
  public NetworkEntry getRequest(final NetworkEntry entry) {
    if (entry.isRequest()) {
      return entry;
    }
    List<NetworkEntry> entries = getAllNetworkLogs();
    entries = filterRequests(filterByRequestId(entries, entry.getRequestId()));
    if (null == entries || entries.isEmpty()) {
      return null;
    }
    return entries.get(0);
  }

  /**
   * The network response
   *
   * @param entry The network entry to get the response for
   * @return network response
   */
  public NetworkEntry getResponse(final NetworkEntry entry) {
    if (entry.isResponse()) {
      return entry;
    }
    List<NetworkEntry> entries = getAllNetworkLogs();
    entries = filterResponses(filterByRequestId(entries, entry.getRequestId()));
    if (null == entries || entries.isEmpty()) {
      return null;
    }
    return entries.get(0);
  }

  /**
   * Filters an arbitrary list of log entries to those earlier than a particular timestamp.
   *
   * @param entries the list of entries to filter
   * @param timestamp the timestamp before which returned log entries should have occurred
   * @param <T> AnalyticsEntry or NetworkEntry
   * @return the filtered list
   */
  public <T extends AnalyticsEntry> List<T> filterByTimestampBefore(
      final List<T> entries, final long timestamp) {
    if (CollectionUtils.isEmpty(entries)) {
      return new ArrayList<>();
    }
    return entries.stream()
        .filter(entry -> entry.isTimestampBefore(timestamp))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of log entries to those equal to or later than a particular
   * timestamp.
   *
   * @param entries the list of entries to filter
   * @param timestamp the timestamp after which returned log entries should have occurred
   * @param <T> AnalyticsEntry or NetworkEntry
   * @return the filtered list
   */
  public <T extends AnalyticsEntry> List<T> filterByTimestampAfter(
      final List<T> entries, long timestamp) {
    if (CollectionUtils.isEmpty(entries)) {
      return new ArrayList<>();
    }
    return entries.stream()
        .filter(entry -> entry.isTimestampAfter(timestamp))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of log entries to those between two timestamps.
   *
   * @param entries the list of entries to filter
   * @param start the timestamp after which returned log entries should have occurred
   * @param end the timestamp before which returned log entries should have occurred
   * @param <T> AnalyticsEntry or NetworkEntry
   * @return the filtered list
   */
  public <T extends AnalyticsEntry> List<T> filterByTimestampBetween(
      final List<T> entries, long start, long end) {
    if (CollectionUtils.isEmpty(entries)) {
      return new ArrayList<>();
    }
    return entries.stream()
        .filter(entry -> entry.isTimestampAfter(start) && entry.isTimestampBefore(end))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of log entries to those with a particular log level.
   *
   * @param entries the list of entries to filter
   * @param level the level which log entries should have
   * @param <T> AnalyticsEntry or NetworkEntry
   * @return the filtered list
   */
  public <T extends AnalyticsEntry> List<T> filterByLevel(
      final List<T> entries, final Level level) {
    if (null == entries) {
      return new ArrayList<>();
    }
    return entries.stream().filter(entry -> entry.isLevel(level)).collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of log entries to those with a particular method.
   *
   * @param entries the list of entries to filter
   * @param method the method which log entries should have
   * @param <T> AnalyticsEntry or NetworkEntry
   * @return the filtered list
   */
  public <T extends AnalyticsEntry> List<T> filterByMethod(
      final List<T> entries, final String method) {
    if (null == entries) {
      return new ArrayList<>();
    }
    return entries.stream()
        .filter(entry -> entry.doesMethodContain(method))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of log entries to those with a particular parameter. Only checks top
   * level parameters.
   *
   * @param entries the list of entries to filter
   * @param key the parameter which log entries should have
   * @param <T> AnalyticsEntry or NetworkEntry
   * @return the filtered list
   */
  public <T extends AnalyticsEntry> List<T> filterByParam(final List<T> entries, final String key) {
    if (null == entries) {
      return new ArrayList<>();
    }
    return entries.stream().filter(entry -> entry.hasParam(key)).collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of log entries to those with a particular parameter equalling an
   * arbitrary value. Only checks top level parameters.
   *
   * @param entries the list of entries to filter
   * @param key the parameter which log entries should have
   * @param value the value of that parameter
   * @param <T> AnalyticsEntry or NetworkEntry
   * @return the filtered list
   */
  public <T extends AnalyticsEntry> List<T> filterByParamEquals(
      final List<T> entries, final String key, final String value) {
    if (null == entries) {
      return new ArrayList<>();
    }
    return entries.stream()
        .filter(entry -> entry.paramEquals(key, value))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of log entries to those with a message containing a piece of
   * arbitrary text. Can be used as a fallback if other filter methods do not produce anticipated
   * results.
   *
   * @param entries the list of entries to filter
   * @param text the arbitrary piece of text that log entries should contain
   * @param <T> AnalyticsEntry or NetworkEntry
   * @return the filtered list
   */
  public <T extends AnalyticsEntry> List<T> filterByContainingText(
      final List<T> entries, final String text) {
    if (null == entries) {
      return new ArrayList<>();
    }
    return entries.stream().filter(entry -> entry.containsText(text)).collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of network entries to those with the method
   * Network.requestWillBeSent.
   *
   * @param entries the list of entries to filter
   * @return the filtered list
   */
  public List<NetworkEntry> filterRequests(final List<NetworkEntry> entries) {
    return entries.stream().filter(NetworkEntry::isRequest).collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of network entries to those with the method Network.responseReceived.
   *
   * @param entries the list of entries to filter
   * @return the filtered list
   */
  public List<NetworkEntry> filterResponses(final List<NetworkEntry> entries) {
    return entries.stream().filter(NetworkEntry::isResponse).collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of network entries to those with a particular initiator parameter.
   *
   * @param entries the list of entries to filter
   * @param key the parameter which network entries should have
   * @return the filtered list
   */
  public List<NetworkEntry> filterByInitiatorParam(
      final List<NetworkEntry> entries, final String key) {
    return entries.stream()
        .filter(entry -> entry.hasInitiatorParam(key))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of log entries to those with a particular initiator parameter
   * equalling an arbitrary value.
   *
   * @param entries the list of entries to filter
   * @param key the parameter which network entries should have
   * @param value the value of that parameter
   * @return the filtered list
   */
  public List<NetworkEntry> filterByInitiatorParamEquals(
      final List<NetworkEntry> entries, final String key, final String value) {
    return entries.stream()
        .filter(entry -> entry.hasInitiatorParam(key) && entry.initiatorParamEquals(key, value))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of network entries to those with a particular request parameter.
   *
   * @param entries the list of entries to filter
   * @param key the parameter which network entries should have
   * @return the filtered list
   */
  public List<NetworkEntry> filterByRequestParam(
      final List<NetworkEntry> entries, final String key) {
    return entries.stream()
        .filter(entry -> entry.hasRequestParam(key))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of log entries to those with a particular request parameter equalling
   * an arbitrary value.
   *
   * @param entries the list of entries to filter
   * @param key the parameter which network entries should have
   * @param value the value of that parameter
   * @return the filtered list
   */
  public List<NetworkEntry> filterByRequestParamEquals(
      final List<NetworkEntry> entries, final String key, final String value) {
    return entries.stream()
        .filter(entry -> entry.hasRequestParam(key) && entry.requestParamEquals(key, value))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of network entries to those with a particular response parameter.
   *
   * @param entries the list of entries to filter
   * @param key the parameter which network entries should have
   * @return the filtered list
   */
  public List<NetworkEntry> filterByResponseParam(
      final List<NetworkEntry> entries, final String key) {
    return entries.stream()
        .filter(entry -> entry.hasResponseParam(key))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of log entries to those with a particular response parameter
   * equalling an arbitrary value.
   *
   * @param entries the list of entries to filter
   * @param key the parameter which network entries should have
   * @param value the value of that parameter
   * @return the filtered list
   */
  public List<NetworkEntry> filterByResponseParamEquals(
      final List<NetworkEntry> entries, final String key, final String value) {
    return entries.stream()
        .filter(entry -> entry.hasResponseParam(key) && entry.responseParamEquals(key, value))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of network entries to those with a particular HTTP header.
   *
   * @param entries the list of entries to filter
   * @param header the header which network entries should have
   * @return the filtered list
   */
  public List<NetworkEntry> filterByHeader(final List<NetworkEntry> entries, final String header) {
    return entries.stream()
        .filter(entry -> entry.hasInitiatorParam(header))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of log entries to those with a particular HTTP header equalling an
   * arbitrary value.
   *
   * @param entries the list of entries to filter
   * @param header the header which network entries should have
   * @param value the value of that header
   * @return the filtered list
   */
  public List<NetworkEntry> filterByHeaderContains(
      final List<NetworkEntry> entries, final String header, final String value) {
    return entries.stream()
        .filter(entry -> entry.hasHeader(header) && entry.headerContains(header, value))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of network entries to those with a particular request ID.
   *
   * @param entries the list of entries to filter
   * @param requestId the requestId which network entries should have
   * @return the filtered list
   */
  public List<NetworkEntry> filterByRequestId(
      final List<NetworkEntry> entries, final String requestId) {
    return entries.stream()
        .filter(entry -> entry.isRequestId(requestId))
        .collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of network entries to requests and responses sent to and from a
   * particular URL.
   *
   * @param entries the list of entries to filter
   * @param url the url which network entries should have
   * @return the filtered list
   */
  public List<NetworkEntry> filterByURL(final List<NetworkEntry> entries, final String url) {
    return entries.stream().filter(entry -> entry.doesUrlContain(url)).collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of network entries to those with a particular query parameter.
   *
   * @param entries the list of entries to filter
   * @param key the parameter which network entries should have
   * @return the filtered list
   */
  public List<NetworkEntry> filterByQueryParam(final List<NetworkEntry> entries, final String key) {
    return entries.stream().filter(entry -> entry.hasQueryParam(key)).collect(Collectors.toList());
  }

  /**
   * Filters an arbitrary list of log entries to those with a particular query parameter equalling
   * an arbitrary value.
   *
   * @param entries the list of entries to filter
   * @param key the parameter which network entries should have
   * @param value the value of that parameter
   * @return the filtered list
   */
  public List<NetworkEntry> filterByQueryParamEquals(
      final List<NetworkEntry> entries, final String key, final String value) {
    return entries.stream()
        .filter(entry -> entry.hasQueryParam(key) && entry.queryParamEquals(key, value))
        .collect(Collectors.toList());
  }

  /**
   * Gets and stores the current network logs for later use in afterMethodAssert(). This method is
   * automatically run before methods annotated with @AnalyticsCall.
   */
  public void beforeMethodAssert() {
    logger.debug("Found @AnalyticsCall annotation. Gathering network logs before method executes.");
    preMethodLogs = getAllNetworkLogs();
  }

  /**
   * Finds any requests matching the criteria in an @AnalyticsCall annotation sent since
   * beforeMethodAssert(). Verifies that each of these requests received a response. This method is
   * automatically run after methods annotated with @AnalyticsCall.
   *
   * @param metadata the @AnalyticsCall annotation containing filtering criteria
   */
  public void afterMethodAssert(final AnalyticsCall metadata) {
    // Filter down to only those requests sent during the execution of the annotated method.
    List<NetworkEntry> postMethodLogs = getAllNetworkLogs();
    postMethodLogs.removeAll(preMethodLogs);

    // Apply filters according to the criteria specified by the user in the annotation. One at a
    // time.
    postMethodLogs = getRequestsWithMultipleFilters(postMethodLogs, metadata);

    // Ensure that, after filtering for all the criteria in the annotation, there was at least one
    // request sent.
    if (postMethodLogs.isEmpty()) {
      throw new RuntimeException(
          "No requests matching criteria in @AnalyticsCall annotation were sent during "
              + "method execution.");
    }
    logger.debug(
        String.format(
            "Found [%d] requests matching criteria during method execution. Waiting for "
                + "responses.",
            postMethodLogs.size()));

    // For each of the requests sent during the method, ensure that a response was received.
    for (NetworkEntry request : postMethodLogs) {
      waitForResponse(request);
    }
    logger.debug("All requests sent during method execution received responses.");
  }

  /**
   * Iteratively filters for each of the criteria passed in an @AnalyticsCall annotation. Only
   * returns network requests.
   *
   * @param entries the list of AnalyticsEntry or NetworkEntry to perform the filters upon.
   * @param metadata the @AnalyticsCall annotation containing filtering criteria
   * @param <T> entry type
   * @return filtered entries
   */
  @SuppressWarnings("checkstyle:CyclomaticComplexity")
  public <T extends AnalyticsEntry> List<NetworkEntry> getRequestsWithMultipleFilters(
      final List<T> entries, final AnalyticsCall metadata) {
    List<NetworkEntry> filtered = filterNetworkLogs(entries);
    filtered = filterRequests(filtered);

    if (metadata.googleAnalytics()) {
      filtered = filterGoogleAnalyticsLogs(filtered);
    }
    if (metadata.omniture()) {
      filtered = filterOmnitureLogs(filtered);
    }
    if (metadata.timestampBefore() != 0) {
      filtered = filterByTimestampBefore(filtered, metadata.timestampBefore());
    }
    if (metadata.timestampAfter() != 0) {
      filtered = filterByTimestampAfter(filtered, metadata.timestampAfter());
    }
    if (!metadata.method().isEmpty()) {
      filtered = filterByMethod(filtered, metadata.method());
    }
    if (!metadata.url().isEmpty()) {
      filtered = filterByURL(filtered, metadata.url());
    }
    if (metadata.params().length > 0) {
      String[] keys = metadata.params();
      String[] values = metadata.paramVals();
      if (values.length == keys.length) {
        for (int i = 0; i < keys.length; i++) {
          filtered = filterByParamEquals(filtered, keys[i], values[i]);
        }
      } else {
        for (String key : keys) {
          filtered = filterByParam(filtered, key);
        }
      }
    }
    if (!metadata.text().isEmpty()) {
      filtered = filterByContainingText(filtered, metadata.text());
    }
    if (metadata.initiatorParams().length > 0) {
      String[] keys = metadata.initiatorParams();
      String[] values = metadata.initiatorParamVals();
      if (values.length == keys.length) {
        for (int i = 0; i < keys.length; i++) {
          filtered = filterByInitiatorParamEquals(filtered, keys[i], values[i]);
        }
      } else {
        for (String key : keys) {
          filtered = filterByInitiatorParam(filtered, key);
        }
      }
    }
    if (metadata.requestParams().length > 0) {
      String[] keys = metadata.requestParams();
      String[] values = metadata.requestParamVals();
      if (values.length == keys.length) {
        for (int i = 0; i < keys.length; i++) {
          filtered = filterByRequestParamEquals(filtered, keys[i], values[i]);
        }
      } else {
        for (String key : keys) {
          filtered = filterByRequestParam(filtered, key);
        }
      }
    }
    if (metadata.responseParams().length > 0) {
      String[] keys = metadata.responseParams();
      String[] values = metadata.responseParamVals();
      if (values.length == keys.length) {
        for (int i = 0; i < keys.length; i++) {
          filtered = filterByResponseParamEquals(filtered, keys[i], values[i]);
        }
      } else {
        for (String key : keys) {
          filtered = filterByResponseParam(filtered, key);
        }
      }
    }
    if (metadata.headers().length > 0) {
      String[] keys = metadata.headers();
      String[] values = metadata.headerVals();
      if (values.length == keys.length) {
        for (int i = 0; i < keys.length; i++) {
          filtered = filterByHeaderContains(filtered, keys[i], values[i]);
        }
      } else {
        for (String key : keys) {
          filtered = filterByHeader(filtered, key);
        }
      }
    }
    if (!metadata.requestId().isEmpty()) {
      filtered = filterByRequestId(filtered, metadata.requestId());
    }
    if (metadata.queryParams().length > 0) {
      String[] keys = metadata.queryParams();
      String[] values = metadata.queryParamVals();
      if (values.length == keys.length) {
        for (int i = 0; i < keys.length; i++) {
          filtered = filterByQueryParamEquals(filtered, keys[i], values[i]);
        }
      } else {
        for (String key : keys) {
          filtered = filterByQueryParam(filtered, key);
        }
      }
    }
    return filtered;
  }

  /**
   * Returns a new instance of {@link AnalyticsInterceptor} associated with this object.
   *
   * @return A new {@link AnalyticsInterceptor} instance.
   */
  public AnalyticsInterceptor getInterceptor() {
    return new AnalyticsInterceptor(this);
  }
}
