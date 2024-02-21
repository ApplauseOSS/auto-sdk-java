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

import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * An implementation of AppPushConfig. Identifies what might change during execution and what can
 * not (We can change the appPushClass as that is often inferred from the name of the provider)
 * change
 */
@AllArgsConstructor
public class AppPushConfigImpl implements AppPushConfig {
  /** The app push provider */
  public static final String PARAM_APP_PUSH_PVD = "appPushProvider";

  /** The app push source */
  public static final String PARAM_APP_PUSH_SOURCE = "appPushSource";

  /** The app push target */
  public static final String PARAM_APP_PUSH_TARGET = "appPushTarget";

  /** The app push user */
  public static final String PARAM_APP_PUSH_USERNAME = "appPushUser";

  /** The app push password */
  public static final String PARAM_APP_PUSH_PASSWORD = "appPushPassword";

  /** The app push class */
  public static final String PARAM_APP_PUSH_CLASSNAME = "appPushClass";

  private final String providerName;
  private final String pushSource;
  private final String targetUrl;
  private final String userName;
  private final String password;
  private String className;

  /**
   * The copy constructor
   *
   * @param cfg an existing config
   */
  public AppPushConfigImpl(final AppPushConfig cfg) {
    this(
        cfg.appPushProvider(),
        cfg.appPushSource(),
        cfg.appPushTargetUrl(),
        cfg.appPushUser(),
        cfg.appPushPassword(),
        cfg.appPushClass());
  }

  /**
   * The name of the provider to push an application image to. Examples: BrowserStack, SauceLabs
   *
   * @return The name of the provider to push an application image to
   */
  @Override
  public String appPushProvider() {
    return providerName;
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
    return className;
  }

  @Override
  public AppPushConfigImpl appPushClass(final String newVal) {
    className = newVal;
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
    return pushSource;
  }

  /**
   * The target URL where the image is to be pushed.
   *
   * @return The target URL where the image is to be pushed.
   */
  @Override
  public String appPushTargetUrl() {
    return targetUrl;
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
    return userName;
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
    return password;
  }

  /**
   * Check if the configuration is sufficient to perform an application push
   *
   * @return whether configuration is good enough
   */
  @Override
  @SuppressWarnings("checkstyle:BooleanExpressionComplexity")
  public boolean isComplete() {
    return !StringUtils.isBlank(providerName)
        && !StringUtils.isBlank(pushSource)
        && !StringUtils.isBlank(userName)
        && !StringUtils.isBlank(password)
        && !StringUtils.isBlank(className);
  }

  @Override
  public List<String> missingParameters() {
    LinkedList<String> params = new LinkedList<>();
    if (StringUtils.isBlank(providerName)) {
      params.add(PARAM_APP_PUSH_PVD);
    }
    if (StringUtils.isBlank(pushSource)) {
      params.add(PARAM_APP_PUSH_SOURCE);
    }
    if (StringUtils.isBlank(targetUrl)) {
      params.add(PARAM_APP_PUSH_TARGET);
    }
    if (StringUtils.isBlank(userName)) {
      params.add(PARAM_APP_PUSH_USERNAME);
    }
    if (StringUtils.isBlank(password)) {
      params.add(PARAM_APP_PUSH_PASSWORD);
    }
    if (StringUtils.isBlank(className)) {
      params.add(PARAM_APP_PUSH_CLASSNAME);
    }
    return params;
  }
}
