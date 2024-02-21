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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.framework.selenium.EnhancedCapabilities;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AutoBuildHelperTest {
  @Test
  public void testGetApp() {
    // test null input with no EnvironmentConfig
    Object actualValue = AutoBuildHelper.getApp(null);
    Assert.assertNull(actualValue, "null appCaps, return null from Bean");

    // test non-null input with no EnvironmentConfig.override
    EnhancedCapabilities caps = mock(EnhancedCapabilities.class);
    when(caps.getCapability("app")).thenReturn("app-from-cfg1");
    Assert.assertEquals(
        "app-from-cfg1",
        AutoBuildHelper.getApp(caps),
        "with appCaps, return 'app-from-cfg1' from Bean");

    // test null input with EnvironmentConfig.override
    EnvironmentConfigurationManager.INSTANCE.override(Map.of("app", "app-from-cfg2"));
    actualValue = AutoBuildHelper.getApp(null);
    Assert.assertEquals(
        "app-from-cfg2", actualValue, "null appCaps, return 'app-from-cfg2' from Bean");

    // Finally, ensure we prioritize input over EnvironmentConfig.override
    when(caps.getCapability("app")).thenReturn("preferred-app");
    EnvironmentConfigurationManager.INSTANCE.override(Map.of("app", "app-from-cfg3"));
    Assert.assertEquals(
        "preferred-app",
        AutoBuildHelper.getApp(caps),
        "with appCaps, return 'preferred-app' from Bean");
  }
}
