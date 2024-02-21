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

/**
 * InstructionsDto
 *
 * @param inScope -- GETTER -- Get inScope
 * @param outOfScope -- GETTER -- Get outOfScope
 * @param specialInstructions -- GETTER -- Get specialInstructions
 * @param setupInstructions -- GETTER -- Get setupInstructions
 * @param testCaseInstructions -- GETTER -- Get testCaseInstructions
 * @param issueReportingInstructions -- GETTER -- Get issueReportingInstructions
 */
public record InstructionsDto(
    String inScope,
    String outOfScope,
    String specialInstructions,
    String setupInstructions,
    String testCaseInstructions,
    String issueReportingInstructions) {}
