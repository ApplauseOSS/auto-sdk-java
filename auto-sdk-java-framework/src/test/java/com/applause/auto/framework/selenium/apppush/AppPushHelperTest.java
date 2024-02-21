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
package com.applause.auto.framework.selenium.apppush;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applause.auto.config.AppPushConfig;
import java.io.File;
import java.io.IOException;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SuppressWarnings("SpellCheckingInspection")
public class AppPushHelperTest {
  private static final Logger logger = LogManager.getLogger(AppPushHelperTest.class);
  private static File DATA_DIR = null;

  @BeforeClass
  public static void initEnv() {
    try {
      String codeRoot = new File(".").getCanonicalPath();
      if (codeRoot.endsWith("auto-sdk-java-framework")) {
        DATA_DIR = new File(codeRoot, "src/test/java/com/applause/auto/framework/selenium/apppush");
      } else {
        DATA_DIR =
            new File(
                codeRoot,
                "auto-sdk-java-framework/src/test/java/com/applause/auto/framework/selenium/apppush");
      }
    } catch (IOException e) {
      logger.warn("unable to determine top of code tree to find data files");
    }
  }

  @Test
  public void testCreateErrMsg() throws IOException {
    logger.trace("Just some msg to I can keep a logger");
    final Response mockRsp = mock(Response.class);
    when(mockRsp.code()).thenReturn(404);
    final String src = "my-source";
    final String myTarget = "http://www.fubar.com/destination";

    // Test the case where there is no ResponseBody
    String actErrMsg = AppPushHelper.createErrMsg(mockRsp, src, myTarget);
    Assert.assertEquals(
        "Unable to push application 'my-source' to 'http://www.fubar.com/destination'.  status=404(success=false), msg=''",
        actErrMsg,
        "Incorrect msg format");

    // Test the case where there is a ResponseBody, but 'body()' throws an IOException
    ResponseBody mockBodyEx = mock(ResponseBody.class);
    when(mockBodyEx.string()).thenThrow(new IOException("Unable to parse response"));
    when(mockRsp.code()).thenReturn(401);
    when(mockRsp.isSuccessful()).thenReturn(true);
    when(mockRsp.body()).thenReturn(mockBodyEx);
    actErrMsg = AppPushHelper.createErrMsg(mockRsp, src, myTarget);
    Assert.assertEquals(
        "Unable to push application 'my-source' to 'http://www.fubar.com/destination'.  status=401, msg='Exception while parsing body: Unable to parse response'",
        actErrMsg,
        "Incorrect msg format");

    // Test case where we have empty string in body
    ResponseBody mockBody = mock(ResponseBody.class);
    when(mockBody.string()).thenReturn("");
    when(mockRsp.code()).thenReturn(403);
    when(mockRsp.body()).thenReturn(mockBody);
    actErrMsg = AppPushHelper.createErrMsg(mockRsp, src, myTarget);
    Assert.assertEquals(
        "Unable to push application 'my-source' to 'http://www.fubar.com/destination'.  status=403, msg=''",
        actErrMsg,
        "Incorrect msg format");

    // Test case where we have a single line message in the body
    mockBody = mock(ResponseBody.class);
    when(mockBody.string()).thenReturn("Your password is bad");
    when(mockRsp.code()).thenReturn(403);
    when(mockRsp.body()).thenReturn(mockBody);
    actErrMsg = AppPushHelper.createErrMsg(mockRsp, src, myTarget);
    Assert.assertEquals(
        "Unable to push application 'my-source' to 'http://www.fubar.com/destination'.  status=403, msg='Your password is bad'",
        actErrMsg,
        "Incorrect msg format");

    // Test case where we have a multiple line message in the body
    mockBody = mock(ResponseBody.class);
    when(mockBody.string())
        .thenReturn(
            "Something bad happened\n We wish we could tell you exactly what\nhowever, the best we can provide is this error code: 42");
    when(mockRsp.code()).thenReturn(411);
    when(mockRsp.body()).thenReturn(mockBody);
    actErrMsg = AppPushHelper.createErrMsg(mockRsp, src, myTarget);
    Assert.assertEquals(
        """
                    Unable to push application 'my-source' to 'http://www.fubar.com/destination'.  status=411, msg=
                         Something bad happened
                     We wish we could tell you exactly what
                    however, the best we can provide is this error code: 42
                    """,
        actErrMsg,
        "Incorrect msg format");
  }

  @Test(expectedExceptions = {RuntimeException.class})
  public void testValidateSourceBadFile() {
    // If we give it a file that doesn't exist,  it should throw an exception
    AppPushHelper.validateSource("this-file-doesn't-exist");
  }

  @Test(expectedExceptions = {RuntimeException.class})
  public void testValidateSourceBadUrl() {
    // If we give it a URL that's bad,  it should throw an exception
    AppPushHelper.validateSource("http://:abc/");
  }

  @Test
  public void testValidateSourceGoodUrl() {
    // The test is that this should NOT throw an exception
    AppPushHelper.validateSource("http://www.fubar.com/fubar");
  }

  @Test
  public void testValidateSourceGoodFile() throws IOException {
    // The test is that this should NOT throw an exception
    File fileThatExists = new File(DATA_DIR, "AppPushHelperTest.java");
    AppPushHelper.validateSource(fileThatExists.getCanonicalPath());
  }

  @Test(expectedExceptions = {RuntimeException.class})
  public void testPushProviderToPushAppClassUnknownProvider() {
    // If we give it a file that doesn't exist,  it should throw an exception
    AppPushHelper.pushProviderToPushAppClass("this-provider-doesn't-exist");
  }

