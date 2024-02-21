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
package com.applause.auto.framework.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GsonHelperTest {
  private static final Logger logger = LogManager.getLogger(GsonHelperTest.class);
  private static File DATA_DIR = null;

  @BeforeClass
  public static void initEnv() {
    try {
      String codeRoot = new File(".").getCanonicalPath();
      if (codeRoot.endsWith("auto-sdk-java-framework")) {
        DATA_DIR = new File(codeRoot, "src/test/testData/GsonHelper");
      } else {
        DATA_DIR = new File(codeRoot, "auto-sdk-java-framework/src/test/testData/GsonHelper");
      }
    } catch (IOException e) {
      logger.warn("unable to determine top of code tree to find data files");
    }
  }

  @Test
  public void testFromJsonStringGoodPath() throws IOException, BadJsonFormatException {
    final File p1Chrome = new File(DATA_DIR, "simple_serverSide.json");
    final String p1ChromeStr = FileUtils.readFileToString(p1Chrome, "UTF-8");
    final JsonElement shouldBeJson = GsonHelper.str2JsonElement(p1ChromeStr);
    Assert.assertNotNull(shouldBeJson);
    final JsonObject shouldBeJsonObject = GsonHelper.str2JsonObject(p1ChromeStr);
    Assert.assertNotNull(shouldBeJsonObject);
  }
}
