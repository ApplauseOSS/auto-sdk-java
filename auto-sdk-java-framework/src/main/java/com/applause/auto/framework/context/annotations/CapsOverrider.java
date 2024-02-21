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
package com.applause.auto.framework.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows for specification of a Capability Override function. It should be
 * annotated on a method in the same test class that will use the override. This is not a required
 * annotation, but will allow you to inject custom capabilities at runtime through Java code.
 *
 * <p>This expects that the function is static and will take in a single Capabilities object and
 * return a MutableCapabilities. IF the annotated function does not meet these criteria, it will not
 * be invoked.
 *
 * <p>The system does support multiple CapsOverride annotations in a single test class. To choose
 * which one should be used for a @Test, you can specify the @WithCapsOverride annotation with a
 * value matching the value provided in this annotation.
 *
 * @see WithDriver
 * @see WithCapsOverride
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CapsOverrider {

  /**
   * The key of the CapsOverrider, to be used in the @WithCapsOverride annotation
   *
   * @return the name of the caps overrider
   */
  String value();
}
