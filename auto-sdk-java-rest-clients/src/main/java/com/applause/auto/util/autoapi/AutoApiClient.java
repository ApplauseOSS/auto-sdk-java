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
package com.applause.auto.util.autoapi;

import com.applause.auto.util.CommonOkhttpInterceptor;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Class that opens a connection to Applause's Auto API server. Necessary for non-Selenium REST
 * calls.
 */
public enum AutoApiClient {
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
  public static AutoApi getClient(
      final String url, final String apiKey, @Nullable final Proxy httpProxy) {
    Builder httpClient =
        // sometimes asset uploads take... forever
        // ...and sometimes setting up a real device takes even longer.
        new Builder().readTimeout(20, TimeUnit.MINUTES).writeTimeout(20, TimeUnit.MINUTES);
    httpClient.addInterceptor(chain -> CommonOkhttpInterceptor.apiKeyAuthChain(chain, apiKey));
    httpClient
        .addInterceptor(chain -> CommonOkhttpInterceptor.errorHandlerChain(chain, "auto-api"))
        .proxy(httpProxy);

    OkHttpClient client = httpClient.build();
    return new Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(AutoApi.class);
  }
}
