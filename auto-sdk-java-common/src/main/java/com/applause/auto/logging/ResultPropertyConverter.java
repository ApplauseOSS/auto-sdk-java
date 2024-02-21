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
package com.applause.auto.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

/**
 * A Log4j2 plugin to enable a custom pattern converter. To use a framework configured property
 * inside the log messages, you can add %rp{propertyName} inside the layout configured in your
 * log4j2.properties. See below for a list of configurable variables for this pattern converter:
 *
 * <p>delimiter The delimiter used to separate multiple properties. Default " "
 *
 * <p>defaultString If the processed property string is empty, this will be the default value.
 * Default: emptyString
 *
 * <p>showKeys: Determines if the property keys should be displayed in the processed string. Default
 * false
 *
 * <p>filterNulls: Tells the converter whether to remove null property values from the string.
 * Default: false
 */
@Plugin(name = "resultProperty", category = "Converter")
@ConverterKeys({"rp", "resultProperty"})
public final class ResultPropertyConverter extends LogEventPatternConverter {
  private final List<String> properties;
  private final String delimiter;
  private final String defaultString;
  private final boolean showKeys;
  private final boolean filterNulls;

  /**
   * Private constructor.
   *
   * @param properties options, may be null.
   */
  @SuppressWarnings({
    "PMD.UnusedFormalParameter",
    "PMD.UseVarargs",
  })
  private ResultPropertyConverter(final String... properties) {
    super("ResultProperty", "resultProperty");
    this.properties = new ArrayList<>();

    String delimiterValue = " ";
    String defaultStringValue = "";
    boolean showKeysValue = false;
    boolean filterNullsValue = false;
    for (String property : properties) {
      if ("-showKeys".equals(property)) {
        showKeysValue = true;
      } else if ("-filterNulls".equals(property)) {
        filterNullsValue = true;
      } else if (property.startsWith("delimiter:")) {
        delimiterValue = property.substring(10);
      } else if (property.startsWith("default:")) {
        defaultStringValue = property.substring(8);
      } else {
        this.properties.add(property);
      }
    }
    this.delimiter = delimiterValue;
    this.defaultString = defaultStringValue;
    this.showKeys = showKeysValue;
    this.filterNulls = filterNullsValue;
  }

  /**
   * Obtains an instance of pattern converter.
   *
   * @param properties options, may be null.
   * @return instance of pattern converter.
   */
  @SuppressWarnings({
    "PMD.UnusedFormalParameter",
    "PMD.UseVarargs",
  })
  public static ResultPropertyConverter newInstance(final String... properties) {
    return new ResultPropertyConverter(properties);
  }

  @Override
  public void format(final LogEvent event, final StringBuilder toAppendTo) {
    String propertyString =
        properties.stream()
            .map(prop -> Pair.of(prop, ResultPropertyMap.getProperty(prop)))
            .filter(prop -> !this.filterNulls || Objects.nonNull(prop.getValue()))
            .map(prop -> (this.showKeys ? prop + "=" : "") + prop.getValue())
            .collect(Collectors.joining(delimiter));

    toAppendTo.append(propertyString.isEmpty() ? this.defaultString : propertyString);
  }
}
