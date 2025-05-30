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
package com.applause.auto.helpers.testdata;

import java.util.Collection;

public interface TestDataProvider {

  /**
   * read single value from test data file
   *
   * @param dataPathSyntax file path
   * @param <T> test data type
   * @return test data
   */
  <T> T readSingleValueFromTestDataFile(String dataPathSyntax);

  /**
   * read collection of values from test data file
   *
   * @param dataPathSyntax file path
   * @param <C> test data type
   * @return list of test data
   */
  <C extends Collection<?>> C readValuesFromTestDataFile(String dataPathSyntax);
}
