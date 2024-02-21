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
package com.applause.auto.integrations;

import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.config.TestCycleCloneMode;
import com.applause.auto.helpers.ApplauseConfigHelper;
import com.applause.auto.helpers.CommunityTestCycleUtil;
import com.applause.auto.helpers.InternalTestCycleUtil;
import com.applause.auto.logging.ResultPropertyMap;
import com.google.common.collect.ImmutableMap;
import java.util.Calendar;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;

/** Utility functions for cloning test cycles */
@Log4j2
public final class TestCycleCloneUtil {
  private TestCycleCloneUtil() {}

  public static void performTestCycleCloneIfNecessary() {
    final var cloneMode =
        ApplauseEnvironmentConfigurationManager.INSTANCE.get().cloneTestCycleMode();
    if (cloneMode == TestCycleCloneMode.DISABLED) {
      return;
    }
    final var templateId =
        ApplauseEnvironmentConfigurationManager.INSTANCE.get().templateTestCycleId();
    if (templateId == null) {
      log.warn("Test Cycle Cloning is enabled, but no template test cycle id was provided.");
      return;
    }
    var outputParam =
        ApplauseEnvironmentConfigurationManager.INSTANCE.get().testCycleCloneOutputParameter();
    if (cloneMode == TestCycleCloneMode.INTERNAL) {
      performInternalTestCycleClone(
              templateId,
              ApplauseEnvironmentConfigurationManager.INSTANCE.get().cloneToTestCycleName())
          .ifPresent(
              id -> {
                ApplauseEnvironmentConfigurationManager.INSTANCE.override(
                    ImmutableMap.of(outputParam, id.toString()));
                ResultPropertyMap.setGlobalProperty(outputParam, id.toString());
              });

    } else if (cloneMode == TestCycleCloneMode.COMMUNITY) {
      performCommunityTestCycleClone(
              templateId,
              ApplauseEnvironmentConfigurationManager.INSTANCE.get().cloneToTestCycleName())
          .ifPresent(
              id -> {
                ApplauseEnvironmentConfigurationManager.INSTANCE.override(
                    ImmutableMap.of(outputParam, id.toString()));
                ResultPropertyMap.setGlobalProperty(outputParam, id.toString());
              });
    }
  }

  /**
   * Clones a community test cycle
   *
   * @param templateId The template test cycle id
   * @param newTestCycleName The new name for the cloned test cycle
   * @return The new test cycle id
   */
  public static Optional<Long> performCommunityTestCycleClone(
      final long templateId, final String newTestCycleName) {

    final var communityTestCycleUtil =
        new CommunityTestCycleUtil(
            ApplauseEnvironmentConfigurationManager.INSTANCE.get().applausePublicApiUrl(),
            ApplauseEnvironmentConfigurationManager.INSTANCE.get().apiKey(),
            ApplauseConfigHelper.getHttpProxy());
    var now = Calendar.getInstance().getTime();
    return communityTestCycleUtil.cloneCommunityTestCycle(
        templateId,
        ApplauseEnvironmentConfigurationManager.INSTANCE.get().productId(),
        newTestCycleName,
        DateUtils.addDays(now, 1),
        DateUtils.addDays(now, 2));
  }

  /**
   * Clones an internal test cycle
   *
   * @param templateId The template test cycle id
   * @param newTestCycleName The new name for the cloned test cycle
   * @return The new test cycle id
   */
  public static Optional<Long> performInternalTestCycleClone(
      long templateId, final String newTestCycleName) {
    final var internalTestCycleUtil =
        new InternalTestCycleUtil(
            ApplauseEnvironmentConfigurationManager.INSTANCE.get().applausePublicApiUrl(),
            ApplauseEnvironmentConfigurationManager.INSTANCE.get().apiKey(),
            ApplauseConfigHelper.getHttpProxy());
    return internalTestCycleUtil.cloneInternalTestCycle(templateId, newTestCycleName);
  }
}
