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
package com.applause.auto.data.enums;

/** Enum to specify the different driver strings for browsers and devices. */
public enum DriverType {
  /** A generic browser driver */
  BROWSER("browser"), //
  /** A generic mobile native driver */
  MOBILENATIVE("mobilenative"), //
  /** A generic mobile web driver */
  MOBILEWEB("mobileweb"), //
  /** A firefox driver */
  FIREFOX("firefox"), //
  /** A chrome driver */
  CHROME("chrome"), //
  /** An internet explorer driver */
  INTERNETEXPLORER("internet explorer"), //
  /** A safari driver */
  SAFARI("safari"), //
  /** A Microsoft Edge driver */
  EDGE("edge"), //
  /** An Android driver */
  ANDROID("android"), //
  /** An IOS driver */
  IOS("ios");

  private final String name;

  DriverType(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * * Convert string to enum.
   *
   * @param val string rep of enum value
   * @return null if unknown, the enum otherwise
   */
  public static DriverType fromString(final String val) {
    for (DriverType dt : DriverType.values()) {
      if (dt.toString().equalsIgnoreCase(val)) {
        return dt;
      }
    }
    return null;
  }
}
