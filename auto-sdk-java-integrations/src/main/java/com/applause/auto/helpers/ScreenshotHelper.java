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

import com.applause.auto.config.ApplauseEnvironmentConfigurationManager;
import com.applause.auto.context.FrameworkContext;
import com.applause.auto.context.IFrameworkExtension;
import com.applause.auto.data.enums.AssetType;
import com.applause.auto.framework.ContextManager;
import com.applause.auto.framework.SdkHelper;
import com.applause.auto.templates.TemplateManager;
import com.applause.auto.util.autoapi.AutoApi;
import com.applause.auto.util.autoapi.AutoApiClient;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/**
 * Use to easily take screenshots of the currently executing test. Applause leverages this class
 * upon any test failure, but it can be called for non-failure purposes as well.
 *
 * <p>Requested screenshots will be saved in the PNG format.
 */
@AllArgsConstructor
public final class ScreenshotHelper implements IFrameworkExtension {

  private static final String DEFAULT_NAME = "screenshot.png";
  private static final Proxy httpProxy = ApplauseConfigHelper.getHttpProxy();
  private static final AutoApi autoApi =
      AutoApiClient.getClient(
          ApplauseEnvironmentConfigurationManager.INSTANCE.get().autoApiUrl(),
          ApplauseEnvironmentConfigurationManager.INSTANCE.get().apiKey(),
          httpProxy);
  private static final Logger logger = LogManager.getLogger(ScreenshotHelper.class);
  private FrameworkContext context;

  /**
   * Takes and stores screenshot to configured screenshot location
   *
   * @param screenshotName the name of the screenshot to be captured (if blank, the default will be
   *     used)
   */
  public void takeScreenshot(final @NonNull String screenshotName) {
    takeScreenshot(screenshotName, false);
  }

  /**
   * Takes and stores screenshot to configured screenshot location
   *
   * @param screenshotName the name of the screenshot to be captured (if blank, the default will be
   *     used)
   * @param isFailureScreenshot enables extra handling for failure screenshots
   */
  public void takeScreenshot(
      final @Nullable String screenshotName, final boolean isFailureScreenshot) {
    logger.debug("Taking a screenshot.");
    final String nonEmptyName =
        screenshotName == null || screenshotName.isEmpty() ? DEFAULT_NAME : screenshotName;
    final String name =
        nonEmptyName.toLowerCase(Locale.ENGLISH).endsWith(".png")
            ? nonEmptyName
            : nonEmptyName + ".png";

    // take the screenshot
    final File screenshot =
        context
            .getDriver()
            .map(driver -> ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE))
            .orElseThrow(() -> new RuntimeException("Unable to capture screenshot"));

    // save to various locations based on run type
    try {
      saveLocally(name, screenshot);
    } catch (TemplateManager.TemplateProcessException e) {
      logger.error(
          "Could not parse local results directory template, not saving screenshot '"
              + name
              + "' locally",
          e);
    }
    if (ApplauseEnvironmentConfigurationManager.INSTANCE.get().reportingEnabled()) {
      upload(name, isFailureScreenshot, screenshot, autoApi);
    }
  }

  /**
   * Write the screenshot to disk.
   *
   * @param screenshotName name of screenshot to save
   * @param screenshot the screenshot file handle
   */
  void saveLocally(final @NonNull String screenshotName, final @NonNull File screenshot)
      throws TemplateManager.TemplateProcessException {
    // setup save location
    final String saveLocation = getScreenshotFullyQualifiedPath(screenshotName);

    // setup save location as File
    final File saveLocationFile = new File(saveLocation);
    try {
      // save the screenshot to save location
      FileUtils.copyFile(screenshot, saveLocationFile);
    } catch (Exception ex) {
      logger.warn(
          String.format(
              "Exception was thrown while storing the screenshot locally (Message: `%s`). Moving on...",
              ex.getMessage()));
    }
  }

  /**
   * Uploads the screenshot to Applause's Automation APIs storage. Fails silently if the screenshot
   * failed to upload.
   *
   * @param screenshotName name of asset (screenshot) to be saved in storage
   * @param isFailureScreenshot whether this screenshot denotes a failure or not
   * @param screenshot the screenshot itself
   * @param autoApiClient client to reach out to auto api
   */
  static void upload(
      final @NonNull String screenshotName,
      final boolean isFailureScreenshot,
      final @NonNull File screenshot,
      final @NonNull AutoApi autoApiClient) {
    final var testResultId = SdkHelper.getTestResultId();
    try {
      if (Objects.isNull(testResultId)) {
        logger.debug("Could not upload screenshot: no test result id detected");
        return;
      }
      // create multipart
      final RequestBody requestAsset =
          RequestBody.create(
              IOUtils.toByteArray(screenshot.toURI()), MediaType.parse("application/octet-stream"));
      final MultipartBody.Part body =
          MultipartBody.Part.createFormData("file", screenshotName, requestAsset);

      final RequestBody nameParam =
          MultipartBody.create(screenshotName, MediaType.parse("text/plain"));
      final var response =
          autoApiClient
              .uploadTestResultAsset(
                  testResultId,
                  body,
                  nameParam,
                  (isFailureScreenshot ? AssetType.FAILURE_SCREENSHOT : AssetType.SCREENSHOT)
                      .toString())
              .get(); // interrupted, execution
      if (response.isSuccessful()) {
        logger.debug("Screenshot uploaded Successfully");
      } else {
        try (var errBody = response.errorBody()) {
          if (errBody != null) {
            logger.error("Screenshot upload failed: {}", errBody);
          } else {
            logger.error("Screenshot upload failed: unknown error occurred");
          }
        }
      }
    } catch (InterruptedException | ExecutionException | IOException e) {
      logger.warn("screenshot upload failed, continuing", e);
    }
  }

  /**
   * Generate the fully qualified path where the screenshot will ultimately be written to disk
   * locally.
   *
   * @param screenshotName name of screenshot
   * @return fully qualified path
   */
  static String getScreenshotFullyQualifiedPath(final @NonNull String screenshotName)
      throws TemplateManager.TemplateProcessException {
    final var currentContext =
        ContextManager.INSTANCE
            .getCurrentContext()
            .orElseThrow(
                () -> new RuntimeException("Unable to get screenshot path - no context set up"));
    final Path finalPath = currentContext.getOutputPath().resolve(screenshotName);
    logger.debug("Screenshot will be stored at [{}]", finalPath);
    return finalPath.toString();
  }
}
