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
package com.applause.auto.cmd;

import com.applause.auto.config.AutoApiPropertyHelper;
import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.helpers.ApplauseAppPushHelper;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Allow the application push to be a separate command in a Maven file */
@SuppressWarnings({"checkstyle:MultipleStringLiterals"})
public final class MobileAppImagePusher {
  private static final Logger logger = LogManager.getLogger(MobileAppImagePusher.class);

  private MobileAppImagePusher() {
    // empty private CTOR
  }

  /**
   * entry point for this command. The following parameters are supported
   *
   * <ol>
   *   <li>-DappPushProvider={{Provider}}: Required. Examples BrowserStack, SauceLabs When present,
   *       this activates the feature and the provider name is used to look up a class that provides
   *       additional implementation functionality
   *   <li>-DappPushUser={{UserName}} : Optional. If not specified, the appPushApplause must be true
   *       OR the implementation of AppPushTarget must not require authentication The username to
   *       use for HTTP authentication for the pushing of the contents
   *   <li>-DappPushPasswd={{Password}} : Optional. If not specified, the appPushApplause must be
   *       true OR the implementation of AppPushTarget must not require authentication The password
   *       to use for HTTP authentication for the pushing of the contents
   *   <li>-DappPushSource={FILE|URL} : Optional. The source of the image to be used. This is ONLY
   *       optional when appPushApplause=true This is either the image on the file system to push,
   *       or a publicly accessible URL to the image IF THIS IS MISSING, the system will attempt
   *       application auto-discover In this case apiKey MUST be present and the system will query
   *       the public API and attempt to discover the build to use
   *   <li>-DappPushTargetUrl={URL} : Optional. The implementations of AppPushTarget contain a
   *       default value This allows the value to be changed
   * </ol>
   *
   * @param args parameters passed on command-line
   */
  public static void main(final String[] args) {
    dumpConfigProperties();
    // If we do an app-auto detect, we'll return a modified applauseConfigBean.
    // If we need app-auto detect, and it fails, we'll throw a run-time exception
    ApplauseAppPushHelper.autoDetectBuildIfNecessary();
    ApplauseAppPushHelper.performApplicationPushIfNecessary();
    logger.info("APP=" + EnvironmentConfigurationManager.INSTANCE.get().app());
  }

  /** Dump the configuration elements we care about when App Pushing. */
  static void dumpConfigProperties() {
    // By default, config dumping is off, unless we are running in a debug mode
    if (!EnvironmentConfigurationManager.INSTANCE.get().dumpConfig()) {
      // There are only a set of properties we are interested in (it started small, but it's grown)
      Set<String> propSet = new HashSet<>();
      propSet.add("appPushProvider");
      propSet.add("appPushTargetUrl");
      propSet.add("appPushSource");
      propSet.add("appPushUser");
      propSet.add("appPushPassword");
      propSet.add("app");
      propSet.add("applausePublicApiUrl");
      propSet.add("autoApiUrl");
      propSet.add("seleniumProxyUrl");
      propSet.add("apiKey");
      propSet.add("versionId");
      propSet.add("buildId");
      propSet.add("productId");
      AutoApiPropertyHelper.dumpConfigProperties(propSet);
    }
  }
}
