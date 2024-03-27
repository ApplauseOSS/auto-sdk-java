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
package com.applause.auto.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import lombok.val;

/**
 * maintains a thread-safe global queue of log messages flushed each time a test completes. Works
 * with the ServerSideRemoteLogAppender to capture test logs
 */
public enum LogOutputSingleton {
  ;

  /** Max number of log statements per test */
  public static final int MAX_LOG_BUFFER_CAPACITY = 100000;

  /** threadsafe global log queue */
  private static final ThreadLocal<BlockingQueue<String>> logsQueue =
      ThreadLocal.withInitial(() -> new ArrayBlockingQueue<>(MAX_LOG_BUFFER_CAPACITY, true));

  /** threadsafe global log queue */
  private static final ThreadLocal<List<BlockingQueue<String>>> pipedQueues =
      ThreadLocal.withInitial(ArrayList::new);

  /**
   * thread specific array we dump logs into. Defined here to avoid making a new array every time we
   * dump
   */
  private static final ThreadLocal<List<String>> localDrainAry =
      ThreadLocal.withInitial(() -> new ArrayList<>(MAX_LOG_BUFFER_CAPACITY));

  /**
   * flushes log statements gathered since the last flush. Also removes any lingering pipes
   *
   * @return list of the log statements
   */
  public static List<String> flush() {
    pipedQueues.get().clear();
    localDrainAry.get().clear();
    val numDrained = logsQueue.get().drainTo(localDrainAry.get());
    if (numDrained > MAX_LOG_BUFFER_CAPACITY - 1) {
      localDrainAry
          .get()
          .add(
              "\n\n LIKELY LOG OVERFLOW DETECTED FOR TEST, EXCEEDED "
                  + MAX_LOG_BUFFER_CAPACITY
                  + " STATEMENTS");
    }
    return localDrainAry.get();
  }

  /**
   * Gets the underlying logs queue for this thread. Should only be used for piping logs between
   * threads
   *
   * @return The underlying logs queue
   */
  public static BlockingQueue<String> getLogsQueue() {
    return logsQueue.get();
  }

  /**
   * puts a log statement into the local queue
   *
   * @param log the log string
   * @return true if the put succeeded to all queues
   */
  public static boolean put(final String log) {
    boolean successful = true;
    for (final var pipeQueue : pipedQueues.get()) {
      successful = successful && pipeQueue.offer(log);
    }
    return successful && logsQueue.get().offer(log);
  }

  /**
   * Adds a queue as output for the logs
   *
   * @param pipeQueue a queue to pipe the logs to
   */
  public static void pipeTo(final BlockingQueue<String> pipeQueue) {
    pipedQueues.get().add(pipeQueue);
  }
}
