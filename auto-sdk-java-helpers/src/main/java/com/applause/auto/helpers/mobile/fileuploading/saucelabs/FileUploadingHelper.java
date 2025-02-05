/*
 *
 * Copyright © 2025 Applause App Quality, Inc.
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
package com.applause.auto.helpers.mobile.fileuploading.saucelabs;

import com.applause.auto.helpers.mobile.MobileUtils;
import com.applause.auto.helpers.util.ThreadHelper;
import com.google.common.collect.ImmutableMap;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.android.AndroidDriver;
import io.restassured.internal.util.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/** The type File Uploading Helper. */
public final class FileUploadingHelper {

  private static final Logger logger = LogManager.getLogger(FileUploadingHelper.class);

  public static final String DEVICE_IMAGES_LOCATION_ANDROID = "sdcard/Download/%s";

  private FileUploadingHelper() {
    // utility class
  }

  /**
   * Upload Images to the device.
   *
   * @param driver The AppiumDriver instance.
   * @param imagesPath the image file path.
   * @param timeOuts The timeout in milliseconds.
   * @param <T> The type of AppiumDriver, which must extend both AppiumDriver and InteractsWithApps.
   * @throws RuntimeException If an exception occurs during file upload.
   */
  public static <T extends AppiumDriver & InteractsWithApps> void uploadImages(
      @NonNull final T driver, @NonNull final String imagesPath, final long timeOuts) {
    String bundleIdentifier = MobileUtils.getBundleIdentifier(driver);
    MobileUtils.moveAppToBackground(driver);
    /*
     * we have static wait because there is no way to check if the app was moved to the Background.
     * Sometimes, especially in the cloud, I found that image uploading started too early, and they
     * were uploaded but the app didn't see them
     */
    ThreadHelper.sleep(5000);
    getFilesFromPaths(imagesPath)
        .forEach(
            image -> {
              try {
                uploadFile(driver, image);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
    ThreadHelper.sleep(timeOuts);
    MobileUtils.activateApp(driver, bundleIdentifier);
  }

  /**
   * Upload one file.
   *
   * @param driver the WebDriver instance to use.
   * @param fileName the file to upload.
   * @throws RuntimeException if an error occurs during file upload.
   */
  public static void uploadFile(@NonNull final WebDriver driver, @NonNull final File fileName) {
    try {
      logger.info("Uploading [{}] file", fileName);
      ((AndroidDriver) MobileUtils.getMobileDriver(driver))
          .pushFile(String.format(DEVICE_IMAGES_LOCATION_ANDROID, fileName.getName()), fileName);
    } catch (Exception e) {
      logger.error(e);
      throw new RuntimeException(e); // Re-throw the exception after logging it.
    }
  }

  /**
   * Get Files From Path
   *
   * @param pathToFolder the folder path
   * @return A Set of Files found in the given path. Returns an empty set if an IOException occurs.
   */
  private static Set<File> getFilesFromPaths(@NonNull final String pathToFolder) {
    try (Stream<Path> stringStream =
        Files.walk(Paths.get(pathToFolder)).filter(Files::isRegularFile)) {
      List<Path> pathList = stringStream.toList();
      return pathList.stream().map(path -> new File(path.toString())).collect(Collectors.toSet());
    } catch (IOException e) {
      logger.info(e);
    }
    return Collections.emptySet();
  }

  /**
   * Clears the Measurement images folder on the device.
   *
   * <p>This method clears the folder located at "sdcard/greendot/hijacking/strip". It uses an ADB
   * shell command to remove all files and subdirectories within the specified folder.
   *
   * @param driver The WebDriver instance used to interact with the device. Must not be null. =
   */
  @SuppressWarnings("PMD.DoNotHardCodeSDCard")
  public static void clearImagesFolder(@NonNull final WebDriver driver) {
    logger.info(
        "Clearing Mocked Measurement images folder on device [sdcard/greendot/hijacking/strip]");
    try {
      List<String> removePicsArgs = Arrays.asList("-rf", "/sdcard/greendot/hijacking/strip/*.*");
      Map<String, Object> removePicsCmd = ImmutableMap.of("command", "rm", "args", removePicsArgs);
      ((AndroidDriver) MobileUtils.getMobileDriver(driver))
          .executeScript("mobile: shell", removePicsCmd);
    } catch (Exception e) {
      logger.info(e.getMessage());
      logger.info(
          "\n\nTo run any shell commands, we need to set the 'relaxed security' flag. Please start your Appium server with [appium --relaxed-security]\n");
    }
  }

  /**
   * Camera image injection or camera mocking Inject any image and then use device Camera with that
   * image in front
   *
   * <p>Need to use this caps to Enable image-injection on RDC ->
   * desiredCapabilities.setCapability("sauceLabsImageInjectionEnabled", true);
   *
   * <p>Link - <a
   * href="https://docs.saucelabs.com/mobile-apps/features/camera-image-injection/">...</a>
   *
   * @param driver the webdriver
   * @param fileLocation file path
   */
  public static void injectImageToSauce(
      @NonNull final WebDriver driver, @NonNull final String fileLocation) {
    try {
      logger.info("Injecting [{}] file to Sauce device.", fileLocation);
      FileInputStream in = new FileInputStream(fileLocation);
      String qrCodeImage = Base64.getEncoder().encodeToString(IOUtils.toByteArray(in));

      // Provide the transformed image to the device
      ((JavascriptExecutor) driver).executeScript("sauce:inject-image=" + qrCodeImage);
    } catch (Exception e) {
      logger.error(e);
      throw new RuntimeException("Image injection error.", e);
    }
  }
}
