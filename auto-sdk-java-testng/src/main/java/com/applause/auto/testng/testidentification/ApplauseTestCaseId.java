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
 * Assign a list of Applause test case identifiers to a specific test case. Use this class to
 * decorate specific methods of a java class. This method should be used in conjunction with the
 * TestNG Test Annotation.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApplauseTestCaseId {

  /**
   * Specify the Applause Test Case Identifiers. These can either be the unique numeric identifier,
   * for example 12345 or a name. If a name, it falls upon the developer to ensure that this name is
   * globally unique for the company and/or product
   *
   * @return the Applause Test Case Identifiers
   */
  String[] value();
}
