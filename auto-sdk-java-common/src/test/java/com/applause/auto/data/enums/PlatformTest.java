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
package com.applause.auto.data.enums;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PlatformTest {
  protected final Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

  @Test
  public void getPlatformTest() {
    Assert.assertEquals(Platform.getPlatform("Unspecified"), Platform.DEFAULT);
    Assert.assertEquals(Platform.getPlatform("Something"), Platform.DEFAULT);
    Assert.assertEquals(Platform.getPlatform("MOBILE"), Platform.MOBILE);
    Assert.assertEquals(Platform.getPlatform("web"), Platform.WEB);
    Assert.assertEquals(
        Platform.getPlatform("mObIlEaNdRoIdSmAlLtAbLeT"), Platform.MOBILE_ANDROID_SMALL_TABLET);
    Assert.assertEquals(Platform.getPlatform("web_ios_phone"), Platform.WEB_IOS_PHONE);
    Assert.assertEquals(
        Platform.getPlatform("MOBILE_ANDROID_PHONE"), Platform.MOBILE_ANDROID_PHONE);
  }

  @Test
  public void platformFallbacksTest() {
    logger.info("Verifying that DEFAULT falls back to null.");
    Assert.assertNull(Platform.DEFAULT.getFallback());

    logger.info("Verifying that MOBILE and WEB fall back to DEFAULT.");
    Assert.assertEquals(Platform.MOBILE.getFallback(), Platform.DEFAULT);
    Assert.assertEquals(Platform.WEB.getFallback(), Platform.DEFAULT);

    logger.info("Verifying that MOBILE_ANDROID and MOBILE_IOS fall back to MOBILE.");
    Assert.assertEquals(Platform.MOBILE_ANDROID.getFallback(), Platform.MOBILE);
    Assert.assertEquals(Platform.MOBILE_IOS.getFallback(), Platform.MOBILE);

    logger.info(
        "Verifying that MOBILE_ANDROID_PHONE, MOBILE_ANDROID_TABLET, and MOBILE_ANDROID_SMALLTABLET fall back"
            + " to MOBILE_ANDROID.");
    Assert.assertEquals(Platform.MOBILE_ANDROID_PHONE.getFallback(), Platform.MOBILE_ANDROID);
    Assert.assertEquals(Platform.MOBILE_ANDROID_TABLET.getFallback(), Platform.MOBILE_ANDROID);
    Assert.assertEquals(
        Platform.MOBILE_ANDROID_SMALL_TABLET.getFallback(), Platform.MOBILE_ANDROID);

    logger.info(
        "Verifying that MOBILE_IOS_PHONE, MOBILE_IOS_TABLET, and MOBILE_IOS_SMALLTABLET fall back to "
            + "MOBILE_IOS.");
    Assert.assertEquals(Platform.MOBILE_IOS_PHONE.getFallback(), Platform.MOBILE_IOS);
    Assert.assertEquals(Platform.MOBILE_IOS_TABLET.getFallback(), Platform.MOBILE_IOS);
    Assert.assertEquals(Platform.MOBILE_IOS_SMALL_TABLET.getFallback(), Platform.MOBILE_IOS);

    logger.info("Verifying that WEB_DESKTOP and WEB_MOBILE fall back to WEB.");
    Assert.assertEquals(Platform.WEB_DESKTOP.getFallback(), Platform.WEB);
    Assert.assertEquals(Platform.WEB_MOBILE.getFallback(), Platform.WEB);

    logger.info(
        "Verifying that WEB_DESKTOP_CHROME, WEB_DESKTOP_EDGE, WEB_DESKTOP_FIREFOX, WEB_DESKTOP_IE, and "
            + "WEB_DESKTOP_SAFARI fall back to WEB_DESKTOP.");
    Assert.assertEquals(Platform.WEB_DESKTOP_CHROME.getFallback(), Platform.WEB_DESKTOP);
    Assert.assertEquals(Platform.WEB_DESKTOP_EDGE.getFallback(), Platform.WEB_DESKTOP);
    Assert.assertEquals(Platform.WEB_DESKTOP_FIREFOX.getFallback(), Platform.WEB_DESKTOP);
    Assert.assertEquals(Platform.WEB_DESKTOP_IE.getFallback(), Platform.WEB_DESKTOP);
    Assert.assertEquals(Platform.WEB_DESKTOP_SAFARI.getFallback(), Platform.WEB_DESKTOP);

    logger.info(
        "Verifying that WEB_MOBILE_PHONE, WEB_MOBILE_TABLET, and WEB_MOBILE_SMALLTABLET fall back to "
            + "WEB_MOBILE.");
    Assert.assertEquals(Platform.WEB_MOBILE_PHONE.getFallback(), Platform.WEB_MOBILE);
    Assert.assertEquals(Platform.WEB_MOBILE_TABLET.getFallback(), Platform.WEB_MOBILE);
    Assert.assertEquals(Platform.WEB_MOBILE_SMALL_TABLET.getFallback(), Platform.WEB_MOBILE);

    logger.info(
        "Verifying that WEB_ANDROID_PHONE and WEB_IOS_PHONE fall back to WEB_MOBILE_PHONE.");
    Assert.assertEquals(Platform.WEB_ANDROID_PHONE.getFallback(), Platform.WEB_MOBILE_PHONE);
    Assert.assertEquals(Platform.WEB_IOS_PHONE.getFallback(), Platform.WEB_MOBILE_PHONE);

    logger.info(
        "Verifying that WEB_ANDROID_TABLET and WEB_IOS_TABLET fall back to WEB_MOBILE_TABLET.");
    Assert.assertEquals(Platform.WEB_ANDROID_TABLET.getFallback(), Platform.WEB_MOBILE_TABLET);
    Assert.assertEquals(Platform.WEB_IOS_TABLET.getFallback(), Platform.WEB_MOBILE_TABLET);

    logger.info(
        "Verifying that WEB_ANDROID_SMALL_TABLET and WEB_IOS_SMALL_TABLET fall back to "
            + "WEB_MOBILE_SMALL_TABLET.");
    Assert.assertEquals(
        Platform.WEB_ANDROID_SMALL_TABLET.getFallback(), Platform.WEB_MOBILE_SMALL_TABLET);
    Assert.assertEquals(
        Platform.WEB_IOS_SMALL_TABLET.getFallback(), Platform.WEB_MOBILE_SMALL_TABLET);
  }

  @DataProvider(name = "platformFallbackIsNative")
  public static Object[][] validatePlatformFallbackNative() {
    return new Object[][] {
      {Platform.DEFAULT, false},
      {Platform.MOBILE, true},
      {Platform.MOBILE_IOS, true},
      {Platform.MOBILE_ANDROID_SMALL_TABLET, true},
      {Platform.WEB_MOBILE, false},
      {Platform.WEB_ANDROID_PHONE, false},
      {Platform.WEB_IOS_SMALL_TABLET, false},
      {Platform.OTT_FIRE_TV, false}
    };
  }

  @Test(dataProvider = "platformFallbackIsNative")
  public void testHasNativeFallback(Platform platform, boolean expectedResult) {
    Assert.assertEquals(expectedResult, Platform.hasNativeFallback(platform));
  }
}
