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

/**
 * TestSuiteTestCaseDto
 *
 * @param id -- GETTER -- Get id
 * @param name -- GETTER -- Get name
 * @param testCaseVersionId -- GETTER -- Get testCaseVersionId
 * @param testCaseId -- GETTER -- Get testCaseId
 * @param testCaseStatus -- GETTER -- Get testCaseStatus
 */
public record TestSuiteTestCaseDto(
    Long id,
    String name,
    Long testCaseVersionId,
    Long testCaseId,
    TestCaseStatusEnum testCaseStatus) {}
