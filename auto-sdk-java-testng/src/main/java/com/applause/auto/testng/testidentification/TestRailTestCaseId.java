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
package com.applause.auto.testng.testidentification;

import java.lang.annotation.*;

/**
 * Assign a TestRail test case identifier to a specific test case. Assignment happens on a method.
 * This is a replacement to putting the identifier in the description field of the testng Test
 * annotation. Use this class to decorate specific methods of a java class. This method should be
 * used in conjunction with the TestNG Test Annotation.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestRailTestCaseId {
  /**
   * Specify the TestRail Test Case Identifier
   *
   * @return the TestRail Test Case Identifier
   */
  String value();
}
