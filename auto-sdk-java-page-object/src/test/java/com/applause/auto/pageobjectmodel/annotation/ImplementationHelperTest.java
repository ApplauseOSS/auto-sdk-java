/*
 *
 * Copyright © 2025 Applause App Quality, Inc.
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

import static org.testng.Assert.*;

import com.applause.auto.data.enums.Platform;
import org.testng.annotations.Test;

public class ImplementationHelperTest {

  @Test
  public void testAbstractParentImplementations() {

    final var androidClass =
        ImplementationHelper.getImplementation(
            AbstractParentImplementation.class, Platform.MOBILE_ANDROID);
    assertEquals(androidClass.getSimpleName(), "ChildAndroidImplementation");

    final var iosClass =
        ImplementationHelper.getImplementation(
            AbstractParentImplementation.class, Platform.MOBILE_IOS);
    assertEquals(iosClass.getSimpleName(), "ChildIOSImplementation");

    final var webClass =
        ImplementationHelper.getImplementation(AbstractParentImplementation.class, Platform.WEB);
    assertEquals(webClass.getSimpleName(), "AbstractParentImplementation");
  }
}
