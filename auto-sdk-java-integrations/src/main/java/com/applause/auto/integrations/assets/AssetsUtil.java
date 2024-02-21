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
package com.applause.auto.integrations.assets;

import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.config.EnvironmentConfigurationManager;
import com.applause.auto.context.FrameworkContext;
import com.applause.auto.data.enums.AssetType;
import com.applause.auto.framework.ContextManager;
import com.applause.auto.helpers.ApplauseConfigHelper;
import com.applause.auto.helpers.ScreenshotHelper;
import com.applause.auto.logging.LogOutputSingleton;
import com.applause.auto.logging.ResultPropertyMap;
import com.applause.auto.templates.TemplateManager;
import com.applause.auto.util.autoapi.AutoApi;
import com.applause.auto.util.autoapi.AutoApiClient;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.Proxy;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import javax.annotation.Nullable;
import lombok.Cleanup;
import lombok.NonNull;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import retrofit2.Response;

/** Utility class for fetching and storing assets from a driver */
public final class AssetsUtil {
  private static final Logger logger = LogManager.getLogger(AssetsUtil.class);
  private static final RetryPolicy<Object> assetRetryPolicy =
      RetryPolicy.builder()
          .handle(Exception.class)
          .withDelay(Duration.ofSeconds(30L))
          .abortOn(InvalidObjectException.class)
          .onRetry(res -> logger.info("Test Run assets are not ready yet. Retrying in 30 seconds."))
          .withMaxRetries(
              ApplauseEnvironmentConfigurationManager.INSTANCE.get().assetFetchRetries().intValue())
          .build();
  private static final String autoApiUrl =
      ApplauseEnvironmentConfigurationManager.INSTANCE.get().autoApiUrl();
  private static final String apiKey =
      ApplauseEnvironmentConfigurationManager.INSTANCE.get().apiKey();
  private static final Proxy httpProxy = ApplauseConfigHelper.getHttpProxy();
  private static final AutoApi autoApi = AutoApiClient.getClient(autoApiUrl, apiKey, httpProxy);

  private AssetsUtil() {}

  /**
   * waits until test asset upload is complete then retrieves the zipped asset
   *
   * @param testRunId the current run id
   */
  public static void waitForTestRunAssets(final long testRunId) {
    String urlString = waitForAssetArchive(testRunId);
    if (urlString == null) {
      return;
    }
    Path preparedPath = prepareResultsDownload(testRunId, urlString);
    retrieveZippedAssets(urlString, preparedPath);
  }

  /**
   * Captures assets for the current context
   *
   * @param isFailure If the failure assets should be captured as well
   */
  public static void captureAssetsForDriver(final boolean isFailure) {
    try {
      ContextManager.INSTANCE
          .getCurrentContext()
          .ifPresent(
              context -> {
                uploadAndSaveLogs(context);
                // Only attempt page source and screenshot if we have a driver
                if (context.getDriver().isPresent()) {
                  uploadAndSavePageSource(context);
                  if (isFailure) {
                    new ScreenshotHelper(ContextManager.INSTANCE.getCurrentContext().get())
                        .takeScreenshot(
                            context.getConnector().getTestCaseName() + "_fail.png", true);
                  }
                }
              });
    } catch (Exception e) {
      logger.error("Caught error trying to capture test assets.", e);
    }
  }

  /**
   * Wait 2 minutes for assets to become ready, then begin attempting asset-archival. When
   * completed, will still need to download assets from Url.
   *
   * @param testRunId test run to fetch assets
   * @return url to download assets
   */
  private static String waitForAssetArchive(final long testRunId) {
    try {
      logger.info("Waiting for 2 minutes while provider assets become available.");
      Thread.sleep(120L * 1000L);
    } catch (InterruptedException e) {
      logger.error("Wait interrupted!", e);
      logger.info(
          String.format(
              "Manually GET results with auth headers: %sapi/v1.0/test-run/%d/archive-results",
              autoApiUrl, testRunId));
      return null;
    }
    return Failsafe.with(assetRetryPolicy)
        .onFailure(
            failure ->
                logger.error("Failed to download assets, OUT OF RETRIES", failure.getException()))
        .get(
            () -> {
              var innerResp = autoApi.archiveAssets(testRunId).join();
              return switch (innerResp.code()) {
                case 200 ->
                    // 200 - Success
                    innerResp.body();
                case 404 ->
                    // 404 - Test run not found
                    throw new InvalidObjectException(
                        "Test Run not found - cannot retrieve results.");
                case 408 ->
                    // 408 - Test Run In an Invalid State
                    throw new RuntimeException("Test Run assets are not ready");
                default ->
                    throw new RuntimeException(
                        "Unhandled status code "
                            + innerResp.code()
                            + " during results retrieval for Test Run ID."
                            + testRunId
                            + ".");
              };
            });
  }

