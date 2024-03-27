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

import com.applause.auto.util.applausepublicapi.dto.AttachmentWithHashesDto;
import com.applause.auto.util.applausepublicapi.dto.PageProductVersionDetailsDto;
import com.applause.auto.util.applausepublicapi.dto.ProductVersionDetailsDto;
import com.applause.auto.util.applausepublicapi.dto.ProductVersionDto;
import com.applause.auto.util.applausepublicapi.dto.ProductVersionUpdateDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.*;

public interface BuildApi {
  /**
   * Creates a new build
   *
   * @param name (required)
   * @param productId (required)
   * @param file (optional)
   * @param description (optional)
   * @param releaseUrls (optional)
   * @return ProductVersionDetailsDto
   */
  @Multipart
  @POST("v2/builds")
  CompletableFuture<Response<ProductVersionDetailsDto>> createBuild(
      @Query("name") String name,
      @Query("productId") Long productId,
      @Part("file\"; filename=\"file") RequestBody file,
      @Query("description") String description,
      @Query("releaseUrls") List<String> releaseUrls);

  /**
   * Returns details for a build specified by id
   *
   * @param buildId (required)
   * @return ProductVersionDetailsDto
   */
  @GET("v2/builds/{buildId}")
  CompletableFuture<Response<ProductVersionDetailsDto>> getBuildDetails(
      @Path("buildId") Long buildId);

  /**
   * Returns a list of builds for specified criteria
   *
   * @param productId (required)
   * @param page Page number you want to retrieve (0..N) (optional)
   * @param size Number of records per page (optional)
   * @param sort Sorting criteria in the format: property(,asc|desc). See method description for
   *     default sorting and supported fields (optional)
   * @return PageProductVersionDetailsDto
   */
  @GET("v2/builds")
  CompletableFuture<Response<PageProductVersionDetailsDto>> getBuilds(
      @Query("productId") Long productId,
      @Query("page") Long page,
      @Query("size") Long size,
      @Query("sort") String sort);

  /**
   * Updates existing build
   *
   * @param body (required)
   * @param buildId (required)
   * @return ProductVersionDto
   */
  @Headers({"Content-Type:application/json"})
  @PUT("v2/builds/{buildId}")
  CompletableFuture<Response<ProductVersionDto>> updateBuild(
      @Body ProductVersionUpdateDto body, @Path("buildId") Long buildId);

  /**
   * Uploads a new application-type attachment for a build by id
   *
   * @param buildId (required)
   * @param file (optional)
   * @return AttachmentWithHashesDto
   */
  @Multipart
  @POST("v2/builds/{buildId}/attachments")
  CompletableFuture<Response<AttachmentWithHashesDto>> createAttachment(
      @Path("buildId") Long buildId, @Part("file\"; filename=\"file") RequestBody file);

  /**
   * Returns information about a build attachment specified by id
   *
   * @param buildId (required)
   * @param attachmentId (required)
   * @return AttachmentWithHashesDto
   */
  @GET("v2/builds/{buildId}/attachments/{attachmentId}")
  CompletableFuture<Response<AttachmentWithHashesDto>> getAttachment(
      @Path("buildId") Long buildId, @Path("attachmentId") Long attachmentId);

  /**
   * Returns application-type attachments for a given build ID
   *
   * @param buildId (required)
   * @return List&lt;AttachmentWithHashesDto&gt;
   */
  @GET("v2/builds/{buildId}/attachments")
  CompletableFuture<Response<List<AttachmentWithHashesDto>>> getBuildAttachments(
      @Path("buildId") Long buildId);

  /**
   * Deletes the attachment with specified id
   *
   * @param buildId (required)
   * @param attachmentId (required)
   * @return Void
   */
  @DELETE("v2/builds/{buildId}/attachments/{attachmentId}")
  CompletableFuture<Response<Void>> removeAttachment(
      @Path("buildId") Long buildId, @Path("attachmentId") Long attachmentId);
}
