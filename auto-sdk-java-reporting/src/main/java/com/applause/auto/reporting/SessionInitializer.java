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

import com.applause.auto.reporting.config.ApplauseReporterConfig;
import com.applause.auto.reporting.config.ApplauseTestCycleReportingConfig;
import com.applause.auto.reporting.config.ApplauseTestRailConfig;
import com.applause.auto.reporting.params.ApplauseRunCreation;
import com.applause.auto.util.autoapi.AutoApi;
import com.applause.auto.util.autoapi.AutoApiClient;
import com.applause.auto.util.autoapi.TestRunConfigurationDto;
import com.applause.auto.util.autoapi.TestRunConfigurationParamDto;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import retrofit2.Response;

/** Handles initializing a new Applause TestRun */
@AllArgsConstructor
@Log4j2
public class SessionInitializer implements ISessionInitializer {

  private final @NonNull ApplauseReporterConfig config;
  private final @Nullable ApplauseTestRailConfig testRailConfig;
  private final @Nullable ApplauseTestCycleReportingConfig testCycleConfig;

  @Override
  public IApplauseReporter startTestRun(final @NonNull ApplauseRunCreation params) {
    AutoApi autoApi =
        AutoApiClient.getClient(config.getAutoApiUrl(), config.getApiKey(), config.getProxy());
    final var sdkVersion = new SdkVersionReader(autoApi).getSdkVersion();
    TestRunConfigurationParamDto testRunConfigurationParamDto =
        new TestRunConfigurationParamDto()
            .setSdkVersion(sdkVersion)
            .setDriverConfig(params.getDriverConfig())
            .setProductId(config.getProductId())
            .setTests(params.getPrecreatedResults());

    if (testRailConfig != null) {
      preValidateTestrail(autoApi);
      testRunConfigurationParamDto
          .setTestRailReportingEnabled(true)
          .setTestRailPlanName(testRailConfig.testRailPlanName())
          .setTestRailRunName(testRailConfig.testRailRunName())
          .setAddAllTestsToPlan(testRailConfig.addAllTestsToPlan())
          .setTestRailSuiteId(testRailConfig.testRailSuiteId())
          .setTestRailProjectId(testRailConfig.testRailProjectId());
    }
    if (testCycleConfig != null) {
      testRunConfigurationParamDto.setItwTestCycleId(testCycleConfig.applauseTestCycleId());
    }
    // Add a unique ID generated on the client side that the server can use to determine
    // if the submission has been made before (to catch duplicate submissions)
    testRunConfigurationParamDto.setClientSubmissionId(UUID.randomUUID().toString());
    Response<TestRunConfigurationDto> apiResp =
        autoApi.createTestRun(testRunConfigurationParamDto).join();
    if (!apiResp.isSuccessful()) {
      try (var errBody = apiResp.errorBody()) {
        if (errBody == null) {
          throw new RuntimeException(
              "Too many communication failures with Applause Server. Exiting with unknown error.");
        }
        // After max number of failures with server, stop
        throw new RuntimeException(
            "Too many communication failures with Applause Server. Exiting: " + errBody.string());
      } catch (IOException e) {
        throw new RuntimeException(
            "Too many communication failures with Applause Server. Exiting with unknown error.", e);
      }
    }

    final var sessionInfo = apiResp.body();
    if (sessionInfo == null) {
      throw new RuntimeException(
          "Failed to initialize run with Applause Server - no response body return");
    }

    log.info("Applause session created. Run [{}].", sessionInfo.getRunId());
    return new SessionHandler(config, testRailConfig, testCycleConfig, sessionInfo.getRunId());
  }

  private static void preValidateTestrail(final @NonNull AutoApi autoApi) {
    try {
      Response<List<String>> response = autoApi.prevalidateTestrail().join();
      List<String> preValidationErrors = response.body();
      if (preValidationErrors == null) {
        log.trace("Could not perform Testrail pre-validation checks - no response body returned");
        return;
      }
      if (!preValidationErrors.isEmpty()) {
        log.warn("*** WARNING *** Testrail configuration problems found. *** WARNING ***");
        preValidationErrors.forEach(log::warn);
      }
    } catch (Exception e) {
      log.error("Could not perform Testrail pre-validation checks.", e);
      // fail silently. This should NOT block the tests from running.
    }
  }
}
