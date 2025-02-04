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
package com.applause.auto.helpers.jira.restclient;

import static com.applause.auto.helpers.jira.requestData.XrayRequestHeaders.*;

import com.applause.auto.helpers.http.restassured.RestApiDefaultRestAssuredApiHelper;
import com.applause.auto.helpers.jira.exceptions.JiraPropertiesFileException;
import io.restassured.specification.RequestSpecification;
import java.io.File;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class XrayRestAssuredClient {

  private static final Logger logger = LogManager.getLogger(XrayRestAssuredClient.class);
  private static final String JIRA_PROPERTIES_FILE_PATH = "src/main/resources/jira.properties";

  private static String jiraUrl;
  private static String token;
  private static IJiraAppConfig configApp = ConfigFactory.create(IJiraAppConfig.class);

  private static void loadProperties() {
    if (!new File(JIRA_PROPERTIES_FILE_PATH).exists()) {
      throw new JiraPropertiesFileException(
          "Couldn't find jira.properties file in resources folder!");
    }
    token = configApp.jiraToken();
    jiraUrl = configApp.jiraUrl();
    checkJiraUrl(jiraUrl);
  }

  public static RequestSpecification getRestClient() {
    loadProperties();
    RestApiDefaultRestAssuredApiHelper restApiDefaultRestAssuredApiHelper =
        new RestApiDefaultRestAssuredApiHelper();
    return restApiDefaultRestAssuredApiHelper
        .withDefaultRestHttpClientConfigsSpecification()
        .baseUri(jiraUrl)
        .header(getBearerAuthorizationHeader(token))
        .header(getAcceptApplicationJsonHeader())
        .header(getJSONContentTypeHeader());
  }

  private static void checkJiraUrl(String jiraUrl) {
    if (!jiraUrl.startsWith("https")) {
      logger.warn("API calls might return 415 because provided Jira url is not https!");
    }
  }
}
