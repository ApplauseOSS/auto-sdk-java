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
import org.aeonbits.owner.Converter;

/** Converts Strings to Longs, but accounts for the possibility of an empty or invalid String. */
public class EmptyLongConverter implements Converter<Object> {
  @Override
  public Object convert(final Method targetMethod, final String text) {
    if (targetMethod.getReturnType() != Long.class) {
      throw new UnsupportedOperationException(
          "EmptyLongConverter only supports Long type! Don't use this type converter!");
    }
    try {
      return Long.parseLong(text);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
