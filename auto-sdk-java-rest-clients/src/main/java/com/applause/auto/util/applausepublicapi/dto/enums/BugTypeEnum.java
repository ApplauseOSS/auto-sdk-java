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

/** Gets or Sets type */
@Getter
@SuppressWarnings("PMD.ExcessivePublicCount")
public enum BugTypeEnum {
  VISUAL("VISUAL"),

  FUNCTIONAL("FUNCTIONAL"),

  TECHNICAL("TECHNICAL"),

  FEEDBACK("FEEDBACK"),

  TEST_CASE_RESULT("TEST_CASE_RESULT"),

  MONITORING_TASK("MONITORING_TASK"),

  CORRUPTED_CHARACTERS("CORRUPTED_CHARACTERS"),

  MISSING_TRANSLATION("MISSING_TRANSLATION"),

  POOR_TRANSLATION("POOR_TRANSLATION"),

  TRUNCATION_AND_OVERLAP("TRUNCATION_AND_OVERLAP"),

  NUMBER_FORMAT("NUMBER_FORMAT"),

  OTHER_GENERAL("OTHER_GENERAL"),

  CRASH("CRASH"),

  SECURITY_A1_INJ("SECURITY_A1_INJ"),

  SECURITY_A2_AUTH("SECURITY_A2_AUTH"),

  SECURITY_A3_XSS("SECURITY_A3_XSS"),

  SECURITY_A4_REF("SECURITY_A4_REF"),

  SECURITY_A5_CONF("SECURITY_A5_CONF"),

  SECURITY_A6_DISC("SECURITY_A6_DISC"),

  SECURITY_A7_ACL("SECURITY_A7_ACL"),

  SECURITY_A8_CSRF("SECURITY_A8_CSRF"),

  SECURITY_A9_COMP("SECURITY_A9_COMP"),

  SECURITY_A10_REDIR("SECURITY_A10_REDIR"),

  SECURITY_D1_DRM("SECURITY_D1_DRM"),

  SECURITY_M1("SECURITY_M1"),

  SECURITY_M2("SECURITY_M2"),

  SECURITY_M3("SECURITY_M3"),

  SECURITY_M4("SECURITY_M4"),

  SECURITY_M5("SECURITY_M5"),

  SECURITY_M6("SECURITY_M6"),

  SECURITY_M7("SECURITY_M7"),

  SECURITY_M8("SECURITY_M8"),

  SECURITY_M9("SECURITY_M9"),

  SECURITY_M10("SECURITY_M10"),

  AUTOMATION("AUTOMATION"),

  LOAD_GUI("LOAD_GUI"),

  LOAD_FUNCTIONAL("LOAD_FUNCTIONAL"),

  LOAD_TECHNICAL("LOAD_TECHNICAL"),

  BFV_TEST_CASE_RESULT("BFV_TEST_CASE_RESULT"),

  CONTENT("CONTENT"),

  PERFORMANCE("PERFORMANCE"),

  ACCESSIBILITY_COLOR_CONTRAST("ACCESSIBILITY_COLOR_CONTRAST"),

  ACCESSIBILITY_KEYBOARD_NAVIGATION("ACCESSIBILITY_KEYBOARD_NAVIGATION"),

  ACCESSIBILITY_SCREEN_READERS("ACCESSIBILITY_SCREEN_READERS"),

  ACCESSIBILITY_HTML_VALIDATOR("ACCESSIBILITY_HTML_VALIDATOR"),

  ACCESSIBILITY_ZOOM("ACCESSIBILITY_ZOOM"),

  ACCESSIBILITY_COLOR("ACCESSIBILITY_COLOR"),

  ACCESSIBILITY_ANIMATION("ACCESSIBILITY_ANIMATION"),

  ACCESSIBILITY_VIDEO("ACCESSIBILITY_VIDEO"),

  ACCESSIBILITY_OTHER_A11Y("ACCESSIBILITY_OTHER_A11Y"),

  VOICE_VISUAL("VOICE_VISUAL"),

  VOICE_FUNCTIONAL("VOICE_FUNCTIONAL"),

  VOICE_TECHNICAL("VOICE_TECHNICAL"),

  VOICE_CONTENT("VOICE_CONTENT"),

  VOICE_PERFORMANCE("VOICE_PERFORMANCE"),

  VOICE_CRASH("VOICE_CRASH"),

