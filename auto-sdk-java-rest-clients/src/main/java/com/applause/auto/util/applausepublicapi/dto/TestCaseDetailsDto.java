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
import com.applause.auto.util.applausepublicapi.dto.enums.TestCaseTypeEnum;
import java.util.List;

/**
 * TestCaseDetailsDto
 *
 * @param testCaseVersionId -- GETTER -- Get testCaseVersionId
 * @param name -- GETTER -- Get name
 * @param testCaseId -- GETTER -- Get testCaseId
 * @param status -- GETTER -- Get status
 * @param type -- GETTER -- Get type
 * @param testSuites -- GETTER -- Get testSuites
 * @param product -- GETTER -- Get product
 * @param description -- GETTER -- Get description
 * @param preconditions -- GETTER -- Get preconditions
 * @param steps -- GETTER -- Get steps
 * @param customFields -- GETTER -- Get customFields
 */
public record TestCaseDetailsDto(
    Long testCaseVersionId,
    String name,
    Long testCaseId,
    TestCaseStatusEnum status,
    TestCaseTypeEnum type,
    List<NamedEntityDto> testSuites,
    NamedEntityDto product,
    String description,
    String preconditions,
    List<TestCaseStepDto> steps,
    List<CustomFieldDto> customFields) {}
