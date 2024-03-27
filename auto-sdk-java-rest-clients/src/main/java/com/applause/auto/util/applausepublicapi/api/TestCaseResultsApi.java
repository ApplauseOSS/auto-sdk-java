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

public interface TestCaseResultsApi {
  /**
   * Approve test case result based on id
   *
   * @param testCaseResultId (required)
   * @return Void
   */
  @PATCH("v2/test-case-results/{testCaseResultId}/approve")
  CompletableFuture<Response<Void>> approveTestCaseResult(
      @Path("testCaseResultId") Long testCaseResultId);

  /**
   * Returns details of a specified automation test case result.
   *
   * @param testCaseResultId (required)
   * @return AutoTestCaseResultDto
   */
  @GET("v2/test-case-results/auto/{testCaseResultId}")
  CompletableFuture<Response<AutoTestCaseResultDto>> getAutoTestCaseResultDetails(
      @Path("testCaseResultId") Long testCaseResultId);

  /**
   * Returns details of a specified manual test case result.
   *
   * @param testCaseResultId (required)
   * @return ManualTestCaseResultDto
   */
  @GET("v2/test-case-results/manual/{testCaseResultId}")
  CompletableFuture<Response<ManualTestCaseResultDto>> getManualTestCaseResultDetails(
      @Path("testCaseResultId") Long testCaseResultId);

  /**
   * Returns a list of bugs related to test case result.
   *
   * @param testCaseResultId (required)
   * @param page Page number you want to retrieve (0..N) (optional)
   * @param size Number of records per page (optional)
   * @param sort Sorting criteria in the format: property(,asc|desc). See method description for
   *     default sorting and supported fields (optional)
   * @return PageBugDto
   */
  @GET("v2/test-case-results/{testCaseResultId}/issues")
  CompletableFuture<Response<PageBugDto>> getTestCaseResultIssues(
      @Path("testCaseResultId") Long testCaseResultId,
      @Query("page") Long page,
      @Query("size") Long size,
      @Query("sort") String sort);

  /**
   * Returns a paged list of test case results. At least one of filters must be applied by:
   * testCycleId or testCaseId Default sort is by id ascending. Supported sort criteria: id,
   * createDate, testCaseId, testCycleId
   *
   * @param testCycleId ID of the test cycle to find results by. Note: api-key must grant access to
   *     requested test cycle (optional)
   * @param testCaseId ID of the test case to filter results by. Note: api-key must grant access to
   *     requested test case (optional)
   * @param statuses (optional)
   * @param approvalStatuses (optional)
   * @param page Page number you want to retrieve (0..N) (optional)
   * @param size Number of records per page (optional)
   * @param sort Sorting criteria in the format: property(,asc|desc). See method description for
   *     default sorting and supported fields (optional)
   * @return PageBaseTestCaseResultDto
   */
  @GET("v2/test-case-results")
  CompletableFuture<Response<PageBaseTestCaseResultDto>> getTestCaseResults(
      @Query("testCycleId") Long testCycleId,
      @Query("testCaseId") Long testCaseId,
      @Query("statuses") List<Long> statuses,
      @Query("approvalStatuses") List<Long> approvalStatuses,
      @Query("page") Long page,
      @Query("size") Long size,
      @Query("sort") String sort);

  /**
   * Returns basic info about a specified test case result
   *
   * @param testCaseResultId (required)
   * @return BaseTestCaseResultDto
   */
  @GET("v2/test-case-results/{testCaseResultId}")
  CompletableFuture<Response<BaseTestCaseResultDto>> getTestCaseResult(
      @Path("testCaseResultId") Long testCaseResultId);
}
