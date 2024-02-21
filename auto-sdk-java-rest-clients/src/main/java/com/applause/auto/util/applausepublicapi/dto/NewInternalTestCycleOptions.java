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

import java.util.*;

/**
 * NewInternalTestCycleOptions
 *
 * @param name -- GETTER -- Get name
 * @param productVersionId -- GETTER -- Get productVersionId
 * @param developmentStageId -- GETTER -- Get developmentStageId
 * @param startDate -- GETTER -- Get startDate
 * @param deactivationDate -- GETTER -- Get deactivationDate
 * @param instructions -- GETTER -- Get instructions
 * @param environmentFilters -- GETTER -- Get environmentFilters
 * @param config -- GETTER -- Get config
 * @param removedAttachments -- GETTER -- Get removedAttachments
 * @param bugCustomFields -- GETTER -- Get bugCustomFields
 */
public record NewInternalTestCycleOptions(
    String name,
    Long productVersionId,
    Long developmentStageId,
    Date startDate,
    Date deactivationDate,
    InstructionsDto instructions,
    EnvironmentFiltersDto environmentFilters,
    ConfigDto config,
    List<Long> removedAttachments,
    BugCustomFieldsDto bugCustomFields) {}
