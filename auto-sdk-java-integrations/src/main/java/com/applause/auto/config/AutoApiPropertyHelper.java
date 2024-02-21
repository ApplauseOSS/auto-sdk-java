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

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Helper class for checking and dumping applause properties */
public final class AutoApiPropertyHelper extends PropertyHelper {
  private static final Logger logger = LogManager.getLogger(AutoApiPropertyHelper.class);

  private AutoApiPropertyHelper() {
    super();
    // Empty CTOR.  don't let this  class be instantiated
  }

  /**
   * Log the configuration settings and if they differ from the well known config file
   *
   * @param propertyFilter A set of property names. This set identifies the properties we are
   *     interested in. If empty, all properties are logged
   */
  public static void dumpConfigProperties(final Set<String> propertyFilter) {
    final SdkConfigBean sdkConfigBean = EnvironmentConfigurationManager.INSTANCE.get();
    final ApplauseSdkConfigBean applauseConfigBean =
        ApplauseEnvironmentConfigurationManager.INSTANCE.get();
    if (sdkConfigBean.dumpConfig()) {
      Properties props = loadProperties();
      logConfiguration(sdkConfigBean, SdkConfigBean.class, props, propertyFilter);
      logConfiguration(applauseConfigBean, ApplauseSdkConfigBean.class, props, propertyFilter);
    } else {
      logger.info("Not dumping configuration");
    }
  }

  /** Log the configuration settings and if they differ from the well known config file */
  public static void dumpConfigProperties() {
    dumpConfigProperties(new HashSet<>());
  }
}
