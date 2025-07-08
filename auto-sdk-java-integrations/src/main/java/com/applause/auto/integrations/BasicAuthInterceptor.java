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
package com.applause.auto.integrations;

import com.google.common.net.HttpHeaders;
import java.io.IOException;
import lombok.NonNull;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

class BasicAuthInterceptor implements Interceptor {

  private final String credentials;

  BasicAuthInterceptor(final String user, final String password) {
    if (null != user && null != password) {
      this.credentials = Credentials.basic(user, password);
    } else {
      this.credentials = null;
    }
  }

  @Override
  public @NonNull Response intercept(final Chain chain) throws IOException {
    Request request = chain.request();
    Request forwardedRequest;
    if (null != credentials) {
      forwardedRequest =
          request.newBuilder().header(HttpHeaders.AUTHORIZATION, credentials).build();
    } else {
      forwardedRequest = request.newBuilder().build();
    }
    return chain.proceed(forwardedRequest);
  }
}
