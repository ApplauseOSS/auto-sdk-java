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
package com.applause.auto.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A static helper to set properties to use in the Log4j2 logger pattern. There are three types of
 * supported properties: global, local, and keyed. 1. Global properties are shared between all
 * threads, but can be overwritten by local or keyed properties. 2. Local Properties are set and
 * stored by a single thread 3. Keyed Properties allow for shared properties between one or more
 * threads
 *
 * @see ResultPropertyConverter for how these properties are configured in the logger
 */
public final class ResultPropertyMap {
  /** Global Properties allow you to set a property across all threads */
  private static final ConcurrentHashMap<String, Object> globalProperties =
      new ConcurrentHashMap<>();

  /** Local Properties allow you to set a property for the current thread */
  private static final ThreadLocal<ConcurrentHashMap<String, Object>> localProperties =
      ThreadLocal.withInitial(ConcurrentHashMap::new);

  /** Local Properties allow you to set a property for the current thread */
  private static final ThreadLocal<String> localKey = new ThreadLocal<>();

  /**
   * Keyed Properties allow you to transfer data across threads and store it based on a shared "key"
   */
  private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Object>>
      keyedProperties = new ConcurrentHashMap<>();

  // Private Utility Constructor
  private ResultPropertyMap() {}

  /**
   * Adds a global property
   *
   * @param property The property key
   * @param value The property value
   */
  public static void setGlobalProperty(final String property, final Object value) {
    globalProperties.put(property, value);
  }

  /**
   * Loads a map of properties into the global map
   *
   * @param properties A collection of properties
   */
  public static void loadGlobalProperties(final Map<String, Object> properties) {
    if (Objects.isNull(properties)) {
      return;
    }
    globalProperties.putAll(removeNullKeysAndValues(properties));
  }

  /**
   * Clears a global property
   *
   * @param property The property key to remove
   */
  public static void clearGlobalProperty(final String property) {
    globalProperties.remove(property);
  }

  /**
   * Sets the local key for key-based properties
   *
   * @param key The key for this thread to use in key-lookups
   */
  public static void setLocalKey(final String key) {
    localKey.set(key);
  }

  /** Remove the local key for key-based properties */
  public static void clearLocalKey() {
    localKey.remove();
  }

  /**
   * Sets a new local property for this thread
   *
   * @param property The property key
   * @param value The value to set it to
   */
  public static void setLocalProperty(final String property, final Object value) {
    localProperties.get().put(property, value);
  }

  /**
   * Loads a map of properties into the local map
   *
   * @param properties A collection of properties
   */
  public static void loadLocalProperties(final Map<String, Object> properties) {
    if (Objects.isNull(properties)) {
      return;
    }
    for (Entry<String, Object> e : removeNullKeysAndValues(properties).entrySet()) {
      localProperties.get().put(e.getKey(), e.getValue());
    }
  }

  /**
   * Removes a property from the local map
   *
   * @param property The property key
   */
  public static void clearLocalProperty(final String property) {
    localProperties.get().remove(property);
  }

  /**
   * Adds a property by a key. To fetch this information, the thread trying to get the data will
   * need to set the local key.
   *
   * @param key The key to apply to property to
   * @param property The property key
   * @param value The value to set the property to
   */
  public static void setKeyedProperty(final String key, final String property, final Object value) {
    keyedProperties.putIfAbsent(key, new ConcurrentHashMap<>());
    keyedProperties.get(key).put(property, value);
  }

  /**
   * Loads a set of properties to the keyed map
   *
   * @param key The key
   * @param properties A collection of properties
   */
  public static void loadKeyedProperties(final String key, final Map<String, Object> properties) {
    if (Objects.isNull(properties)) {
      return;
    }
    keyedProperties.putIfAbsent(key, new ConcurrentHashMap<>());
    for (Entry<String, Object> e : removeNullKeysAndValues(properties).entrySet()) {
      keyedProperties.get(key).put(e.getKey(), e.getValue());
    }
  }

  /**
   * Removes a keyed property
   *
   * @param key The key
   * @param property The property
   */
  public static void clearKeyedProperty(final String key, final String property) {
    if (keyedProperties.containsKey(key)) {
      keyedProperties.get(key).remove(property);
    }
  }

  /**
   * Checks for a given property. Order of precedence: local, keyed, global
   *
   * @param property The property key
   * @return The value, if present
   */
  public static Object getProperty(final String property) {
    return Optional.ofNullable(localProperties.get().get(property))
        .orElseGet(
            () -> {
              String key = localKey.get();
              if (Objects.nonNull(key)
                  && keyedProperties.containsKey(key)
                  && keyedProperties.get(key).containsKey(property)) {
                return keyedProperties.get(key).get(property);
              }
              return globalProperties.get(property);
            });
  }

  /**
   * Generates a map of properties that can be used
   *
   * @return The value, if present
   */
  public static Map<String, Object> getProperties() {
    final Map<String, Object> result = new HashMap<>(globalProperties);
    final String key = localKey.get();
    if (Objects.nonNull(key)) {
      result.putAll(keyedProperties.get(key));
    }
    result.putAll(localProperties.get());
    return result;
  }

  /**
   * Filters out null keys and values from a map before inserting into the ConcurrentHashMAp
   *
   * @param properties A map of properties
   * @return A map with null keys and values removed
   */
  private static Map<String, Object> removeNullKeysAndValues(final Map<String, Object> properties) {
    return properties.entrySet().stream()
        .filter(entry -> Objects.nonNull(entry.getKey()))
        .filter(entry -> Objects.nonNull(entry.getValue()))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }
}
