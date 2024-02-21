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
package com.applause.auto.context;

/**
 * Connection logic from Applause Framework Context to the External Framework (TestNG, Cucumber,
 * Etc.)
 *
 * @param <T> The external framework context type
 */
public interface IContextConnector<T> {
  /**
   * Gets the Applause result id for this context
   *
   * @return the Applause result id for this context
   */
  Long getResultId();

  /**
   * Gets the framework test case name for this context
   *
   * @return the framework test case name for this context
   */
  String getTestCaseName();

  /**
   * Gets the external framework context
   *
   * @return the external framework context
   */
  T externalContext();
}
