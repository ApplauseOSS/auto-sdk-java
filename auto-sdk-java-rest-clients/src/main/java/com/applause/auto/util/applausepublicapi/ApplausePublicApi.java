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

import com.applause.auto.util.applausepublicapi.api.*;
import lombok.AllArgsConstructor;
import retrofit2.Retrofit;

/**
 * Class that opens a connection to Applause's Public API server. Used for automatic build detection
 * or TestCycle management.
 */
@AllArgsConstructor
public class ApplausePublicApi {
  private final Retrofit clients;

  public BuildApi builds() {
    return getClient(BuildApi.class);
  }

  public DictionariesApi dictionaries() {
    return getClient(DictionariesApi.class);
  }

  public IssuesApi issues() {
    return getClient(IssuesApi.class);
  }

  public KeysApi keys() {
    return getClient(KeysApi.class);
  }

  public ProductsApi products() {
    return getClient(ProductsApi.class);
  }

  public TestCaseResultsApi testCaseResults() {
    return getClient(TestCaseResultsApi.class);
  }

  public TestCaseApi testCases() {
    return getClient(TestCaseApi.class);
  }

  public CommunityTestCycleApi communityTestCycles() {
    return getClient(CommunityTestCycleApi.class);
  }

  public InternalTestCycleApi internalTestCycles() {
    return getClient(InternalTestCycleApi.class);
  }

  public TestSuitesApi testSuites() {
    return getClient(TestSuitesApi.class);
  }

  private <T> T getClient(final Class<T> clazz) {
    return clients.create(clazz);
  }
}
