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

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LogOutputSingletonTest {
  private static final String TEST_MESSAGE = "test message";

  @Test
  public void testLogSingleton() {
    // create a log singleton with low overflow level
    // flush any pesky messages created by other tests
    LogOutputSingleton.flush();
    // make sure flush() returns a single log message that matches the one we set
    LogOutputSingleton.put(TEST_MESSAGE);
    Assert.assertEquals(
        TEST_MESSAGE,
        LogOutputSingleton.flush().stream()
            .findFirst()
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "flushing log singleton didn't return a log message as expected")));
    // try overflowing it, make sure output is truncated and last message returned mentions overflow
    IntStream.range(0, LogOutputSingleton.MAX_LOG_BUFFER_CAPACITY + 10)
        .forEach((val) -> LogOutputSingleton.put(TEST_MESSAGE));
    List<String> res = LogOutputSingleton.flush();
    // make sure it actually truncated output (overflow size + 1 extra entry for overflow message)
    Assert.assertEquals(res.size(), LogOutputSingleton.MAX_LOG_BUFFER_CAPACITY + 1);
    // make sure the last element contains overflow warning message
    Assert.assertTrue(res.get(res.size() - 1).contains("OVERFLOW"));
  }

  @Test
  public void testLogSingletonPipeToNewQueue() {
    // create a log singleton with low overflow level
    // flush any pesky messages created by other tests
    LogOutputSingleton.flush();
    ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(1000);
    // make sure flush() returns a single log message that matches the one we set
    LogOutputSingleton.put(TEST_MESSAGE);

    Assert.assertTrue(queue.isEmpty());

    LogOutputSingleton.pipeTo(queue);
    LogOutputSingleton.put(TEST_MESSAGE);

    Assert.assertEquals(queue.size(), 1);
    Assert.assertEquals(queue.element(), TEST_MESSAGE);

    LogOutputSingleton.put(TEST_MESSAGE);
    LogOutputSingleton.put(TEST_MESSAGE);
    LogOutputSingleton.put(TEST_MESSAGE);
    Assert.assertEquals(queue.size(), 4);

    LogOutputSingleton.flush();
    Assert.assertEquals(queue.size(), 4);

    LogOutputSingleton.put(TEST_MESSAGE);
    Assert.assertEquals(queue.size(), 4);
  }

  @Test
  public void testLogSingletonThreadLocal() throws InterruptedException, ExecutionException {
    // create a log singleton with low overflow level
    // flush any pesky messages created by other tests
    LogOutputSingleton.flush();

    // Offer a message in another thread and make sure it doesn't leak
    CompletableFuture.runAsync(() -> LogOutputSingleton.put(TEST_MESSAGE)).get();

    // Make sure this thread is still empty
    Assert.assertEquals(LogOutputSingleton.flush().size(), 0);
  }

  @Test
  public void testLogSingletonPipeToAnotherThread()
      throws InterruptedException, ExecutionException {
    // create a log singleton with low overflow level
    // flush any pesky messages created by other tests
    LogOutputSingleton.flush();
    final var mainThreadQueue = LogOutputSingleton.getLogsQueue();

    CompletableFuture.runAsync(
            () -> {
              // Set up the pipe
              LogOutputSingleton.pipeTo(mainThreadQueue);

              LogOutputSingleton.put(TEST_MESSAGE);
              LogOutputSingleton.put(TEST_MESSAGE);
              LogOutputSingleton.put(TEST_MESSAGE);
              LogOutputSingleton.flush();

              // The flush clears any pipes out as well, so this message shouldn't make it to the
              // main thread.
              LogOutputSingleton.put(TEST_MESSAGE);
            })
        .get();

    Assert.assertEquals(LogOutputSingleton.flush().size(), 3);
  }
}
