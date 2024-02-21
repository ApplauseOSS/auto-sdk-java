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
package com.applause.auto.util.applausepublicapi;

import com.applause.auto.util.CommonOkhttpInterceptor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.Proxy;
import java.time.Duration;
import javax.annotation.Nullable;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Class that opens a connection to Applause's Public API server. Used for automatic build detection
 * or TestCycle management.
 */
public enum ApplausePublicApiClient {
  ;

  /**
   * Prepare and return an HttpClient
   *
   * @param url The url of the api instance to connect to
   * @param apiKey The API Key to authenticate with
   * @param httpProxy If the system has been configured with an HTTP proxy, this contains the proxy
   *     information
   * @return client that can communicate with Applause's Auto API.
   */
  public static ApplausePublicApi getClient(
      final String url, final String apiKey, @Nullable final Proxy httpProxy) {
    OkHttpClient client =
        new Builder()
            .addInterceptor(chain -> CommonOkhttpInterceptor.apiKeyAuthChain(chain, apiKey))
            .proxy(httpProxy)
            .connectTimeout(Duration.ofSeconds(60)) // default 10s
            .readTimeout(Duration.ofSeconds(60))
            .writeTimeout(Duration.ofSeconds(60))
            .build();

    // change snake_case to camelCase during json conversion
    // set date format, so we can compare dates later
    Gson gson =
        new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'z'")
            .create();

    var retrofit =
        new Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(client)
            .build();
    return new ApplausePublicApi(retrofit);
  }
}
