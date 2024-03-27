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

public interface TestCaseApi {
  /**
   * Archives test case with specified id
   *
   * @param testCaseId (required)
   * @return Void
   */
  @POST("v2/test-cases/{testCaseId}/archive")
  CompletableFuture<Response<Void>> archiveTestCase(@Path("testCaseId") Long testCaseId);

  /**
   * Clones a given test case
   *
   * @param testCaseId (required)
   * @return Long
   */
  @POST("v2/test-cases/{testCaseId}/clone")
  CompletableFuture<Response<Long>> cloneTestCase(@Path("testCaseId") Long testCaseId);

  /**
   * Creates a new test case
   *
   * @param body (required)
   * @return Long
   */
  @Headers({"Content-Type:application/json"})
  @POST("v2/test-cases")
  CompletableFuture<Response<Long>> createTestCase(@Body TestCaseCreateDto body);

  /**
   * Create test case step for given test case id
   *
   * @param body (required)
   * @param testCaseId (required)
   * @return TestCaseStepDto
   */
  @Headers({"Content-Type:application/json"})
  @POST("v2/test-cases/{testCaseId}/steps")
  CompletableFuture<Response<TestCaseStepDto>> createTestCaseStep(
      @Body TestCaseStepCreateDto body, @Path("testCaseId") Long testCaseId);

  /**
   * Discards test case with specified id
   *
   * @param testCaseId (required)
   * @return Void
   */
  @POST("v2/test-cases/{testCaseId}/discard")
  CompletableFuture<Response<Void>> discardTestCase(@Path("testCaseId") Long testCaseId);

  /**
   * Returns details of a test case with given ID.
   *
   * @param testCaseId (required)
   * @return TestCaseDetailsDto
   */
  @GET("v2/test-cases/{testCaseId}")
  CompletableFuture<Response<TestCaseDetailsDto>> getTestCaseDetails(
      @Path("testCaseId") Long testCaseId);

  /**
   * Returns test case result history with given test case Id.
   *
   * @param testCaseId (required)
   * @return PageTestCaseResultHistoryDto
   */
  @GET("v2/test-cases/{testCaseId}/history")
  CompletableFuture<Response<PageTestCaseResultHistoryDto>> getTestCaseResultHistory(
      @Path("testCaseId") Long testCaseId);

  /**
   * Returns a paged list of test cases for the given product. Default sort is by createDate
   * descending. Supported sort criteria: id, name, createDate
   *
   * @param productId ID of the product. Note: api-key must grant access to requested product id
   *     (required)
   * @param statusIds IDs of test case statuses to display. Default: draft and published (optional)
   * @param excludeBfvTestCases Describes if the response should contain BFV test cases. Default:
   *     true (optional)
   * @param page Page number you want to retrieve (0..N) (optional)
   * @param size Number of records per page (optional)
   * @param sort Sorting criteria in the format: property(,asc|desc). See method description for
   *     default sorting and supported fields (optional)
   * @return PageTestCaseDto
   */
  @GET("v2/test-cases")
  CompletableFuture<Response<PageTestCaseDto>> getTestCases(
      @Query("productId") Long productId,
      @Query("statusIds") List<Long> statusIds,
      @Query("excludeBfvTestCases") Boolean excludeBfvTestCases,
      @Query("page") Long page,
      @Query("size") Long size,
      @Query("sort") String sort);

  /**
   * Publishes test case with specified id
   *
   * @param testCaseId (required)
   * @return Void
   */
  @POST("v2/test-cases/{testCaseId}/publish")
  CompletableFuture<Response<Void>> publishTestCase(@Path("testCaseId") Long testCaseId);

  /**
   * Deletes test case step with specified id
   *
   * @param testCaseId (required)
   * @param stepId (required)
   * @return Void
   */
  @DELETE("v2/test-cases/{testCaseId}/steps/{stepId}")
  CompletableFuture<Response<Void>> removeTestCaseStep(
      @Path("testCaseId") Long testCaseId, @Path("stepId") Long stepId);

  /**
   * Updates elements of a test case with given ID.
   *
   * @param body (required)
   * @param testCaseId (required)
   * @return Void
   */
  @Headers({"Content-Type:application/json"})
  @PATCH("v2/test-cases/{testCaseId}")
  CompletableFuture<Response<Void>> updateTestCase(
      @Body TestCaseUpdateDto body, @Path("testCaseId") Long testCaseId);

  /**
   * Update test case test suites given list of ids
   *
   * @param body (required)
   * @param testCaseId (required)
   * @return List&lt;TestSuiteTestCaseDto&gt;
   */
  @Headers({"Content-Type:application/json"})
  @PUT("v2/test-cases/{testCaseId}/test-suites")
  CompletableFuture<Response<List<TestSuiteTestCaseDto>>> updateTestCaseTestSuites(
      @Body List<Long> body, @Path("testCaseId") Long testCaseId);
}
