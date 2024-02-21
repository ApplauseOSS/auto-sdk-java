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
package com.applause.auto.util.applausepublicapi.api;

import com.applause.auto.util.applausepublicapi.dto.AppComponentDto;
import com.applause.auto.util.applausepublicapi.dto.CustomFieldDefinitionDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import retrofit2.Response;
import retrofit2.http.*;

public interface ProductsApi {
  /**
   * Returns app components for given product
   *
   * @param productId (required)
   * @return List&lt;AppComponentDto&gt;
   */
  @GET("v2/products/{productId}/app-components")
  CompletableFuture<Response<List<AppComponentDto>>> getProductAppComponents(
      @retrofit2.http.Path("productId") Long productId);

  /**
   * Returns custom fields for given product
   *
   * @param productId (required)
   * @return List&lt;CustomFieldDefinitionDto&gt;
   */
  @GET("v2/products/{productId}/custom-fields")
  CompletableFuture<Response<List<CustomFieldDefinitionDto>>> getProductCustomFields(
      @retrofit2.http.Path("productId") Long productId);
}
