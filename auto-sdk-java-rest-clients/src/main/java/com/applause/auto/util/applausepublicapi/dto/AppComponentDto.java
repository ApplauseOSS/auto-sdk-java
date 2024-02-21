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

import com.applause.auto.util.applausepublicapi.dto.enums.StatusEnum;
import java.util.List;

/**
 * AppComponentDto
 *
 * @param id -- GETTER -- Get id
 * @param name -- GETTER -- Get name
 * @param description -- GETTER -- Get description
 * @param productId -- GETTER -- Get productId
 * @param status -- GETTER -- Get status
 * @param order -- GETTER -- Get order
 * @param numUsedTestRuns -- GETTER -- Get numUsedTestRuns
 * @param parentAppComponentId -- GETTER -- Get parentAppComponentId
 * @param subAppComponents -- GETTER -- Get subAppComponents
 */
public record AppComponentDto(
    Long id,
    String name,
    String description,
    Long productId,
    StatusEnum status,
    Long order,
    Long numUsedTestRuns,
    Long parentAppComponentId,
    List<AppComponentDto> subAppComponents) {}
