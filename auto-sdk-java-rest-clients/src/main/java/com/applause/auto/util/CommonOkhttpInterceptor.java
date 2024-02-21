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
package com.applause.auto.util;

import com.google.common.base.Strings;
import java.io.IOException;
import java.util.stream.Collectors;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Common, custom, interceptor methods used when making retrofit/okhttp connections */
public final class CommonOkhttpInterceptor {

  private static final Logger logger = LogManager.getLogger();

  private CommonOkhttpInterceptor() {}

  /**
   * Performs apiKey pre-validation
   *
   * @param chain the interceptor chain
   * @param apiKey the apiKey
   * @return Response
   * @throws IOException the exception
   * @throws RuntimeException when apikey is missing
   */
  public static Response apiKeyAuthChain(final Interceptor.Chain chain, final String apiKey)
      throws IOException {
    if (Strings.isNullOrEmpty(apiKey)) {
      throw new RuntimeException(
          "apiKey parameter cannot be blank! Please add your API key to the command line parameters or properties file");
    }
    Request newRequest = chain.request().newBuilder().addHeader("X-API-Key", apiKey).build();

    return chain.proceed(newRequest);
  }

  /**
   * Handle and display errors.
   *
   * @param chain the interceptor chain
   * @param serviceReportingName the name of the service to be shown in logs
   * @return Response
   * @throws IOException the exception
   */
  public static Response errorHandlerChain(
      final Interceptor.Chain chain, final String serviceReportingName) throws IOException {
    Request request = chain.request();
    // don't bother outgoing requests, it's server response we care about
    Response response = chain.proceed(request);
    // check for nasty error codes in response
    if (!response.isSuccessful() && response.code() != 408 && response.code() != 404) {
      logger.fatal(
          "================================Error response from {}!===============================",
          serviceReportingName);
      logger.fatal("Original request: " + request.url());
      logger.fatal("Response Message: " + response.message());
      logger.fatal("Redirect?: " + response.isRedirect());
      logger.fatal("Response Code: " + response.code());
      logger.fatal("Error body : " + response.peekBody(Long.MAX_VALUE).string());
      logger.fatal(
          "Headers extracted: \n"
              + response.headers().toMultimap().entrySet().stream()
                  .map(keyVal -> "    Key: " + keyVal.getKey() + "    Value: " + keyVal.getValue())
                  .collect(Collectors.joining("\n")));
    }
    return response;
  }
}
