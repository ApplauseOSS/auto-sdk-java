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
package com.applause.auto.testng.dataprovider;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Basic Override for an Applause Test Case ID to be used within a TestNG DataProvider
 *
 * @param applauseTestCaseId The Applause Test Case ID Override
 */
public record ApplauseTestCaseIdOverride(List<String> applauseTestCaseId)
    implements IApplauseTestCaseIdOverride {
  @Override
  public String toString() {
    return StringUtils.join(applauseTestCaseId, ", ");
  }
}
