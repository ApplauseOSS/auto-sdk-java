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
package com.applause.auto.integrations;

import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.framework.selenium.ApplauseCapabilitiesConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.DesiredCapabilities;

/** A collection of common static capability override functions that may be used */
@Log4j2
public final class CapabilityOverriders {

  private CapabilityOverriders() {}

  /**
   * Enables performance logging for the given capabilities
   *
   * @param caps The input capabilities
   * @return The update capabilities
   */
  @SuppressWarnings("unchecked")
  public static MutableCapabilities enablePerformanceLogging(final @NonNull Capabilities caps) {
    final var finalCaps = new MutableCapabilities(caps);
    if (!EnvironmentConfigurationManager.INSTANCE.get().performanceLogging()) {
      return finalCaps;
    }

    Map<String, Object> loggingPreferences =
        Optional.ofNullable(finalCaps.getCapability(ChromeOptions.LOGGING_PREFS))
            .map(prefs -> (Map<String, Object>) prefs)
            .orElseGet(HashMap::new);
    loggingPreferences.put(LogType.PERFORMANCE, Level.ALL.toString());
    finalCaps.setCapability(ChromeOptions.LOGGING_PREFS, loggingPreferences);

    Map<String, Object> applauseOptions =
        Optional.ofNullable(finalCaps.getCapability(ApplauseCapabilitiesConstants.APPLAUSE_OPTIONS))
            .map(options -> (Map<String, Object>) options)
            .orElseGet(HashMap::new);
    applauseOptions.put(ApplauseCapabilitiesConstants.PERFORMANCE_LOGGING, true);
    finalCaps.setCapability(ApplauseCapabilitiesConstants.APPLAUSE_OPTIONS, applauseOptions);

    return finalCaps;
  }

  /**
   * Adds all applause options from the command line into the provided capabilities
   *
   * @param caps The input capabilities
   * @return The updated capabilities
   */
  @SuppressWarnings("unchecked")
  public static MutableCapabilities addApplauseOptions(final @NonNull Capabilities caps) {
    log.info("Adding applause options!");
    MutableCapabilities result = new MutableCapabilities(caps);
    final var config = ApplauseEnvironmentConfigurationManager.INSTANCE.get();
    Map<String, Object> applauseOptions =
        Optional.ofNullable(result.getCapability(ApplauseCapabilitiesConstants.APPLAUSE_OPTIONS))
            .map(options -> (Map<String, Object>) options)
            .orElseGet(HashMap::new);
    applauseOptions.put(ApplauseCapabilitiesConstants.API_KEY, config.apiKey());
    applauseOptions.put(ApplauseCapabilitiesConstants.RUN_NAME, config.providerSessionRunName());
    applauseOptions.put(ApplauseCapabilitiesConstants.PRODUCT_ID, config.productId());
    result.setCapability(ApplauseCapabilitiesConstants.APPLAUSE_OPTIONS, applauseOptions);
    return result;
  }

  /**
   * Appends or overrides capabilities
   *
   * @param environmentCapabilities a set of desired capabilities merged into the
   *     currentCapabilities
   * @return the overridden capabilities
   */
  public static Function<Capabilities, MutableCapabilities> mergeInCapabilities(
      final @NonNull DesiredCapabilities environmentCapabilities) {
    return caps -> new MutableCapabilities(caps).merge(environmentCapabilities);
  }
}
