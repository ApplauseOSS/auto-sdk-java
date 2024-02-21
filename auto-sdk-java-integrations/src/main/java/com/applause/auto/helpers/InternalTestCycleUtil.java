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

import com.applause.auto.util.applausepublicapi.ApplausePublicApi;
import com.applause.auto.util.applausepublicapi.ApplausePublicApiClient;
import com.applause.auto.util.applausepublicapi.dto.NewInternalTestCycleOptions;
import java.net.Proxy;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class InternalTestCycleUtil {

  private final ApplausePublicApi client;

  /**
   * Sets up a Public API Instance
   *
   * @param applausePublicApiUrl The Base URL for the public API
   * @param apiKey The API Key used to interface with the public API
   * @param proxy An optional HTTP proxy
   */
  public InternalTestCycleUtil(
      @NonNull final String applausePublicApiUrl,
      @NonNull final String apiKey,
      @Nullable final Proxy proxy) {
    this.client = ApplausePublicApiClient.getClient(applausePublicApiUrl, apiKey, proxy);
  }

  /**
   * Clones an internal test cycle
   *
   * @param internalTestCycleId The internal test cycle id
   * @param newTestCycleName The new name of the cloned test cycle
   * @return The new id
   */
  public Optional<Long> cloneInternalTestCycle(
      final Long internalTestCycleId, final String newTestCycleName) {
    final var res =
        this.client
            .internalTestCycles()
            .createTestCycle(
                getInternalTestCycleOptions(),
                internalTestCycleId,
                false,
                false,
                0L,
                newTestCycleName,
                List.of())
            .join();
    if (!res.isSuccessful()) {
      log.warn("Unable to clone test cycle: " + res.message());
      return Optional.empty();
    }
    final var newTestCycle = res.body();
    if (newTestCycle == null) {
      log.warn("Unable to clone test cycle: invalid response received");
      return Optional.empty();
    }
    final var newTestCycleId = newTestCycle.id();
    log.info(
        String.format(
            "Successfully cloned TestCycle [%s/%s -> %s/%s]",
            internalTestCycleId, newTestCycleName, newTestCycleId, newTestCycleId));
    return Optional.ofNullable(newTestCycleId);
  }

  private NewInternalTestCycleOptions getInternalTestCycleOptions() {
    return new NewInternalTestCycleOptions(
        null, null, null, null, null, null, null, null, null, null);
  }
}
