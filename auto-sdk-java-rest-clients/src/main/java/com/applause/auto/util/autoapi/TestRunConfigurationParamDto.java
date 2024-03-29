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
import lombok.NoArgsConstructor;

/** POJO representation of the JSON body sent as part of client-side test execution. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestRunConfigurationParamDto {
  private boolean addAllTestsToPlan;

  private String driverConfig;
  private Long itwTestCycleId;
  private Long productId;
  private String sdkVersion;
  private String testRailPlanName;
  private Long testRailProjectId;
  private boolean testRailReportingEnabled;
  private String testRailRunName;
  private Long testRailSuiteId;
  private Set<String> tests;
  private String clientSubmissionId;
}
