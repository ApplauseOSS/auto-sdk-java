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
package com.applause.auto.apppush;

import com.applause.auto.config.AppPushConfig;
import com.applause.auto.config.AppPushConfigImpl;
import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.config.SdkConfigBean;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Contains the configuration for feature used when used by Applause Customers */
@Getter
@AllArgsConstructor
public class ApplauseAppPushConfig implements AppPushConfig {
  private final AppPushConfigImpl basicConfig;
  @Setter private boolean userFromAutoApi;
  @Setter private boolean passwdFromAutoApi;

  /**
   * Create an Applause App Push Config from an ApplauseSdkConfigBean
   *
   * @return The Applause push config from the environment
   */
  public static ApplauseAppPushConfig fromApplauseSdkConfigBean() {
    final SdkConfigBean sdkConfig = EnvironmentConfigurationManager.INSTANCE.get();
    AppPushConfigImpl baseCfg =
        new AppPushConfigImpl(
            sdkConfig.appPushProvider(),
            sdkConfig.appPushSource(),
            sdkConfig.appPushTargetUrl(),
            sdkConfig.appPushUser(),
            sdkConfig.appPushPassword(),
            sdkConfig.appPushClass());
    // When we first create, we don't know the information that's available on auto-api
    return new ApplauseAppPushConfig(baseCfg, false, false);
  }

  /**
   * The name of the provider to push an application image to. Examples: BrowserStack, SauceLabs
   *
   * @return The name of the provider to push an application image to
   */
  @Override
  public String appPushProvider() {
    return basicConfig.appPushProvider();
  }

  /**
   * The canonical java class that identifies the class that implements the AppPushTarget interface.
   * Usually we determine this class from the appPushProvider. This is a way for SDETs to write
   * their own implementation of AppPushTarget
   *
   * @return A canonical java class name.
   */
  @Override
  public String appPushClass() {
    return basicConfig.appPushClass();
  }

  @Override
  public ApplauseAppPushConfig appPushClass(final String newVal) {
    basicConfig.appPushClass(newVal);
    return this;
  }

  /**
   * The source image to push. Two forms are possible. A URL that begins with "http://" or
   * "https://". Anything that is not a URL in this form is assumed to be a file on the local file
   * system
   *
   * @return The name of the provider to push an application image to
   */
  @Override
  public String appPushSource() {
    return basicConfig.appPushSource();
  }

  /**
   * The target URL where the image is to be pushed.
   *
   * @return The target URL where the image is to be pushed.
   */
  @Override
  public String appPushTargetUrl() {
    return basicConfig.appPushTargetUrl();
  }

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
  @Override
  public String appPushUser() {
    return basicConfig.appPushUser();
  }

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
  @Override
  public String appPushPassword() {
    return basicConfig.appPushPassword();
  }

  /**
   * Check if the configuration is sufficient to perform an application push
   *
   * @return whether is complete
   */
  @Override
  public boolean isComplete() {
    List<String> missingParams = basicConfig.missingParameters();
    if (missingParams.isEmpty()) {
      return true;
    }
    boolean result =
        !missingParams.contains(AppPushConfigImpl.PARAM_APP_PUSH_USERNAME) || this.userFromAutoApi;
    // If we are missing the username/password and those are available from the Applause Server,
    // then we can
    if (missingParams.contains(AppPushConfigImpl.PARAM_APP_PUSH_PASSWORD)
        && !this.passwdFromAutoApi) {
      result = false;
    }
    return result;
  }

  /**
   * List the set of missing parameters required to be present in order of isComplete to return true
   *
   * @return A list of missing parameters. Empty list if all required parameters are present.
   */
  @Override
  public List<String> missingParameters() {
    List<String> missingParams = basicConfig.missingParameters();
    if (missingParams.isEmpty()) {
      return missingParams;
    }
    // If we are missing the username/password and those are available from the Applause Server,
    // then we can check if that data is available from auto-api
    if (this.userFromAutoApi) {
      missingParams.remove(AppPushConfigImpl.PARAM_APP_PUSH_USERNAME);
    }
    if (this.passwdFromAutoApi) {
      missingParams.remove(AppPushConfigImpl.PARAM_APP_PUSH_PASSWORD);
    }
    return missingParams;
  }
}
