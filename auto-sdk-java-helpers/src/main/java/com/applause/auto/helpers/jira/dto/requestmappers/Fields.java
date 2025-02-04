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
import lombok.Data;

@Data
public class Fields {
  // summary represents ticket's title.
  private String summary;
  // issueTypeId is different per project, and unique per ticket type (defect, task, test plan etc).
  // Example: Test Plan: 12106, Xray Test: 10402.
  private Issuetype issuetype;
  // Project id is the identifier of the project.
  private Project project;
}
