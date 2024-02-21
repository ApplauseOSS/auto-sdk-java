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

import java.util.List;

/** Basic configuration information for an app push. Can be used by off-boarded customers */
public interface AppPushConfig {
  /**
   * The name of the provider to push an application image to. Examples: BrowserStack, SauceLabs
   *
   * @return The name of the provider to push an application image to
   */
  String appPushProvider();

  /**
   * The canonical java class that identifies the class that implements the AppPushTarget interface.
   * Usually we determine this class from the appPushProvider. This is a way for SDETs to write
   * their own implementation of AppPushTarget
   *
   * @return A canonical java class name.
   */
  String appPushClass();

  /**
   * Let the system assign an appPushClass if we've been able to deduce the value from the
   * providerID
   *
   * @param newValue the value to save
   * @return this so setting can be chained.
   */
  AppPushConfig appPushClass(String newValue);

  /**
   * The source image to push. Two forms are possible. A URL that begins with "http://" or
   * "https://". Anything that is not a URL in this form is assumed to be a file on the local file
   * system
   *
   * @return The name of the provider to push an application image to
   */
  String appPushSource();

  /**
   * The target URL where the image is to be pushed.
   *
   * @return The target URL where the image is to be pushed.
   */
  String appPushTargetUrl();

  /**
   * If HTTP authentication is required for the push of the application (and it usually is), this is
   * the username that should be used for HTTP basic authentication. There are two conditions under
   * which this can be null:
   *
   * <ol>
   *   <li>HTTP authentication is not required for the pushing if a file/url to the provider (this
   *       is rare)
   *   <li>The push is through the Applause automation services and they will provide the
   *       authentication information
   * </ol>
   *
   * @return The username to use for HTTP basic authentication:
   */
  String appPushUser();

  /**
   * If HTTP authentication is required for the push of the application (and it usually is), this is
   * the password that should be used for HTTP basic authentication. There are two conditions under
   * which this can be null:
   *
   * <ol>
   *   <li>HTTP authentication is not required for the pushing if a file/url to the provider (this
   *       is rare)
   *   <li>The push is through the Applause automation services and they will provide the
   *       authentication information
   * </ol>
   *
   * @return The password to use for HTTP basic authentication:
   */
  String appPushPassword();

  /**
   * Check if the configuration is sufficient to perform an application push
   *
   * @return true if the configuration is complete (sufficient to perform an application push) false
   *     otherwise
   */
  boolean isComplete();

  /**
   * List the set of missing parameters required to be present in order of isComplete to return true
   *
   * @return A list of missing parameters. Empty list if all required parameters are present.
   */
  List<String> missingParameters();
}