  @Test
  public void testPushProviderToPushAppClass() {
    // If we give it a file that doesn't exist,  it should throw an exception
    String actResultBstack = AppPushHelper.pushProviderToPushAppClass("BrOwSeRsTaCk");
    Assert.assertEquals(
        "com.applause.auto.framework.selenium.apppush.providers.BrowserstackPusher",
        actResultBstack);
    String actResultSauceL = AppPushHelper.pushProviderToPushAppClass("SAUCELabs");
    Assert.assertEquals(
        "com.applause.auto.framework.selenium.apppush.providers.SauceLabsPusher", actResultSauceL);
    logger.info(actResultBstack + ", " + actResultSauceL);
  }

  @Test(expectedExceptions = {RuntimeException.class})
  public void testThrowCorrectException401() {
    final Response mockRsp = mock(Response.class);
    when(mockRsp.code()).thenReturn(401);
    AppPushHelper.throwCorrectException(mockRsp, "this-is-source", "this-is-targ-url");
  }

  @Test(expectedExceptions = {RuntimeException.class})
  public void testThrowCorrectException400() {
    final Response mockRsp = mock(Response.class);
    when(mockRsp.code()).thenReturn(400);
    AppPushHelper.throwCorrectException(mockRsp, "this-is-source", "this-is-targ-url");
  }

  @Test(expectedExceptions = {RuntimeException.class})
  public void testThrowCorrectException403() {
    final Response mockRsp = mock(Response.class);
    when(mockRsp.code()).thenReturn(403);
    AppPushHelper.throwCorrectException(mockRsp, "this-is-source", "this-is-targ-url");
  }

  @Test(expectedExceptions = {AppPushRetryableException.class})
  public void testThrowCorrectException500() throws IOException {
    final Response mockRsp = mock(Response.class);
    final var mockBody = mock(ResponseBody.class);
    when(mockBody.string()).thenReturn("Error");
    when(mockRsp.isSuccessful()).thenReturn(true);
    when(mockRsp.code()).thenReturn(500);
    when(mockRsp.isSuccessful()).thenReturn(true);
    when(mockRsp.body()).thenReturn(mockBody);
    when(mockRsp.isSuccessful()).thenReturn(false);
    AppPushHelper.throwCorrectException(mockRsp, "this-is-source", "this-is-targ-url");
  }

  @Test(expectedExceptions = {AppPushRetryableException.class})
  public void testThrowCorrectException404() throws IOException {
    final Response mockRsp = mock(Response.class);
    final var mockBody = mock(ResponseBody.class);
    when(mockBody.string()).thenReturn("Error");
    when(mockRsp.isSuccessful()).thenReturn(true);
    when(mockRsp.code()).thenReturn(404);
    when(mockRsp.isSuccessful()).thenReturn(true);
    when(mockRsp.body()).thenReturn(mockBody);
    AppPushHelper.throwCorrectException(mockRsp, "this-is-source", "this-is-targ-url");
  }

  @Test
  public void testVerifyHttp200OrThrow_200() {
    final AppPushConfig mockCfg = mock(AppPushConfig.class);
    when(mockCfg.appPushSource()).thenReturn("this-is-the-src");
    final Response mockRsp = mock(Response.class);
    when(mockRsp.isSuccessful()).thenReturn(true);
    when(mockRsp.code()).thenReturn(200);
    AppPushHelper.verifyHttp200OrThrow(mockRsp, mockCfg, "this-is-targ-url");

    when(mockRsp.code()).thenReturn(204);
    AppPushHelper.verifyHttp200OrThrow(mockRsp, mockCfg, "this-is-targ-url");
  }

  @Test(expectedExceptions = {RuntimeException.class})
  public void testVerifyHttp200OrThrow_NotSuccess() {
    final AppPushConfig mockCfg = mock(AppPushConfig.class);
    when(mockCfg.appPushSource()).thenReturn("this-is-the-src");
    final Response mockRsp = mock(Response.class);
    when(mockRsp.isSuccessful()).thenReturn(false);
    when(mockRsp.code()).thenReturn(200);
    AppPushHelper.verifyHttp200OrThrow(mockRsp, mockCfg, "this-is-targ-url");
  }

  @Test(expectedExceptions = {RuntimeException.class})
  public void testVerifyHttp200OrThrow_403() {
    final AppPushConfig mockCfg = mock(AppPushConfig.class);
    when(mockCfg.appPushSource()).thenReturn("this-is-the-src");
    final Response mockRsp = mock(Response.class);
    when(mockRsp.code()).thenReturn(403);
    AppPushHelper.verifyHttp200OrThrow(mockRsp, mockCfg, "this-is-targ-url");
  }

  @Test(expectedExceptions = {AppPushRetryableException.class})
  public void testVerifyHttp200OrThrow_503() throws IOException {
    final AppPushConfig mockCfg = mock(AppPushConfig.class);
    when(mockCfg.appPushSource()).thenReturn("this-is-the-src");
    final Response mockRsp = mock(Response.class);
    final var mockBody = mock(ResponseBody.class);
    when(mockBody.string()).thenReturn("Error");
    when(mockRsp.isSuccessful()).thenReturn(true);
    when(mockRsp.code()).thenReturn(503);
    when(mockRsp.isSuccessful()).thenReturn(true);
    when(mockRsp.body()).thenReturn(mockBody);
    AppPushHelper.verifyHttp200OrThrow(mockRsp, mockCfg, "this-is-targ-url");

    when(mockRsp.code()).thenReturn(204);
    AppPushHelper.verifyHttp200OrThrow(mockRsp, mockCfg, "this-is-targ-url");
  }
}
