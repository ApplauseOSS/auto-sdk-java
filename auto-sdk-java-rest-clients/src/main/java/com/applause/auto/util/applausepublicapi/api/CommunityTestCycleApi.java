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
import com.applause.auto.util.applausepublicapi.dto.enums.TestingMethodologyEnum;
import com.applause.auto.util.applausepublicapi.dto.enums.TestingTypeEnum;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.*;

public interface CommunityTestCycleApi {
  /**
   * Closes test cycle (Automation only)
   *
   * @param testCycleId (required)
   * @return CommunityTestCycleDto
   */
  @POST("v2/community-test-cycles/{testCycleId}/close")
  CompletableFuture<Response<CommunityTestCycleDto>> closeTestCycle(
      @Path("testCycleId") Long testCycleId);

  /**
   * Activates test cycle (Automation only)
   *
   * @param testCycleId (required)
   * @return CommunityTestCycleDto
   */
  @POST("v2/community-test-cycles/{testCycleId}/activate")
  CompletableFuture<Response<CommunityTestCycleDto>> activateTestCycle(
      @Path("testCycleId") Long testCycleId);

  /**
   * Creates test cycle. Requires at least one previously created test cycle of the given type and
   * methodology on the product. If provided methodology and type, it will clone the most recent
   * test cycle of the given type and methodology for the product and change the fields specified,
   * leaving the others as they were in the cloned test cycle. If provided templateTestCycleId
   * instead, it will clone the test cycle provided and use it as a template, while changing the
   * specified fields.
   *
   * @param body (required)
   * @param type Type of test cycle to clone (optional)
   * @param methodology Methodology of test cycle to clone (optional)
   * @return CommunityTestCycleDto
   */
  @Headers({"Content-Type:application/json"})
  @POST("v2/community-test-cycles")
  CompletableFuture<Response<CommunityTestCycleDto>> createTestCycle(
      @Body CommunityTestCycleCreateDto body,
      @Query("type") TestingTypeEnum type,
      @Query("methodology") TestingMethodologyEnum methodology);

  /**
   * Creates test cycle. Requires at least one previously created test cycle of the given type and
   * methodology on the product. If provided methodology and type, it will clone the most recent
   * test cycle of the given type and methodology for the product and change the fields specified,
   * leaving the others as they were in the cloned test cycle. If provided templateTestCycleId
   * instead, it will clone the test cycle provided and use it as a template, while changing the
   * specified fields.
   *
   * @param body (required)
   * @param templateTestCycleId (optional)
   * @param cloneSlots Param indicating whether slots should be cloned to the new test cycle
   *     (optional)
   * @return CommunityTestCycleDto
   */
  @Headers({"Content-Type:application/json"})
  @POST("v2/community-test-cycles")
  CompletableFuture<Response<CommunityTestCycleDto>> cloneTestCycle(
      @Body CommunityTestCycleCreateDto body,
      @Query("templateTestCycleId") Long templateTestCycleId,
      @Query("cloneSlots") Boolean cloneSlots);

  /**
   * Discards test cycle
   *
   * @param testCycleId (required)
   * @return CommunityTestCycleDto
   */
  @POST("v2/community-test-cycles/{testCycleId}/discard")
  CompletableFuture<Response<CommunityTestCycleDto>> discardTestCycle(
      @Path("testCycleId") Long testCycleId);

  /**
   * Generate csv from test case results
   *
   * @param body (required)
   * @param testCycleId (required)
   * @return Object
   */
  @Headers({"Content-Type:application/json"})
  @POST("v2/community-test-cycles/{testCycleId}/test-case-results/export")
  CompletableFuture<Response<Object>> exportTestCaseResultsToCsv(
      @Body TestCaseResultExportCriteria body, @Path("testCycleId") Long testCycleId);

