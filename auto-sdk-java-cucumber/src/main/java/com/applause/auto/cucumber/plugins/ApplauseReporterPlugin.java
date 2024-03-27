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
package com.applause.auto.cucumber.plugins;

import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.config.ApplauseSdkConfigBean;
import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.config.SdkConfigBean;
import com.applause.auto.config.TestRailConfigBean;
import com.applause.auto.config.TestRailConfigurationManager;
import com.applause.auto.cucumber.utils.CucumberUtils;
import com.applause.auto.framework.ContextManager;
import com.applause.auto.helpers.ApplauseConfigHelper;
import com.applause.auto.logging.ResultPropertyMap;
import com.applause.auto.reporting.ApplauseReporter;
import com.applause.auto.reporting.config.ApplauseReporterConfig;
import com.applause.auto.reporting.config.ApplauseTestCycleReportingConfig;
import com.applause.auto.reporting.config.ApplauseTestRailConfig;
import com.applause.auto.reporting.params.ApplauseResultCreation;
import com.applause.auto.reporting.params.ApplauseResultSubmission;
import com.applause.auto.reporting.params.ApplauseRunCreation;
import com.applause.auto.reporting.params.ApplauseRunEnd;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Synchronized;

/** Cucumber Plugin to handle reporting to auto-api */
public class ApplauseReporterPlugin implements ConcurrentEventListener {
  /** Constant describing the attachment file name for the Applause context */
  public static final String APPLAUSE_CONTEXT_ID_ATTACHMENT = "applause-context-id";

  private final Map<String, String> testCaseContextMap = new ConcurrentHashMap<>();

  @SuppressWarnings("PMD.UnusedFormalParameter")
  @Synchronized
  private void testRunStarted(final TestRunStarted event) {
    // Configure the reporter if reporting is enabled
    final ApplauseSdkConfigBean applauseConfigBean =
        ApplauseEnvironmentConfigurationManager.INSTANCE.get();
    if (!applauseConfigBean.reportingEnabled()) {
      return;
    }
    ApplauseReporter.INSTANCE.configure(
        new ApplauseReporterConfig(
            applauseConfigBean.autoApiUrl(),
            applauseConfigBean.apiKey(),
            applauseConfigBean.productId(),
            ApplauseConfigHelper.getHttpProxy()));
    if (applauseConfigBean.applauseTestCycleId() != null) {
      ApplauseReporter.INSTANCE.enableTestCycleReporting(
          new ApplauseTestCycleReportingConfig(applauseConfigBean.applauseTestCycleId()));
    }

    final SdkConfigBean sdkConfigBean = EnvironmentConfigurationManager.INSTANCE.get();
    final TestRailConfigBean testRailConfigBean = TestRailConfigurationManager.INSTANCE.get();

    if (applauseConfigBean.testRailReportingEnabled()
        && !sdkConfigBean.sdkTestRailResultSubmissionEnabled()) {
      ApplauseReporter.INSTANCE.enableTestRail(
          new ApplauseTestRailConfig(
              testRailConfigBean.addAllTestsToPlan(),
              testRailConfigBean.testRailProjectId(),
              testRailConfigBean.testRailSuiteId(),
              testRailConfigBean.testRailPlanName(),
              testRailConfigBean.testRailRunName()));
    }
    ApplauseReporter.INSTANCE.startTestRun(
        new ApplauseRunCreation().setDriverConfig(sdkConfigBean.capsFile()));
  }

  @Synchronized
  private void testCaseStarted(final TestCaseStarted event) {
    final var testCaseId = event.getTestCase().getId().toString();
    final var applauseTestCaseIds =
        CucumberUtils.getApplauseTestCaseIdsFromTestCase(event.getTestCase());
    final var testRailCaseId =
        CucumberUtils.getTestRailTestCaseIdsFromTestCase(event.getTestCase());
    ApplauseReporter.INSTANCE.createTestResult(
        testCaseId,
        new ApplauseResultCreation(event.getTestCase().getName())
            .setTestRailCaseId(testRailCaseId)
            .addApplauseTestCaseIds(applauseTestCaseIds)
            .addProviderSessionIds(
                ContextManager.INSTANCE.getProviderSessionIdsForContext(
                    this.testCaseContextMap.get(testCaseId)))
            .setTestRunId((Long) ResultPropertyMap.getProperty("testRunId")));
  }

  @Synchronized
  private void testCaseFinished(final TestCaseFinished event) {
    final var testCaseId = event.getTestCase().getId().toString();
    ApplauseReporter.INSTANCE.submitTestResult(
        testCaseId,
        new ApplauseResultSubmission(
                CucumberUtils.cucumberStatusToApplauseStatus(event.getResult().getStatus()))
            .addProviderSessionIds(
                ContextManager.INSTANCE.getProviderSessionIdsForContext(
                    this.testCaseContextMap.get(testCaseId))));
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  @Synchronized
  private void testRunFinished(final TestRunFinished event) {
    ApplauseReporter.INSTANCE.endTestRun(new ApplauseRunEnd());
  }

  @Synchronized
  private void scenarioDataAttached(final EmbedEvent event) {
    if (!APPLAUSE_CONTEXT_ID_ATTACHMENT.equals(event.getName())) {
      return;
    }
    this.testCaseContextMap.put(
        event.getTestCase().getId().toString(),
        new String(event.getData(), StandardCharsets.UTF_8));
  }

  @Override
  public void setEventPublisher(final EventPublisher publisher) {
    if (ApplauseEnvironmentConfigurationManager.INSTANCE.get().reportingEnabled()) {
      publisher.registerHandlerFor(TestCaseStarted.class, this::testCaseStarted);
      publisher.registerHandlerFor(TestCaseFinished.class, this::testCaseFinished);
      publisher.registerHandlerFor(TestRunStarted.class, this::testRunStarted);
      publisher.registerHandlerFor(TestRunFinished.class, this::testRunFinished);
      publisher.registerHandlerFor(EmbedEvent.class, this::scenarioDataAttached);
    }
  }
}
