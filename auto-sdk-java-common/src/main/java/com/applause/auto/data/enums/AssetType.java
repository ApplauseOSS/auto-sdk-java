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

import lombok.Getter;

/** Enum to define the different types of assets supported by the framework */
@Getter
public enum AssetType {
  /** A Screenshot Asset */
  SCREENSHOT("SCREENSHOT"),
  /** A Screenshot Asset taken on failure of a test case */
  FAILURE_SCREENSHOT("FAILURE_SCREENSHOT"),
  /** A Video Asset */
  VIDEO("VIDEO"),
  /** A Har Log from the tested device */
  NETWORK_HAR("NETWORK_HAR"),
  /** A Vitals Log from the tested device */
  VITALS_LOG("VITALS_LOG"),
  /** A Console Log from the SDK */
  CONSOLE_LOG("CONSOLE_LOG"),
  /** A network Log from the tested browser */
  NETWORK_LOG("NETWORK_LOG"),
  /** A console Log from the tested device */
  DEVICE_LOG("DEVICE_LOG"),
  /** A network Log from the selenium server */
  SELENIUM_LOG("SELENIUM_LOG"),
  /** A console Log from the tested browser */
  BROWSER_LOG("BROWSER_LOG"),
  /** A console Log from the selenium server */
  FRAMEWORK_LOG("FRAMEWORK_LOG"),
  /** An Email asset */
  EMAIL("EMAIL"),
  /** The source of the tested page */
  PAGE_SOURCE("PAGE_SOURCE"),
  /** Any other uncategorized asset */
  UNKNOWN("UNKNOWN");

  private final String value;

  AssetType(final String value) {
    this.value = value;
  }
}
