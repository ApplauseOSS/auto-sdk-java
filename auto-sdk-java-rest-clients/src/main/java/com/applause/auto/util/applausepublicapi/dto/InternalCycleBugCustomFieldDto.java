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

import com.applause.auto.util.applausepublicapi.dto.enums.FieldTypeEnum;
import java.util.List;

/**
 * InternalCycleBugCustomFieldDto
 *
 * @param id -- GETTER -- Get id
 * @param fieldLabel -- GETTER -- Get fieldLabel
 * @param description -- GETTER -- Get description
 * @param fieldType -- GETTER -- Get fieldType
 * @param order -- GETTER -- Get order
 * @param options -- GETTER -- Get options
 */
public record InternalCycleBugCustomFieldDto(
    Long id,
    String fieldLabel,
    String description,
    FieldTypeEnum fieldType,
    Long order,
    List<InternalCycleBugCustomFieldOption> options) {}