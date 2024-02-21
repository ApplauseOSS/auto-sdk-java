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

import com.applause.auto.util.applausepublicapi.dto.enums.TestCycleStatusEnum;
import java.util.Date;

/**
 * CommunityTestCycleProgressDto
 *
 * @param id -- GETTER -- Get id
 * @param issueStatistics -- GETTER -- Get issueStatistics
 * @param startDate -- GETTER -- Get startDate
 * @param closeDate -- GETTER -- Get closeDate
 * @param activationDate -- GETTER -- Get activationDate
 * @param deactivationDate -- GETTER -- Get deactivationDate
 * @param status -- GETTER -- Get status
 */
public record CommunityTestCycleProgressDto(
    Long id,
    IssueStatisticsDto issueStatistics,
    Date startDate,
    Date closeDate,
    Date activationDate,
    Date deactivationDate,
    TestCycleStatusEnum status) {}
