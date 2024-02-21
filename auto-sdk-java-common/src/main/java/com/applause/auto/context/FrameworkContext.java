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

import com.applause.auto.logging.ResultPropertyMap;
import com.applause.auto.templates.TemplateManager;
import com.google.common.base.Suppliers;
import freemarker.template.Template;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.*;
import org.openqa.selenium.WebDriver;

/** The Applause Framework Implementation */
@AllArgsConstructor
@Data
@RequiredArgsConstructor
public class FrameworkContext {
  @Setter(value = AccessLevel.NONE)
  private final String contextId = UUID.randomUUID().toString();

  @NonNull private Template outputPathTemplate;
  @Nullable private IPageObjectContext pageObjectContext;
  @NonNull private IContextConnector<?> connector = new EmptyContextConnector();

  // Context extensions allow us to provide additional functionality that is tied to
  @NonNull
  private Map<Class<? extends IContextExtension>, Supplier<? extends IContextExtension>>
      extensionSuppliers = new HashMap<>();

  /**
   * Gets the current PageObjectContext wrapped in an Optional
   *
   * @return The Optional PageObjectContext
   */
  public Optional<IPageObjectContext> getPageObjectContext() {
    return Optional.ofNullable(pageObjectContext);
  }

  /**
   * Gets the current WebDriver from the PageObjectContext, wrapped in an Optional
   *
   * @return The Optional WebDriver
   */
  public Optional<WebDriver> getDriver() {
    return Optional.ofNullable(pageObjectContext).map(IPageObjectContext::getDriver);
  }

  /**
   * Gets the output path after evaluating the Freemarker Template
   *
   * @return The Evaluated Template
   * @throws TemplateManager.TemplateProcessException If the output path template could not be
   *     processed
   */
  public Path getOutputPath() throws TemplateManager.TemplateProcessException {
    return Path.of(TemplateManager.process(outputPathTemplate, ResultPropertyMap.getProperties()));
  }

  /**
   * Sets the output path template
   *
   * @param outputPathTemplate The output path template as a String
   * @throws TemplateManager.TemplateGenerationException If a template could not be generated from
   *     the provided String
   */
  public void setOutputPathTemplate(final @NonNull String outputPathTemplate)
      throws TemplateManager.TemplateGenerationException {
    // validate that the template with placeholders removed is a valid directory
    this.outputPathTemplate = TemplateManager.generateTemplate(outputPathTemplate);
  }

  /**
   * Registers a Framework Extension
   *
   * @param clazz The Framework Extension Class
   * @param extensionInitializer A function that produces the Extension for the context
   */
  public void registerFrameworkExtension(
      final Class<? extends IFrameworkExtension> clazz,
      final Function<FrameworkContext, ? extends IFrameworkExtension> extensionInitializer) {
    extensionSuppliers.put(clazz, Suppliers.memoize(() -> extensionInitializer.apply(this)));
  }

  /**
   * Registers a PageObjectExtension
   *
   * @param clazz The PageObjectExtension Class
   * @param extensionInitializer A function that produces the Extension for the context
   */
  public void registerPageObjectExtension(
      final Class<? extends IPageObjectExtension> clazz,
      final Function<IPageObjectContext, ? extends IPageObjectExtension> extensionInitializer) {
    extensionSuppliers.put(
        clazz, Suppliers.memoize(() -> extensionInitializer.apply(this.pageObjectContext)));
  }

  /**
   * Gets the extension instance for this context
   *
   * @param <T> The type of Context Extension
   * @param clazz The Context Extension Class
   * @return The Context Extension instance
   */
  @SuppressWarnings("unchecked")
  public <T extends IContextExtension> T getExtension(final Class<T> clazz) {
    if (!extensionSuppliers.containsKey(clazz)) {
      throw new RuntimeException(
          "Extension " + clazz.getSimpleName() + " has not been registered to the context");
    }
    // This will return the same extension
    return (T) extensionSuppliers.get(clazz).get();
  }
}
