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
package com.applause.auto.helpers.jira.clients;

import com.applause.auto.helpers.jira.clients.modules.jira.JiraProjectAPI;
import com.applause.auto.helpers.jira.clients.modules.jira.SearchAPI;
import com.applause.auto.helpers.jira.clients.modules.xray.ExecutionsAPI;
import com.applause.auto.helpers.jira.clients.modules.xray.IterationsAPI;
import com.applause.auto.helpers.jira.clients.modules.xray.StepsAPI;
import com.applause.auto.helpers.jira.clients.modules.xray.TestrunAPI;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class for communicating with JIRA/X-Ray Server + DC instance API end-points.
 *
 * <p>Cannot be used with Cloud instances, use GraphQL instead.
 *
 * <p>X-Ray documentation: https://docs.getxray.app/display/XRAY/REST+API
 */
public class JiraXrayClient {

  private static final Logger logger = LogManager.getLogger(JiraXrayClient.class);

  private JiraProjectAPI jiraProjectAPI;
  private SearchAPI searchAPI;
  private ExecutionsAPI executionsAPI;
  private IterationsAPI iterationsAPI;
  private StepsAPI stepsAPI;
  private TestrunAPI testrunAPI;

  public JiraProjectAPI getJiraProjectApi() {
    if (Objects.isNull(jiraProjectAPI)) {
      jiraProjectAPI = new JiraProjectAPI();
    }
    return jiraProjectAPI;
  }

  public SearchAPI getSearchAPI() {
    if (Objects.isNull(searchAPI)) {
      searchAPI = new SearchAPI();
    }
    return searchAPI;
  }

  public ExecutionsAPI getExecutionsAPI() {
    if (Objects.isNull(executionsAPI)) {
      executionsAPI = new ExecutionsAPI();
    }
    return executionsAPI;
  }

  public IterationsAPI getIterationsAPI() {
    if (Objects.isNull(iterationsAPI)) {
      iterationsAPI = new IterationsAPI();
    }
    return iterationsAPI;
  }

  public StepsAPI getStepsAPI() {
    if (Objects.isNull(stepsAPI)) {
      stepsAPI = new StepsAPI();
    }
    return stepsAPI;
  }

  public TestrunAPI getTestrunAPI() {
    if (Objects.isNull(testrunAPI)) {
      testrunAPI = new TestrunAPI();
    }
    return testrunAPI;
  }
}
