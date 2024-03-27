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

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/** Enum describing the valid states of the Applause Reporter */
public enum ApplauseReporterState {
  /** When the Applause Reporter is being configured */
  CONFIGURING,
  /** When the Applause Reporter is done being configured */
  CONFIGURED,
  /** When the Applause Reporter has started */
  STARTED,
  /** When the Applause Reporter is ended */
  ENDED;

  /** A set of configuration states that occur before the reporting has started */
  public static final Set<ApplauseReporterState> CONFIGURATION_STATES =
      ImmutableSet.of(CONFIGURING, CONFIGURED);
}
