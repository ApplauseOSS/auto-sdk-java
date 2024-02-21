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
 * TestCaseStepDto
 *
 * @param id -- GETTER -- Get id
 * @param stepNumber -- GETTER -- Get stepNumber
 * @param name -- GETTER -- Get name
 * @param instruction -- GETTER -- Get instruction
 * @param expectedResult -- GETTER -- Get expectedResult
 * @param estimatedTimeInMin -- GETTER -- Get estimatedTimeInMin
 * @param testCaseVersionId -- GETTER -- Get testCaseVersionId
 * @param minNumberOfAttachments -- GETTER -- Get minNumberOfAttachments
 * @param testCaseStepModules -- GETTER -- Get testCaseStepModules
 * @param branchingEntityId -- GETTER -- Get branchingEntityId
 */
public record TestCaseStepDto(
    Long id,
    Long stepNumber,
    String name,
    String instruction,
    String expectedResult,
    Long estimatedTimeInMin,
    Long testCaseVersionId,
    Long minNumberOfAttachments,
    List<TestCaseStepModuleDto> testCaseStepModules,
    Long branchingEntityId) {}
