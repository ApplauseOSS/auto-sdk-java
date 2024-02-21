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
package com.applause.auto.framework.logger;

import com.applause.auto.logging.ResultPropertyMap;
import java.util.concurrent.CompletableFuture;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ResultPropertyMapTest {
  private static final String PROPERTY = "property";
  private static final String KEY_1 = "key1";
  private static final String KEY_2 = "key2";

  @AfterMethod(alwaysRun = true)
  public void resetPropertyMap() {
    ResultPropertyMap.clearLocalKey();
    ResultPropertyMap.clearGlobalProperty(PROPERTY);
    ResultPropertyMap.clearLocalProperty(PROPERTY);
    ResultPropertyMap.clearKeyedProperty(KEY_1, PROPERTY);
    ResultPropertyMap.clearKeyedProperty(KEY_2, PROPERTY);
  }

  @Test
  public void testGlobalPropertyAssignment() {
    ResultPropertyMap.setGlobalProperty(PROPERTY, "testGlobalPropertyAssignment");
    Assert.assertEquals(ResultPropertyMap.getProperty(PROPERTY), "testGlobalPropertyAssignment");
  }

  @Test
  public void testGlobalPropertyAssignmentInOtherThread() {
    CompletableFuture.runAsync(
            () ->
                ResultPropertyMap.setGlobalProperty(
                    PROPERTY, "testGlobalPropertyAssignmentInOtherThread"))
        .join();
    Assert.assertEquals(
        ResultPropertyMap.getProperty(PROPERTY), "testGlobalPropertyAssignmentInOtherThread");
  }

  @Test
  public void testLocalPropertyAssignment() {
    ResultPropertyMap.setLocalProperty(PROPERTY, "testLocalPropertyAssignment");
    Assert.assertEquals(ResultPropertyMap.getProperty(PROPERTY), "testLocalPropertyAssignment");
  }

  @Test
  public void testLocalPropertyAssignmentInOtherThread() {
    CompletableFuture.runAsync(
            () ->
                ResultPropertyMap.setLocalProperty(
                    PROPERTY, "testLocalPropertyAssignmentInOtherThread"))
        .join();
    Assert.assertNotEquals(
        ResultPropertyMap.getProperty(PROPERTY), "testLocalPropertyAssignmentInOtherThread");
  }

  @Test
  public void testKeyedPropertyAssignment() {
    ResultPropertyMap.setLocalKey(KEY_1);
    ResultPropertyMap.setKeyedProperty(KEY_1, PROPERTY, "testKeyedPropertyAssignment");
    Assert.assertEquals(ResultPropertyMap.getProperty(PROPERTY), "testKeyedPropertyAssignment");
  }

  @Test
  public void testKeyedPropertyAssignmentInOtherThread() {
    ResultPropertyMap.setLocalKey(KEY_1);
    CompletableFuture.runAsync(
            () ->
                ResultPropertyMap.setKeyedProperty(
                    KEY_1, PROPERTY, "testKeyedPropertyAssignmentInOtherThread"))
        .join();
    Assert.assertEquals(
        ResultPropertyMap.getProperty(PROPERTY), "testKeyedPropertyAssignmentInOtherThread");
  }

  @Test
  public void testKeyedPropertyNoLocalKey() {
    CompletableFuture.runAsync(
            () ->
                ResultPropertyMap.setKeyedProperty(
                    KEY_1, PROPERTY, "testKeyedPropertyAssignmentInOtherThread"))
        .join();
    Assert.assertNull(ResultPropertyMap.getProperty(PROPERTY));
  }

  @Test
  public void testPropertyValueOrdering() {
    ResultPropertyMap.setLocalKey(KEY_1);
    ResultPropertyMap.setGlobalProperty(PROPERTY, "GLOBAL");
    ResultPropertyMap.setLocalProperty(PROPERTY, "LOCAL");
    ResultPropertyMap.setKeyedProperty(KEY_1, PROPERTY, "KEYED");

    Assert.assertEquals(ResultPropertyMap.getProperty(PROPERTY), "LOCAL");

    ResultPropertyMap.clearLocalProperty(PROPERTY);

    Assert.assertEquals(ResultPropertyMap.getProperty(PROPERTY), "KEYED");

    ResultPropertyMap.clearLocalKey();

    Assert.assertEquals(ResultPropertyMap.getProperty(PROPERTY), "GLOBAL");

    ResultPropertyMap.setLocalKey(KEY_1);
    ResultPropertyMap.clearKeyedProperty(KEY_1, PROPERTY);

    Assert.assertEquals(ResultPropertyMap.getProperty(PROPERTY), "GLOBAL");

    ResultPropertyMap.clearGlobalProperty(PROPERTY);

    Assert.assertNull(ResultPropertyMap.getProperty(PROPERTY));
  }
}
