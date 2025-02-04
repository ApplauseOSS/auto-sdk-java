/*
 *
 * Copyright © 2025 Applause App Quality, Inc.
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
package com.applause.auto.helpers.jira.dto.responsemappers.steps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents data with a type and value.
 *
 * @param type The type of the data.
 * @param value The value of the data.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Data(String type, Value value) {}
