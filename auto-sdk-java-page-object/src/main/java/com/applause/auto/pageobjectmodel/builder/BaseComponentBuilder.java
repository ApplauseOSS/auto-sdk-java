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
package com.applause.auto.pageobjectmodel.builder;

import com.applause.auto.context.IPageObjectContext;
import com.applause.auto.pageobjectmodel.annotation.ImplementationHelper;
import com.applause.auto.pageobjectmodel.annotation.LocatorHelper;
import com.applause.auto.pageobjectmodel.annotation.SubComponent;
import com.applause.auto.pageobjectmodel.base.BaseComponent;
import com.applause.auto.pageobjectmodel.base.BaseElement;
import com.applause.auto.pageobjectmodel.base.ComponentInterceptor;
import com.applause.auto.pageobjectmodel.base.UIElement;
import com.applause.auto.pageobjectmodel.factory.LazyList;
import com.applause.auto.pageobjectmodel.factory.LazyWebElement;
import com.applause.auto.pageobjectmodel.factory.Locator;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebElement;

/**
 * A Builder Class for constructing different BaseComponent implementations
 *
 * @param <T> The type of BaseComponent being constructed
 */
@Log4j2
public class BaseComponentBuilder<T extends BaseComponent> extends UiElementBuilder<T> {

  private final TypeToken<T> typeToken;

  @SuppressWarnings("unchecked")
  BaseComponentBuilder(final TypeToken<T> typeToken, final IPageObjectContext context) {
    super(context);
    this.typeToken =
        TypeToken.of(
            ImplementationHelper.getImplementation(
                (Class<T>) typeToken.getRawType(), context.getPlatform()));
    if (Modifier.isAbstract(this.typeToken.getRawType().getModifiers())) {
      throw new UnsupportedOperationException(
          String.format(
              "BaseComponentBuilder cannot create an instance of an abstract BaseComponent. Class [%s].",
              typeToken.getRawType().getSimpleName()));
    }
  }

  /**
   * Initializes a new BaseComponent with no underlying element
   *
   * @return The initialized component
   */
  public T initialize() {
    return this.initialize((LazyWebElement) null);
  }

  @Override
  public T initialize(final @NonNull Locator locator) {
    final var underlying =
        PageObjectBuilder.withContext(context)
            .forLazyWebElement()
            .withParent(parent)
            .initialize(locator);
    return this.initialize(underlying);
  }

  @Override
  public T initialize(final @Nullable WebElement element, final @NonNull Locator locator) {
    final LazyWebElement underlying =
        PageObjectBuilder.withContext(context)
            .forLazyWebElement()
            .withParent(parent)
            .initialize(element, locator);
    return this.initialize(underlying);
  }

  /**
   * Initializes a Component with an underlying BaseElement
   *
   * @param element The underlying element
   * @return The initialized component
   */
  public T initialize(final @Nullable BaseElement element) {
    if (element == null) {
      return this.initialize((LazyWebElement) null);
    }
    return this.initialize(element.getUnderlying());
  }

