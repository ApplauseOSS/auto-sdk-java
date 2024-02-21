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
package com.applause.auto.helpers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.applause.auto.framework.ContextBuilder;
import com.applause.auto.framework.ContextManager;
import com.applause.auto.framework.SdkHelper;
import com.applause.auto.framework.context.annotations.Driverless;
import com.applause.auto.templates.TemplateManager;
import com.applause.auto.templates.TemplateManager.TemplateGenerationException;
import com.applause.auto.util.autoapi.AutoApi;
import com.google.common.base.Suppliers;
import com.google.common.io.Files;
import com.google.gson.Gson;
import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import retrofit2.Response;

@Driverless
@SuppressWarnings("unchecked")
public class ScreenshotHelperTest {
  private static final Logger logger = LogManager.getLogger(ScreenshotHelperTest.class);
  public static final String SCREENSHOT_NAME_WITHOUT_PNG = "unit-test-screenshot-name";
  public static final String SCREENSHOT_NAME_WITH_PNG = SCREENSHOT_NAME_WITHOUT_PNG + ".png";
  static final Supplier<Gson> getJsonCapsDeserializer = Suppliers.memoize(Gson::new);
  private File testScreenshot;
  private final String sessionId = "test";

  @BeforeClass(alwaysRun = true)
  public void setup() throws TemplateGenerationException {
    testScreenshot = new File("src/test/resources/objection.png");
    System.setProperty("headlessChrome", "true");
    System.setProperty("performanceLogging", "true");
    ContextManager.INSTANCE.setOutputPathTemplate(
        Paths.get(System.getProperty("java.io.tmpdir"), "howdy", this.sessionId).toString());
  }

  @BeforeMethod(alwaysRun = true)
  @SuppressWarnings("PMD.UseVarargs")
  public void setupContext(final ITestResult testResult, final Object[] params) {
    ContextBuilder.setup().getAsMainContext();
  }

  @AfterMethod(alwaysRun = true)
  public void resetContext() {
    ContextManager.INSTANCE.resetContext();
  }

  @Test
  public void testGetScreenshotDirectoryWithProjectRootWithoutColon()
      throws TemplateManager.TemplateProcessException {
    Assert.assertEquals(
        ScreenshotHelper.getScreenshotFullyQualifiedPath(SCREENSHOT_NAME_WITH_PNG),
        Paths.get(
                System.getProperty("java.io.tmpdir"),
                "howdy",
                this.sessionId,
                SCREENSHOT_NAME_WITH_PNG)
            .toString(),
        "Should have derived the right path");
  }

  @Test
  public void testGetScreenshotDirectoryWithProjectRootWithColon()
      throws TemplateManager.TemplateProcessException {
    final String fullPath =
        ScreenshotHelper.getScreenshotFullyQualifiedPath(SCREENSHOT_NAME_WITH_PNG);
    Assert.assertEquals(
        fullPath,
        Paths.get(
                System.getProperty("java.io.tmpdir"),
                "howdy",
                this.sessionId,
                SCREENSHOT_NAME_WITH_PNG)
            .toString(),
        "Should have derived the right path");
  }

  @Test
  public void testGetScreenshotDirectoryWithoutProjectRootWithoutColon()
      throws TemplateManager.TemplateProcessException {
    Assert.assertTrue(
        ScreenshotHelper.getScreenshotFullyQualifiedPath(SCREENSHOT_NAME_WITH_PNG)
            .startsWith(System.getProperty("java.io.tmpdir")),
        "Should have derived the right path");
    Assert.assertTrue(
        ScreenshotHelper.getScreenshotFullyQualifiedPath(SCREENSHOT_NAME_WITH_PNG)
            .endsWith(
                File.separator
                    + "howdy"
                    + File.separator
                    + sessionId
                    + File.separator
                    + SCREENSHOT_NAME_WITH_PNG),
        "Should have derived the right path");
  }

  @Test
  public void testGetScreenshotDirectoryWithoutProjectRootWithColon()
      throws TemplateManager.TemplateProcessException {
    Assert.assertTrue(
        ScreenshotHelper.getScreenshotFullyQualifiedPath(SCREENSHOT_NAME_WITH_PNG)
            .startsWith(System.getProperty("java.io.tmpdir")),
        "Should have derived the right path");
    Assert.assertTrue(
        ScreenshotHelper.getScreenshotFullyQualifiedPath(SCREENSHOT_NAME_WITH_PNG)
            .endsWith(
                File.separator
                    + "howdy"
                    + File.separator
                    + sessionId
                    + File.separator
                    + SCREENSHOT_NAME_WITH_PNG),
        "Should have derived the right path");
  }

