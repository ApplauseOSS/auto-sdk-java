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

/** Gets or Sets testingType */
@Getter
public enum TestingTypeEnum {
  FUNCTIONAL("FUNCTIONAL"),

  USABILITY("USABILITY"),

  LOAD_PERFORMANCE("LOAD_PERFORMANCE"),

  SECURITY("SECURITY"),

  LOCALIZATION("LOCALIZATION"),

  AUTOMATION("AUTOMATION"),

  ACCESSIBILITY("ACCESSIBILITY"),

  PAYMENT_INSTRUMENTS("PAYMENT_INSTRUMENTS"),

  OMNI_CHANNEL("OMNI_CHANNEL"),

  VOICE("VOICE"),

  MISCELLANEOUS("MISCELLANEOUS");

  private final String value;

  TestingTypeEnum(final String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static TestingTypeEnum fromValue(final String input) {
    for (TestingTypeEnum b : TestingTypeEnum.values()) {
      if (b.value.equals(input)) {
        return b;
      }
    }
    return null;
  }
}
