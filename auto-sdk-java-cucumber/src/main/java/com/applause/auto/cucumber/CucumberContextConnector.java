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
package com.applause.auto.cucumber;

import com.applause.auto.context.IContextConnector;
import com.applause.auto.reporting.ApplauseReporter;
import com.applause.auto.reporting.ResultRecord;
import io.cucumber.java.Scenario;
import java.util.Optional;

/**
 * A Context Connector to join the Cucumber Scenario to the Applause Framework context
 *
 * @param externalContext The cucumber scenario
 */
public record CucumberContextConnector(Scenario externalContext)
    implements IContextConnector<Scenario> {
  @Override
  public Long getResultId() {
    return Optional.ofNullable(ApplauseReporter.INSTANCE.getResults().get(externalContext.getId()))
        .map(ResultRecord::getTestResultId)
        .orElse(null);
  }

  @Override
  public String getTestCaseName() {
    return externalContext.getName().replace(" ", "_");
  }
}
