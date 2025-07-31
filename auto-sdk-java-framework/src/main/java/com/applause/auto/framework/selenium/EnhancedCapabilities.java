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
package com.applause.auto.framework.selenium;

import com.applause.auto.framework.json.BadJsonFormatException;
import com.applause.auto.framework.json.GsonHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

/** Defines an Applause framework extension of the standard webdriver capabilities */
public class EnhancedCapabilities implements Capabilities {
  private final ImmutableCapabilities resolvedCaps;
  private @Getter final ApplauseOptions applauseOptions;

  /**
   * Constructs a new EnhancedCapabilities by parsing out the applause:options from the WebDriver
   * Capabilities
   *
   * @param caps The WebDriver Capabilities
   */
  public EnhancedCapabilities(@NonNull final Capabilities caps) {
    resolvedCaps = new ImmutableCapabilities(caps);
    applauseOptions = generateApplauseOptions(caps).orElse(null);
  }

  /**
   * Create an ApplauseCapabilities object from a JSON string
   *
   * @param jsonStr A string containing valid JSON representing Applause Capabilities
   * @return An instance
   * @throws BadJsonFormatException if capabilities couldn't be converted
   */
  public static EnhancedCapabilities fromJsonString(@NonNull final String jsonStr)
      throws BadJsonFormatException {
    Preconditions.checkArgument(
        !StringUtils.isBlank(jsonStr),
        String.format("Applause Capabilities string is empty or null '%s'", jsonStr));
    final var jsonObject = GsonHelper.str2JsonObject(jsonStr);
    final var jsonAsMap = GsonHelper.jsonObject2Map(jsonObject);
    // In order for use to work with this we need to have a section that contains "applause:options"
    final var applauseCapsObj =
        Optional.ofNullable(jsonAsMap.get(ApplauseCapabilitiesConstants.APPLAUSE_OPTIONS))
            .orElseThrow(
                () ->
                    new BadJsonFormatException(
                        String.format(
                            "No '%s' section found in JSON",
                            ApplauseCapabilitiesConstants.APPLAUSE_OPTIONS)));
    // It should be a Map
    if (!(applauseCapsObj instanceof Map<?, ?>)) {
      throw new BadJsonFormatException(
          String.format(
              "'%s' section is not a JSON object (a Map)",
              ApplauseCapabilitiesConstants.APPLAUSE_OPTIONS));
    }
    return new EnhancedCapabilities(new ImmutableCapabilities(new DesiredCapabilities(jsonAsMap)));
  }

  @SuppressWarnings("unchecked")
  private static Optional<ApplauseOptions> generateApplauseOptions(
      @NonNull final Capabilities resolvedCaps) {
    return Optional.ofNullable(
            resolvedCaps.getCapability(ApplauseCapabilitiesConstants.APPLAUSE_OPTIONS))
        .map(
            caps -> {
              if (caps instanceof Capabilities) {
                return ((Capabilities) caps).asMap();
              }
              // The Selenium API doesn't always return this field as an instance of Capabilities.
              // It can come back as a Map
              else if (caps instanceof Map<?, ?>) {
                return (Map<String, Object>) caps;
              } else {
                throw new RuntimeException(
                    " Applause options "
                        + ApplauseCapabilitiesConstants.APPLAUSE_OPTIONS
                        + " is not type Capabilities or Map<String, Object> , it is "
                        + caps.getClass());
              }
            })
        .map(ApplauseOptions::new);
  }

  @Override
  public Map<String, Object> asMap() {
    return resolvedCaps.asMap();
  }

  /**
   * Outputs the serialized Capabilities as a JSON String
   *
   * @return The serialized Capabilities as a JSON string
   */
  public String asString() {
    return GsonHelper.map2JsonString(asMapDeep(resolvedCaps));
  }

  /**
   * recursively convert Capabilities object to Map String,Object
   *
   * @param caps capabilities object
   * @return converted map
   */
  private static Map<String, Object> asMapDeep(@NonNull final Capabilities caps) {
    return caps.asMap().entrySet().stream()
        .map(
            entry ->
                new AbstractMap.SimpleImmutableEntry<>(
                    entry.getKey(),
                    entry.getValue() instanceof Capabilities
                        ? asMapDeep((Capabilities) entry.getValue())
                        : entry.getValue()))
        .collect(
            ImmutableMap.toImmutableMap(
                AbstractMap.SimpleImmutableEntry::getKey,
                AbstractMap.SimpleImmutableEntry::getValue));
  }

  public String getApp() {
    return Optional.ofNullable(resolvedCaps)
        .map(caps -> caps.getCapability("appium:app"))
        .map(Object::toString)
        .orElse(null);
  }

  @Override
  public Object getCapability(final @NonNull String capabilityName) {
    return resolvedCaps.getCapability(capabilityName);
  }
}
