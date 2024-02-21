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

import com.applause.auto.util.applausepublicapi.dto.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import retrofit2.Response;
import retrofit2.http.*;

public interface TestSuitesApi {
  /**
   * Adds test cases to a test suite
   *
   * @param body (required)
   * @param testSuiteId (required)
   * @return Void
   */
  @Headers({"Content-Type:application/json"})
  @POST("v2/test-suites/{testSuiteId}/test-cases")
  CompletableFuture<Response<Void>> addTestCasesToTestSuite(
      @retrofit2.http.Body List<Long> body, @retrofit2.http.Path("testSuiteId") Long testSuiteId);

  /**
   * Creates a new test suite
   *
   * @param body (required)
   * @return TestSuiteDto
   */
  @Headers({"Content-Type:application/json"})
  @POST("v2/test-suites")
  CompletableFuture<Response<TestSuiteDto>> createTestSuite(
      @retrofit2.http.Body TestSuiteCreateDto body);

  /**
   * Deletes a test suite
   *
   * @param testSuiteId (required)
   * @return Void
   */
  @DELETE("v2/test-suites/{testSuiteId}")
  CompletableFuture<Response<Void>> deleteTestSuite(
      @retrofit2.http.Path("testSuiteId") Long testSuiteId);

  /**
   * Generate csv from test case results belonging to test suite
   *
   * @param testSuiteId (required)
   * @param showColumnSteps Set to true if data in csv file should be formatted column-wise
   *     (optional)
   * @param separateMultiSelect Set to true if multi-selected values should be split into separate
   *     columns (optional)
   * @return byte[]
   */
  @POST("v2/test-suites/{testSuiteId}/test-case-results/export")
  CompletableFuture<Response<byte[]>> exportTestCaseResultsToCsv(
      @retrofit2.http.Path("testSuiteId") Long testSuiteId,
      @retrofit2.http.Query("showColumnSteps") Boolean showColumnSteps,
      @retrofit2.http.Query("separateMultiSelect") Boolean separateMultiSelect);

  /**
   * Returns test cases for a given test suite
   *
   * @param testSuiteId (required)
   * @return List&lt;TestSuiteTestCaseDto&gt;
   */
  @GET("v2/test-suites/{testSuiteId}/test-cases")
  CompletableFuture<Response<List<TestSuiteTestCaseDto>>> getTestCasesByTestSuiteId(
      @retrofit2.http.Path("testSuiteId") Long testSuiteId);

  /**
   * Returns test suite details by id
   *
   * @param testSuiteId (required)
   * @return TestSuiteDetailsDto
   */
  @GET("v2/test-suites/{testSuiteId}")
  CompletableFuture<Response<TestSuiteDetailsDto>> getTestSuiteDetails(
      @retrofit2.http.Path("testSuiteId") Long testSuiteId);

  /**
   * Returns a paged list of test suites for the given product. Default sort is by createDate
   * descending. Supported sort criteria: id, name, createDate
   *
   * @param productId ID of the product. Note: api-key must grant access to requested product id
   *     (required)
   * @param page Page number you want to retrieve (0..N) (optional)
   * @param size Number of records per page (optional)
   * @param sort Sorting criteria in the format: property(,asc|desc). See method description for
   *     default sorting and supported fields (optional)
   * @return PageTestSuiteDto
   */
  @GET("v2/test-suites")
  CompletableFuture<Response<PageTestSuiteDto>> getTestSuites(
      @retrofit2.http.Query("productId") Long productId,
      @retrofit2.http.Query("page") Long page,
      @retrofit2.http.Query("size") Long size,
      @retrofit2.http.Query("sort") String sort);

  /**
   * Removes a test case from a test suite
   *
   * @param testSuiteId (required)
   * @param testCaseId (required)
   * @return Void
   */
  @DELETE("v2/test-suites/{testSuiteId}/test-cases/{testCaseId}")
  CompletableFuture<Response<Void>> removeTestCaseFromTestSuite(
      @retrofit2.http.Path("testSuiteId") Long testSuiteId,
      @retrofit2.http.Path("testCaseId") Long testCaseId);
}
