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
package com.applause.auto.framework;

import com.applause.auto.context.FrameworkContext;
import com.applause.auto.context.IFrameworkExtension;
import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.context.IPageObjectExtension;
import com.applause.auto.framework.templates.DriverConfigTemplate;
import com.applause.auto.templates.TemplateManager;
import com.google.common.collect.Sets;
import freemarker.template.Template;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

/** Singleton class to maintain the current primary context */
public final class ContextManager {

  /** The singleton instance of the ContextManager */
  public static final ContextManager INSTANCE = new ContextManager();

  private static final Logger logger = LogManager.getLogger();

  @Getter private final ThreadLocal<FrameworkContext> threadContext = new ThreadLocal<>();

  @Getter
  private final ConcurrentHashMap<String, Set<String>> resultDriverMap = new ConcurrentHashMap<>();

  @Getter
  private final ConcurrentHashMap<String, DriverConfigTemplate> driverMap =
      new ConcurrentHashMap<>();

  @Getter
  private final ConcurrentHashMap<
          Class<? extends IFrameworkExtension>,
          Function<FrameworkContext, ? extends IFrameworkExtension>>
      frameworkExtensionSuppliers = new ConcurrentHashMap<>();

  @Getter
  private final ConcurrentHashMap<
          Class<? extends IPageObjectExtension>,
          Function<IPageObjectContext, ? extends IPageObjectExtension>>
      pageObjectExtensionSuppliers = new ConcurrentHashMap<>();

  @Getter
  private final List<Function<Capabilities, MutableCapabilities>> capabilityOverriders =
      Collections.synchronizedList(new ArrayList<>());

  private final AtomicReference<AbstractDriverManager> driverManager = new AtomicReference<>();
  private final AtomicReference<String> defaultDriverConfig = new AtomicReference<>();
  private final AtomicReference<Template> outputPathTemplate;

  private final AtomicReference<Duration> defaultTimeout =
      new AtomicReference<>(Duration.ofSeconds(10));
  private final AtomicReference<Duration> defaultPollingInterval =
      new AtomicReference<>(Duration.ofSeconds(1));

