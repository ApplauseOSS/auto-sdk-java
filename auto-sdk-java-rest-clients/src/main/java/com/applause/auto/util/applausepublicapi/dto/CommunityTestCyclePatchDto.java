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

import java.util.Date;

/**
 * CommunityTestCyclePatchDto
 *
 * @param name -- GETTER -- Get name
 * @param buildId -- GETTER -- Get buildId
 * @param inScope -- GETTER -- Get inScope
 * @param outOfScope -- GETTER -- Get outOfScope
 * @param issueReportingInstructions -- GETTER -- Get issueReportingInstructions
 * @param testCaseInstructions -- GETTER -- Get testCaseInstructions
 * @param teamContactInformation -- GETTER -- Get teamContactInformation
 * @param setupInstructions -- GETTER -- Get setupInstructions
 * @param specialInstructions -- GETTER -- Get specialInstructions
 * @param specialRequirement -- GETTER -- Get specialRequirement
 * @param ttlInstructions -- GETTER -- Get ttlInstructions
 * @param productDevelopmentStageId -- GETTER -- Get productDevelopmentStageId
 * @param startDate -- GETTER -- Get startDate
 * @param endDate -- GETTER -- Get endDate
 */
public record CommunityTestCyclePatchDto(
    String name,
    Long buildId,
    String inScope,
    String outOfScope,
    String issueReportingInstructions,
    String testCaseInstructions,
    String teamContactInformation,
    String setupInstructions,
    String specialInstructions,
    String specialRequirement,
    String ttlInstructions,
    Long productDevelopmentStageId,
    Date startDate,
    Date endDate) {}
