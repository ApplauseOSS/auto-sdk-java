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
package com.applause.auto.config;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.NonNull;
import okhttp3.HttpUrl;
import org.aeonbits.owner.Config;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Utility methods for validating configuration parameters */
public final class ConfigUtils {
  private static final Logger logger = LogManager.getLogger(ConfigUtils.class);
  private static final RetryPolicy<Object> retryPolicy =
      RetryPolicy.builder()
          .handle(RuntimeException.class)
          .withDelay(Duration.ofSeconds(15))
          .withMaxRetries(2)
          .build();

  private ConfigUtils() {
    // This class just has static methods
  }

  /**
   * Validate a URL input parameter
   *
   * @param urlAsString The URL to validate represented as a String
   * @param urlRequired If true, the function will return false if the string is null or blank.
   * @param validateReverseDns If true, the reverse DNS must be possible and not from a pseudo entry
   *     from an ISP
   * @return empty if the URL is valid and DNS validations pass (or the field was empty), error
   *     string otherwise
   */
  @SuppressWarnings("checkstyle:CyclomaticComplexity")
  public static Optional<String> validateUrl(
      final String urlAsString, final boolean urlRequired, final boolean validateReverseDns) {
    final var trimUrlStr =
        Optional.ofNullable(urlAsString).map(String::trim).filter(StringUtils::isNotBlank);
    if (trimUrlStr.isEmpty()) {
      if (urlRequired) {
        return Optional.of("Required URL is empty or null");
      }
      return Optional.empty();
    }
    // Since it's not null or empty, we insist that it be a valid URL
    final URL url;
    try {
      url = new URI(urlAsString).toURL();
    } catch (MalformedURLException | URISyntaxException e) {
      return Optional.of("'" + urlAsString + "' is not a valid URL: " + e.getMessage());
    }
    final String hostPart = url.getHost();
    try {
      final var addr = InetAddress.getByName(hostPart);
      final String addrHost = addr.getHostAddress();
      if (!validateReverseDns || hostPart.equals(addrHost) || isLocalHost(addr)) {
        // Either the caller doesn't care about reverse DNS or the hostPart was an IP address
        // in which case we won't check the reverse DNS.  In any event.  We have succeeded
        return Optional.empty();
      }
      if (!checkReverseDnsWithRetry(addr)) {
        // there is no reverse DNS entry for this host.  While not wrong for external partners
        return Optional.of(
            "No reverse DNS available for '"
                + hostPart
                + "/"
                + addrHost
                + "' for URL "
                + urlAsString);
      }

    } catch (UnknownHostException e) {
      return Optional.of(
          "Unable to resolve host '"
              + hostPart
              + "' in URL "
              + urlAsString
              + " with error: "
              + e.getMessage());
    }

    // Everything validated.  Return no error!
    return Optional.empty();
  }

  /**
   * Checks to see if a given InetAddress is a localhost address
   *
   * @param addr address
   * @return true if localhost
   */
  public static boolean isLocalHost(@NonNull final InetAddress addr) {
    // Check if the address is a valid special local or loop back
    if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
      return true;
    }

    // Check if the address is defined on any interface
    try {
      return NetworkInterface.getByInetAddress(addr) != null;
    } catch (SocketException e) {
      return false;
    }
  }

  static boolean checkReverseDnsWithRetry(final InetAddress addr) {
    try {
      return Failsafe.with(retryPolicy)
          .onFailure(failure -> logger.info("Reverse DNS inconclusive.  retrying"))
          .get(() -> checkReverseDns(addr));
    } catch (RuntimeException rte) {
      return false;
    }
  }

  static boolean checkReverseDns(final InetAddress addr) {
    final String canonicalName = addr.getCanonicalHostName();
    if (canonicalName != null) {
      return !canonicalName.startsWith("unallocated");
    }
    throw new RuntimeException("Reverse DNS inconclusive");
  }

  /**
   * Converts a config to a property map
   *
   * @param <T> the type of config
   * @param config The config
   * @param configClass The config class
   * @return A map containing all properties and values
   */
  public static <T extends Config> Map<String, Object> toPropertyMap(
      final T config, final Class<T> configClass) {
    Map<String, Object> result = new HashMap<>();
    Method[] methods = config.getClass().getMethods();
    for (Method meth : methods) {
      if (!Modifier.isPublic(meth.getModifiers())
          || meth.getReturnType().equals(Void.TYPE)
          || meth.getParameterCount() != 0) {
        continue; // Not a public getter method, ignore
      }
      final String methodName = meth.getName();
      Object methodValue;
      try {
        methodValue = meth.invoke(config);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        logger.info("Could not invoke method: " + methodName);
        continue;
      }
      result.put(methodName, methodValue);
    }
    return result;
  }

  /**
   * Converts the system properties to a property map
   *
   * @return A map containing all properties and values
   */
  public static Map<String, Object> getSystemProperties() {
    return System.getProperties().entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getKey().toString(), Map.Entry::getValue));
  }

  /**
   * Return any configured proxy object
   *
   * @param httpProxyUrl The httpProxyUrl
   * @return null if no proxy was configured. An instance of Proxy if one was
   */
  public static @Nullable Proxy getHttpProxy(@Nullable final String httpProxyUrl) {
    // In the configuration, the 'httpProxyUrl' field is set.  This means that this instance of the
    // SDK is running behind an HTTP proxy of some sort
    if (StringUtils.isBlank(httpProxyUrl)) {
      return null;
    }
    try {
      final HttpUrl parsedUrl = HttpUrl.get(httpProxyUrl);
      SocketAddress sockAddr = new InetSocketAddress(parsedUrl.host(), parsedUrl.port());
      // NOTE:  This works because the Proxy object does not contain state (so we can recreate it)
      return new Proxy(Proxy.Type.HTTP, sockAddr);
    } catch (IllegalArgumentException ex) {
      throw new RuntimeException("Failed to convert URL '" + httpProxyUrl, ex);
    }
  }
}
