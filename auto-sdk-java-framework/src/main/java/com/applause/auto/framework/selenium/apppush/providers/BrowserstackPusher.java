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
package com.applause.auto.framework.selenium.apppush.providers;

import com.applause.auto.config.AppPushConfig;
import com.applause.auto.framework.json.GsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import lombok.NonNull;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** App Push Implementation for the BrowserStack Provider */
public class BrowserstackPusher extends BaseAppPusher {
  private static final Logger logger = LogManager.getLogger(BrowserstackPusher.class);

  /** Initializes a new BrowserStack App Pushed with the default browserstack URL. */
  public BrowserstackPusher() {
    super("https://api-cloud.browserstack.com/app-automate/upload");
  }

  @Override
  public RequestBody getRequestBody() {
    RequestBody requestBody;
    File fileToPush = getFileToPush();
    if (fileToPush == null) {
      logger.trace("Pushing URL");
      // This basically matches the content that we'd see if we use the
      // Browserstack curl example.  The only difference is that by default the Java library
      // puts in Content-Length within the MIME content
      // https://github.com/square/okhttp/issues/2138
      requestBody =
          new MultipartBody.Builder()
              .setType(MultipartBody.FORM)
              .addFormDataPart("url", getUrlToPush())
              .build();
    } else {
      logger.trace("Pushing File");
      MediaType mediaType = MultipartBody.FORM;
      MediaType interiorMediaType = MediaType.parse("application/octet-stream");
      requestBody =
          new MultipartBody.Builder()
              .setType(mediaType)
              .addFormDataPart(
                  "file", fileToPush.getName(), RequestBody.create(fileToPush, interiorMediaType))
              .build();
    }
    return requestBody;
  }

  @Override
  public String getApplicationValue(@NonNull final Response response) {
    // What comes back is a chunk of JSON
    // {"app_url":"bs://c9db3ca89e7fc23575b8545852068ac273a11f2d"}
    // We need to parse this and return the value for "app_url"
    if (response.code() != 200) {
      // The surrounding code should have caught this, but we check anyhow
      throw new RuntimeException(
          "Unexpect response code="
              + response.code()
              + ", expected 200.  Unable to parse response from application push to Browserstack");
    }
    JsonObject jsonObj = GsonHelper.httpRspToJsonObject(response);
    final JsonElement elem = jsonObj.get("app_url");
    if (elem == null) {
      throw new RuntimeException(
          "Unable to parse result from Browserstack.  Expected field 'app_url' is missing from: "
              + jsonObj);
    }
    return elem.getAsString();
  }

  @Override
  protected boolean customConfig(@NonNull final AppPushConfig cfg, @NonNull final String name) {
    if (!StringUtils.isBlank(cfg.appPushTargetUrl())) {
      setTargetUrl(cfg.appPushTargetUrl());
    }
    return true;
  }
}