  @Test
  public void testUploadSuccessful() throws Exception {
    final AutoApi mockAutoApi = mock(AutoApi.class);
    final long testResultId = 11121987L;
    // set up arg captors
    final ArgumentCaptor<Long> testResultArg = ArgumentCaptor.forClass(Long.class);
    final ArgumentCaptor<MultipartBody.Part> bodyArg =
        ArgumentCaptor.forClass(MultipartBody.Part.class);
    final ArgumentCaptor<RequestBody> nameArg = ArgumentCaptor.forClass(RequestBody.class);
    final ArgumentCaptor<String> assetTypeArg = ArgumentCaptor.forClass(String.class);

    // set up response handling
    final CompletableFuture<Response<Void>> mockFuture = mock(CompletableFuture.class);
    final Response<Void> mockResponse = mock(Response.class);
    when(mockAutoApi.uploadTestResultAsset(any(), any(), any(), any())).thenReturn(mockFuture);
    when(mockFuture.get()).thenReturn(mockResponse);
    when(mockResponse.isSuccessful()).thenReturn(true);
    Reporter.getCurrentTestResult().setAttribute("testResultId", testResultId);

    try (MockedStatic<SdkHelper> utilities = Mockito.mockStatic(SdkHelper.class)) {
      // mock out this static method that returns null
      utilities.when(SdkHelper::getTestResultId).thenReturn(11121987L);
      // actually call the method and validate state
      ScreenshotHelper.upload(SCREENSHOT_NAME_WITH_PNG, true, testScreenshot, mockAutoApi);
    }

    // validate args passed to auto api
    verify(mockAutoApi)
        .uploadTestResultAsset(
            testResultArg.capture(), bodyArg.capture(), nameArg.capture(), assetTypeArg.capture());
    Assert.assertEquals(
        testResultArg.getValue().longValue(), testResultId, "test result arg should be correct");
    final MultipartBody.Part request = bodyArg.getValue();
    // assert headers
    final String contentHeader =
        Objects.requireNonNull(request.headers()).get("Content-Disposition");
    Assert.assertNotNull(contentHeader, "Should have Content-Disposition header sent to auto api");
    Assert.assertTrue(contentHeader.contains("name=\"file\""), "Should have name correct");
    Assert.assertTrue(
        contentHeader.contains("filename=\"" + SCREENSHOT_NAME_WITH_PNG + "\""),
        "Should have name correct");
    Assert.assertTrue(contentHeader.contains("form-data"), "Should have name correct");
    // assert actual content
    Assert.assertEquals(
        Objects.requireNonNull(request.body().contentType()).type(),
        "application",
        "should have right media type");
    Assert.assertEquals(
        Objects.requireNonNull(request.body().contentType()).subtype(),
        "octet-stream",
        "should have right media type");
    Assert.assertEquals(
        testScreenshot.length(),
        request.body().contentLength(),
        "should have the right length for content");
    final Buffer buffer = new Buffer();
    request.body().writeTo(buffer);
    Assert.assertEquals(
        Files.toByteArray(testScreenshot), buffer.readByteArray(), "content should match");
  }

  @Test
  public void testUploadNotSuccessful() throws Exception {
    final File testScreenshot = new File("src/test/resources/objection.png");
    final AutoApi mockAutoApi = mock(AutoApi.class);
    // set up response handling
    final CompletableFuture<Response<Void>> mockFuture = mock(CompletableFuture.class);
    final Response<Void> mockResponse = mock(Response.class);
    final ResponseBody responseBody = mock(ResponseBody.class);
    when(mockAutoApi.uploadTestResultAsset(any(), any(), any(), any())).thenReturn(mockFuture);
    when(mockFuture.get()).thenReturn(mockResponse);
    when(mockResponse.isSuccessful()).thenReturn(false);
    when(mockResponse.errorBody()).thenReturn(responseBody);
    when(responseBody.toString()).thenReturn("expected");

    try {
      // actually call the method and ensure it fails silently
      ScreenshotHelper.upload(SCREENSHOT_NAME_WITH_PNG, true, testScreenshot, mockAutoApi);
    } catch (Exception e) {
      logger.error("!!!", e);
    }
  }

  @Test
  public void testUploadThrows() throws Exception {
    final File testScreenshot = new File("src/test/resources/objection.png");
    final AutoApi mockAutoApi = mock(AutoApi.class);
    // set up response handling
    for (Exception exception :
        new Exception[] {
          new InterruptedException("interrupted"), new ExecutionException(new Exception())
        }) {
      final CompletableFuture<Response<Void>> mockFuture = mock(CompletableFuture.class);
      when(mockAutoApi.uploadTestResultAsset(any(), any(), any(), any())).thenReturn(mockFuture);
      when(mockFuture.get()).thenThrow(exception);

      try (MockedStatic<SdkHelper> utilities = Mockito.mockStatic(SdkHelper.class)) {
        // mock out this static method that returns null
        utilities.when(SdkHelper::getTestResultId).thenReturn(11121987L);
        // actually call the method and ensure it fails silently
        ScreenshotHelper.upload(SCREENSHOT_NAME_WITH_PNG, true, testScreenshot, mockAutoApi);
      }
    }
  }
}
