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

import com.applause.auto.util.applausepublicapi.dto.enums.TestCaseStatusEnum;
import java.util.List;

/**
 * TestCaseResultHistoryDto
 *
 * @param id -- GETTER -- Get id
 * @param status -- GETTER -- Get status
 * @param issues -- GETTER -- Get issues
 * @param productId -- GETTER -- Get productId
 * @param build -- GETTER -- Get build
 * @param testCycle -- GETTER -- Get testCycle
 * @param primaryEnvironmentGroup -- GETTER -- Get primaryEnvironmentGroup
 * @param additionalEnvironmentGroup -- GETTER -- Get additionalEnvironmentGroup
 */
public record TestCaseResultHistoryDto(
    Long id,
    TestCaseStatusEnum status,
    Boolean automated,
    List<BugDto> issues,
    Long productId,
    NamedEntityDto build,
    NamedEntityDto testCycle,
    NamedEntityDto primaryEnvironmentGroup,
    NamedEntityDto additionalEnvironmentGroup) {}
