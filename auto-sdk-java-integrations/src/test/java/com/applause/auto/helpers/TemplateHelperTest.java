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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applause.auto.config.SdkConfigBean;
import com.applause.auto.framework.json.BadJsonFormatException;
import com.applause.auto.framework.templates.DriverConfigTemplateHelper;
import java.io.File;
import java.io.IOException;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TemplateHelperTest {
  private static final Logger logger = LogManager.getLogger(TemplateHelperTest.class);
  private static File DATA_DIR = null;

  @BeforeClass
  public static void initEnv() {
    try {
      String codeRoot = new File(".").getCanonicalPath();
      if (codeRoot.endsWith("auto-sdk-java-integrations")) {
        DATA_DIR = new File(codeRoot, "src/test/testData/ProviderModeHelper");
      } else {
        DATA_DIR =
            new File(codeRoot, "auto-sdk-java-integrations/src/test/testData/ProviderModeHelper");
      }
    } catch (IOException e) {
      logger.warn("unable to determine top of code tree to find data files");
    }
  }

  @SuppressWarnings("PMD.NPathComplexity")
  @Test
  @SneakyThrows
  public void testMakeCapabilities() {
    SdkConfigBean sdkCfg = mock(SdkConfigBean.class);
    when(sdkCfg.capsFile()).thenReturn("file/does/not/exist.json");
    try {
      DriverConfigTemplateHelper.makeCapabilities("file/does/not/exist.json");
    } catch (BadJsonFormatException | IOException e) {
      Assert.assertTrue(e instanceof IOException);
      Assert.assertEquals(e.getMessage(), "file/does/not/exist.json");
    }

    File goodCaps1 = new File(DATA_DIR, "good_caps_1.json");
    final String testDataPath = goodCaps1.getAbsolutePath();
    Assert.assertTrue(goodCaps1.canRead(), "Unable to read test data: " + testDataPath);
    when(sdkCfg.capsFile()).thenReturn(testDataPath);
    val appCap = DriverConfigTemplateHelper.makeCapabilities(testDataPath);
    Assert.assertNotNull(appCap);
    /*
     Need to mock the Caps so that it can find and not find a file
     Need files that are good and bad
     Need to mock the env too
    */
  }
}
