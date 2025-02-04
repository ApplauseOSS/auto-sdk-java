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
package com.applause.auto.helpers.jira.dto.requestmappers;

import com.applause.auto.helpers.jira.dto.shared.Issuetype;
import com.applause.auto.helpers.jira.dto.shared.Project;

public class JiraFields {

  private Fields fields = null;

  public JiraFields(String issueType, String projectId, String summary) {
    fields = new Fields();

    Issuetype issuetype = new Issuetype();
    issuetype.setId(issueType);
    fields.setIssuetype(issuetype);

    Project project = new Project();
    project.setId(projectId);
    fields.setProject(project);

    fields.setSummary(summary);
  }

  public Fields getFields() {
    return fields;
  }
}
