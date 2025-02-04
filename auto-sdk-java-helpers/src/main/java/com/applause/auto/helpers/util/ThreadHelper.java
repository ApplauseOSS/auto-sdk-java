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
package com.applause.auto.helpers.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Utility helper class that provides common actions needed to control the current Thread. */
public final class ThreadHelper {
  private static final Logger logger = LogManager.getLogger(ThreadHelper.class);

  private ThreadHelper() {}

  /**
   * Suspends the current thread for a specified period of milliseconds.
   *
   * @param milliseconds A long value that represents the milliseconds.
   */
  public static void sleep(long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {
      logger.error(e);
    }
  }
}
