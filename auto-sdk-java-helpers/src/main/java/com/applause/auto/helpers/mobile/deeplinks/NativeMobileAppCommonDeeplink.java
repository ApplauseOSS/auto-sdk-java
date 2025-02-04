/*
 *
 * Copyright © 2025 Applause App Quality, Inc.
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
package com.applause.auto.helpers.mobile.deeplinks;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** Deeplink DTO to keep deeplink data for mobile deeplink navigation */
@Builder
@Data
public class NativeMobileAppCommonDeeplink {

  private AndroidDeepLink androidDeepLink;
  private iOSDeepLink iOSDeepLink;

  /** Android deep link class. */
  @Data
  @AllArgsConstructor
  public static class AndroidDeepLink {
    private String deepLinkUrl;
    private String deepLinkPackage;
  }

  /** iOS deep link class. */
  @Data
  @AllArgsConstructor
  public static class iOSDeepLink {
    private String deepLinkUrl;
  }
}
