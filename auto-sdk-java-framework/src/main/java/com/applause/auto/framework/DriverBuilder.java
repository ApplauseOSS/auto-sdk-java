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

import com.applause.auto.framework.json.BadJsonFormatException;
import com.applause.auto.framework.selenium.EnhancedCapabilities;
import com.applause.auto.framework.templates.DriverConfigTemplate;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;

/**
 * Builder class to convert the Applause Framework EnhancedCapabilities into a Selenium/Appium
 * WebDriver
 */
@Getter
public class DriverBuilder {
  private EnhancedCapabilities caps;

  protected DriverBuilder(final EnhancedCapabilities caps) {
    this.caps = caps;
    for (final var overrider : ContextManager.INSTANCE.getCapabilityOverriders()) {
      this.overrideCaps(overrider);
    }
  }

  /**
   * Sets up a new DriverBuilder from a driver registered with the ApplauseFramework
   *
   * @param driverName The short name of the driver to create
   * @return The newly created DriverBuilder
   * @throws BadJsonFormatException If the driver is not found, or the capability template does not
   *     resolve
   */
  public static DriverBuilder create(final @NonNull String driverName)
      throws BadJsonFormatException {
    return fromTemplate(ContextManager.INSTANCE.lookupDriver(driverName));
  }

  /**
   * Sets up a new DriverBuilder with a set of capabilities
   *
   * @param caps The capabilities to set the driver up with
   * @return The newly created DriverBuilder
   */
  public static DriverBuilder withBaseCapabilities(final @NonNull Capabilities caps) {
    return new DriverBuilder(new EnhancedCapabilities(caps));
  }

  /**
   * Sets up a new driver builder from a given template.
   *
   * @param template The capability template for the driver
   * @return The newly created DriverBuilder
   * @throws BadJsonFormatException If the capabilities do not resolve correctly
   */
  public static DriverBuilder fromTemplate(final @NonNull DriverConfigTemplate template)
      throws BadJsonFormatException {
    return new DriverBuilder(template.reProcess().getCurrentCapabilities());
  }

  /**
   * Overrides the current capabilities. If the override function returns null, the current
   * capabilities will be used.
   *
   * @param capsOverrider The capabilities overrider function
   * @return The DriverBuilder with the overridden capabilities
   */
  public final DriverBuilder overrideCaps(
      final @Nullable Function<Capabilities, MutableCapabilities> capsOverrider) {
    if (Objects.isNull(capsOverrider)) {
      return this;
    }
    // Overrides the caps. If the function returns null, it indicates no changes are necessary.
    final Capabilities result =
        Optional.ofNullable((Capabilities) capsOverrider.apply(this.caps)).orElse(this.caps);
    this.caps = new EnhancedCapabilities(result);
    return this;
  }

  /**
   * Builds the driver with the generated capabilities
   *
   * @param driverManager The DriverManager to build the driver with
   * @return The built WebDriver
   */
  public WebDriver build(final @NonNull AbstractDriverManager driverManager) {
    return driverManager.getDriver(this.caps);
  }

  /**
   * Builds the driver with the generated capabilities
   *
   * @param driverManager The DriverManager to build the driver with
   * @return The built WebDriver
   */
  public WebDriver buildAndRegister(final @NonNull AbstractDriverManager driverManager) {
    final var driver = driverManager.getDriver(this.caps);
    ContextManager.INSTANCE.registerDriverToCurrentContext(driver);
    return driver;
  }

  /**
   * Maps the driver builder to another object
   *
   * @param <T> The type to map the DriverBuilder to
   * @param mapper The mapping function
   * @return The mapped object
   */
  public <T> T map(final @NonNull Function<DriverBuilder, T> mapper) {
    return mapper.apply(this);
  }
}
