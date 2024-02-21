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
import com.applause.auto.util.autoapi.SdkHeartbeatDto;
import com.google.common.util.concurrent.AbstractScheduledService;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import retrofit2.Response;

/**
 * This service runs in its own thread and sends a "heartbeat" signal to auto-api in client and
 * server side mode so that auto-api knows the SDK is still functioning. If the heartbeats stop
 * updating, auto-api will mark those sessions as errored and release resources
 */
@AllArgsConstructor
public class SdkHeartBeatService extends AbstractScheduledService {

  private static final Logger logger = LogManager.getLogger(SdkHeartBeatService.class);
  private final long testRunId;
  private final AutoApi autoApi;

  /** Guava's magic will run this in a different thread */
  @Override
  protected void runOneIteration() {
    logger.trace("Checking for Test Run Heartbeat.");
    Response<Void> response = autoApi.sdkHeartbeat(new SdkHeartbeatDto(testRunId)).join();
    if (response.code() >= 400) {
      logger.warn(
          "SDK heartbeat endpoint returned a non-success response code! " + response.code());
      try (var errBody = response.errorBody()) {
        if (errBody != null) {
          logger.warn("SDK heartbeat response error body was: " + errBody.string());
        } else {
          logger.warn("SDK heartbeat did not contain an error body");
        }
      } catch (IOException e) {
        logger.warn("could not read response body", e);
      }
    }
  }

  @Override
  protected @NonNull Scheduler scheduler() {
    return Scheduler.newFixedRateSchedule(0L, 30L, TimeUnit.SECONDS);
  }
}
