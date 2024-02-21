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
package com.applause.auto.testng.annotations;

import com.applause.auto.framework.ApplauseFramework;
import com.applause.auto.framework.ContextBuilder;
import com.applause.auto.framework.ContextManager;
import com.applause.auto.framework.DriverBuilder;
import com.applause.auto.framework.context.annotations.CapsOverrider;
import com.applause.auto.framework.context.annotations.Driverless;
import com.applause.auto.framework.context.annotations.WithCapsOverride;
import com.applause.auto.framework.context.annotations.WithDriver;
import com.applause.auto.framework.json.BadJsonFormatException;
import com.applause.auto.testng.TestNgContextUtils;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WithDriver
public class ContextAnnotationsTest {
  private static final Logger logger = LogManager.getLogger(ContextAnnotationsTest.class);
  public final ContextManager contextManager = ContextManager.INSTANCE;
  private FakeDriverManager dm;

  @BeforeClass
  @SneakyThrows
  public void setup() {
    logger.info("Setting up Context Annotations");
    dm = new FakeDriverManager();
    ApplauseFramework.INSTANCE.registerDrivers("src/test/resources/cfg/").setDriverManager(dm);
  }

  @BeforeMethod(alwaysRun = true)
  @SuppressWarnings("PMD.UseVarargs")
  public void setupContext(final ITestResult testResult, final Object[] params)
      throws BadJsonFormatException {
    DriverBuilder.create("local_firefox.json")
        .overrideCaps(TestNgContextUtils.getCapsOverrider(testResult))
        .map(ContextBuilder::fromDriver)
        .getAsMainContext();
  }

  @AfterMethod(alwaysRun = true)
  public void resetContext() {
    this.contextManager.resetContext();
  }

  @CapsOverrider("caps1")
  public static MutableCapabilities capsOverride1(final Capabilities caps) {
    MutableCapabilities c = new MutableCapabilities(caps);
    c.setCapability("acceptInsecureCerts", true);
    return c;
  }

  @CapsOverrider("non-static")
  public MutableCapabilities nonStatic(final Capabilities caps) {
    MutableCapabilities c = new MutableCapabilities(caps);
    c.setCapability("acceptInsecureCerts", true);
    return c;
  }

  @CapsOverrider("bad-return")
  public void badReturn(final Capabilities caps) {
    // Empty
  }

  @CapsOverrider("bad-input")
  public static MutableCapabilities badInput(final Capabilities caps, String other) {
    MutableCapabilities c = new MutableCapabilities(caps);
    c.setCapability("acceptInsecureCerts", true);
    return c;
  }

  @Driverless
  @Test
  public void testDriverlessTest() {
    logger.info("Starting Driverless");
    final var c = contextManager.getCurrentContext().orElse(null);
    Assert.assertNotNull(c);
  }

  @WithDriver
  @Test
  public void testDriverTest() {
    logger.info("Starting testDriverTest");
    final var c = contextManager.getThreadContext().get().getPageObjectContext();
    Assert.assertTrue(c.isPresent());
  }

  @WithDriver
  @WithCapsOverride("caps1")
  @Test
  public void testCapsOverride() {
    Assert.assertEquals(dm.getLastConfigCaps().getCapability("acceptInsecureCerts"), true);
  }

  @WithDriver
  @WithCapsOverride("non-static")
  @Test
  public void testNonStaticCapsOverride() {
    Assert.assertEquals(dm.getLastConfigCaps().getCapability("acceptInsecureCerts"), false);
  }

  @WithDriver
  @WithCapsOverride("bad-return")
  @Test
  public void testBadReturnCapsOverride() {
    Assert.assertEquals(dm.getLastConfigCaps().getCapability("acceptInsecureCerts"), false);
  }

  @WithDriver
  @WithCapsOverride("bad-input")
  @Test
  public void testBadInputCapsOverride() {
    Assert.assertEquals(dm.getLastConfigCaps().getCapability("acceptInsecureCerts"), false);
  }
}
