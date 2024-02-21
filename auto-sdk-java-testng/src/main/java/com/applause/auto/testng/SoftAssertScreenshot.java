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
package com.applause.auto.testng;

import com.applause.auto.framework.ContextManager;
import com.applause.auto.helpers.ScreenshotHelper;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

/** A Soft Assertion that takes a screenshot when the assertion fails */
public class SoftAssertScreenshot extends SoftAssert {

  @Override
  public void onAssertFailure(final IAssert<?> a, final AssertionError ex) {
    StackTraceElement trace = Thread.currentThread().getStackTrace()[4];
    String fileName =
        String.format(
            "%s.%s_line%d_fail.png",
            trace.getClassName(), trace.getMethodName(), trace.getLineNumber());
    final var frameworkContext =
        ContextManager.INSTANCE
            .getCurrentContext()
            .orElseThrow(() -> new RuntimeException("No Framework context instance found"));
    final var screenshotHelper = new ScreenshotHelper(frameworkContext);
    screenshotHelper.takeScreenshot(fileName, true);
  }
}
