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
package com.applause.auto.testng;

import com.applause.auto.config.TestRailConfigBean;
import com.applause.auto.testrail.client.TestRailResultUploader;
import com.applause.auto.testrail.client.models.config.TestRailConfig;
import lombok.Getter;
import lombok.NonNull;

/** Convenience class that maps the TestRail config interface into a class */
@Getter
@SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation")
public final class TestRailConfigBeanMapper {

  private TestRailConfigBeanMapper() {
    // intentionally empty
  }

  /**
   * Handles mapping the Owner config to the TestRailUploader ProjectConfiguration
   *
   * @param testRailConfigBean The TestRail config bean
   * @return The Project Configuration
   */
  public static TestRailResultUploader.ProjectConfiguration projectConfigFromBean(
      @NonNull final TestRailConfigBean testRailConfigBean) {

    return new TestRailResultUploader.ProjectConfiguration(
        testRailConfigBean.testRailProjectId(),
        testRailConfigBean.testRailSuiteId(),
        testRailConfigBean.addAllTestsToPlan(),
        testRailConfigBean.testRailPlanName(),
        testRailConfigBean.testRailRunName(),
        testRailConfigBean.statusPassed(),
        testRailConfigBean.statusFailed(),
        testRailConfigBean.statusSkipped(),
        testRailConfigBean.statusError(),
        testRailConfigBean.statusCanceled());
  }

  /**
   * Handles mapping the Owner config to the General TestRailConfig class
   *
   * @param testRailConfigBean The TestRail config bean
   * @return The TestRailConfig
   */
  public static TestRailConfig testRailConfigFromBean(
      @NonNull final TestRailConfigBean testRailConfigBean) {
    return TestRailConfig.builder()
        .url(testRailConfigBean.testRailBaseUrl())
        .apiKey(testRailConfigBean.testRailApiKey())
        .email(testRailConfigBean.testRailUsername())
        .build();
  }
}
