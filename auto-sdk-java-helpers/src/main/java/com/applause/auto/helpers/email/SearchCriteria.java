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
package com.applause.auto.helpers.email;

/**
 * Simple record that represents the search criteria when searching for emails.
 *
 * @param emailSubject The subject of the email to search for.
 * @param sentFrom The sender of the email to search for.
 * @param sentTo The recipient of the email to search for.
 */
public record SearchCriteria(String emailSubject, String sentFrom, String sentTo) {}
