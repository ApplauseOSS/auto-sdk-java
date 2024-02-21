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

import com.applause.auto.data.enums.ContextType;
import com.applause.auto.util.autoapi.TestResultEndStatus;
import java.util.Set;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** A data record that contains all current information about an Applause TestResult */
@Data
@RequiredArgsConstructor
public class ResultRecord {
  private final @NonNull String testResultUuid;
  private String testCaseName;
  private String testCaseIterationTag;
  private String parameterString;
  private String contextId;
  private ContextType contextType;
  private String testRailCaseId;
  private Long applauseTestCaseId;
  private Set<String> providerSessionIds;
  private Long testResultId;
  private boolean submitted;
  private TestResultEndStatus status;
}
