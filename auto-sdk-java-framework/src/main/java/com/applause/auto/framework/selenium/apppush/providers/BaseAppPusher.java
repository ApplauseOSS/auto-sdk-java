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
package com.applause.auto.framework.selenium.apppush.providers;

import com.applause.auto.config.AppPushConfig;
import com.applause.auto.framework.selenium.apppush.AppPushTarget;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import lombok.Data;
import lombok.NonNull;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Abstract Base Class for all App Push implementations */
@Data
public abstract class BaseAppPusher implements AppPushTarget {
  private static final Logger logger = LogManager.getLogger(BaseAppPusher.class);
  private File fileToPush;
  private String urlToPush;
  private String targetUrl;
  private String defaultTargetUrl;

  BaseAppPusher(final String url) {
    defaultTargetUrl = url;
  }

  /**
   * Update an important setting (and write a log message that we are doing it)
   *
   * @param newValue The new target URL value
   * @return this
   */
  public BaseAppPusher setTargetUrl(final String newValue) {
    logger.info("Overwriting target URL for " + this.getClass().getName() + " to " + newValue);
    targetUrl = newValue;
    return this;
  }

  /**
   * Initialize the instance to push a file
   *
   * @param appImageFile the file to push
   * @param cfg the current configuration
   * @return true if initialized correctly, false otherwise
   */
  @Override
  public boolean init(@NonNull final File appImageFile, @NonNull final AppPushConfig cfg) {
    fileToPush = appImageFile;
    return customConfig(cfg, appImageFile.getName());
  }

  /**
   * Initialize the instance to push a URL
   *
   * @param appImageUrl the url to push
   * @param cfg the current configuration
   * @return true if initialized correctly, false otherwise
   */
  @Override
  public boolean init(@NonNull final String appImageUrl, @NonNull final AppPushConfig cfg) {
    urlToPush = appImageUrl;
    // Take the last part of the URL
    URL url;
    try {
      url = new URI(appImageUrl).toURL();
    } catch (MalformedURLException | URISyntaxException m) {
      return false;
    }
    String name = FilenameUtils.getName(url.getPath());
    if (StringUtils.isBlank(name)) {
      name = "app-pushed";
    }
    return customConfig(cfg, name);
  }

  protected abstract boolean customConfig(@NonNull AppPushConfig cfg, @NonNull String name);

  @Override
  public String getTargetUrl() {
    if (targetUrl != null) {
      return targetUrl;
    }
    return defaultTargetUrl;
  }

  /**
   * Usually HttpMethod POST is the correct HTTP method
   *
   * @return HttpMethod.POST
   */
  @Override
  public String httpMethod() {
    return "POST";
  }

  /**
   * Every provide seems to have a different way to create the HTTP body that contains the data to
   * be pushed. This method is customized for each provider
   *
   * @return The HTTP request body to push to the target identified by getTargetUrl()
   */
  @Override
  public abstract RequestBody getRequestBody();

  /**
   * Every provider sends back a different structure that contains the id that should be used in the
   * app field
   *
   * @param response the response received for the HTTP operation
   * @return a string The value for the 'app' field
   */
  @Override
  public abstract String getApplicationValue(Response response);
}
