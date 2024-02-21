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

import com.applause.auto.util.applausepublicapi.dto.enums.BugFrequencyEnum;
import com.applause.auto.util.applausepublicapi.dto.enums.BugQualityRatingEnum;
import com.applause.auto.util.applausepublicapi.dto.enums.BugSeverityEnum;
import com.applause.auto.util.applausepublicapi.dto.enums.BugTypeEnum;
import java.util.List;

/**
 * BugUpdateDto
 *
 * @param type -- GETTER -- Get type
 * @param frequency -- GETTER -- Get frequency
 * @param severity -- GETTER -- Get severity
 * @param subject -- GETTER -- Get subject
 * @param additionalEnvironmentInfo -- GETTER -- Get additionalEnvironmentInfo
 * @param actionPerform -- GETTER -- Get actionPerform
 * @param expectedResult -- GETTER -- Get expectedResult
 * @param actualResult -- GETTER -- Get actualResult
 * @param errorMessage -- GETTER -- Get errorMessage
 * @param buildId -- GETTER -- Get buildId
 * @param qualityRating -- GETTER -- Get qualityRating
 * @param environmentGroupId -- GETTER -- Get environmentGroupId
 * @param appComponentId -- GETTER -- Get appComponentId
 */
public record BugUpdateDto(
    BugTypeEnum type,
    BugFrequencyEnum frequency,
    BugSeverityEnum severity,
    String subject,
    String additionalEnvironmentInfo,
    String actionPerform,
    String expectedResult,
    String actualResult,
    String errorMessage,
    Long buildId,
    BugQualityRatingEnum qualityRating,
    List<Long> environmentGroupId,
    Long appComponentId) {}
