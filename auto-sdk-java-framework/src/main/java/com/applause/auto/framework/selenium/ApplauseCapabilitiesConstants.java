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
package com.applause.auto.framework.selenium;

/** A list of capability constants used by the Applause framework */
@SuppressWarnings("PMD.DataClass")
public final class ApplauseCapabilitiesConstants {
  /** The Applause Options sub capability */
  public static final String APPLAUSE_OPTIONS = "applause:options";

  /**
   * An applause:options capability The type of the driver, used for determining which class we
   * should instantiate
   */
  public static final String DRIVER_TYPE = "driverType";

  /**
   * An applause:options capability specifying the OS for the driver, used for determining which
   * class we should instantiate
   */
  public static final String OS_NAME = "osName";

  /**
   * An applause:options capability specifying the platform key, used to determine which
   * locators/class implementation to use
   */
  public static final String FACTORY_KEY = "factoryKey";

  /**
   * An applause:options capability specifying whether the driver is a mobile native driver, used
   * for determining whether to do app auto-detection or app push
   */
  public static final String IS_MOBILE_NATIVE = "isMobileNative";

  /** An applause:options capability specifying the applause api key */
  public static final String API_KEY = "apiKey";

  /** An applause:options capability specifying the applause product id */
  public static final String PRODUCT_ID = "productId";

  /** An applause:options capability specifying a grouping name for linking provider sessions */
  public static final String RUN_NAME = "runName";

  /** An applause:options capability specifying the applause test cycle id */
  public static final String TEST_CYCLE_ID = "testCycleId";

  /** An applause:options capability specifying an applause test cycle name */
  public static final String TEST_CYCLE_NAME = "testCycleName";

  /** An applause:options capability specifying an applause test cycle grouping name */
  public static final String TEST_CYCLE_GROUP_NAME = "testCycleGroupName";

  /**
   * An applause:options capability specifying an applause test cycle to use as a template for a
   * cloned test cycle
   */
  public static final String TEST_CYCLE_TEMPLATE_ID = "testCycleTemplateId";

  /** An applause:options capability specifying the ttl for the test cycle group name */
  public static final String TEST_CYCLE_TTL = "testCycleTTL";

  /** An applause:options capability specifying a test cycle start date for a clone test cycle */
  public static final String TEST_CYCLE_START_DATE = "testCycleStartDate";

  /** An applause:options capability specifying a test cycle end date for a clone test cycle */
  public static final String TEST_CYCLE_END_DATE = "testCycleEndDate";

  /**
   * An applause:options capability specifying whether performance logging is enabled for the
   * provider session
   */
  public static final String PERFORMANCE_LOGGING = "performanceLogging";

  private ApplauseCapabilitiesConstants() {
    // Keep the linter happy
  }
}
