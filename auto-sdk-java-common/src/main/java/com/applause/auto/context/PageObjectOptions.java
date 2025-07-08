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
package com.applause.auto.context;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** Additional options used by the PageObjectContext that change how the PageObjectModel behaves */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PageObjectOptions {
  /** Default timeout for locating elements */
  @Builder.Default private Duration timeout = Duration.ofSeconds(10);

  /** Default polling interval for locating elements */
  @Builder.Default private Duration pollingInterval = Duration.ofSeconds(1);

  /**
   * For lazy list implementations, this flag determines if we throw an exception when no elements
   * are found in the list
   */
  private @Builder.Default boolean throwExceptionOnEmptyList = true;

  /**
   * For lazy list implementation, this flag determines if we should automatically refresh the
   * elements in the list, in case elements are added/removed in between steps
   */
  private boolean autoRefreshList;

  /**
   * When initializing a LazyWebElement or LazyList, this flag dictates whether we should
   * automatically re-locate the underlying elements in the parent chain.
   */
  private boolean autoRefreshParentChain;
}
