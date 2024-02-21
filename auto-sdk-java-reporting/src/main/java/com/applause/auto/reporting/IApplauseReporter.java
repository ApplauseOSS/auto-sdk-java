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
package com.applause.auto.reporting;

import com.applause.auto.reporting.params.ApplauseResultCreation;
import com.applause.auto.reporting.params.ApplauseResultSubmission;
import com.applause.auto.reporting.params.ApplauseRunEnd;
import com.applause.auto.util.autoapi.TestRunAssetLinksDto;
import java.util.Map;

/** Interface defining Applause Reporter Actions */
public interface IApplauseReporter {

  /**
   * Gets the TestRun id from this reporter
   *
   * @return The TestRun ID
   */
  Long getTestRunId();

  /**
   * Creates a TestResult from the provided parameters
   *
   * @param testResultUuid The Unique ID of a Test Result
   * @param params The Creation Params for this Test Result
   * @return The generated ResultRecord
   */
  ResultRecord createTestResult(String testResultUuid, ApplauseResultCreation params);

  /**
   * Submits a TestResult from the provided parameters
   *
   * @param testResultUuid The TestResult UUID to submit
   * @param params The Submission Params for this TestResult
   * @return The updated ResultRecord
   */
  ResultRecord submitTestResult(String testResultUuid, ApplauseResultSubmission params);

  /**
   * Ends the given TestRun
   *
   * @param params The Run Ending Parameters
   * @return The closed reporter
   */
  IApplauseReporter endTestRun(ApplauseRunEnd params);

  /**
   * Gets a collection of Assets for the TestRun after it has ended
   *
   * @return The Asset Links for the Run
   */
  TestRunAssetLinksDto getAssets();

  /**
   * Gets the Map of TestResult UUIDs to ResultRecords
   *
   * @return The Map of TestResult UUIDs to ResultRecords
   */
  Map<String, ResultRecord> getResults();
}
