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
package com.applause.auto.helpers.http.restassured;

import com.applause.auto.helpers.http.mapping.JacksonJSONRestObjectMapping;
import com.applause.auto.helpers.http.restassured.client.RestApiDefaultRestAssuredApiClient;
import com.applause.auto.helpers.http.restassured.client.RestAssuredApiClient;
import io.restassured.specification.RequestSpecification;
import java.time.Duration;
import lombok.Getter;

/**
 * Default rest assured http client for REST API usage wrapper class For requests with different
 * retry policies - AwaitilityWaitUtils could be used
 */
@Getter
public class RestApiDefaultRestAssuredApiHelper {

  private RestAssuredApiClient restAssuredApiClient;

  public RestApiDefaultRestAssuredApiHelper() {
    restAssuredApiClient = new RestApiDefaultRestAssuredApiClient(Duration.ofSeconds(60));
  }

  /**
   * 'with' describing default RestAssuredConfig
   *
   * @return
   */
  public RequestSpecification withDefaultRestHttpClientConfigsSpecification() {
    return restAssuredApiClient.restAssuredRequestSpecification(new JacksonJSONRestObjectMapping());
  }
}
