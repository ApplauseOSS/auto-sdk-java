/*
 *
 * Copyright © 2025 Applause App Quality, Inc.
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
package com.applause.auto.helpers.jira.helper;

import io.restassured.response.Response;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResponseValidator {

  private static final Logger logger = LogManager.getLogger(ResponseValidator.class);

  public static void checkResponseInRange(
      Response response, Range<Integer> expectedRange, String action) {
    int statusCode = response.statusCode();
    if (expectedRange.contains(statusCode)) {
      logger.info("{} was successfully performed", action);
      logger.info(response.getBody().asString());
    } else {
      logger.error("{} failed with status code {}", action, statusCode);
      if (response.getBody() != null) {
        logger.error(response.getBody().asString());
      }
    }
  }
}
