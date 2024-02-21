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
package com.applause.auto.util.autoapi;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.*;

/** Retrofit interface to define all calls to the Applause automation API */
public interface AutoApi {

  /**
   * Creates a test run
   *
   * @param dto transfer object containing the SDK version, product ID and other required parameters
   * @return Information about the newly created test run
   */
  @POST("/api/v1.0/test-run/create")
  CompletableFuture<Response<TestRunConfigurationDto>> createTestRun(
      @Body TestRunConfigurationParamDto dto);

  /**
   * aborts a test run
   *
   * @param testRunIdToCancel id to cancel
   * @param reason reason string
   * @return void
   */
  @PUT("/api/v1.0/test-run/{id}/sdkcancel")
  CompletableFuture<Response<Void>> cancelTestRun(
      @Path("id") Long testRunIdToCancel, @Query("reason") String reason);

  /**
   * Endpoint called by test listener to save test result information
   *
   * @param testResult information about how test ran
   * @return Void
   */
  @POST("/api/v1.0/test-result")
  CompletableFuture<Response<Void>> submitTestResult(@Body TestResultParamDto testResult);

  /**
   * Endpoint called to create a new test result
   *
   * @param createInfo info to create a new result
   * @return the new testResult linked to test run, marked as IN_PROGRESS. This includes both our
   *     testResultId and Sauce's ProviderSessionId for use with their API
   */
  @POST("/api/v1.0/test-result/create-result")
  CompletableFuture<Response<CreateTestResultDto>> createTestResult(
      @Body CreateTestResultParamDto createInfo);

  /**
   * image upload endpoint for test result when test in progress
   *
   * @param resultIdForUpdate test result ID to tie image to
   * @param file binary image file
   * @param assetName name of the file on S3
   * @param assetType type of the asset
   * @return void
   */
  @Multipart
  @POST("/api/v1.0/test-result/{id}/upload")
  CompletableFuture<Response<Void>> uploadTestResultAsset(
      @Path("id") Long resultIdForUpdate,
      @Part MultipartBody.Part file,
      @Part("assetName") RequestBody assetName,
      @Part("assetType") String assetType);

  /**
   * image upload endpoint for test result when test in progress
   *
   * @param resultIdForUpdate test result ID to tie image to
   * @param providerSessionId the id of the provider session
   * @param file binary image file
   * @param assetName name of the file on S3
   * @param assetType type of the asset
   * @return void
   */
  @Multipart
  @POST("/api/v1.0/test-result/{id}/upload")
  CompletableFuture<Response<Void>> uploadTestResultAsset(
      @Path("id") Long resultIdForUpdate,
      @Part("sessionId") String providerSessionId,
      @Part MultipartBody.Part file,
      @Part("assetName") RequestBody assetName,
      @Part("assetType") String assetType);

  /**
   * Retrieve S3 keys for all assets associated with the given test result id.
   *
   * @param testResultId result id to retrieve assets for
   * @return list of s3 keys, which can be used for individual retrieval.
   */
  @GET("/api/v1.0/test-result/{id}/assets")
  CompletableFuture<Response<List<String>>> getAssetKeysForResult(@Path("id") Long testResultId);

  /**
   * Retrieve an asset.
   *
   * @param testResultId test result
   * @param assetKey s3 asset key
   * @return the asset
   */
  @Streaming
  @Headers({"Accept: application/octet-stream"})
  @GET("/api/v1.0/test-result/{id}/download-asset")
  CompletableFuture<Response<ResponseBody>> getAsset(
      @Path("id") Long testResultId, @Query("assetKey") String assetKey);

  /**
   * returns 400 bad request with a rejection reason if this version of the SDK is too old
   *
   * @param sdkVersion current SDK version
   * @return Void
   */
  @POST("/api/check-deprecated")
  CompletableFuture<Response<Void>> checkDeprecated(@Query("sdkVersion") String sdkVersion);

  /**
   * Endpoint to update a Test Run's Status
   *
   * @param runId the Test Run ID to update
   * @param testRunStatus the new status this test run should have
   * @return void
   */
  @DELETE("/api/v1.0/test-run/{runId}")
  CompletableFuture<Response<TestRunAssetLinksDto>> updateTestRunStatus(
      @Path("runId") Long runId, @Query("endingStatus") TestRunStatus testRunStatus);

  /**
   * Archive the test run results and related assets into a zip
   *
   * <p>If a 400 is returned, the Test Run has not completed yet. If a 404 is returned, no
   * associated Test Run was provided. Otherwise, upon a 200 being returned, a ZIP file is received.
   *
   * @param testRunId the Test Run to check for completion and gather results
   * @return url to download the assets
   */
  @GET("/api/v1.0/test-run/{id}/archive-results")
  CompletableFuture<Response<String>> archiveAssets(@Path("id") long testRunId);

  /**
   * updates heartbeat signal to keep auto-api running the Test Run
   *
   * @param heartbeat the Test Run Heartbeat info
   * @return Void
   */
  @POST("/api/v2.0/sdk-heartbeat")
  CompletableFuture<Response<Void>> sdkHeartbeat(@Body SdkHeartbeatDto heartbeat);

  /**
   * Generate / Retrieve an Applause-controlled email account, given a prefix. The same prefix
   * should route to the same email account.
   *
   * @param prefix the first part of the email address. ex: "test123"
   * @return model to hold the generated email address. ex: test123@test.applause.com
   */
  @GET("/api/v1.0/email/get-address")
  CompletableFuture<Response<EmailAddressResponse>> getEmailAddress(@Query("prefix") String prefix);

  /**
   * Queries the Email Provider with the given email address, uploads the result to the Applause
   * asset service, and returns a URL where the file can be accessed.
   *
   * @param emailRetrievalRequest model to hold email account anticipating a new email
   * @return model to hold a URL from the Applause asset service linking to the just received email
   *     received by this address;
   */
  @POST("/api/v1.0/email/download-email-as-asset")
  CompletableFuture<Response<EmailRetrievalResponse>> awaitEmail(
      @Body EmailRetrievalRequest emailRetrievalRequest);

  /**
   * Given an Applause-generated email address, clear its inbox.
   *
   * @param emailRetrievalRequest model to hold email address to be cleared
   * @return void
   */
  @POST("/api/v1.0/email/clear-inbox")
  CompletableFuture<Response<Void>> clearInbox(@Body EmailClearInboxRequest emailRetrievalRequest);

  /**
   * Perform some checks on testrail configuration
   *
   * @return List of messages for failed validation
   */
  @GET("/api/v1.0/testrails/prevalidate")
  CompletableFuture<Response<List<String>>> prevalidateTestrail();

  /**
   * Fetches information about a provider from the Automation API for Application Push
   *
   * @param provider The provider to fetch
   * @return The Provider Information
   */
  @GET("/api/v1.0/mobilesupport/app-push-info/{provider}")
  CompletableFuture<Response<AutoApiImagePushDataDto>> getAppPushForProvider(
      @Path("provider") String provider);
}
