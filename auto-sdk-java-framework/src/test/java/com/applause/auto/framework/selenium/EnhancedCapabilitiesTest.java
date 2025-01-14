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

import com.applause.auto.data.enums.DriverType;
import com.applause.auto.data.enums.Platform;
import com.applause.auto.framework.json.BadJsonFormatException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EnhancedCapabilitiesTest {
  private static final Logger logger = LogManager.getLogger(EnhancedCapabilitiesTest.class);
  private static File DATA_DIR = null;

  @BeforeClass
  public static void initEnv() {
    try {
      String codeRoot = new File(".").getCanonicalPath();
      if (codeRoot.endsWith("auto-sdk-java-framework")) {
        DATA_DIR = new File(codeRoot, "src/test/testData/ApplauseCapabilities");
      } else {
        DATA_DIR =
            new File(codeRoot, "auto-sdk-java-framework/src/test/testData/ApplauseCapabilities");
      }
    } catch (IOException e) {
      logger.warn("unable to determine top of code tree to find data files");
    }
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = ".*Applause Capabilities string is empty or null.*")
  public void testFromJsonStringCapabilitiesIsBlank() throws BadJsonFormatException {
    EnhancedCapabilities.fromJsonString("   ");
  }

  @Test(
      expectedExceptions = BadJsonFormatException.class,
      expectedExceptionsMessageRegExp = ".*Expected a single JSON object:.*")
  public void testFromJsonStringCapabilitiesIsNotJson() throws BadJsonFormatException {
    EnhancedCapabilities.fromJsonString("3");
  }

  @Test(
      expectedExceptions = BadJsonFormatException.class,
      expectedExceptionsMessageRegExp = ".*Expected a single JSON object.*")
  public void testFromJsonStringCapabilitiesIsJsonArray() throws BadJsonFormatException {
    final String jsonArray = "[{\"a\" : 1}, {\"b\" : 2}]";
    EnhancedCapabilities.fromJsonString(jsonArray);
  }

  @Test(
      expectedExceptions = BadJsonFormatException.class,
      expectedExceptionsMessageRegExp = ".*No 'applause:options' section found in JSON.*")
  public void testFromJsonStringCapabilitiesNoApplauseOptions() throws BadJsonFormatException {
    final String jsonArray = "{\"a\" : 1, \"b\" : 2}";
    EnhancedCapabilities.fromJsonString(jsonArray);
  }

  @Test(
      expectedExceptions = BadJsonFormatException.class,
      expectedExceptionsMessageRegExp =
          ".*applause:options' section is not a JSON object \\(a Map\\).*")
  public void testFromJsonStringCapabilitiesApplauseOptionsSectionIsNotMap()
      throws BadJsonFormatException {
    final String jsonObjStr1 = "{\"a\" : 1, \"applause:options\" : 2}";
    EnhancedCapabilities.fromJsonString(jsonObjStr1);
  }

  @Test
  public void testFromJsonStringCapabilitiesGood() throws BadJsonFormatException {
    final String jsonObjStr1 = "{\"a\" : 1, \"applause:options\" : {\"a\": 1, \"b\": 2}}";
    EnhancedCapabilities.fromJsonString(jsonObjStr1);
  }

  @Test
  public void testNullChecks() {
    EnhancedCapabilities appCap = new EnhancedCapabilities(new DesiredCapabilities());
    Assert.assertNotNull(appCap.asMap());
    Assert.assertEquals(0, appCap.asMap().size());
    Assert.assertNull(appCap.getCapability("anything"));
    Assert.assertNull(appCap.getApplauseOptions());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFromJsonStringGoodPath() throws IOException, BadJsonFormatException {
    final File p1Chrome = new File(DATA_DIR, "applauseCaps_provider1_chrome.json");
    final String p1ChromeStr = FileUtils.readFileToString(p1Chrome, "UTF-8");
    final EnhancedCapabilities appCap = EnhancedCapabilities.fromJsonString(p1ChromeStr);
    Assert.assertNotNull(appCap);
    // Check values that we expect to be there
    Assert.assertFalse(appCap.getApplauseOptions().isMobileNative());
    Assert.assertEquals("chrome", appCap.getCapability("browserName"));
    // Check to see that we have NOT converted Longs to Doubles as out-of-the-box Gson done
    Assert.assertEquals("600", appCap.getCapability("commandTimeout").toString());
    Assert.assertEquals("3000", appCap.getCapability("newCommandTimeout").toString());
    Assert.assertEquals("3.14159", appCap.getCapability("pi").toString());
    Assert.assertEquals(DriverType.BROWSER, appCap.getApplauseOptions().getDriverType());

    // We have an array in the class.  Check that
    final Object shouldBeList = appCap.getCapability("someArray");
    Assert.assertNotNull(shouldBeList);
    Assert.assertTrue(shouldBeList instanceof List);
    List<Object> myList = (List<Object>) shouldBeList;
    Assert.assertEquals("1", myList.get(0).toString());
    Assert.assertEquals("2.71", myList.get(2).toString());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFromJsonStringGoodPath2() throws IOException, BadJsonFormatException {
    final File p1Chrome = new File(DATA_DIR, "applauseCaps_provider4_iphone.json");
    final String p1ChromeStr = FileUtils.readFileToString(p1Chrome, "UTF-8");
    final EnhancedCapabilities appCap = EnhancedCapabilities.fromJsonString(p1ChromeStr);
    Assert.assertNotNull(appCap);
    // Check values that we expect to be there
    // Check to see that we have NOT converted Longs to Doubles as out-of-the-box Gson done
    Assert.assertEquals("600", appCap.getCapability("commandTimeout").toString());
    Assert.assertEquals(DriverType.MOBILENATIVE, appCap.getApplauseOptions().getDriverType());
    Assert.assertEquals(Platform.MOBILE_IOS_PHONE, appCap.getApplauseOptions().getFactoryKey());

    // We have an array in the class.  Check that
    final Map<String, Object> shouldBeMap =
        (Map<String, Object>) appCap.asMap().get(ApplauseCapabilitiesConstants.APPLAUSE_OPTIONS);
    Assert.assertNotNull(shouldBeMap);
    final Map<String, Object> embeddedMap = (Map<String, Object>) shouldBeMap.get("embeddedMap");
    Assert.assertEquals("3", embeddedMap.get("three").toString());
    Assert.assertEquals("6.626", embeddedMap.get("plank").toString());
  }
}
