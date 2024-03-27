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

import com.applause.auto.util.applausepublicapi.dto.InternalTestCycleGetDto;
import com.applause.auto.util.applausepublicapi.dto.NewInternalTestCycleOptions;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import retrofit2.Response;
import retrofit2.http.*;

public interface InternalTestCycleApi {
  /**
   * Creates test cycle by cloning an existing one. Optionally, some fields in the new cycle can be
   * modified.
   *
   * @param body (required)
   * @param templateTestCycleId (required)
   * @param cloneKnownIssues (required)
   * @param cloneSlots (required)
   * @param timeZoneOffsetSeconds (required)
   * @param newTestCycleName (optional)
   * @param cloneAttachmentIds (optional)
   * @return InternalTestCycleGetDto
   */
  @Headers({"Content-Type:application/json"})
  @POST("v2/internal-test-cycles")
  CompletableFuture<Response<InternalTestCycleGetDto>> createTestCycle(
      @Body NewInternalTestCycleOptions body,
      @Query("templateTestCycleId") Long templateTestCycleId,
      @Query("cloneKnownIssues") Boolean cloneKnownIssues,
      @Query("cloneSlots") Boolean cloneSlots,
      @Query("timeZoneOffsetSeconds") Long timeZoneOffsetSeconds,
      @Query("newTestCycleName") String newTestCycleName,
      @Query("cloneAttachmentIds") List<Long> cloneAttachmentIds);
}
