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
package com.applause.auto.reporting;

import com.applause.auto.util.autoapi.AutoApi;
import com.google.common.base.Suppliers;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import retrofit2.Response;

/** Util class to read the SDK version from the VERSION.txt file that we package inside the pom */
@AllArgsConstructor
public final class SdkVersionReader {

  // util class
  private static final String SDK_VERSION_FILE_NAME = "VERSION.txt";
  private static final Logger logger = LogManager.getLogger(SdkVersionReader.class);
  private final @NonNull AutoApi autoApi;
  private final Supplier<String> loadedVersion = Suppliers.memoize(this::verifySdkVersion);

  /**
   * Gets the loaded SDK Version
   *
   * @return The SDK Version String
   */
  public String getSdkVersion() {
    return loadedVersion.get();
  }

  /**
   * checks the SDK version using the VERSION.txt file that gets stashed in the JAR by build task
   *
   * @return the sdk version
   */
  private String verifySdkVersion() {
    final var version = loadSdkVersion();
    Response<Void> resp = autoApi.checkDeprecated(version).join();
    if (resp.isSuccessful()) {
      return version;
    }
    String errString;
    try (var errBody = resp.errorBody()) {
      errString = errBody != null ? errBody.string() : "null";
    } catch (IOException e) {
      logger.fatal("could not read error content", e);
      throw new RuntimeException("could not read error content", e);
    }
    if (resp.code() == 400) {
      throw new RuntimeException("Incorrect SDK version " + errString);
    } else {
      throw new RuntimeException(
          "error attempting to check Applause Automation SDK version: Code was: "
              + resp.code()
              + " Error content: "
              + errString);
    }
  }

  private String loadSdkVersion() {
    try (InputStream fileUrl =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(SDK_VERSION_FILE_NAME)) {
      if (fileUrl == null) {
        throw new RuntimeException(
            "Could not read current Applause Automation SDK version from classpath. Version file not found");
      }
      return IOUtils.toString(fileUrl, (Charset) null);
    } catch (IOException e) {
      logger.fatal("Could not read current Applause Automation SDK version from classpath", e);
      throw new RuntimeException(
          "Could not read current Applause Automation SDK version from classpath", e);
    }
  }
}