  /** package private for testability * */
  ContextManager() {
    try {
      outputPathTemplate =
          new AtomicReference<>(
              TemplateManager.generateTemplate(System.getProperty("java.io.tmpdir")));
    } catch (TemplateManager.TemplateGenerationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets the current default DriverManager
   *
   * @return The default DriverManager
   */
  public AbstractDriverManager getDriverManager() {
    return driverManager.get();
  }

  /**
   * Gets the default driver config name
   *
   * @return The default driver config name
   */
  public String getDefaultDriverConfig() {
    return defaultDriverConfig.get();
  }

  /**
   * Sets the default driver manager
   *
   * @param driverManager The default driver manager
   */
  public void setDriverManager(final @NonNull AbstractDriverManager driverManager) {
    this.driverManager.set(driverManager);
  }

  /**
   * Sets the default driver config
   *
   * @param defaultDriverConfig The default driver config name
   */
  public void setDefaultDriverConfig(final @Nullable String defaultDriverConfig) {
    this.defaultDriverConfig.set(defaultDriverConfig);
  }

  /**
   * Overrides an existing context
   *
   * @param context The context to set
   * @return The previous context for the thread.
   */
  public Optional<FrameworkContext> overrideContext(final @NonNull FrameworkContext context) {
    final var oldContext = getCurrentContext();
    if (oldContext.isPresent()) {
      logger.warn(
          "Overriding existing context - this may cause test failures and/or asset issues if used improperly");
    }
    threadContext.set(context);
    return oldContext;
  }

  /**
   * Gets the current context if one is set up
   *
   * @return The current context for the thread, if present
   */
  public Optional<FrameworkContext> getCurrentContext() {
    return Optional.ofNullable(threadContext.get());
  }

  /**
   * Looks up the template for a driverName
   *
   * @param driverName The name of the driver to look up
   * @return The template matching that driver name
   */
  public DriverConfigTemplate lookupDriver(final String driverName) {

    // We are requiring a driver, make sure that a driver was passed in here
    if (Objects.isNull(driverName)) {
      throw new RuntimeException("No driver found for context");
    }

    // Verify that we have a driver with the given name
    if (!driverMap.containsKey(driverName)) {
      throw new RuntimeException("Missing Driver With Key: " + driverName);
    }

    // Now, reprocess the driver
    return driverMap.get(driverName);
  }

  /**
   * Gets the provider session ids from the given context id
   *
   * @param contextId The id of the context
   * @return The set of provider session guids
   */
  public Set<String> getProviderSessionIdsForContext(final String contextId) {
    if (contextId == null) {
      return new HashSet<>();
    }
    if (this.driverManager.get() instanceof LocalDriverManager) {
      return new HashSet<>();
    }
    return Optional.ofNullable(resultDriverMap.get(contextId)).orElseGet(HashSet::new);
  }

  /**
   * Registers that a test result was run in the current context
   *
   * @param driver The Selenium Driver
   */
  public void registerDriverToCurrentContext(@NonNull final WebDriver driver) {
    final var currentContext = this.threadContext.get();
    if (this.threadContext.get() != null) {
      this.registerDriver(driver, currentContext);
    } else {
      logger.debug("Cannot register driver to current context - no context present");
    }
  }

  /**
   * Registers that a test result was run in the current context
   *
   * @param driver The Selenium Driver
   * @param context The context
   */
  public void registerDriver(
      @NonNull final WebDriver driver, @NonNull final FrameworkContext context) {
    if (!(driver instanceof RemoteWebDriver)) {
      logger.debug("Cannot register non-remote drivers");
      return;
    }

    if (Objects.isNull(context.getContextId())) {
      throw new RuntimeException("Cannot register driver when no context set up");
    }

    // Make sure a map is set up for this context
    resultDriverMap.putIfAbsent(context.getContextId(), Sets.newConcurrentHashSet());

    // And finally add the driver session id to the context
    resultDriverMap
        .get(context.getContextId())
        .add(((RemoteWebDriver) driver).getSessionId().toString());
  }

  /** Resets the current context, quitting the driver if necessary */
  public void resetContext() {
    Optional.ofNullable(threadContext.get())
        .flatMap(FrameworkContext::getDriver)
        .ifPresent(driver -> getDriverManager().quitDriver(driver));
    this.detachContext();
  }

  /** Detaches the main context for the thread */
  public void detachContext() {
    threadContext.remove();
  }

  /**
   * Gets the current output path template
   *
   * @return The output path template
   */
  public Template getOutputPathTemplate() {
    return outputPathTemplate.get();
  }

  /**
   * Sets the current output path template
   *
   * @param outputPathTemplate The output path template, as a String
   * @throws TemplateManager.TemplateGenerationException If the template string cannot be parsed
   */
  public void setOutputPathTemplate(final @NonNull String outputPathTemplate)
      throws TemplateManager.TemplateGenerationException {
    this.outputPathTemplate.set(TemplateManager.generateTemplate(outputPathTemplate));
  }

  /**
   * Gets the default timeout
   *
   * @return The default timeout duration
   */
  public Duration getDefaultTimeout() {
    return defaultTimeout.get();
  }

  /**
   * Sets the default timeout duration
   *
   * @param newTimeout The new timeout duration
   */
  public void setDefaultTimeout(final @NonNull Duration newTimeout) {
    this.defaultTimeout.set(newTimeout);
  }

  /**
   * Gets the default polling interval
   *
   * @return The default polling interval
   */
  public Duration getDefaultPollingInterval() {
    return defaultPollingInterval.get();
  }

  /**
   * Sets the default polling interval
   *
   * @param newPollingInterval The new default polling interval
   */
  public void setDefaultPollingInterval(final @NonNull Duration newPollingInterval) {
    this.defaultPollingInterval.set(newPollingInterval);
  }

  /**
   * Registers a new Framework Extension
   *
   * @param clazz The Framework Extension Class
   * @param extensionInitializer A function to generate a new extension from a given context
   * @param <T> The type of extension to register
   */
  public <T extends IFrameworkExtension> void registerFrameworkExtension(
      final Class<T> clazz, final Function<FrameworkContext, T> extensionInitializer) {
    frameworkExtensionSuppliers.put(clazz, extensionInitializer);
  }

  /**
   * Registers a new PageObject Extension
   *
   * @param clazz The PageObject Extension Class
   * @param extensionInitializer A function to generate a new extension from a given context
   * @param <T> The type of extension to register
   */
  public <T extends IPageObjectExtension> void registerPageObjectExtension(
      final Class<T> clazz, final Function<IPageObjectContext, T> extensionInitializer) {
    pageObjectExtensionSuppliers.put(clazz, extensionInitializer);
  }

  /**
   * Registers a new Capability Overrider
   *
   * @param capsOverrider The capability overrider function
   */
  public void registerCapabilityOverrider(
      final Function<Capabilities, MutableCapabilities> capsOverrider) {
    this.capabilityOverriders.add(capsOverrider);
  }
}
