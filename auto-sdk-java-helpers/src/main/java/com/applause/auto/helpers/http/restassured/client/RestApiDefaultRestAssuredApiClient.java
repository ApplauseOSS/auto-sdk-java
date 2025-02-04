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

import static io.restassured.config.ConnectionConfig.connectionConfig;

import com.applause.auto.helpers.http.mapping.IRestObjectMapper;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.specification.RequestSpecification;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;

/** Default rest assured REST API client impl. */
public class RestApiDefaultRestAssuredApiClient extends RestAssuredApiClient {

  public RestApiDefaultRestAssuredApiClient(final Duration apiRequestWaitTimeoutDuration) {
    super(apiRequestWaitTimeoutDuration);
  }

  /**
   * get rest assured request specification object for default client
   *
   * @return request specification
   */
  @Override
  public RequestSpecification restAssuredRequestSpecification(
      @NonNull final IRestObjectMapper restObjectMapper) {
    return super.restAssuredRequestSpecification(restObjectMapper)
        .filter(new RequestLoggingFilter(LogDetail.URI))
        .response()
        .logDetail(LogDetail.STATUS)
        .request();
  }

  /**
   * rest assured config with object mapper for default client
   *
   * @param restObjectMapper the object mapper
   * @return configured RestAssured client
   */
  @Override
  protected RestAssuredConfig restAssuredConfig(@NonNull final IRestObjectMapper restObjectMapper) {
    RestAssuredConfig restAssuredConfig = new RestAssuredConfig();
    restAssuredConfig =
        restAssuredConfig
            .objectMapperConfig(
                new ObjectMapperConfig()
                    .jackson2ObjectMapperFactory(
                        (cls, charset) -> restObjectMapper.restJsonObjectMapper()))
            .connectionConfig(
                connectionConfig()
                    .closeIdleConnectionsAfterEachResponseAfter(
                        getApiRequestWaitTimeoutDuration().getSeconds(), TimeUnit.SECONDS));
    return restAssuredConfig;
  }
}
