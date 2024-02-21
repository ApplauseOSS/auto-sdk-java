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
package com.applause.auto.testng;

import com.applause.auto.context.IContextConnector;
import lombok.NonNull;
import org.testng.ITestResult;

/**
 * A ContextConnector to connect the Applause FrameworkContext to the TestNG ITestResult
 *
 * @param externalContext The TestNG Test Result
 */
public record TestNgContextConnector(@NonNull ITestResult externalContext)
    implements IContextConnector<ITestResult> {
  @Override
  public Long getResultId() {
    return (Long) this.externalContext.getAttribute("testResultId");
  }

  @Override
  public String getTestCaseName() {
    return externalContext.getTestClass().getName()
        + "."
        + externalContext.getMethod().getMethodName();
  }
}
