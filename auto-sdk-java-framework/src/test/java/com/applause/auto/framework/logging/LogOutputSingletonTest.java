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
package com.applause.auto.framework.logging;

import com.applause.auto.logging.LogOutputSingleton;
import java.util.List;
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
}
