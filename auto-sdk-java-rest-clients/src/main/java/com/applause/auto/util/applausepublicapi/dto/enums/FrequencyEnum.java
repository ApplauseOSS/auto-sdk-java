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
package com.applause.auto.util.applausepublicapi.dto.enums;

import lombok.Getter;

/** Gets or Sets frequency */
@Getter
public enum FrequencyEnum {
  UNSPECIFIED("UNSPECIFIED"),

  DAILY("DAILY"),

  EVERY_3_DAYS("EVERY_3_DAYS"),

  WEEKLY("WEEKLY"),

  EVERY_2_WEEKS_OR_MORE("EVERY_2_WEEKS_OR_MORE");

  private final String value;

  FrequencyEnum(final String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static FrequencyEnum fromValue(final String input) {
    for (FrequencyEnum b : values()) {
      if (b.value.equals(input)) {
        return b;
      }
    }
    return null;
  }
}
