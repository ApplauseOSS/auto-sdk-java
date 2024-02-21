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
import com.applause.auto.config.ApplauseSdkConfigBean;
import com.applause.auto.config.AutoApiPropertyHelper;
import com.applause.auto.config.ConfigUtils;
import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.config.SdkConfigBean;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/** have configuration checks in a common place */
public final class ApplauseConfigHelper {

  private ApplauseConfigHelper() {
    // this class just has static methods
  }

  /**
   * Return any configured proxy object
   *
   * @return null if no proxy was configured. An instance of Proxy if one was
   */
  public static @Nullable Proxy getHttpProxy() {
    return ConfigUtils.getHttpProxy(EnvironmentConfigurationManager.INSTANCE.get().httpProxyUrl());
  }

  /**
   * Check the SdkConfigBean for common issues and provide an actionable error message
   *
   * @throws RuntimeException on error
   */
  public static void validateConfiguration() {
    final SdkConfigBean sdkConfigBean = EnvironmentConfigurationManager.INSTANCE.get();
    final ApplauseSdkConfigBean applauseConfigBean =
        ApplauseEnvironmentConfigurationManager.INSTANCE.get();
    // Check the URLs that we need to function.
    boolean reqRevDns = !sdkConfigBean.noReverseDnsCheck();
    var errorList =
        Stream.of(
                validateUrl(applauseConfigBean.autoApiUrl(), true, reqRevDns),
                // While we are in the process of separating the proxy, allow the seleniumProxyUrl
                // to be optional We'll take the autoApiUrl value if this isn't set
                validateUrl(applauseConfigBean.seleniumProxyUrl(), true, reqRevDns),
                validateUrl(applauseConfigBean.applausePublicApiUrl(), true, reqRevDns),
                validateUrl(sdkConfigBean.localAppiumUrl(), false, reqRevDns))
            // filter out empty optionals and make list of remaining strings
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toCollection(ArrayList::new));
    // If we have useSeleniumGrid set, then we must have seleniumGridLocation and this
    // must be a valid URL
    if (sdkConfigBean.useSeleniumGrid()) {
      validateUrl(sdkConfigBean.seleniumGridLocation(), true, reqRevDns).ifPresent(errorList::add);
    }

    Set<String> recursiveValueNames =
        AutoApiPropertyHelper.findRecursiveValues(applauseConfigBean, ApplauseSdkConfigBean.class);
    for (String recursiveName : recursiveValueNames) {
      errorList.add(
          "Configuration property "
              + recursiveName
              + " has a self-referential value and will not resolve. Maybe an environment variable isn't being substituted correctly?");
    }

    if (!errorList.isEmpty()) {
      // We had validation errors.
      throw new RuntimeException("\nConfiguration errors: " + String.join("\n  ", errorList));
    }
  }

  /**
   * For a resource via the classes loaded. Allow the extension on file to be optional
   *
   * @param clazz the class to use for resource loading
   * @param name the resource name
   * @return an InputStreamReader for reading the resource
   */
  public static InputStreamReader getResourceAsStream(final Class<?> clazz, final String name) {
    try (var resource = clazz.getResourceAsStream(name)) {
      if (resource == null) {
        throw new IOException("Could not find resource " + name);
      }
      return new InputStreamReader(resource, StandardCharsets.UTF_8);
    } catch (IOException rte) {
      if (name.endsWith(".json")) {
        return null;
      }
      final String namePlusExt = name + ".json";
      return getResourceAsStream(clazz, namePlusExt);
    }
  }

  /**
   * returns error string if we got one, empty otherwise
   *
   * @param urlAsString the string URL
   * @param required whether the URL is required
   * @param reqRevDns whether reverse DNS is required
   * @return error message if validation failed
   */
  static Optional<String> validateUrl(
      final String urlAsString, final boolean required, final boolean reqRevDns) {
    return ConfigUtils.validateUrl(urlAsString, required, reqRevDns);
  }
}
