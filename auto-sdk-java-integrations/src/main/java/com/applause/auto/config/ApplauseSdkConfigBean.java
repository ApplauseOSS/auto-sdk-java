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

import com.applause.auto.data.enums.ResultsMode;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

/**
 * main configuration class that holds global config vars coming from system.properties or
 * overridden on the command line
 */
@LoadPolicy(LoadType.MERGE)
// system.properties comes from the user of the SDK.
// applause_environment.properties is in this project and contains the defaults
@Sources({"classpath:props/system.properties", "classpath:applause_environment.properties"})
public interface ApplauseSdkConfigBean extends Config {
  /**
   * Gets Applause API Key
   *
   * @return The Applause API Key
   */
  String apiKey();

  /**
   * The Results Download Mode
   *
   * @see ResultsMode
   * @return The Results Download Mode
   */
  @ConverterClass(AnyCaseEnumConverter.class)
  @DefaultValue("ON_FAILURE")
  ResultsMode downloadResults();

  /**
   * The maximum number of attempts to download the asset zip file after the run completes
   *
   * @return The maximum number of asset download attempts
   */
  @DefaultValue("5")
  Long assetFetchRetries();

  /**
   * The Applause Test Cycle ID for reporting
   *
   * @return The Applause Test Cycle ID
   */
  Long applauseTestCycleId();

  /**
   * A flag to specify whether test cycle cloning is enabled
   *
   * @return True if test cycle cloning is enabled
   */
  @ConverterClass(AnyCaseEnumConverter.class)
  @DefaultValue("DISABLED")
  TestCycleCloneMode cloneTestCycleMode();

  /**
   * A flag to specify whether test cycle cloning is enabled
   *
   * @return True if test cycle cloning is enabled
   */
  @DefaultValue("applauseTestCycleId")
  String testCycleCloneOutputParameter();

  /**
   * A test cycle ID to clone
   *
   * @return The test cycle ID to clone
   */
  Long templateTestCycleId();

  /**
   * An optional override for the test cycle name when test cycle cloning is enabled
   *
   * @return The test cycle name override if provided
   */
  String cloneToTestCycleName();

  /**
   * Determines if TestRail reporting is enabled through Applause Services
   *
   * @return True if TestRail reporting is enabled
   */
  @DefaultValue("false")
  boolean testRailReportingEnabled();

  /**
   * The Applause Product ID
   *
   * @return the applause product id
   */
  @ConverterClass(EmptyLongConverter.class)
  Long productId();

  /**
   * The Applause Automation API URL
   *
   * @return the Automation API URL
   */
  String autoApiUrl();

  /**
   * The Applause Selenium Proxy URL
   *
   * @return the Applause Selenium Proxy URL
   */
  String seleniumProxyUrl();

  /**
   * The Applause Public API URL
   *
   * @return the Public API URL
   */
  String applausePublicApiUrl();

  /**
   * The Application Build ID in the Applause Platform
   *
   * @return The build id
   */
  Long buildId();

  /**
   * The Application Version ID in the Applause Platform
   *
   * @return The version id
   */
  Long versionId();

  /**
   * Toggle configuration and use of applause backend services
   *
   * @return boolean of backend services enabled/disabled
   */
  @DefaultValue("true")
  boolean reportingEnabled();

  /**
   * A grouping name for provider sessions in the Applause automation admin panel
   *
   * @return The provider session grouping name
   */
  String providerSessionRunName();
}
