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
package com.applause.auto.config;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import org.aeonbits.owner.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Owner Config convertor to process a string parameter into a LocalDateTime */
public class DateConverter implements Converter<LocalDateTime> {

  private static final Logger logger = LogManager.getLogger(DateConverter.class);

  @Override
  public LocalDateTime convert(final Method targetMethod, final String text) {
    if (targetMethod.getReturnType() != LocalDateTime.class) {
      throw new UnsupportedOperationException("Date Converter only supports the LocalDate type");
    }
    if (text == null) {
      return null;
    }

    LocalDateTime date = null;

    try {
      date = LocalDateTime.parse(text);
    } catch (DateTimeParseException e) {
      logger.error("Exception Parsing Date String " + text, e);
    }

    return date;
  }
}
