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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** A Helper for registering a shutdown hook on the JVM */
@SuppressWarnings({
  "PMD.AvoidUsingVolatile",
  "PMD.AvoidSynchronizedAtMethodLevel",
  "PMD.AvoidCatchingThrowable",
  "PMD.DataClass"
})
public final class ShutdownHandler extends Thread {
  private static final Logger logger = LogManager.getLogger(ShutdownHandler.class);
  private static volatile ShutdownHandler instance;
  private Long testRunId;
  private AutoApi client;
  private boolean normalExit;

  /**
   * This is a singleton pattern. get the instance and ensure it is registered with the runtime
   *
   * @return The ShutdownHandler for this JVM
   */
  public static synchronized ShutdownHandler getInstance() {
    if (instance == null) {
      instance = new ShutdownHandler();
      Runtime.getRuntime().addShutdownHook(instance);
    }
    return instance;
  }

  private ShutdownHandler() {
    // This is a singleton
  }

  /**
   * Sets the TestRun ID for the Shutdown Handler
   *
   * @param id The TestRun ID to shut down
   */
  public void setTestRunId(final long id) {
    testRunId = id;
  }

  /**
   * Sets the Applause Automation-API Client
   *
   * @param autoApiClient The Applause Automation API Client Instance
   */
  public void setClient(final AutoApi autoApiClient) {
    client = autoApiClient;
  }

  /**
   * Sets whether the JVM exited regularly
   *
   * @param val True if the JVM is ending regularly
   */
  public void setNormalExit(final boolean val) {
    normalExit = val;
  }

  @Override
  public void run() {
    if (testRunId == null || client == null) {
      logger.debug("Nothing to shutdown");
      return;
    }
    logger.debug("Shutdown started");
    if (!normalExit) {
      logger.warn("Attempting to cancel test run id =" + testRunId);
      try {
        client.cancelTestRun(testRunId, "Shutdown hook activated").join();
        sleep(1000); // Give a little time for this message to get out
      } catch (Throwable t) {
        logger.warn("Shutdown failed to contact auto-api: ", t);
      }
    }
    logger.debug("Shutdown completed");
  }
}
