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

/**
 * Deeplink DTO to keep deeplink data for mobile deeplink navigation.
 *
 * @param androidDeepLink The Android deep link information.
 * @param iOSDeepLink The iOS deep link information.
 */
public record NativeMobileAppCommonDeeplink(
    AndroidDeepLink androidDeepLink, IOSDeepLink iOSDeepLink) {

  /**
   * Android deep link record.
   *
   * @param deepLinkUrl The deep link URL.
   * @param deepLinkPackage The deep link package.
   */
  public record AndroidDeepLink(String deepLinkUrl, String deepLinkPackage) {}

  /**
   * iOS deep link record.
   *
   * @param deepLinkUrl The deep link URL.
   */
  @SuppressWarnings("checkstyle:TypeName")
  public record IOSDeepLink(String deepLinkUrl) {}
}
