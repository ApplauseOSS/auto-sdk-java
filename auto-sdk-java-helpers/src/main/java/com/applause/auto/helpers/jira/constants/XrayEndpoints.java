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
package com.applause.auto.helpers.jira.constants;

@SuppressWarnings("PMD.DataClass")
public final class XrayEndpoints {
  public static final String LATEST_API = "/api/latest";

  public static final String XRAY_PATH = "/raven/latest/api/";
  public static final String ISSUE_PATH = LATEST_API + "/issue";

  public static final String PROJECT = "project";
  public static final String TEST_PLAN = "testplan";
  public static final String TEST = "test";
  public static final String STEP = "step";
  public static final String STEPS = "steps";
  public static final String TEST_EXECUTION = "testexecution";
  public static final String TEST_EXEC = "testexec";
  public static final String TEST_RUN = "testrun";
  public static final String ITERATION = "iteration";
  public static final String STATUS = "status";
  public static final String ATTACHMENT = "attachment";
  public static final String ATTACHMENTS = "attachments";
  public static final String ISSUE_TYPES = "issuetypes";
  public static final String CREATEMETA = "createmeta";
  public static final String SEARCH = "search";
  public static final String COMMENT = "comment";

  /** Query parameters */
  public static final String testExecIssueKeyParam = "testExecIssueKey=";

  public static final String testIssueKeyParam = "testIssueKey=";
  public static final String statusParam = "status=";

  private XrayEndpoints() {
    // utility class
  }
}
