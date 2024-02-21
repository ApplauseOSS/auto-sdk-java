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

import com.google.common.base.Suppliers;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.aeonbits.owner.ConfigFactory;

/** thread-safe utility class for loading the SDK configuration. */
public enum TestRailConfigurationManager {
  /** The Singleton instance of the TestRailConfigurationManager */
  INSTANCE;

  private Supplier<TestRailConfigBean> configCache = createConfigBean(null);
  private final Map<String, Object> overwrittenProps = new HashMap<>();

  /**
   * thread-safe immutable configuration object
   *
   * @return the SDK configuration
   */
  public TestRailConfigBean get() {
    return configCache.get();
  }

  /**
   * overrides selected properties. This overwrites the main config!!!
   *
   * @param overrideProps the properties to override
   */
  public void override(final Map<String, ?> overrideProps) {
    synchronized (this) {
      overwrittenProps.putAll(overrideProps);
      configCache = createConfigBean(overwrittenProps);
    }
  }

  private Supplier<TestRailConfigBean> createConfigBean(
      @Nullable final Map<String, ?> overrideProps) {
    return Suppliers.memoize(
        () ->
            // order is critical here. order is in 'most->least' priority
            ConfigFactory.create(
                TestRailConfigBean.class,
                Optional.ofNullable(overrideProps).orElse(new HashMap<>()),
                System.getProperties(),
                System.getenv()));
  }
}
