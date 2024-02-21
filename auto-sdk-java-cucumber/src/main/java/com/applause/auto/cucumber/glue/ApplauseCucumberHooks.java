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
package com.applause.auto.cucumber.glue;

import com.applause.auto.cucumber.CucumberContextConnector;
import com.applause.auto.cucumber.plugins.ApplauseReporterPlugin;
import com.applause.auto.framework.ContextManager;
import com.applause.auto.integrations.assets.AssetsUtil;
import com.google.common.net.MediaType;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.Status;

/** Base class to help with cucumber connections */
public class ApplauseCucumberHooks {

  /**
   * Cucumber hook that executes before a scenario begins. Helps us to tie the context to the
   * framework
   *
   * @param scenario The Cucumber Scenario
   */
  @Before
  public void beforeScenario(final Scenario scenario) {
    ContextManager.INSTANCE
        .getCurrentContext()
        .ifPresent(
            context -> {
              // Connect the context to the scenario
              context.setConnector(new CucumberContextConnector(scenario));

              // Attach the context id to the scenario
              scenario.attach(
                  context.getContextId(),
                  MediaType.PLAIN_TEXT_UTF_8.toString(),
                  ApplauseReporterPlugin.APPLAUSE_CONTEXT_ID_ATTACHMENT);
            });
  }

  /**
   * Cucumber hook that executes after the scenario ends. Used for asset capture from the driver(s).
   *
   * @param scenario The Cucumber Scenario
   */
  @After
  public void afterScenario(final Scenario scenario) {
    AssetsUtil.captureAssetsForDriver(scenario.getStatus() == Status.FAILED);
  }
}
