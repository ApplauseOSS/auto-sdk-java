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
import java.util.Arrays;
import lombok.val;
import org.aeonbits.owner.Converter;

/** Makes enum config value matching case-insensitive!! */
public class AnyCaseEnumConverter implements Converter<Object> {

  @Override
  public Object convert(final Method targetMethod, final String text) {
    @SuppressWarnings("unchecked")
    val enumClazz = (Class<? extends Enum<?>>) targetMethod.getReturnType();
    if (!Enum.class.isAssignableFrom(enumClazz)) {
      throw new UnsupportedOperationException(
          "config value was '"
              + text
              + "'. Class "
              + enumClazz.getSimpleName()
              + " is not an Enum! Don't use this type converter!!!");
    }
    // gets enum constants, then compares them to config string value, then returns the "properly
    // cased" enum value string for conversion into actual enum below
    return Arrays.stream(enumClazz.getEnumConstants())
        .filter(
            e ->
                e.name().equalsIgnoreCase(text.trim())
                    || e.toString().equalsIgnoreCase(text.trim()))
        .findAny()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "config value '"
                        + text
                        + "' does not match a valid constant for enum"
                        + enumClazz.getSimpleName()));
  }
}
