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
package com.applause.auto.pageobjectmodel.base;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * An Interceptor for a Component, to allow hooks to be called before and after any methods matched
 * by this interceptor.
 */
@Log4j2
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public abstract class ComponentInterceptor {

  /**
   * Methods in a page object class that meet the criteria in match() are subject to the extra logic
   * in intercept().
   *
   * @return whether element is annotated with Analytics
   */
  public abstract ElementMatcher<MethodDescription> match();

  /** Runs before methods meeting the criteria in match(). */
  public abstract void before();

  /**
   * Runs after methods meeting the criteria in match().
   *
   * @param method the method that just ran
   */
  public abstract void after(Method method);

  /**
   * Wraps a method with calls to before() and after(). Don't mess with this unless you know what //
   * you're doing.
   *
   * @param callable the original method
   * @param method call stack origin
   * @param args method args
   * @return the call's original return value
   * @throws Exception if original method does
   */
  @RuntimeType
  public Object intercept(
      final @SuperCall Callable<?> callable,
      final @Origin Method method,
      final @AllArguments Object... args)
      throws Exception {
    before();
    Object returnable = callable.call();
    after(method);
    return returnable;
  }

  /**
   * Overrides a given class by wrapping it using ByteBuddy
   *
   * @param <T> The type of class to override
   * @param clazz The class to override
   * @return The overwritten class
   */
  public <T> Class<? extends T> overrideClass(final @NonNull Class<T> clazz) {
    final var byteBuddyInterceptor =
        new ByteBuddy().subclass(clazz).method(this.match()).intercept(MethodDelegation.to(this));
    try (var unloaded = byteBuddyInterceptor.make()) {
      return unloaded.load(Thread.currentThread().getContextClassLoader()).getLoaded();
    }
  }
}
