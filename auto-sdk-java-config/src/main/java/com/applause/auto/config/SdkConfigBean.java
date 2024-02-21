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

import static org.aeonbits.owner.Config.DisableableFeature.PARAMETER_FORMATTING;
import static org.aeonbits.owner.Config.DisableableFeature.VARIABLE_EXPANSION;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

/**
 * main configuration class that holds global config vars coming from system.properties or
 * overridden on the command line
 */
@LoadPolicy(LoadType.MERGE)
@Sources({"classpath:props/system.properties"})
@SuppressWarnings("PMD.ExcessivePublicCount")
public interface SdkConfigBean extends Config {

  /**
   * The default output path for assets
   *
   * @return The default output path for assets
   */
  @DisableFeature(VARIABLE_EXPANSION)
  String localResultsDirectory();

  /**
   * The Appium URL to be used when local drivers are enabled
   *
   * @return The local Appium URL
   */
  String localAppiumUrl();

  /**
   * Toggle use of local selenium grid
   *
   * @return local grid enabled/disabled
   */
  @DefaultValue("false")
  boolean useSeleniumGrid();

  /**
   * The location the selenium grid should point to
   *
   * @return The selenium grid location
   */
  String seleniumGridLocation();

  /**
   * file containing selenium capabilities configuration
   *
   * @return the capabilities file
   */
  String capsFile();

  /**
   * The mobile app under test
   *
   * @return app resource under test
   */
  @DisableFeature(PARAMETER_FORMATTING)
  String app();

  /**
   * Toggle performance logging
   *
   * @return boolean of logging enabled
   */
  @DefaultValue("false")
  boolean performanceLogging();

  /**
   * Determines whether the applause configuration should be dumped to the console
   *
   * @return true, if the config should be logged
   */
  @DefaultValue("true")
  boolean dumpConfig();

  /**
   * The default timeout for element selection
   *
   * @return the timeout in seconds
   */
  @DefaultValue("10")
  long defaultTimeoutSeconds();

  /**
   * The polling interval for element selection
   *
   * @return the interval in seconds
   */
  @DefaultValue("1")
  long defaultPollingIntervalSeconds();

  /**
   * The selenium http-client read timeout
   *
   * @return the timeout in minutes
   */
  @DefaultValue("60")
  long seleniumReadTimeoutMinutes();

  /**
   * The maximum number of times to retry the driver connection
   *
   * @return The number of driver connection retries
   */
  @DefaultValue("1")
  int driverRetryCount();

  /**
   * Support pushing of mobile app images to a Selenium Provider
   *
   * @return the provider
   */
  String appPushProvider();

  /**
   * The class to use for mobile app image pushing
   *
   * @return the app push class
   */
  String appPushClass();

  /**
   * The source of the app to push
   *
   * @return the app push source
   */
  @DisableFeature(PARAMETER_FORMATTING)
  String appPushSource();

  /**
   * The target url to use for mobile app image pushing
   *
   * @return the app push target
   */
  @DisableFeature(PARAMETER_FORMATTING)
  String appPushTargetUrl();

  /**
   * The user to use for mobile app image pushing
   *
   * @return the app push user
   */
  String appPushUser();

  /**
   * The password to use for mobile app image pushing
   *
   * @return the app push password
   */
  String appPushPassword();

  /**
   * URL of proxy to use for ALL http requests
   *
   * @return the String URL
   */
  String httpProxyUrl();

  /**
   * A feature flag since this needs to be turned off until it's integrated with TestRail upload in
   * auto-api, or upload code in auto-api is removed
   *
   * @return whether enabled
   */
  @DefaultValue("false")
  boolean sdkTestRailResultSubmissionEnabled();

  /**
   * Toggles use of local drivers for framework
   *
   * @return boolean enabled/disabled
   */
  @DefaultValue("false")
  boolean useLocalDrivers();

  /**
   * Toggles reverseDns validation
   *
   * @return boolean enabled/disabled
   */
  @DefaultValue("false")
  boolean noReverseDnsCheck();
}
