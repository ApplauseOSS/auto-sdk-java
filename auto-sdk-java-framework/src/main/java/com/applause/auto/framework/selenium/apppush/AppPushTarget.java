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
package com.applause.auto.framework.selenium.apppush;

import com.applause.auto.config.AppPushConfig;
import java.io.File;
import okhttp3.RequestBody;
import okhttp3.Response;

/** Interface to define a target for an Application push */
public interface AppPushTarget {
  /**
   * Initialize the instance to push a file
   *
   * @param appImageFile the file to push
   * @param cfg the current configuration
   * @return true if initialized correctly, false otherwise
   */
  boolean init(File appImageFile, AppPushConfig cfg);

  /**
   * Initialize the instance to push a URL
   *
   * @param appImageUrl the url to push
   * @param cfg the current configuration
   * @return true if initialized correctly, false otherwise
   */
  boolean init(String appImageUrl, AppPushConfig cfg);

  /**
   * Usually, the target URL that we POST/PUT to is static for the provider. In case it isn't,
   * customizers can overload this
   *
   * @return an HTTP url where data is pushed to. Example: <a
   *     href="https://api-cloud.browserstack.com/app-automate/upload"></a>
   */
  String getTargetUrl();

  /**
   * Most service we push to use an HTTP "POST". If an HTTP "PUT" is required, this method can be
   * overridden
   *
   * @return The HTTP method. When in doubt, use HttpMethod POST
   */
  String httpMethod();

  /**
   * Every provide seems to have a different way to create the HTTP body that contains the data to
   * be pushed. This method is customized for each provider
   *
   * @return The HTTP request body to push to the target identified by getTargetUrl()
   */
  RequestBody getRequestBody();

  /**
   * Every provider sends back a different structure that contains the id that should be used in the
   * app field
   *
   * @param response the response received for the HTTP operation
   * @return a string The value for the 'app' field
   */
  String getApplicationValue(Response response);
}
