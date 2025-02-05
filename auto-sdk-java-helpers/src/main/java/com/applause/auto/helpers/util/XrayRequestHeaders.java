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
package com.applause.auto.helpers.util;

import io.restassured.http.Header;

public final class XrayRequestHeaders {
  private XrayRequestHeaders() {
    // utility class
  }

  public static Header getAcceptApplicationJsonHeader() {
    return new Header("Accept", "application/json");
  }

  public static Header getContentTypeMultipartFormDataHeader() {
    return new Header("Content-Type", "multipart/form-data");
  }

  public static Header getBearerAuthorizationHeader(final String token) {
    return new Header("Authorization", "Bearer " + token);
  }

  public static Header getJSONContentTypeHeader() {
    return new Header("Content-Type", "application/json; charset=utf-8");
  }

  public static Header getAtlassianNoCheckHeader() {
    return new Header("X-Atlassian-Token", "no-check");
  }
}
