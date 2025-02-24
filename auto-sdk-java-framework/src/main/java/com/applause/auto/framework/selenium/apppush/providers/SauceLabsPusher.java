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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import lombok.NonNull;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/** An Application Push implementation for SauceLabs */
public class SauceLabsPusher extends BaseAppPusher {
  private static final String SAUCE_PUSH_DEFAULT_URL =
      "https://api.us-west-1.saucelabs.com/v1/storage/upload";

  /** Initialize the SauceLabs App Pusher */
  public SauceLabsPusher() {
    super("filled in by customConfig");
  }

  @Override
  public RequestBody getRequestBody() {
    File fileToPush = getFileToPush();

    // If we don't have a file to push, we will need to fetch the app from the provided url as
    // SauceLabs does not support pushing
    // URLs directly.  This is a workaround for the fact that SauceLabs does not support pushing
    // URLs directly.
    if (fileToPush == null) {
      try {
        URL url = new URI(getUrlToPush()).toURL();
        final var pathTokens = url.getPath().split("/");
        final var fileName = pathTokens[pathTokens.length - 1];
        fileToPush = Files.createTempFile(null, fileName).toFile();
        FileUtils.copyURLToFile(url, fileToPush);
      } catch (IOException | URISyntaxException e) {
        throw new RuntimeException(
            "Unable to download app file for SauceLabs upload. Please check your permissions.", e);
      }
    }
    return new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "payload",
            "name",
            RequestBody.create(fileToPush, MediaType.parse("application/octet-stream")))
        .addFormDataPart("name", fileToPush.getName())
        .build();
  }

  @Override
  public String getApplicationValue(@NonNull final Response response) {
    // What comes back is a chunk of JSON. API is documented here:
    // https://docs.saucelabs.com/dev/api/storage/
    // We need to parse this and return the id passed back from the response
    JsonObject jsonObj = GsonHelper.httpRspToJsonObject(response);
    final var appId =
        Optional.ofNullable(jsonObj.getAsJsonObject("item"))
            .map(itemSubObj -> itemSubObj.get("id"))
            .map(JsonElement::getAsString)
            .orElse(null);
    if (appId == null) {
      throw new RuntimeException(
          "Unable to parse result from SauceLabs. Expected field 'item.id' is missing from response.");
    }
    return "storage:" + appId;
  }

  @Override
  protected boolean customConfig(@NonNull final AppPushConfig cfg, @NonNull final String name) {
    if (!StringUtils.isBlank(cfg.appPushTargetUrl())) {
      setTargetUrl(cfg.appPushTargetUrl());
    } else {
      setTargetUrl(SAUCE_PUSH_DEFAULT_URL);
    }
    return true;
  }
}
