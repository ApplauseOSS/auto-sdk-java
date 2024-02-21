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

import com.applause.auto.util.applausepublicapi.dto.enums.TestCaseStepModuleTypeEnum;
import com.applause.auto.util.applausepublicapi.dto.enums.TestCaseStepSubmoduleTypeEnum;
import java.util.List;

/**
 * TestCaseStepModuleDto
 *
 * @param id -- GETTER -- Get id
 * @param moduleType -- GETTER -- Get moduleType
 * @param submoduleType -- GETTER -- Get submoduleType
 * @param sortOrder -- GETTER -- Get sortOrder
 * @param expectedResult -- GETTER -- Get expectedResult
 * @param options -- GETTER -- Get options
 * @param instruction -- GETTER -- Get instruction
 * @param minNumberOfAttachments -- GETTER -- Get minNumberOfAttachments
 * @param title -- GETTER -- Get title
 * @param branchingEntityId -- GETTER -- Get branchingEntityId
 */
public record TestCaseStepModuleDto(
    Long id,
    TestCaseStepModuleTypeEnum moduleType,
    TestCaseStepSubmoduleTypeEnum submoduleType,
    Long sortOrder,
    String expectedResult,
    Boolean isBranchingEnabled,
    List<PassFailOptionDto> options,
    String instruction,
    Long minNumberOfAttachments,
    String title,
    Long branchingEntityId) {}
