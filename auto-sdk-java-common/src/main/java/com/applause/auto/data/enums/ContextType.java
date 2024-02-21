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

/** Enum defining the different types of contexts to set up */
public enum ContextType {
  /** Driverless - no selenium driver is created or attached to the context */
  DRIVERLESS("DRIVERLESS"),
  /** Driver - creates a selenium driver and attaches it to the context */
  DRIVER("DRIVER");

  private final String type;

  ContextType(final String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return this.type;
  }
}