  @Override
  public T initialize(final @Nullable LazyWebElement underlying) {
    try {
      // Lookup the constructor
      final var constructor = getConstructor(formatArgs);
      // Create a new instance
      final T view = constructor.newInstance(formatArgs);

      // Set the underlying values
      view.setContext(context);
      view.setUnderlying(underlying);

      // Initialize the fields
      initializeFields(view);

      // Run the afterInit hook
      view.afterInit();
      return view;
    } catch (final ReflectiveOperationException e) {
      throw new UnsupportedOperationException(
          String.format(
              "Instantiation of new View threw a "
                  + "ReflectiveOperationException - see details below. Class [%s].",
              typeToken.getType().getTypeName()),
          e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<T> initializeList(final @NonNull Locator locator) {
    return new LazyList<>((Class<T>) typeToken.getRawType(), locator, parent, context);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<T> initializeList(final List<WebElement> elements, final @NonNull Locator locator) {
    return new LazyList<>((Class<T>) typeToken.getRawType(), locator, parent, elements, context);
  }

  /**
   * Adds a ComponentInterceptor that overrides the class implementations
   *
   * @param interceptor The ComponentInterceptor
   * @return The BaseComponentBuilder for the overwritten class
   */
  @SuppressWarnings("unchecked")
  public BaseComponentBuilder<T> withInterceptor(final @NonNull ComponentInterceptor interceptor) {
    // If an interceptor is provided, then override the class using the interceptor
    final var extendedClass =
        TypeToken.of((Class<T>) interceptor.overrideClass(typeToken.getRawType()));
    return new BaseComponentBuilder<>(extendedClass, context);
  }

  @Override
  public BaseComponentBuilder<T> withFormat(final Object... newFormatArgs) {
    super.withFormat(newFormatArgs);
    return this;
  }

  @Override
  public BaseComponentBuilder<T> withParent(final @NonNull Locator locator) {
    super.withParent(locator);
    return this;
  }

  @Override
  public BaseComponentBuilder<T> withParent(
      final @NonNull Locator locator, final LazyWebElement grandParentElement) {
    super.withParent(locator, grandParentElement);
    return this;
  }

  @Override
  public BaseComponentBuilder<T> withParent(final WebElement parentElement, final Locator locator) {
    super.withParent(parentElement, locator);
    return this;
  }

  @Override
  public BaseComponentBuilder<T> withParent(
      final WebElement parentElement,
      final LazyWebElement grandParentElement,
      final Locator locator) {
    super.withParent(parentElement, grandParentElement, locator);
    return this;
  }

  @Override
  public BaseComponentBuilder<T> withParent(final LazyWebElement parentElement) {
    super.withParent(parentElement);
    return this;
  }

  @Override
  public BaseComponentBuilder<T> withParent(final BaseElement parentElement) {
    super.withParent(parentElement);
    return this;
  }

  @Override
  public BaseComponentBuilder<T> withParent(final BaseComponent parentElement) {
    super.withParent(parentElement);
    return this;
  }

  /**
   * Returns a constructor for a given class file matching a set of arguments.
   *
   * @param args The arguments for which a matching constructor will be found.
   * @return class instance constructed
   * @throws NoSuchMethodException if no constructor matching the args exists
   */
  @SuppressWarnings({"unchecked", "PMD.AvoidAccessibilityAlteration"})
  private Constructor<T> getConstructor(final Object... args) throws NoSuchMethodException {
    // Get an array of types corresponding to the passed-in arguments.
    final Class<?>[] types = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
    // Check if there's a constructor for the modified class matching those types.
    for (final Constructor<?> constructor : typeToken.getRawType().getDeclaredConstructors()) {
      if (Arrays.equals(constructor.getParameterTypes(), types)) {
        // Forcing the constructor to be accessible allows us to initialize the class, even if
        // it is scoped differently
        constructor.setAccessible(true);
        return (Constructor<T>) constructor;
      }
    }
    // If we can't find an appropriate constructor, let the user know.
    throw new NoSuchMethodException(
        String.format(
            "No constructor for class matches the arguments passed into BaseComponentBuilder. Missing constructor [%s.<init>(%s)].",
            typeToken.getType().getTypeName(),
            String.join(", ", Arrays.stream(types).map(Class::getTypeName).toList())));
  }

  private void initializeFields(final T component) {
    // Move up the inheritance chain to make sure we initialize all fields in the chain
    for (final var type : typeToken.getTypes()) {
      for (final var field : type.getRawType().getDeclaredFields()) {

        // Verify that there is a locator
        if (LocatorHelper.hasLocator(field)) {
          this.initializeLocatableElement(type, field, component);
          continue;
        }
        // Verify that there is a locator
        if (field.isAnnotationPresent(SubComponent.class)) {
          this.initializeSubComponent(type, field, component);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void initializeSubComponent(
      final TypeToken<?> type, final Field field, final BaseComponent component) {
    // For Generic types, we need to resolve the type from the type token
    final var resolvedFieldType = type.resolveType(field.getGenericType());

    if (BaseComponent.class.isAssignableFrom(resolvedFieldType.getRawType())) {
      // After this validation, we can initialize the list and assign it to the field
      final var res =
          PageObjectBuilder.withContext(context)
              .forBaseComponent((TypeToken<BaseComponent>) resolvedFieldType)
              .withParent(component)
              .initialize();
      this.setField(res, field, component, resolvedFieldType);
      return;
    }

    // If the field has a locator and is not a UIElement or List, then we throw an
    // UnsupportedOperationException
    throw new UnsupportedOperationException(
        String.format(
            "Type [%s] cannot be annotated with SubComponent.class. Field [%s].",
            resolvedFieldType.getRawType().getSimpleName(), field.getName()));
  }

  @SuppressWarnings("unchecked")
  private void initializeLocatableElement(
      final TypeToken<?> type, final Field field, final BaseComponent component) {
    // Look up the locator for the field
    final var newLocator = LocatorHelper.getLocator(field, context.getPlatform());

    // For Generic types, we need to resolve the type from the type token
    final var resolvedFieldType = type.resolveType(field.getGenericType());

    // Check to see if we are initializing a list type.
    if (List.class.isAssignableFrom(resolvedFieldType.getRawType())) {
      // We need to extract out the inner type of the list. This may also be generic, so we will
      // need to resolve it with the type token
      final var innerType =
          typeToken.resolveType(
              ((ParameterizedType) resolvedFieldType.getType()).getActualTypeArguments()[0]);

      // Now that we have the inner type, we need to verify that this is a UIElement type
      if (!UIElement.class.isAssignableFrom(innerType.getRawType())) {
        throw new UnsupportedOperationException(
            String.format(
                "Type [%s] cannot be annotated with Locate.class. Field [%s].",
                innerType.getRawType().getSimpleName(), field.getName()));
      }

      // After this validation, we can initialize the list and assign it to the field
      final var res =
          PageObjectBuilder.withContext(context)
              .forUiElement((TypeToken<UIElement>) innerType)
              .withParent(component)
              .initializeList(newLocator);
      this.setField(res, field, component, innerType);
      return;
    }

    // Next, verify whether we are assigning to a UIElement field. This doesn't require as much
    // validation as the list
    // as there aren't any additional inner types to worry about here.
    if (UIElement.class.isAssignableFrom(resolvedFieldType.getRawType())) {
      // Initialize the element and set it to the field
      final var res =
          PageObjectBuilder.withContext(context)
              .forUiElement((TypeToken<UIElement>) resolvedFieldType)
              .withParent(component)
              .initialize(newLocator);
      this.setField(res, field, component, resolvedFieldType);
      return;
    }

    // If the field has a locator and is not a UIElement or List, then we throw an
    // UnsupportedOperationException
    throw new UnsupportedOperationException(
        String.format(
            "Type [%s] cannot be annotated with Locate.class. Field [%s].",
            resolvedFieldType.getRawType().getSimpleName(), field.getName()));
  }

  /**
   * Sets the value of the field to the supplied value
   *
   * @param value The value to store in this field
   * @param field field
   * @param component component
   * @param fieldType The type of the field
   */
  @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
  private void setField(
      final Object value,
      final Field field,
      final BaseComponent component,
      final TypeToken<?> fieldType) {
    field.setAccessible(true);
    try {
      field.set(component, value);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      log.error(
          "Could not initialize field {} in class {}",
          field.getName(),
          fieldType.getType().getTypeName(),
          e);
    }
  }
}
