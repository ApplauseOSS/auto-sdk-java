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
 * IssueStatisticsDto
 *
 * @param total -- GETTER -- Get total
 * @param newIssues -- GETTER -- Get newIssues
 * @param approved -- GETTER -- Get approved
 * @param rejected -- GETTER -- Get rejected
 * @param disputed -- GETTER -- Get disputed
 * @param pendingApproval -- GETTER -- Get pendingApproval
 * @param pendingRejection -- GETTER -- Get pendingRejection
 * @param infoRequested -- GETTER -- Get infoRequested
 * @param underReview -- GETTER -- Get underReview
 * @param discarded -- GETTER -- Get discarded
 */
public record IssueStatisticsDto(
    Long total,
    Long newIssues,
    Long approved,
    Long rejected,
    Long disputed,
    Long pendingApproval,
    Long pendingRejection,
    Long infoRequested,
    Long underReview,
    Long discarded) {}
