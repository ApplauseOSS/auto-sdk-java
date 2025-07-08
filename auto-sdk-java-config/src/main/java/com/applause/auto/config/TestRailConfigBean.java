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
package com.applause.auto.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

/**
 * testrail configuration class that holds global config vars coming from testrail.properties or
 * overridden on the command line
 */
@LoadPolicy(LoadType.MERGE)
// testrail.properties comes from the user of the SDK.
@Sources({"classpath:props/testrail.properties", "classpath:cfg/testrail_default.properties"})
public interface TestRailConfigBean extends Config {

  /**
   * The base url of the testrail service to use when reporting to testrail from the SDK
   *
   * @return The testrail base url
   */
  @DefaultValue("https://simian.testrail.com/")
  String testRailBaseUrl();

  /**
   * The username to use when reporting to testrail from the SDK
   *
   * @return The testrail username
   */
  String testRailUsername();

  /**
   * The api key to use when reporting to testrail from the SDK
   *
   * @return The testrail api key
   */
  String testRailApiKey();

  /**
   * The testrail status mapping for passed results
   *
   * @return The testrail passed status mapping
   */
  String statusPassed();

  /**
   * The testrail status mapping for failed results
   *
   * @return The testrail failed status mapping
   */
  String statusFailed();

  /**
   * The testrail status mapping for skipped results
   *
   * @return The testrail skipped status mapping
   */
  String statusSkipped();

  /**
   * The testrail status mapping for error results
   *
   * @return The testrail error status mapping
   */
  String statusError();

  /**
   * The testrail status mapping for canceled results
   *
   * @return The testrail canceled status mapping
   */
  String statusCanceled();

  /**
   * The TestRail Project ID
   *
   * @return The TestRail Project ID
   */
  Long testRailProjectId();

  /**
   * The TestRail Suite ID
   *
   * @return The TestRail Suite ID
   */
  Long testRailSuiteId();

  /**
   * The TestRail Plan Name
   *
   * @return The TestRail Plan Name
   */
  String testRailPlanName();

  /**
   * The TestRail Run Name
   *
   * @return The TestRail Run Name
   */
  String testRailRunName();

  /**
   * If all tests for the provided TestRail suite should be automatically added to the TestRail plan
   *
   * @return If all tests should be added to the TestRail plan
   */
  @DefaultValue("false")
  boolean addAllTestsToPlan();
}