  /**
   * Wait for assets and download them to results
   *
   * @param zipUrl url to retrieve zip
   * @param filePath results path to save zip
   */
  private static void retrieveZippedAssets(final String zipUrl, final Path filePath) {
    try {
      logger.info("Waiting in 30 second intervals for zipped assets to become available.");
      Thread.sleep(30L * 1000L);
    } catch (InterruptedException e) {
      logger.error("Wait interrupted!", e);
      logger.info("Manually GET results with: " + zipUrl);
    }

    final var zipFile = new File(filePath + "/results.zip");
    final var zipRetryPolicy30Sec =
        RetryPolicy.builder()
            .handle(Exception.class)
            .withDelay(Duration.ofSeconds(30L))
            .withMaxRetries(5)
            .build();

    Failsafe.with(zipRetryPolicy30Sec)
        .onFailure(
            failure ->
                logger.error(
                    "Failed to save zip from url [" + zipUrl + "], OUT OF RETRIES", failure))
        .get(
            () -> {
              @Cleanup
              ReadableByteChannel readableByteChannel =
                  Channels.newChannel(new URI(zipUrl).toURL().openStream());
              @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
              return fileOutputStream
                  .getChannel()
                  .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            });
  }

  /**
   * Set up the directory structure
   *
   * @param testRunId the testRun id
   * @param zipAssetUrl the url for zipAssets
   * @return Path for results to be downloaded
   */
  private static Path prepareResultsDownload(final long testRunId, final String zipAssetUrl) {
    Path basePath = null;
    File zipArchiveFile = null;
    try {
      final var baseDir =
          TemplateManager.process(
              ContextManager.INSTANCE.getOutputPathTemplate(), ResultPropertyMap.getProperties());

      // replace project root placeholder with actual project root
      if (baseDir.startsWith("<project_root>")) {
        basePath =
            Paths.get(System.getProperty("user.dir"), baseDir.replaceFirst("<project_root>", ""));
      } else {
        basePath = Paths.get(baseDir);
      }
      // add current test run id to base path
      basePath = Paths.get(basePath + File.separator + testRunId);
      final var directory = new File(basePath.toString());
      final var madeDirectories = directory.mkdirs();
      if (madeDirectories) {
        logger.debug("Directory {} didn't exist, created it.", directory.getAbsolutePath());
      }

      zipArchiveFile = new File(basePath + "/downloadLink.txt");
      final var fileCreated = zipArchiveFile.createNewFile();
      if (fileCreated) {
        logger.debug(
            "Results ZIP file {} didn't exist, created it.", zipArchiveFile.getAbsolutePath());
      }
      FileUtils.writeStringToFile(zipArchiveFile, zipAssetUrl + "\n", StandardCharsets.UTF_8);

    } catch (IOException e) {
      logger.error(String.format("Unable to create or write zip file: %s", zipArchiveFile), e);
    } catch (Exception e) {
      logger.error(String.format("Unable to create file or folder in: %s", basePath), e);
    }
    return basePath;
  }

  /**
   * uploads an asset to auto-api and connects it to the test result
   *
   * @param context the applause context
   * @param name The file name
   * @param asset asset content, as a byte array
   * @param assetType the type of the asset to save
   */
  public static void uploadBinaryAssetToTestResult(
      final FrameworkContext context,
      final String name,
      final byte[] asset,
      final AssetType assetType) {
    // Passing in a 'null' driver indicates that we should upload the asset to the test result
    uploadBinaryAssetToProviderSession(context, name, null, asset, assetType);
  }