  /**
   * Returns a paged list of test case results for the given test cycle.Default sort is by id
   * ascending. Supported sort criteria: id, createDate, testCaseId, testCycleId
   *
   * @param testCycleId (required)
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
  @GET("v2/community-test-cycles/{testCycleId}/test-case-results")
  CompletableFuture<Response<PageBaseTestCaseResultDto>> getCommunityTestCaseResults(
      @Path("testCycleId") Long testCycleId,
      @Query("testCaseId") Long testCaseId,
      @Query("statuses") List<Long> statuses,
      @Query("approvalStatuses") List<Long> approvalStatuses,
      @Query("page") Long page,
      @Query("size") Long size,
      @Query("sort") String sort);

  /**
   * Returns details of test cycle by ID
   *
   * @param testCycleId (required)
   * @return CommunityCommunityTestCycleDetailsDto
   */
  @GET("v2/community-test-cycles/{testCycleId}")
  CompletableFuture<Response<CommunityCommunityTestCycleDetailsDto>> getTestCycleDetails(
      @Path("testCycleId") Long testCycleId);

  /**
   * Returns information about test cycle progress
   *
   * @param testCycleId (required)
   * @return CommunityTestCycleProgressDto
   */
  @GET("v2/community-test-cycles/{testCycleId}/progress")
  CompletableFuture<Response<CommunityTestCycleProgressDto>> getTestCycleProgress(
      @Path("testCycleId") Long testCycleId);

  /**
   * Returns a paged list of test cycles for the company and product ids that the api-key has access
   * to. Default sort is by createDate descending. Supported sort criteria: id, name, createDate
   *
   * @param productIds IDs of products to narrow down the search. Note: api-key must grant access to
   *     all requested product ids (optional)
   * @param page Page number you want to retrieve (0..N) (optional)
   * @param size Number of records per page (optional)
   * @param sort Sorting criteria in the format: property(,asc|desc). See method description for
   *     default sorting and supported fields (optional)
   * @return PageCommunityTestCycleDto
   */
  @GET("v2/community-test-cycles")
  CompletableFuture<Response<PageCommunityTestCycleDto>> getTestCycles(
      @Query("productIds") List<Long> productIds,
      @Query("page") Long page,
      @Query("size") Long size,
      @Query("sort") String sort);

  /**
   * Locks test cycle
   *
   * @param testCycleId (required)
   * @return CommunityTestCycleDto
   */
  @POST("v2/community-test-cycles/{testCycleId}/lock")
  CompletableFuture<Response<CommunityTestCycleDto>> lockTestCycle(
      @Path("testCycleId") Long testCycleId);

  /**
   * Reactivates test cycle
   *
   * @param testCycleId (required)
   * @return CommunityTestCycleDto
   */
  @POST("v2/community-test-cycles/{testCycleId}/reactivate")
  CompletableFuture<Response<CommunityTestCycleDto>> reactivateTestCycle(
      @Path("testCycleId") Long testCycleId);

  /**
   * Requests activation of a test cycle
   *
   * @param testCycleId (required)
   * @return CommunityTestCycleDto
   */
  @POST("v2/community-test-cycles/{testCycleId}/request-activation")
  CompletableFuture<Response<CommunityTestCycleDto>> requestTestCycleActivation(
      @Path("testCycleId") Long testCycleId);

  /**
   * Requests test cycle closure
   *
   * @param testCycleId (required)
   * @return CommunityTestCycleDto
   */
  @POST("v2/community-test-cycles/{testCycleId}/request-closure")
  CompletableFuture<Response<CommunityTestCycleDto>> requestTestCycleClosure(
      @Path("testCycleId") Long testCycleId);

  /**
   * Updates specified fields of an existing test cycle
   *
   * @param body (required)
   * @param testCycleId (required)
   * @return CommunityCommunityTestCycleDetailsDto
   */
  @Headers({"Content-Type:application/json"})
  @PATCH("v2/community-test-cycles/{testCycleId}")
  CompletableFuture<Response<CommunityCommunityTestCycleDetailsDto>> updateTestCycle(
      @Body CommunityTestCyclePatchDto body, @Path("testCycleId") Long testCycleId);

  /**
   * Uploads a new general attachment for a test cycle specified by id
   *
   * @param testCycleId (required)
   * @param file (optional)
   * @return AttachmentDto
   */
  @Multipart
  @POST("v2/community-test-cycles/{testCycleId}/attachments")
  CompletableFuture<Response<AttachmentDto>> createTestCycleAttachments(
      @Path("testCycleId") Long testCycleId, @Part("file\"; filename=\"file") RequestBody file);

