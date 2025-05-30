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
package com.applause.auto.helpers.http.mapping;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Jackson library json REST API typical object mapper instance */
public class JacksonJSONRestObjectMapping implements IRestObjectMapper {
  private static final ObjectMapper objectMapper =
      new ObjectMapper()
          .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);

  /**
   * Object mapper for REST JSON Jackson mapping
   *
   * @return shared ObjectMapper
   */
  @Override
  public ObjectMapper restJsonObjectMapper() {
    return objectMapper;
  }
}
