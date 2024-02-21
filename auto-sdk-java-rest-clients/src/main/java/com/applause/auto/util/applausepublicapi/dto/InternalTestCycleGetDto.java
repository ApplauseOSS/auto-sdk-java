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

import java.util.List;

/**
 * InternalTestCycleGetDto
 *
 * @param id -- GETTER -- Get id
 * @param product -- GETTER -- Get product
 * @param name -- GETTER -- Get name
 * @param status -- GETTER -- Get status
 * @param execution -- GETTER -- Get execution
 * @param productVersion -- GETTER -- Get productVersion
 * @param developmentStage -- GETTER -- Get developmentStage
 * @param testingType -- GETTER -- Get testingType
 * @param testingDateRange -- GETTER -- Get testingDateRange
 * @param instructions -- GETTER -- Get instructions
 * @param environmentFilters -- GETTER -- Get environmentFilters
 * @param config -- GETTER -- Get config
 * @param attachments -- GETTER -- Get attachments
 * @param actionPermissions -- GETTER -- Get actionPermissions
 * @param bugCustomFields -- GETTER -- Get bugCustomFields
 * @param clonedFromCycleId -- GETTER -- Get clonedFromCycleId
 * @param bugTypeSetId -- GETTER -- Get bugTypeSetId
 */
public record InternalTestCycleGetDto(
    Long id,
    EntityDescriptor product,
    String name,
    Long status,
    String execution,
    EntityDescriptor productVersion,
    EntityDescriptor developmentStage,
    Long testingType,
    TestingDateRangeDto testingDateRange,
    InstructionsDto instructions,
    EnvironmentFiltersDto environmentFilters,
    ConfigDto config,
    List<AttachmentDto> attachments,
    ActionPermissionsDto actionPermissions,
    List<InternalCycleBugCustomFieldDto> bugCustomFields,
    Long clonedFromCycleId,
    Long bugTypeSetId) {}