  /**
   * Returns information about a test cycle attachment specified by id
   *
   * @param testCycleId (required)
   * @param attachmentId (required)
   * @return AttachmentDto
   */
  @GET("v2/community-test-cycles/{testCycleId}/attachments/{attachmentId}")
  CompletableFuture<Response<AttachmentDto>> getTestCycleAttachment(
      @Path("testCycleId") Long testCycleId, @Path("attachmentId") Long attachmentId);

  /**
   * Returns general attachments for a given test cycle ID
   *
   * @param testCycleId (required)
   * @return List&lt;AttachmentDto&gt;
   */
  @GET("v2/community-test-cycles/{testCycleId}/attachments")
  CompletableFuture<Response<List<AttachmentDto>>> getTestCycleAttachments(
      @Path("testCycleId") Long testCycleId);

  /**
   * Deletes a general attachment with specified id
   *
   * @param testCycleId (required)
   * @param attachmentId (required)
   * @return Void
   */
  @DELETE("v2/community-test-cycles/{testCycleId}/attachments/{attachmentId}")
  CompletableFuture<Response<Void>> removeTestCycleAttachment(
      @Path("testCycleId") Long testCycleId, @Path("attachmentId") Long attachmentId);

  /**
   * Returns a list of issues for a given test cycle, based on provided criteria. Supported sort
   * criteria: id, status, severity, appComponent, fixWorthinessLevel
   *
   * @param testCycleId (required)
   * @param excludeBugStatuses Exclude bugs with given status ids (optional)
   * @param page Page number you want to retrieve (0..N) (optional)
   * @param size Number of records per page (optional)
   * @param sort Sorting criteria in the format: property(,asc|desc). See method description for
   *     default sorting and supported fields (optional)
   * @return PageBugDto
   */
  @GET("v2/community-test-cycles/{testCycleId}/issues")
  CompletableFuture<Response<PageBugDto>> getTestCycleIssues(
      @Path("testCycleId") Long testCycleId,
      @Query("excludeBugStatuses") List<Long> excludeBugStatuses,
      @Query("page") Long page,
      @Query("size") Long size,
      @Query("sort") String sort);

  /**
   * Returns a list of known issues for given test cycle, based on provided criteria. Supported sort
   * criteria: id, status, severity, appComponent, fixWorthinessLevel
   *
   * @param testCycleId (required)
   * @param includeDisabled Include disabled issues (optional)
   * @param page Page number you want to retrieve (0..N) (optional)
   * @param size Number of records per page (optional)
   * @param sort Sorting criteria in the format: property(,asc|desc). See method description for
   *     default sorting and supported fields (optional)
   * @return PageBugDto
   */
  @GET("v2/community-test-cycles/{testCycleId}/known-issues")
  CompletableFuture<Response<PageBugDto>> getTestCycleKnownIssues(
      @Path("testCycleId") Long testCycleId,
      @Query("includeDisabled") Boolean includeDisabled,
      @Query("page") Long page,
      @Query("size") Long size,
      @Query("sort") String sort);

  /**
   * Add new test cases to existing test cycle test cases list.
   *
   * @param body (required)
   * @param testCycleId (required)
   * @return Void
   */
  @Headers({"Content-Type:application/json"})
  @POST("v2/community-test-cycles/{testCycleId}/test-cases")
  CompletableFuture<Response<Void>> addTestCycleTestCases(
      @Body List<Long> body, @Path("testCycleId") Long testCycleId);

  /**
   * Get test cases from test cycle by its id.
   *
   * @param testCycleId (required)
   * @return List&lt;CommunityTestCycleTestCaseDto&gt;
   */
  @GET("v2/community-test-cycles/{testCycleId}/test-cases")
  CompletableFuture<Response<List<CommunityTestCycleTestCaseDto>>> getTestCycleTestCases(
      @Path("testCycleId") Long testCycleId);

  /**
   * Remove included test case by its id.
   *
   * @param testCycleId (required)
   * @param testCaseId (required)
   * @return Void
   */
  @DELETE("v2/community-test-cycles/{testCycleId}/test-cases/{testCaseId}")
  CompletableFuture<Response<Void>> removeTestCycleTestCase(
      @Path("testCycleId") Long testCycleId, @Path("testCaseId") Long testCaseId);
}
