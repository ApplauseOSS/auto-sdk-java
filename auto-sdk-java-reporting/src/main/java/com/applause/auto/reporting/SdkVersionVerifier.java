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
import com.applause.auto.versioning.SdkVersionReader;
import com.google.common.base.Suppliers;
import java.io.IOException;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import retrofit2.Response;

/** A utility class to verify the SDK version against the Applause backend */
@AllArgsConstructor
public final class SdkVersionVerifier {

  private static final Logger logger = LogManager.getLogger(SdkVersionVerifier.class);
  private final @NonNull AutoApi autoApi;

  /** Supplies the verified SDK Version, performing a network check. */
  private final Supplier<String> verifiedVersionSupplier =
      Suppliers.memoize(this::performVerification);

  /**
   * Gets the SDK Version and verifies it against the Applause backend. Throws a RuntimeException if
   * the version is deprecated or invalid.
   *
   * @return The verified SDK Version String
   */
  public String getVerifiedSdkVersion() {
    return verifiedVersionSupplier.get();
  }

  /**
   * Performs the network validation of the SDK version.
   *
   * @return the sdk version
   */
  private String performVerification() {
    final var version = SdkVersionReader.getSdkVersion();
    if (version == null) {
      throw new RuntimeException("SDK Version could not be read, so it cannot be verified.");
    }

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
}
