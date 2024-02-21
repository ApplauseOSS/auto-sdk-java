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
 * TestCaseCreateDto
 *
 * @param productId -- GETTER -- Get productId
 * @param name -- GETTER -- Get name
 * @param estimatedTimeInMinutes -- GETTER -- Get estimatedTimeInMinutes
 * @param description -- GETTER -- Get description
 * @param preconditions -- GETTER -- Get preconditions
 * @param appComponentId -- GETTER -- Get appComponentId
 * @param customFields -- GETTER -- Get customFields
 */
public record TestCaseCreateDto(
    Long productId,
    String name,
    Long estimatedTimeInMinutes,
    String description,
    String preconditions,
    Long appComponentId,
    List<CustomFieldConfigurationDto> customFields) {}
