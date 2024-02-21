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
 * AttachmentDto
 *
 * @param id -- GETTER -- Get id
 * @param fileName -- GETTER -- Get fileName
 * @param contentType -- GETTER -- Get contentType
 * @param size -- GETTER -- Get size
 * @param url -- GETTER -- Get url
 * @param timeline -- GETTER -- Get timeline
 */
public record AttachmentDto(
    Long id, String fileName, String contentType, Long size, String url, TimelineDto timeline) {}
