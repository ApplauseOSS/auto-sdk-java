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
package com.applause.auto.reporting;

import com.applause.auto.reporting.params.ApplauseRunCreation;

/** Interface describing a TestRun session initializer */
@FunctionalInterface
public interface ISessionInitializer {

  /**
   * Starts a new Applause TestRun with the given parameters
   *
   * @param params The ApplauseRunCreation parameters
   * @return An IApplauseReporter for the new TestRun
   */
  IApplauseReporter startTestRun(ApplauseRunCreation params);
}