  /**
   * uploads an asset to auto-api
   *
   * @param context the applause context
   * @param name The file name
   * @param driver The selenium driver
   * @param asset asset content, as a byte array
   * @param assetType the type of the asset to save
   */
  public static void uploadBinaryAssetToProviderSession(
      final @NonNull FrameworkContext context,
      final @NonNull String name,
      final @Nullable WebDriver driver,
      final byte[] asset,
      final @NonNull AssetType assetType) {
    if (!ApplauseEnvironmentConfigurationManager.INSTANCE.get().reportingEnabled()) {
      logger.debug("Could not upload asset {}. Reporting is disabled", name);
      return;
    }
    // create multipart request
    RequestBody requestAsset =
        RequestBody.create(asset, MediaType.parse("application/zip, application/octet-stream"));
    MultipartBody.Part body = MultipartBody.Part.createFormData("file", name, requestAsset);

    RequestBody nameParam = MultipartBody.create(name, MediaType.parse("text/plain"));
    String sessionId = null;
    // Passing a session id here signals to auto-api that we want to link this asset to a provider
    // session. If we are using local drivers or a selenium grid, then we do not have a provider
    // session to link to
    if (driver instanceof RemoteWebDriver
        && !EnvironmentConfigurationManager.INSTANCE.get().useLocalDrivers()
        && !EnvironmentConfigurationManager.INSTANCE.get().useSeleniumGrid()) {
      sessionId = ((RemoteWebDriver) driver).getSessionId().toString();
    }
    try {
      Response<Void> response =
          autoApi
              .uploadTestResultAsset(
                  context.getConnector().getResultId(),
                  sessionId,
                  body,
                  nameParam,
                  assetType.toString())
              .join();
      if (!response.isSuccessful()) {
        try (var errBody = response.errorBody()) {
          if (errBody != null) {
            logger.error("error calling post test bundle endpoint: {}", errBody.string());
          } else {
            logger.error("error calling post test bundle endpoint: unknown error");
          }
        }
      }
    } catch (IOException e) {
      logger.warn("Uploading {} to asset endpoint failed: {}", name, e);
    }
  }

  /**
   * Gets the HTML source of the current page and uploads it to the test result as a file called
   * page_source.html.
   *
   * @param context active driver context
   */
  public static void uploadAndSavePageSource(final FrameworkContext context) {
    final var driver = context.getDriver().orElse(null);
    if (driver == null) {
      logger.debug("Cannot capture page source - no driver tied to context");
      return;
    }
    try {
      String pageSource = driver.getPageSource();
      if (pageSource == null) {
        logger.warn(
            "Could not capture Page Source from Selenium Driver: no page source returned from driver");
        return;
      }
      final String fileName = context.getConnector().getTestCaseName() + ".html";

      saveAssetFile(
          getPathToResultFile(context.getOutputPath(), fileName, "Page source"), pageSource);

      if (driver instanceof RemoteWebDriver
          && !EnvironmentConfigurationManager.INSTANCE.get().useLocalDrivers()) {
        uploadBinaryAssetToProviderSession(
            context,
            "page_source.html",
            driver,
            pageSource.getBytes(StandardCharsets.UTF_8),
            AssetType.PAGE_SOURCE);
      }
    } catch (WebDriverException e) {
      logger.warn("Could not capture Page Source from Selenium Driver", e);
    } catch (TemplateManager.TemplateProcessException e) {
      logger.error(
          "Could not parse local results directory template, not saving page source to results directory",
          e);
    }
  }

  /**
   * Uploads test console logs to auto-api
   *
   * @param context the context for the executed thread
   */
  private static void uploadAndSaveLogs(final FrameworkContext context) {
    final var logsBlob = String.join("/n", LogOutputSingleton.flush());
    final String fileName = context.getConnector().getTestCaseName() + "_console.log";
    try {
      saveAssetFile(
          getPathToResultFile(context.getOutputPath(), fileName, "Console logs"), logsBlob);
    } catch (TemplateManager.TemplateProcessException e) {
      logger.error(
          "Could not parse local results directory template, not saving console logs to results directory",
          e);
    }
    uploadBinaryAssetToTestResult(
        context,
        "test_console.log",
        logsBlob.getBytes(StandardCharsets.UTF_8),
        AssetType.CONSOLE_LOG);
  }

  private static void saveAssetFile(final String logPath, final String asset) {
    // setup save location as File
    File saveLocationFile = new File(logPath);
    try {
      // save the log file to save location
      FileUtils.writeStringToFile(saveLocationFile, asset, StandardCharsets.UTF_8, true);
    } catch (IOException ex) {
      logger.warn(
          String.format(
              "Exception was thrown while writing the log (Message: %s). Moving on...",
              ex.getMessage()));
    }
  }

  /**
   * For a given result filename, generates and returns the absolute path based on the results
   * directory property.
   *
   * @param basePath the base path the file should be saved to
   * @param resultFile The filename for which an absolute path will be generated.
   * @param description Optionally, a description can be specified to make the logs more friendly.
   *     Can be null.
   * @return the absolute path to the result file.
   */
  private static String getPathToResultFile(
      final Path basePath, final String resultFile, final String description) {

    String finalPath = basePath.resolve(resultFile).toString();

    if (description != null) {
      logger.debug(String.format("%s will be stored at [%s]", description, finalPath));
    } else {
      logger.debug(String.format("[%s] will be stored at [%s]", resultFile, finalPath));
    }

    return finalPath;
  }
}
