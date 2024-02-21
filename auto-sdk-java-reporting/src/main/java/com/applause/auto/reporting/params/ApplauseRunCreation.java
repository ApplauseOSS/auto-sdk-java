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
import lombok.NoArgsConstructor;
import lombok.NonNull;

/** Parameters used to create a new Applause Test Run */
@Data
@NoArgsConstructor
public class ApplauseRunCreation {
  private @Nullable String driverConfig;
  private final @NonNull Set<String> precreatedResults = new HashSet<>();

  /**
   * Adds a collection of results to be pre-created
   *
   * @param tests The collection of tests to be pre-created
   * @return The updated ApplauseRunCreation DTO
   */
  public ApplauseRunCreation addPrecreatedResults(final Collection<String> tests) {
    this.precreatedResults.addAll(tests);
    return this;
  }
}
