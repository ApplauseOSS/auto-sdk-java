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
 * This annotation specifies that the chosen target is a Driverless test. The context manager will
 * automatically generate a DriverlessContext object for you.
 *
 * <p>If this annotation is provided on the Class level, it will be used as the default for
 * all @Tests in the class unless an individual Test has another context annotation on it
 *
 * @see WithDriver
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Driverless {}
