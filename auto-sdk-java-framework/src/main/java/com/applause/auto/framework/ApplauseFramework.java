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
import com.applause.auto.framework.json.BadJsonFormatException;
import com.applause.auto.framework.templates.DriverConfigTemplateHelper;
import com.applause.auto.templates.TemplateManager.TemplateGenerationException;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import okhttp3.HttpUrl;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

/** Singleton */
@Log4j2
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public enum ApplauseFramework {
  /** The Instance of the framework configuration */
  INSTANCE;

  /**
   * Registers a Framework Extension
   *
   * @param <T> The type of the extension
   * @param clazz The Extension Class
   * @param extensionInitializer A Function that provides the extension for the context
   * @return The Applause Framework Instance
   */
  public <T extends IFrameworkExtension> ApplauseFramework registerFrameworkExtension(
      final Class<T> clazz, final Function<FrameworkContext, T> extensionInitializer) {
    ContextManager.INSTANCE.registerFrameworkExtension(clazz, extensionInitializer);
    return this;
  }

  /**
   * Registers a Page Object Extension
   *
   * @param <T> The type of the extension
   * @param clazz The Extension Class
   * @param extensionInitializer A Function that provides the extension for the context
   * @return The Applause Framework Instance
   */
  public <T extends IPageObjectExtension> ApplauseFramework registerPageObjectExtension(
      final Class<T> clazz, final Function<IPageObjectContext, T> extensionInitializer) {
    ContextManager.INSTANCE.registerPageObjectExtension(clazz, extensionInitializer);
    return this;
  }

  /**
   * Registers a Capability Overrider
   *
   * @param <T> The type of the extension
   * @param capsOverrider The Capability Overrider Function
   * @return The Applause Framework Instance
   */
  public <T extends IPageObjectExtension> ApplauseFramework registerCapabilityOverrider(
      final Function<Capabilities, MutableCapabilities> capsOverrider) {
    ContextManager.INSTANCE.registerCapabilityOverrider(capsOverrider);
    return this;
  }

  /**
   * Registers a Single Driver to the Framework
   *
   * @param driverFilePath The path to the driver
   * @return The Applause Framework Instance
   */
  public ApplauseFramework registerSingleDriver(final @NonNull String driverFilePath) {
    return registerSingleDriver(Path.of(driverFilePath));
  }

  /**
   * Registers a Single Driver to the Framework
   *
   * @param driverFilePath The path to the driver
   * @return The Applause Framework Instance
   */
  public ApplauseFramework registerSingleDriver(final @NonNull URI driverFilePath) {
    return registerSingleDriver(Path.of(driverFilePath));
  }

  /**
   * Registers a Single Driver to the Framework
   *
   * @param driverFilePath The path to the driver
   * @return The Applause Framework Instance
   */
  public ApplauseFramework registerSingleDriver(final @NonNull Path driverFilePath) {
    return registerSingleDriver(driverFilePath.toFile());
  }

  /**
   * Registers a Single Driver to the Framework
   *
   * @param driverFile The driver config File
   * @return The Applause Framework Instance
   */
  public ApplauseFramework registerSingleDriver(final @NonNull File driverFile) {
    return registerDrivers(List.of(driverFile));
  }

  /**
   * Registers all drivers under the provided path
   *
   * @param driverDirectoryPath The path to the drivers
   * @return The Applause Framework Instance
   */
  public ApplauseFramework registerDrivers(final @NonNull String driverDirectoryPath) {
    return registerDrivers(Path.of(driverDirectoryPath));
  }

  /**
   * Registers all drivers under the provided path
   *
   * @param driverDirectoryPath The path to the drivers
   * @return The Applause Framework Instance
   */
  public ApplauseFramework registerDrivers(final @NonNull URI driverDirectoryPath) {
    return registerDrivers(Path.of(driverDirectoryPath));
  }

  /**
   * Registers all drivers under the provided path
   *
   * @param driverDirectoryPath The path to the drivers
   * @return The Applause Framework Instance
   */
  public ApplauseFramework registerDrivers(final @NonNull Path driverDirectoryPath) {
    return this.registerDrivers(driverDirectoryPath.toFile());
  }

  /**
   * Registers all drivers under the provided path
   *
   * @param driverDirectoryPath The path to the drivers
   * @return The Applause Framework Instance
   */
  public ApplauseFramework registerDrivers(final @NonNull File driverDirectoryPath) {
    if (!driverDirectoryPath.exists()) {
      throw new RuntimeException(
          "Cannot register drivers at: "
              + driverDirectoryPath.getPath()
              + ". Provided path does not exist.");
    }
    if (!driverDirectoryPath.isDirectory()) {
      throw new RuntimeException(
          "Cannot register drivers at: "
              + driverDirectoryPath.getPath()
              + ". Provided path is not a directory.");
    }
    return registerDrivers(listFiles(driverDirectoryPath));
  }

  /**
   * Registers all provided drivers
   *
   * @param driverFiles A Collection of driver files
   * @return The Applause Framework Instance
   */
  public ApplauseFramework registerDrivers(final @NonNull Collection<File> driverFiles) {
    final var driverMap = ContextManager.INSTANCE.getDriverMap();
    for (final File driverFile : driverFiles) {
      if (!driverFile.exists()) {
        throw new RuntimeException(
            "Cannot register driver: " + driverFile.getName() + ". Path does not exist.");
      }
      if (driverFile.isDirectory()) {
        throw new RuntimeException(
            "Cannot register driver: " + driverFile.getName() + ". Path points to a directory.");
      }
      if (driverMap.containsKey(driverFile.getName())) {
        log.debug(
            "Driver at path {} already registered. Overwriting existing template.",
            driverFile.getName());
      }
      try {
        driverMap.put(
            driverFile.getName(), DriverConfigTemplateHelper.makeCapabilities(driverFile));
      } catch (BadJsonFormatException | TemplateGenerationException | IOException e) {
        log.error("Could not convert path " + driverFile.getName(), e);
      }
    }
    return this;
  }

  /**
   * Sets the default driver config name
   *
   * @param driver The name of the driver
   * @return The Applause Framework Instance
   */
  public ApplauseFramework setDefaultDriver(final @Nullable String driver) {
    ContextManager.INSTANCE.setDefaultDriverConfig(driver);
    return this;
  }

  /**
   * Sets the driver manager
   *
   * @param driverManager The Driver Manager instance
   * @return The Applause Framework Instance
   */
  public ApplauseFramework setDriverManager(final @NonNull AbstractDriverManager driverManager) {
    ContextManager.INSTANCE.setDriverManager(driverManager);
    return this;
  }

  /**
   * Uses a LocalDriverManager as the default driver manager
   *
   * @param localAppiumUrl The appium URL
   * @return The Applause Framework Instance
   * @throws MalformedURLException If the localAppiumUrl is provided and invalid
   * @throws URISyntaxException If the localAppiumUrl is provided and invalid
   * @see LocalDriverManager for information on how drivers are created for this setting
   */
  public ApplauseFramework useLocalDrivers(final @Nullable String localAppiumUrl)
      throws MalformedURLException, URISyntaxException {
    if (localAppiumUrl == null) {
      return this.setDriverManager(new LocalDriverManager(null));
    }
    return this.setDriverManager(new LocalDriverManager(new URI(localAppiumUrl).toURL()));
  }

  /**
   * Uses a LocalDriverManager as the default driver manager
   *
   * @param localAppiumUrl The appium URL
   * @return The Applause Framework Instance
   * @see LocalDriverManager for information on how drivers are created for this setting
   */
  public ApplauseFramework useLocalDrivers(final @Nullable URL localAppiumUrl) {
    return this.setDriverManager(new LocalDriverManager(localAppiumUrl));
  }

  /**
   * Uses a RemoteDriverManager as the default driver manager
   *
   * @param seleniumGridUrl The appium URL
   * @return The Applause Framework Instance
   * @throws MalformedURLException If the seleniumGridUrl is provided and invalid
   * @throws URISyntaxException If the seleniumGridUrl is provided and invalid
   * @see RemoteDriverManager for information on how drivers are created for this setting
   */
  public ApplauseFramework pointToSeleniumGrid(final @NonNull String seleniumGridUrl)
      throws MalformedURLException, URISyntaxException {
    return pointToSeleniumGrid(new URI(seleniumGridUrl).toURL());
  }

  /**
   * Uses a RemoteDriverManager as the default driver manager
   *
   * @param seleniumGridUrl The appium URL
   * @param username The appium URL
   * @param password The appium URL
   * @return The Applause Framework Instance
   * @throws MalformedURLException If the seleniumGridUrl is provided and invalid
   * @throws URISyntaxException If the seleniumGridUrl is provided and invalid
   * @see RemoteDriverManager for information on how drivers are created for this setting
   */
  public ApplauseFramework pointToSeleniumGrid(
      final @NonNull String seleniumGridUrl,
      final @NonNull String username,
      final @NonNull String password)
      throws MalformedURLException, URISyntaxException {
    return pointToSeleniumGrid(new URI(seleniumGridUrl).toURL(), username, password);
  }

  /**
   * Uses a RemoteDriverManager as the default driver manager
   *
   * @param seleniumGridUrl The appium URL
   * @return The Applause Framework Instance
   * @see RemoteDriverManager for information on how drivers are created for this setting
   */
  public ApplauseFramework pointToSeleniumGrid(final @NonNull URL seleniumGridUrl) {
    return this.setDriverManager(new RemoteDriverManager(seleniumGridUrl));
  }

  /**
   * Uses a RemoteDriverManager as the default driver manager
   *
   * @param seleniumGridUrl The appium URL
   * @param username The appium URL
   * @param password The appium URL
   * @return The Applause Framework Instance
   * @see RemoteDriverManager for information on how drivers are created for this setting
   */
  public ApplauseFramework pointToSeleniumGrid(
      final @NonNull URL seleniumGridUrl,
      final @NonNull String username,
      final @NonNull String password) {
    // Strip the leading / if it exists in the path
    String path = seleniumGridUrl.getPath();
    if (path.charAt(0) == '/') {
      path = path.substring(1);
    }
    return this.setDriverManager(
        new RemoteDriverManager(
            new HttpUrl.Builder()
                .scheme(seleniumGridUrl.getProtocol())
                .addPathSegments(path)
                .host(seleniumGridUrl.getHost())
                .port(seleniumGridUrl.getPort())
                .username(username)
                .password(password)
                .build()
                .url()));
  }

  /**
   * Sets the default timeout for all new contexts
   *
   * @param timeout The timeout duration
   * @return The Applause Framework Instance
   */
  public ApplauseFramework setDefaultTimeout(final @NonNull Duration timeout) {
    ContextManager.INSTANCE.setDefaultTimeout(timeout);
    return this;
  }

  /**
   * Sets the default polling interval for all new contexts
   *
   * @param pollingInterval The polling interval
   * @return The Applause Framework Instance
   */
  public ApplauseFramework setDefaultPollingInterval(final @NonNull Duration pollingInterval) {
    ContextManager.INSTANCE.setDefaultPollingInterval(pollingInterval);
    return this;
  }

  /**
   * Sets the output path for any captured assets
   *
   * @param outputPath The output path as a FreeMarker template string
   * @return The Applause Framework Instance
   * @throws TemplateGenerationException If the template string is invalid
   */
  public ApplauseFramework setOutputPath(final @NonNull String outputPath)
      throws TemplateGenerationException {
    ContextManager.INSTANCE.setOutputPathTemplate(outputPath);
    return this;
  }

  /**
   * Lists all files within a given directory
   *
   * @param f the file (or directory) to list
   * @return a list of files relative to the given directory
   */
  private static List<File> listFiles(final @NonNull File f) {
    if (!f.isDirectory()) {
      return List.of();
    }
    return Lists.newArrayList(FileUtils.iterateFiles(f, new String[] {"json"}, true));
  }
}
