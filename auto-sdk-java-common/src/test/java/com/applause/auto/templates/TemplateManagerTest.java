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
package com.applause.auto.templates;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applause.auto.templates.TemplateManager.TemplateGenerationException;
import com.applause.auto.templates.TemplateManager.TemplateProcessException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TemplateManagerTest {
  private static final Logger logger = LogManager.getLogger(TemplateManagerTest.class);
  private static File DATA_DIR = null;
  private static Map<String, Object> envVariables;

  @BeforeClass
  public static void initEnv() {
    try {
      String codeRoot = new File(".").getCanonicalPath();
      if (codeRoot.endsWith("auto-sdk-java-common")) {
        DATA_DIR = new File(codeRoot, "src/test/resources/testData/templateManager");
      } else {
        DATA_DIR =
            new File(codeRoot, "auto-sdk-java-common/src/test/resources/testData/templateManager");
      }
    } catch (IOException e) {
      logger.warn("unable to determine top of code tree to find data files");
    }
  }

  @BeforeMethod
  public static void resetEnvVariables() {
    envVariables = new HashMap<>();
  }

  @Test
  public void testProcess() throws TemplateGenerationException, TemplateProcessException {
    envVariables.put("name", "Applause");
    final String template = "Hello World ${name}";
    final Template res = TemplateManager.generateTemplate("template-test-good", template);
    final String data = TemplateManager.process(res, envVariables);
    Assert.assertNotNull(data);
    Assert.assertEquals("Hello World Applause", data);
  }

  @Test
  public void testProcessSimple() throws TemplateGenerationException, TemplateProcessException {
    // Simple test no variables.  Just a hello world template
    final Configuration cfg = new Configuration(TemplateManager.freemarkerVersion);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setLocale(Locale.getDefault());
    final String name = "template_name";
    final String templateContents = "Hello World!!!";
    Template res = TemplateManager.generateTemplate(name, templateContents, cfg);
    String data = TemplateManager.process(res, envVariables);
    Assert.assertNotNull(res);
    Assert.assertEquals(templateContents, data);
  }

  @Test
  public void testProcessConfigCannotFindTemplate() {
    // Simple test no variables.  Just a hello world template
    final Configuration cfg = new Configuration(TemplateManager.freemarkerVersion);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setLocale(Locale.getDefault());
    final String name = "template_name";
    final String templateContents = "Hello World!!!";
    StringTemplateLoader stl = new StringTemplateLoader();
    stl.putTemplate(name, templateContents);
    cfg.setTemplateLoader(stl);

    try {
      TemplateManager.generateTemplate("no-such-name", cfg);
    } catch (TemplateGenerationException e) {
      Assert.assertTrue(
          e.getMessage()
              .startsWith(
                  "Template no-such-name not found: Template not found for name \"no-such-name\"."));
    }
  }

  @Test
  public void testProcessConfigTemplateNotFoundException() throws IOException {
    // Freemarker has limits on a template name.  We expose that
    // Make sure we return the correct error when we hit that condition
    final Configuration cfg = new Configuration(TemplateManager.freemarkerVersion);
    File file = new File("/tmp");
    cfg.setDirectoryForTemplateLoading(file);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setLocale(Locale.getDefault());
    final String name = "cannot-find";
    try {
      TemplateManager.generateTemplate(name, cfg);
    } catch (TemplateGenerationException e) {
      Assert.assertTrue(
          e.getMessage()
              .startsWith(
                  "Template cannot-find not found: Template not found for name \"cannot-find\""));
    }
  }

  @Test
  public void testProcessConfigMalformedTemplateNameException() throws IOException {
    // Freemarker has limits on a template name.  We expose that
    // Make sure we return the correct error when we hit that condition
    MalformedTemplateNameException ex =
        new MalformedTemplateNameException("name", "some description");
    final Configuration cfg = mock(Configuration.class);
    when(cfg.getTemplate(any())).thenThrow(ex);

    final String name = "any-name-since-we-are-mocked";

    try {
      TemplateManager.generateTemplate(name, cfg);
    } catch (TemplateGenerationException e) {
      Assert.assertEquals("Template name has a Malformed Name: some description", e.getMessage());
    }
  }

  @Test
  public void testProcessConfigIOE() throws IOException {
    // Freemarker has limits on a template name.  We expose that
    // Make sure we return the correct error when we hit that condition
    IOException ioe = new IOException("Unable to read file");
    final Configuration cfg = mock(Configuration.class);
    when(cfg.getTemplate(any())).thenThrow(ioe);

    final String name = "any-name-since-we-are-mocked";
    try {
      TemplateManager.generateTemplate(name, cfg);
    } catch (TemplateGenerationException e) {
      Assert.assertEquals(
          "Unable to read Template any-name-since-we-are-mocked: Unable to read file",
          e.getMessage());
    }
  }

  @Test
  public void testAgainstStoredFiles() throws IOException {
    // First we iterate over a data directory and collect sets of files that match
    // a naming criteria:
    //  <methodName>_<descr>_<result>_<type>.text
    // type:  Is one of 'input', 'data', 'output'  (the 'data' file is optional)
    //        If there's a 'input' there must be a corresponding output with the same other 3 fields
    // result: Is a boolean:  true or false  (what the method that we test with returns)
    // descr:  Something to help the humans and make the file name unique
    // methodName:  The method we will call to run the test.  Should be a static method in
    // TemplateManager
    final Map<String, String> inputMap = new HashMap<>();
    final Map<String, String> dataMap = new HashMap<>();
    final Map<String, String> outputMap = new HashMap<>();
    String[] fileNames = Optional.ofNullable(DATA_DIR.list()).orElseGet(() -> new String[] {});
    for (String fileName : fileNames) {
      if (Strings.isBlank(fileName)) {
        continue;
      }
      fileName = fileName.trim();
      String[] splitName = fileName.split("_");
      if (splitName.length == 4) {
        // Use the first 3 fields as a key
        final String key = String.format("%s_%s_%s", splitName[0], splitName[1], splitName[2]);
        // This is a file we care about.  Save it
        if (splitName[3].startsWith("input")) {
          // Input file
          System.out.println(fileName);
          inputMap.put(key, fileName);
        }
        if (splitName[3].startsWith("data")) {
          // Input file
          dataMap.put(key, fileName);
        }
        if (splitName[3].startsWith("output")) {
          // Input file
          outputMap.put(key, fileName);
        }
      }
    } // END FOR
    // Now iterate over the input files and execute them
    final Configuration cfg = new Configuration(TemplateManager.freemarkerVersion);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setLocale(Locale.getDefault());
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    for (Map.Entry<String, String> inFile : inputMap.entrySet()) {
      final String key = inFile.getKey();
      final String filePath = inFile.getValue();
      final boolean expResult = key.endsWith("_true");
      if (key.startsWith("processConfig")) {
        runProcessConfigTest(filePath, expResult, dataMap.get(key), outputMap.get(key));
      }
    }
  }

  static void runProcessConfigTest(
      final String fileName,
      final boolean expResult,
      final String dataFilePath,
      final String outFilePath)
      throws IOException {
    // All file names are relative to the DATA_DIR
    // Make sure we have an "output" file
    File inF = new File(DATA_DIR, fileName);
    Assert.assertTrue(inF.canRead(), "Unable to read file: " + inF.getAbsolutePath());
    File outF = new File(DATA_DIR, outFilePath);
    Assert.assertFalse(
        Strings.isBlank(outFilePath), "No corresponding out file for: " + inF.getAbsolutePath());
    Assert.assertTrue(outF.canRead(), "Unable to read file: " + outF.getAbsolutePath());

    final Configuration cfg = new Configuration(TemplateManager.freemarkerVersion);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setLocale(Locale.getDefault());
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

    final String templateContents = FileUtils.readFileToString(inF, "UTF-8");
    StringTemplateLoader stl = new StringTemplateLoader();
    stl.putTemplate(fileName, templateContents);
    cfg.setTemplateLoader(stl);
    final Map<String, Object> loadedProperties = loadEnv(dataFilePath);

    final String expOutput = FileUtils.readFileToString(outF, "UTF-8");

    Template actRes;
    try {
      actRes = TemplateManager.generateTemplate(fileName, cfg);
    } catch (TemplateGenerationException e) {
      Assert.assertEquals(
          expOutput.trim(),
          e.getMessage().trim(),
          String.format("Mismatch expected OUTPUT for '%s'", inF.getCanonicalPath()));
      return;
    }
    try {
      String data = TemplateManager.process(actRes, loadedProperties);
      // After all this work, we can finally do the test
      Assert.assertEquals(
          expOutput.trim(),
          data.trim(),
          String.format("Mismatch expected OUTPUT for '%s'", inF.getCanonicalPath()));
    } catch (TemplateProcessException e) {
      Assert.assertEquals(
          expOutput.trim(),
          e.getMessage().trim(),
          String.format("Mismatch expected OUTPUT for '%s'", inF.getCanonicalPath()));
    }
  }

  static Map<String, Object> loadEnv(final String dataFilePath) throws IOException {
    if (Strings.isBlank(dataFilePath)) {
      return null;
    }
    File dataFile = new File(DATA_DIR, dataFilePath);
    if (dataFile.exists()) {
      // The data should be in a JSON format
      final String dataFileJson = FileUtils.readFileToString(dataFile, "UTF-8");
      Gson gson = new Gson();
      final var mapType = new TypeToken<Map<String, ?>>() {}.getType();
      try {
        return gson.fromJson(dataFileJson, mapType);
      } catch (RuntimeException e) {
        throw new RuntimeException("Could not parse json file " + dataFile.getAbsolutePath(), e);
      }
    } else {
      // This should NOT happen, but if someone deletes it while the test is running, maybe. Code
      // safe.
      Assert.fail("Data file: '" + dataFile.getAbsolutePath() + "' does not exist");
    }
    return null;
  }
}
