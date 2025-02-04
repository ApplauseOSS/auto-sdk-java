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
package com.applause.auto.helpers.analytics;

import com.applause.auto.helpers.AnalyticsHelper;
import com.applause.auto.pageobjectmodel.base.ComponentInterceptor;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * This interceptor is added to classes in the PageObjectFactory to facilitate running code before
 * and after methods. Currently, the only use case is for the @AnalyticsCall annotation.
 */
@SuppressWarnings("PMD.SignatureDeclareThrowsException") // since we're intercepting this is okay
public class AnalyticsInterceptor extends ComponentInterceptor {
  private final AnalyticsHelper analyticsHelper;

  /**
   * Constructs a new AnalyticsInterceptor.
   *
   * @param analyticsHelper The AnalyticsHelper instance to use for analytics reporting.
   */
  public AnalyticsInterceptor(final AnalyticsHelper analyticsHelper) {
    this.analyticsHelper = analyticsHelper;
  }

  /**
   * Methods in a page object class that meet the criteria in match() are subject to the extra logic
   * in intercept().
   *
   * @return whether element is annotated with Analytics
   */
  @Override
  public ElementMatcher<MethodDescription> match() {
    return ElementMatchers.isAnnotatedWith(AnalyticsCall.class);
  }

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
  @Override
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

  /** Runs before methods meeting the criteria in match(). */
  @Override
  public void before() {
    this.analyticsHelper.beforeMethodAssert();
  }

  /**
   * Runs after methods meeting the criteria in match().
   *
   * @param method the method that just ran
   */
  @Override
  public void after(final Method method) {
    this.analyticsHelper.afterMethodAssert(method.getAnnotation(AnalyticsCall.class));
  }
}
