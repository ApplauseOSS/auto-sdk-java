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
package com.applause.auto.pageobjectmodel.factory;

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.pageobjectmodel.base.BaseComponent;
import com.applause.auto.pageobjectmodel.base.BaseElement;
import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;
import com.google.common.reflect.TypeToken;
import javax.annotation.Nullable;
import lombok.NonNull;

/**
 * Creates instances of classes extending BaseComponent, filling in fields annotated with @Locate.
 * Wraps methods annotated with @AnalyticsCall with a set of checks of the network traffic that
 * transpired during the execution of those methods.
 *
 * @see BaseComponent
 * @see BaseElement
 * @see LazyWebElement
 * @see LazyList
 */
@Deprecated()
public enum ComponentFactory {
  ;

  /**
   * Creates a new instance of the specified component class with no parent. Creates new instances
   * for all the subcomponent and element fields in that class that are annotated with @Locate.
   *
   * @param clazz a component class
   * @param <T> must extend BaseComponent
   * @param context the underlying context to use
   * @param args constructor arguments for the class - may be left unspecified to use the default
   *     no-args constructor
   * @return a new instance of the component class T
   * @deprecated Use PageObjectBuilder
   */
  @Deprecated
  public static <T extends BaseComponent> T create(
      final @NonNull Class<T> clazz,
      final @NonNull IPageObjectContext context,
      final Object... args) {
    return createWithParent(clazz, null, context, args);
  }

  /**
   * Creates a new instance of the specified component class with no parent. Creates new instances
   * for all the subcomponent and element fields in that class that are annotated with @Locate.
   *
   * @param typeToken a component class
   * @param <T> must extend BaseComponent
   * @param context the underlying context to use
   * @param args constructor arguments for the class - may be left unspecified to use the default
   *     no-args constructor
   * @return a new instance of the component class T
   * @deprecated Use PageObjectBuilder
   */
  @Deprecated
  public static <T extends BaseComponent> T create(
      final @NonNull TypeToken<T> typeToken,
      final @NonNull IPageObjectContext context,
      final Object... args) {
    return createWithParent(typeToken, null, context, args);
  }

  /**
   * Creates a new instance of the specified component class with a parent element. Creates new
   * instances for all the subcomponent and element fields in that class that are annotated
   * with @Locate, which will be found in the DOM relative to the parent element when they're used.
   *
   * @param clazz a component class
   * @param <T> must extend BaseComponent
   * @param parent the BaseElement parent to all elements and components in the class
   * @param context the underlying context to use
   * @param args constructor arguments for the class - may be left unspecified to use the default
   *     no-args constructor
   * @return a new instance of the component class T
   * @deprecated Use PageObjectBuilder
   */
  @Deprecated
  public static <T extends BaseComponent> T createWithParent(
      final @NonNull Class<T> clazz,
      final @Nullable BaseElement parent,
      final @NonNull IPageObjectContext context,
      final Object... args) {
    return createWithParent(TypeToken.of(clazz), parent, context, args);
  }

  /**
   * Creates a new instance of the specified component class with a parent element. Creates new
   * instances for all the subcomponent and element fields in that class that are annotated
   * with @Locate, which will be found in the DOM relative to the parent element when they're used.
   *
   * @param typeToken a component class
   * @param <T> must extend BaseComponent
   * @param parent the BaseElement parent to all elements and components in the class
   * @param context the underlying context to use
   * @param args constructor arguments for the class - may be left unspecified to use the default
   *     no-args constructor
   * @return a new instance of the component class T
   * @deprecated Use PageObjectBuilder
   */
  @Deprecated
  public static <T extends BaseComponent> T createWithParent(
      final @NonNull TypeToken<T> typeToken,
      final @Nullable BaseElement parent,
      final @NonNull IPageObjectContext context,
      final Object... args) {
    return PageObjectBuilder.withContext(context)
        .forBaseComponent(typeToken)
        .withFormat(args)
        .initialize(parent);
  }
}
