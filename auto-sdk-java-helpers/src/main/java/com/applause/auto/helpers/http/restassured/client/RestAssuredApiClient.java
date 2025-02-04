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
package com.applause.auto.helpers.http.restassured.client;

import static io.restassured.RestAssured.given;

import com.applause.auto.helpers.http.mapping.IRestObjectMapper;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import java.time.Duration;
import lombok.Getter;

/** Rest assured api client */
@Getter
public abstract class RestAssuredApiClient {

  private Duration apiRequestWaitTimeoutDuration;

  public RestAssuredApiClient(Duration apiRequestWaitTimeoutDuration) {
    this.apiRequestWaitTimeoutDuration = apiRequestWaitTimeoutDuration;
  }

  /**
   * get rest assured request specification object
   *
   * @return
   */
  public RequestSpecification restAssuredRequestSpecification(IRestObjectMapper restObjectMapper) {
    return given().when().request().config(restAssuredConfig(restObjectMapper));
  }

  /**
   * abstract rest assured config object
   *
   * @return
   */
  protected abstract RestAssuredConfig restAssuredConfig(IRestObjectMapper restObjectMapper);
}
