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
package com.applause.auto.framework.templates;

import com.applause.auto.framework.DriverBuilder;
import com.applause.auto.framework.json.BadJsonFormatException;
import com.applause.auto.framework.selenium.ApplauseCapabilitiesConstants;
import com.applause.auto.framework.selenium.EnhancedCapabilities;
import com.applause.auto.templates.TemplateManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DriverConfigTemplateTest {

  @SuppressWarnings("unchecked")
  @SneakyThrows
  @Test
  public void test() {
    final var t =
        TemplateManager.generateTemplate(
            """
                {
                    \"applause:options\": {
                        \"runName\": \"runName\",
                        \"factoryKey\": \"factoryKey\",
                        \"other\": 123
                    }
                }
                """);
    DriverConfigTemplate template = new DriverConfigTemplate(t);
    template.refresh();
    List<CompletableFuture<EnhancedCapabilities>> futures = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
      futures.add(
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  return DriverBuilder.fromTemplate(template)
                      .overrideCaps(
                          caps -> {
                            MutableCapabilities result = new MutableCapabilities(caps);
                            Map<String, Object> applauseOptions =
                                Optional.ofNullable(
                                        result.getCapability(
                                            ApplauseCapabilitiesConstants.APPLAUSE_OPTIONS))
                                    .map(
                                        options -> {
                                          if (options instanceof Capabilities) {
                                            return ((Capabilities) options).asMap();
                                          }
                                          if (options instanceof Map) {
                                            return (Map<String, Object>) options;
                                          }
                                          return null;
                                        })
                                    .orElseGet(HashMap::new);
                            applauseOptions.put(ApplauseCapabilitiesConstants.API_KEY, "apiKey");
                            applauseOptions.put(
                                ApplauseCapabilitiesConstants.RUN_NAME, "providerRubName");
                            applauseOptions.put(ApplauseCapabilitiesConstants.PRODUCT_ID, "123");
                            result.setCapability(
                                ApplauseCapabilitiesConstants.APPLAUSE_OPTIONS, applauseOptions);
                            return result;
                          })
                      .getCaps();
                } catch (BadJsonFormatException e) {
                  return null;
                }
              }));
    }
    Assert.assertEquals(
        futures.stream()
            .map(CompletableFuture::join)
            .filter(Objects::nonNull)
            .map(c -> c.getApplauseOptions().getCapability("productId"))
            .filter(Objects::isNull)
            .count(),
        0);
  }
}
