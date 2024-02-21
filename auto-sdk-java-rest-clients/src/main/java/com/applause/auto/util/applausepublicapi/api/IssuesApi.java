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

public interface IssuesApi {
  /**
   * Returns attachments for proper test issue
   *
   * @param issueId (required)
   * @return List&lt;AttachmentDto&gt;
   */
  @GET("v2/issues/{issueId}/attachments")
  CompletableFuture<Response<List<AttachmentDto>>> getIssueAttachments(
      @retrofit2.http.Path("issueId") Long issueId);

  /**
   * Returns single test issue by its id
   *
   * @param issueId (required)
   * @return BugDetailsDto
   */
  @GET("v2/issues/{issueId}")
  CompletableFuture<Response<BugDetailsDto>> getIssueDetails(
      @retrofit2.http.Path("issueId") Long issueId);

  /**
   * Marks issue as won&#x27;t fix
   *
   * @param body (required)
   * @param issueId (required)
   * @return BugDto
   */
  @Headers({"Content-Type:application/json"})
  @PUT("v2/issues/{issueId}/wont-fix")
  CompletableFuture<Response<BugDto>> markIssueWontFix(
      @retrofit2.http.Body BugWontFixDto body, @retrofit2.http.Path("issueId") Long issueId);

  /**
   * Marks issue as (un)known
   *
   * @param body (required)
   * @param issueId (required)
   * @return BugDto
   */
  @Headers({"Content-Type:application/json"})
  @PUT("v2/issues/{issueId}/known-issue")
  CompletableFuture<Response<BugDto>> markKnownIssue(
      @retrofit2.http.Body BugKnownIssueDto body, @retrofit2.http.Path("issueId") Long issueId);

  /**
   * Marks issue as (un)resolved
   *
   * @param body (required)
   * @param issueId (required)
   * @return BugDto
   */
  @Headers({"Content-Type:application/json"})
  @PUT("v2/issues/{issueId}/resolve")
  CompletableFuture<Response<BugDto>> resolveIssue(
      @retrofit2.http.Body BugResolutionDto body, @retrofit2.http.Path("issueId") Long issueId);

  /**
   * Requests the bug fix to be verified
   *
   * @param body (required)
   * @return Void
   */
  @Headers({"Content-Type:application/json"})
  @PUT("v2/issues/fix-verification")
  CompletableFuture<Response<Void>> submitIssuesForVerification(
      @retrofit2.http.Body BugVerificationRequestDto body);

  /**
   * Updates issue by id
   *
   * @param body (required)
   * @param issueId (required)
   * @return BugDto
   */
  @Headers({"Content-Type:application/json"})
  @PUT("v2/issues/{issueId}")
  CompletableFuture<Response<BugDto>> updateIssue(
      @retrofit2.http.Body BugUpdateDto body, @retrofit2.http.Path("issueId") Long issueId);

  /**
   * Updates BTS manually exported status
   *
   * @param body (required)
   * @param issueId (required)
   * @return Void
   */
  @Headers({"Content-Type:application/json"})
  @PUT("v2/issues/{issueId}/bts/manually-exported")
  CompletableFuture<Response<Void>> updateIssueManuallyExportedStatus(
      @retrofit2.http.Body BugManuallyExportedUpdateDto body,
      @retrofit2.http.Path("issueId") Long issueId);
}
