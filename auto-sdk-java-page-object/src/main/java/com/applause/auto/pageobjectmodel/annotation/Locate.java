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
package com.applause.auto.pageobjectmodel.annotation;

import com.applause.auto.data.enums.Platform;
import com.applause.auto.pageobjectmodel.enums.Strategy;
import java.lang.annotation.*;

/**
 * Field annotation to mark elements and subcomponents in a component for the PageObjectFactory to
 * create. Contains locator information pointing to the corresponding element in the DOM. If an
 * element is annotated with Locate, that element will be created with a corresponding locator. If a
 * component is annotated with Locate, it will be created with a parent with a corresponding
 * locator; This is a repeatable annotation, allowing the user to specify multiple configurations,
 * like WEB_DESKTOP_CHROME and WEB_DESKTOP_SAFARI, if needed.
 *
 * @see Platform
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(Locates.class)
public @interface Locate {
  /**
   * Platform configuration for this annotation.
   *
   * @return the Platform configuration
   */
  Platform on() default Platform.DEFAULT;

  /**
   * The type of locator strategy to use
   *
   * @return The locator strategy to use for locating this element
   */
  Strategy using();

  /**
   * The Locator string
   *
   * @return The locator string
   */
  String value();

  /**
   * Signals whether the located element is a shadow root element
   *
   * @return True, if the element is a shadow root
   */
  boolean shadowRoot() default false;
}
