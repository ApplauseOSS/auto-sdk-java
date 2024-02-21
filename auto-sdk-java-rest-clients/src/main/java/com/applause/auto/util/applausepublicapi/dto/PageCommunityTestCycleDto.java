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
 * PageCommunityTestCycleDto
 *
 * @param totalPages -- GETTER -- Get totalPages
 * @param totalElements -- GETTER -- Get totalElements
 * @param size -- GETTER -- Get size
 * @param content -- GETTER -- Get content
 * @param number -- GETTER -- Get number
 * @param pageable -- GETTER -- Get pageable
 * @param numberOfElements -- GETTER -- Get numberOfElements
 */
public record PageCommunityTestCycleDto(
    Long totalPages,
    Long totalElements,
    Long size,
    List<CommunityTestCycleDto> content,
    Long number,
    PageableObject pageable,
    Boolean last,
    Long numberOfElements,
    Boolean empty) {}
