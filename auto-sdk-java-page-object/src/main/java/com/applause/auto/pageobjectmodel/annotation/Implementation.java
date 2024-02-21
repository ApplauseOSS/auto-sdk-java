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
import com.applause.auto.pageobjectmodel.base.BaseComponent;
import java.lang.annotation.*;

/**
 * Class annotation to mark variants of a component class for the PageObject builder. The
 * PageObjectBuilder accepts a class parameter, but in certain cases, the user might not know which
 * subclass they want to create until runtime. This annotation allows the user to explicitly
 * indicate that one class or another should be created on a particular Platform. This is a
 * repeatable annotation, allowing the user to specify multiple configurations, like
 * WEB_DESKTOP_CHROME and WEB_DESKTOP_SAFARI, if needed. if no Implementation is provided, the
 * PageObject builder will simply create an instance of the class it's passed.
 *
 * @see Platform
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(Implementations.class)
public @interface Implementation {
  /**
   * Pointer to a class. When the PageObjectBuilder creates an instance of the class this annotation
   * is applied to, it will check this annotation to see if there is a more appropriate subclass for
   * the current Platform.
   *
   * @return the class to create
   */
  Class<? extends BaseComponent> is();

  /**
   * Platform configuration for this annotation.
   *
   * @return the Platform configuration
   */
  Platform on();
}
