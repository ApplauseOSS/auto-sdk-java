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
package com.applause.auto.testng.listeners;

import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.helpers.SyncHelper;
import com.applause.auto.reporting.ApplauseReporter;
import com.applause.auto.reporting.ApplauseReporterState;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import java.time.temporal.ChronoUnit;
import lombok.extern.log4j.Log4j2;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/** A TestNG Suite Listener for reporting results to TestRail after the suite completes */
@Log4j2
public class SdkTestRailSuiteListener implements ISuiteListener {

  private static final RetryPolicy<?> testRailUploadRetryPolicy =
      RetryPolicy.builder()
          .handle(RuntimeException.class)
          .withBackoff(500, 5000, ChronoUnit.MILLIS)
          .build();

  @Override
  public void onFinish(final ISuite suite) {
    if (EnvironmentConfigurationManager.INSTANCE.get().sdkTestRailResultSubmissionEnabled()) {
      waitForApplauseReportingToFinish();
      Failsafe.with(testRailUploadRetryPolicy)
          .onFailure(
              exception ->
                  log.error("failed to upload results to TestRail at end of suite {}", exception))
          .run(
              () ->
                  SuiteListenerTestRailUploader.sendResultsToTestRail(
                      suite.getResults(), ApplauseReporter.INSTANCE.getAssets()));
    }
  }

  /**
   * If reporting is enabled, wait for the reporting to finish so that we can extract the Assets
   * from the result.
   */
  private void waitForApplauseReportingToFinish() {
    if (!ApplauseEnvironmentConfigurationManager.INSTANCE.get().reportingEnabled()) {
      return;
    }
    log.info("Waiting for Applause reporting to finish");

    // Waits up to five minutes for the reporting to finish.
    for (int i = 0; i < 10; i++) {
      if (ApplauseReporter.INSTANCE.getState() == ApplauseReporterState.ENDED) {
        break;
      }
      SyncHelper.sleep(30_000);
    }
  }
}
