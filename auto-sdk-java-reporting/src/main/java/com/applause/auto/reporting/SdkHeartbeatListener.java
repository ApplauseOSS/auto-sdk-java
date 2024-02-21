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

import com.google.common.util.concurrent.Service.Listener;
import com.google.common.util.concurrent.Service.State;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * A Listener to attach to the SdkHeartbeatService to report when the listener hits an ending status
 */
@Log4j2
public class SdkHeartbeatListener extends Listener {
  @Override
  public void failed(final @NonNull State from, final @NonNull Throwable failure) {
    super.failed(from, failure);
    log.info("SDK Heartbeat failed from state {} due to error: {}", from.name(), failure);
  }

  @Override
  public void stopping(final @NonNull State from) {
    super.stopping(from);
    log.info("SDK Heartbeat stopping from state {}", from.name());
  }

  @Override
  public void terminated(final @NonNull State from) {
    super.terminated(from);
    log.info("SDK Heartbeat terminated from state {}", from.name());
  }
}
