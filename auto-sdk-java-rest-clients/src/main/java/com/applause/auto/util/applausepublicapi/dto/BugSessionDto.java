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
 * BugSessionDto
 *
 * @param group -- GETTER -- Get group
 * @param messageId -- GETTER -- Get messageId
 * @param timestamp -- GETTER -- Get timestamp
 * @param message -- GETTER -- Get message
 * @param dataJson -- GETTER -- Get dataJson
 */
public record BugSessionDto(
    String group, String messageId, String timestamp, String message, String dataJson) {}
