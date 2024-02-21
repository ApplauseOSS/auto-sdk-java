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

import com.applause.auto.util.autoapi.TestResultEndStatus;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Parameters used to submit an Applause Result */
@Data
@RequiredArgsConstructor
public class ApplauseResultSubmission {
  private final @NonNull TestResultEndStatus status;
  private @Nullable String failureReason;
  private final @NonNull Set<String> providerSessionIds = new HashSet<>();

  /**
   * Adds a collection of Provider Session IDs
   *
   * @param ids A Collection of Provider Session IDs
   * @return The Updated ApplauseResultSubmission DTO
   */
  public ApplauseResultSubmission addProviderSessionIds(final Collection<String> ids) {
    this.providerSessionIds.addAll(ids);
    return this;
  }
}
