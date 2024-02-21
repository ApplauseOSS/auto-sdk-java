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

import java.lang.annotation.*;

/**
 * This annotation allows for you to choose between multiple CapsOverrider functions in a single
 * Test Class. Leaving the value of this annotation blank will choose the fist @CapsOverrider
 * function that it encounters.
 *
 * <p>If there are multiple CapsOverrider functions, then there will be a warning logger to the
 * console explaining and the first match will be selected. In these cases, a more specific value
 * should be provided for this annotation.
 *
 * @see CapsOverrider
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface WithCapsOverride {

  /**
   * The key used in the @CapsOverrider annotation
   *
   * @return the name of the caps overrider to use
   */
  String value() default "";
}
