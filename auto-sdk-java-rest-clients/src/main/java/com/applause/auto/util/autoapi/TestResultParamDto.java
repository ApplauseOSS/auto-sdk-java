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
package com.applause.auto.util.autoapi;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

/** DTO used for creating test results */
@Data
@AllArgsConstructor
public class TestResultParamDto {
  /** id of test session this result should be tied to */
  private Long testResultId;

  /** how test finished */
  private TestResultEndStatus status;

  /** reason if test failed */
  private String failureReason;

  /** A set of provider session ids from the test result */
  private Set<String> providerSessionGuids;
}
