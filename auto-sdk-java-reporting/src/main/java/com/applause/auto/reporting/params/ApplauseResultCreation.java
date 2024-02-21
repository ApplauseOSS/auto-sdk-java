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
package com.applause.auto.reporting.params;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Parameters used to create a new Applause Result */
@Data
@RequiredArgsConstructor
public class ApplauseResultCreation {
  private final @NonNull String testCaseName;
  private @Nullable String testCaseIterationTag;
  private @Nullable String parameterString;
  private @Nullable String testRailCaseId;
  private final @NonNull Set<String> applauseTestCaseIds = new HashSet<>();
  private final @NonNull Set<String> providerSessionIds = new HashSet<>();
  private @Nullable Long testRunId;

  /**
   * Adds a collection of Applause Test Case IDs
   *
   * @param ids A Collection of Applause Test Case IDs
   * @return The Updated ApplauseResultCreation DTO
   */
  public ApplauseResultCreation addApplauseTestCaseIds(final Collection<String> ids) {
    this.applauseTestCaseIds.addAll(ids);
    return this;
  }

  /**
   * Adds a collection of Provider Session IDs
   *
   * @param ids A Collection of Provider Session IDs
   * @return The Updated ApplauseResultCreation DTO
   */
  public ApplauseResultCreation addProviderSessionIds(final Collection<String> ids) {
    this.providerSessionIds.addAll(ids);
    return this;
  }
}