  PAYMENT_INSTRUMENTS_VISUAL("PAYMENT_INSTRUMENTS_VISUAL"),

  PAYMENT_INSTRUMENTS_FUNCTIONAL("PAYMENT_INSTRUMENTS_FUNCTIONAL"),

  PAYMENT_INSTRUMENTS_TECHNICAL("PAYMENT_INSTRUMENTS_TECHNICAL"),

  PAYMENT_INSTRUMENTS_CONTENT("PAYMENT_INSTRUMENTS_CONTENT"),

  PAYMENT_INSTRUMENTS_PERFORMANCE("PAYMENT_INSTRUMENTS_PERFORMANCE"),

  PAYMENT_INSTRUMENTS_CRASH("PAYMENT_INSTRUMENTS_CRASH"),

  BUGTYPE_SECURITY_A_MISSING_BRUTE_FORCE_FLOODING(
      "BUGTYPE_SECURITY_A_MISSING_BRUTE_FORCE_FLOODING"),

  BUGTYPE_SECURITY_A_OTHER_APPLICATION("BUGTYPE_SECURITY_A_OTHER_APPLICATION"),

  BUGTYPE_SECURITY_A01_2021("BUGTYPE_SECURITY_A01_2021"),

  BUGTYPE_SECURITY_A02_2021("BUGTYPE_SECURITY_A02_2021"),

  BUGTYPE_SECURITY_A03_2017("BUGTYPE_SECURITY_A03_2017"),

  BUGTYPE_SECURITY_A03_2021("BUGTYPE_SECURITY_A03_2021"),

  BUGTYPE_SECURITY_A04_2017("BUGTYPE_SECURITY_A04_2017"),

  BUGTYPE_SECURITY_A04_2021("BUGTYPE_SECURITY_A04_2021"),

  BUGTYPE_SECURITY_A05_2021("BUGTYPE_SECURITY_A05_2021"),

  BUGTYPE_SECURITY_A06_2021("BUGTYPE_SECURITY_A06_2021"),

  BUGTYPE_SECURITY_A07_2017("BUGTYPE_SECURITY_A07_2017"),

  BUGTYPE_SECURITY_A07_2021("BUGTYPE_SECURITY_A07_2021"),

  BUGTYPE_SECURITY_A08_2017("BUGTYPE_SECURITY_A08_2017"),

  BUGTYPE_SECURITY_A08_2021("BUGTYPE_SECURITY_A08_2021"),

  BUGTYPE_SECURITY_A09_2010("BUGTYPE_SECURITY_A09_2010"),

  BUGTYPE_SECURITY_A09_2021("BUGTYPE_SECURITY_A09_2021"),

  BUGTYPE_SECURITY_A10_2021("BUGTYPE_SECURITY_A10_2021"),

  BUGTYPE_SECURITY_M_OTHER_MOBILE("BUGTYPE_SECURITY_M_OTHER_MOBILE"),

  BUGTYPE_SECURITY_M01_2016("BUGTYPE_SECURITY_M01_2016"),

  BUGTYPE_SECURITY_M02_2016("BUGTYPE_SECURITY_M02_2016"),

  BUGTYPE_SECURITY_M03_2016("BUGTYPE_SECURITY_M03_2016"),

  BUGTYPE_SECURITY_M04_2016("BUGTYPE_SECURITY_M04_2016"),

  BUGTYPE_SECURITY_M05_2016("BUGTYPE_SECURITY_M05_2016"),

  BUGTYPE_SECURITY_M06_2016("BUGTYPE_SECURITY_M06_2016"),

  BUGTYPE_SECURITY_M07_2016("BUGTYPE_SECURITY_M07_2016"),

  BUGTYPE_SECURITY_M08_2016("BUGTYPE_SECURITY_M08_2016"),

  BUGTYPE_SECURITY_M09_2016("BUGTYPE_SECURITY_M09_2016"),

  BUGTYPE_SECURITY_M10_2016("BUGTYPE_SECURITY_M10_2016"),

  BUGTYPE_SECURITY_I_IOT_SPECIFIC_FAILURE("BUGTYPE_SECURITY_I_IoT_SPECIFIC_FAILURE");

  private final String value;

  BugTypeEnum(final String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static BugTypeEnum fromValue(final String input) {
    for (BugTypeEnum b : values()) {
      if (b.value.equals(input)) {
        return b;
      }
    }
    return null;
  }
}
