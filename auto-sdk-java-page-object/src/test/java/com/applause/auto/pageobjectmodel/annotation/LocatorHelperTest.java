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
package com.applause.auto.pageobjectmodel.annotation;

import com.applause.auto.data.enums.Platform;
import com.applause.auto.pageobjectmodel.enums.Strategy;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LocatorHelperTest {

  public String noLocatorAnnotation;

  @Locate(on = Platform.DEFAULT, using = Strategy.CSS, value = "")
  public String singleLocator;

  @Locate(on = Platform.MOBILE_IOS_SMALL_TABLET, using = Strategy.CSS, value = "")
  public String noMatchingLocatorAnnotation;

  @Locate(on = Platform.DEFAULT, using = Strategy.CSS, value = "abc")
  @Locate(on = Platform.DEFAULT, using = Strategy.ID, value = "def")
  public String multipleMatchingLocatorAnnotation;

  @Locates(value = {})
  public String emptyLocates;

  @Locates(
      value = {@Locate(on = Platform.MOBILE_IOS_SMALL_TABLET, using = Strategy.CSS, value = "")})
  public String noMatchingLocatorInLocatesAnnotation;

  @Locates(
      value = {
        @Locate(on = Platform.DEFAULT, using = Strategy.CSS, value = "abc"),
        @Locate(on = Platform.DEFAULT, using = Strategy.ID, value = "def")
      })
  public String multipleMatchingLocatorsInLocatesAnnotation;

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = ".*no Locate annotations.*")
  public void testNoMatchingLocators() throws NoSuchFieldException, SecurityException {
    LocatorHelper.getLocator(this.getClass().getField("noLocatorAnnotation"), Platform.DEFAULT);
  }

  @Test
  public void testSingleLocatorLookupWithExactPlatform()
      throws NoSuchFieldException, SecurityException {
    var locator =
        LocatorHelper.getLocator(this.getClass().getField("singleLocator"), Platform.DEFAULT);
    Assert.assertEquals(locator.getLocatorString(), "");
    Assert.assertEquals(locator.getPlatform(), Platform.DEFAULT);
    Assert.assertEquals(locator.getStrategy(), Strategy.CSS);
  }

  @Test
  public void testSingleLocatorFallback() throws NoSuchFieldException, SecurityException {
    var locator =
        LocatorHelper.getLocator(
            this.getClass().getField("singleLocator"), Platform.MOBILE_ANDROID);
    Assert.assertEquals(locator.getLocatorString(), "");
    Assert.assertEquals(locator.getPlatform(), Platform.DEFAULT);
    Assert.assertEquals(locator.getStrategy(), Strategy.CSS);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = ".*no Locate annotations for platform \\[Default\\].*")
  public void testNoMatchingLocatorsForPlatform() throws NoSuchFieldException, SecurityException {
    LocatorHelper.getLocator(
        this.getClass().getField("noMatchingLocatorAnnotation"), Platform.DEFAULT);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          ".*more than one annotation matching the platform \\[Default\\].*")
  public void testMultipleMatchingLocatorsForPlatform()
      throws NoSuchFieldException, SecurityException {
    LocatorHelper.getLocator(
        this.getClass().getField("multipleMatchingLocatorAnnotation"), Platform.DEFAULT);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = ".*no Locate annotations.*")
  public void testEmptyLocates() throws NoSuchFieldException, SecurityException {
    LocatorHelper.getLocator(this.getClass().getField("emptyLocates"), Platform.DEFAULT);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = ".*no Locate annotations for platform \\[Default\\].*")
  public void testNoMatchingLocatorInLocatesAnnotation()
      throws NoSuchFieldException, SecurityException {
    LocatorHelper.getLocator(
        this.getClass().getField("noMatchingLocatorInLocatesAnnotation"), Platform.DEFAULT);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          ".*more than one annotation matching the platform \\[Default\\].*")
  public void testMultipleMatchingLocatorsInLocatesAnnotation()
      throws NoSuchFieldException, SecurityException {
    LocatorHelper.getLocator(
        this.getClass().getField("multipleMatchingLocatorsInLocatesAnnotation"), Platform.DEFAULT);
  }
}
