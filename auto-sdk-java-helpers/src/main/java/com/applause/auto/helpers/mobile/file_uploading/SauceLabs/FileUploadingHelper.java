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
package com.applause.auto.helpers.mobile.file_uploading.SauceLabs;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.TestException;

/** The type File Uploading Helper. */
public class FileUploadingHelper {
  private static final Logger logger = LogManager.getLogger(FileUploadingHelper.class);

  public static final String DEVICE_IMAGES_LOCATION_ANDROID = "sdcard/Download/%s";

  /**
   * Upload Images to the device
   *
   * @param imagesPath
   */
  public static <T extends AppiumDriver & InteractsWithApps> void uploadImages(
      T driver, String imagesPath, long timeOuts) {
    String bundleIdentifier = MobileUtils.getBundleIdentifier(driver);
    MobileUtils.moveAppToBackground(driver);
    /**
     * we have static wait because there is no way to check if the app was moved to the Background.
     * Sometimes, especially in the cloud, I found that image uploading started too early, and they
     * were uploaded but the app didn't see them
     */
    ThreadHelper.sleep(5000);
    getFilesFromPaths(imagesPath).forEach(image -> uploadFile(driver, image));
    ThreadHelper.sleep(timeOuts);
    MobileUtils.activateApp(driver, bundleIdentifier);
  }

  /**
   * Upload one file
   *
   * @param fileName
   */
  public static void uploadFile(WebDriver driver, File fileName) {
    try {
      logger.info("Uploading [{}] file", fileName);
      ((AndroidDriver) MobileUtils.getMobileDriver(driver))
          .pushFile(String.format(DEVICE_IMAGES_LOCATION_ANDROID, fileName.getName()), fileName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Get Files From Path
   *
   * @param pathToFolder
   * @return Set<File>
   */
  private static Set<File> getFilesFromPaths(String pathToFolder) {
    try (Stream<Path> stringStream =
        Files.walk(Paths.get(pathToFolder)).filter(Files::isRegularFile)) {
      List<Path> pathList = stringStream.collect(Collectors.toList());
      Set<File> filesInFolder =
          pathList.stream().map(path -> new File(path.toString())).collect(Collectors.toSet());
      return filesInFolder;
    } catch (IOException e) {
      logger.info(e.getStackTrace().toString());
    }
    return Collections.emptySet();
  }

  /** Clear Measurement images folder. */
  public static void clearImagesFolder(WebDriver driver) {
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
   * <p>Link - https://docs.saucelabs.com/mobile-apps/features/camera-image-injection/
   *
   * @param driver
   * @param fileLocation
   */
  public static void injectImageToSauce(WebDriver driver, String fileLocation) {
    try {
      logger.info("Injecting [{}] file to Sauce device.", fileLocation);
      FileInputStream in = new FileInputStream(fileLocation);
      String qrCodeImage = Base64.getEncoder().encodeToString(IOUtils.toByteArray(in));

      // Provide the transformed image to the device
      ((JavascriptExecutor) driver).executeScript("sauce:inject-image=" + qrCodeImage);
    } catch (Exception e) {
      e.printStackTrace();
      throw new TestException("Image injection error.");
    }
  }
}
