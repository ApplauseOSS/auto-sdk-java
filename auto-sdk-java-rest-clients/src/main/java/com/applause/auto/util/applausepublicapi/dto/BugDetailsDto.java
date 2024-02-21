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

import com.applause.auto.util.applausepublicapi.dto.enums.BugFixWorthinessLevelEnum;
import com.applause.auto.util.applausepublicapi.dto.enums.BugSeverityEnum;
import com.applause.auto.util.applausepublicapi.dto.enums.BugStatusEnum;
import com.applause.auto.util.applausepublicapi.dto.enums.BugTypeEnum;
import java.util.List;

/**
 * BugDetailsDto
 *
 * @param id -- GETTER -- Get id
 * @param subject -- GETTER -- Get subject
 * @param status -- GETTER -- Get status
 * @param fixWorthinessLevel -- GETTER -- Get fixWorthinessLevel
 * @param severity -- GETTER -- Get severity
 * @param type -- GETTER -- Get type
 * @param build -- GETTER -- Get build
 * @param appComponent -- GETTER -- Get appComponent
 * @param numberOfReproducers -- GETTER -- Get numberOfReproducers
 * @param customFields -- GETTER -- Get customFields
 * @param testCaseId -- GETTER -- Get testCaseId
 * @param testCaseResultId -- GETTER -- Get testCaseResultId
 * @param testCaseStepId -- GETTER -- Get testCaseStepId
 * @param bugTrackingIssueUrl -- GETTER -- Get bugTrackingIssueUrl
 * @param bugTrackingIssueId -- GETTER -- Get bugTrackingIssueId
 * @param testCycleId -- GETTER -- Get testCycleId
 * @param tester -- GETTER -- Get tester
 * @param product -- GETTER -- Get product
 * @param resolution -- GETTER -- Get resolution
 * @param description -- GETTER -- Get description
 * @param environments -- GETTER -- Get environments
 * @param recommendation -- GETTER -- Get recommendation
 * @param history -- GETTER -- Get history
 * @param deviceDetails -- GETTER -- Get deviceDetails
 * @param sessionDetails -- GETTER -- Get sessionDetails
 */
public record BugDetailsDto(
    Long id,
    String subject,
    BugStatusEnum status,
    BugFixWorthinessLevelEnum fixWorthinessLevel,
    BugSeverityEnum severity,
    BugTypeEnum type,
    NamedEntityDto build,
    NamedEntityDto appComponent,
    Long numberOfReproducers,
    List<BugCustomFieldDto> customFields,
    Long testCaseId,
    Long testCaseResultId,
    Long testCaseStepId,
    String bugTrackingIssueUrl,
    String bugTrackingIssueId,
    Long testCycleId,
    NamedEntityDto tester,
    NamedEntityDto product,
    String resolution,
    BugDescriptionDto description,
    List<NamedEntityDto> environments,
    BugRecommendationDto recommendation,
    List<BugChangeDto> history,
    BugDeviceDto deviceDetails,
    List<BugSessionDto> sessionDetails) {}
