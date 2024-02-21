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
package com.applause.auto.util.applausepublicapi.dto;

import com.applause.auto.util.applausepublicapi.dto.enums.TestCaseResultApprovalStatusEnum;
import com.applause.auto.util.applausepublicapi.dto.enums.TestCaseResultStatusEnum;
import com.applause.auto.util.applausepublicapi.dto.enums.TestCaseResultTypeEnum;
import com.applause.auto.util.applausepublicapi.dto.enums.TestCaseTypeEnum;
import java.util.*;

/**
 * ManualTestCaseResultDto
 *
 * @param id -- GETTER -- Get id
 * @param testCycleId -- GETTER -- Get testCycleId
 * @param testCaseId -- GETTER -- Get testCaseId
 * @param productId -- GETTER -- Get productId
 * @param buildId -- GETTER -- Get buildId
 * @param status -- GETTER -- Get status
 * @param type -- GETTER -- Get type
 * @param timeline -- GETTER -- Get timeline
 * @param testRunId -- GETTER -- Get testRunId
 * @param testerId -- GETTER -- Get testerId
 * @param approvalStatus -- GETTER -- Get approvalStatus
 * @param testCaseType -- GETTER -- Get testCaseType
 * @param comment -- GETTER -- Get comment
 * @param locale -- GETTER -- Get locale
 * @param device -- GETTER -- Get device
 * @param operatingSystem -- GETTER -- Get operatingSystem
 * @param browser -- GETTER -- Get browser
 * @param startDate -- GETTER -- Get startDate
 * @param endDate -- GETTER -- Get endDate
 * @param submitDate -- GETTER -- Get submitDate
 * @param testCaseSteps -- GETTER -- Get testCaseSteps
 * @param verificationComment -- GETTER -- Get verificationComment
 * @param verificationBugTestCycleUrls -- GETTER -- Get verificationBugTestCycleUrls
 */
public record ManualTestCaseResultDto(
    Long id,
    Long testCycleId,
    Long testCaseId,
    Long productId,
    Long buildId,
    TestCaseResultStatusEnum status,
    TestCaseResultTypeEnum type,
    TimelineDto timeline,
    Long testRunId,
    Long testerId,
    TestCaseResultApprovalStatusEnum approvalStatus,
    TestCaseTypeEnum testCaseType,
    String comment,
    String locale,
    String device,
    String operatingSystem,
    String browser,
    Date startDate,
    Date endDate,
    Date submitDate,
    List<TestCaseResultStepDto> testCaseSteps,
    String verificationComment,
    List<String> verificationBugTestCycleUrls) {}
