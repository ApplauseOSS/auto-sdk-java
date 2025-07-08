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
import com.applause.auto.pageobjectmodel.base.Locatable;
import com.applause.auto.pageobjectmodel.base.UIElement;
import com.applause.auto.pageobjectmodel.builder.PageObjectBuilder;
import com.applause.auto.pageobjectmodel.enums.Strategy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Lazy-loading List implementation for elements and components in the Applause Automation
 * framework. In most cases, LazyLists are created for you by the PageObjectBuilder,
 * BaseComponent.getChildren(), or BaseElement.getChildren().
 *
 * @param <T> a type extending UIElement - either an element or component
 * @see LazyWebElement
 * @see PageObjectBuilder
 */
public class LazyList<T extends UIElement> implements List<T>, Locatable {
  private static final Logger logger = LogManager.getLogger(LazyList.class);

  @Getter private final Class<T> type;
  @Getter private final Locator locator;
  @Getter private final LazyWebElement parent;
  private List<T> underlying;
  @Getter private Object[] formatArgs;
  @Getter private Duration waitTimeout;
  @Getter private Duration waitPollingInterval;
  @Getter private final IPageObjectContext context;

  /**
   * Basic constructor for a LazyList.
   *
   * @param type the type of this List - must be either an element or a component
   * @param locator a Locator pointing at some elements in the DOM
   * @param parent the LazyWebElement parent of this List - used to build chains of Locators for
   *     relative search
   * @param context the underlying context to use
   */
  public LazyList(
      final Class<T> type,
      final Locator locator,
      final LazyWebElement parent,
      final IPageObjectContext context) {
    this.type = type;
    this.locator = locator;
    this.parent = parent;
    this.context = context;
  }

  /**
   * Basic constructor for a LazyList with some elements
   *
   * @param type the type of this List - must be either an element or a component
   * @param locator a Locator pointing at some elements in the DOM
   * @param parent the LazyWebElement parent of this List - used to build chains of Locators for
   *     relative search
   * @param elements elements it contains
   * @param context the underlying context to use
   */
  public LazyList(
      final Class<T> type,
      final Locator locator,
      final LazyWebElement parent,
      final List<WebElement> elements,
      final IPageObjectContext context) {
    this.type = type;
    this.locator = locator;
    this.parent = parent;
    this.context = context;
    this.underlying = buildList(elements);
  }

  /**
   * Builds a {@code List<T> } from a {@code List<WebElement> }. When this is called, we've found a
   * List of underlying WebElements for a set of new elements or components, so we need to create a
   * List of those elements or components with those underlying WebElements.
   *
   * @param elements a List of WebElements found in initialize()
   * @return a List of components or elements, according to the type of this LazyList
   * @throws RuntimeException if initializing the list fails
   */
  private List<T> buildList(final List<WebElement> elements) {
    List<T> list = new ArrayList<>();
    for (int i = 0; i < elements.size(); i++) {
      list.add(
          PageObjectBuilder.withContext(context)
              .forUiElement(type)
              .withParent(this.parent)
              .initialize(elements.get(i), locator.withIndex(i)));
    }
    return list;
  }

  @Override
  public void initialize() {
    if (locator == null || locator.getBy(formatArgs) == null) {
      throw new IllegalArgumentException(
          "Could not initialize List - it doesn't have a locator matching the current Platform. Platform [%s]."
              .formatted(this.context.getPlatform()));
    }
    List<WebElement> elements;
    List<String> bys;
    final var locatorChain = new LocatorChain(this);
    if (locator.getStrategy() == Strategy.JAVASCRIPT) {
      if (locatorChain.size() > 0) {
        logger.warn(
            "Element with JavaScript locator [{}] has a parent element. Relative search cannot be performed with a JavaScript locator. Attempting to locate from the top of the page.",
            locator.getBy(formatArgs));
      }
      elements = locatorChain.synchronizeAndReturnWebElements(locator, formatArgs);
      bys = Collections.singletonList("[" + locator.getBy(formatArgs) + "]");
    }
    // JQuery, on the other hand, requires us to concatenate the string locators together.
    else if (locator.getStrategy() == Strategy.JQUERY) {
      elements =
          locatorChain.synchronizeAndReturnWebElements(
              new Locator(locatorChain.getJQueryBy()), formatArgs);
      bys = Collections.singletonList("[" + locatorChain.getJQueryBy() + "]");
    }
    // Otherwise, loop through the locator chain, performing a series of relative searches.
    else {
      elements = locatorChain.findElementsInChain();
      bys = locatorChain.getByChain();
      if (elements.isEmpty()) {
        // Check if the PageObjectOptions allows for an empty list
        if (!context.getOptions().isThrowExceptionOnEmptyList()) {
          underlying = Collections.emptyList();
          return;
        }
        final String failedLocatorChain = String.join(",", bys);
        throw new NoSuchElementException(
            Optional.ofNullable(locator.getVariableName())
                .map(
                    name ->
                        String.format(
                            "Could not find elements [%s]. Locator [%s].",
                            name, failedLocatorChain))
                .orElse(
                    String.format("Could not find elements. Locator [%s].", failedLocatorChain)));
      }
    }
    underlying = buildList(elements);
    logger.debug(
        String.format(
            "Initialized List<%s>. Locator %s. Platform [%s].",
            type.getSimpleName(), String.join(" -> ", bys), locator.getPlatform()));
  }

  @Override
  public boolean isInitialized() {
    return underlying != null;
  }

  @Override
  public LazyList<T> format(final Object... theFormatArgs) {
    this.formatArgs = theFormatArgs.clone();
    return this;
  }

  @Override
  public void setWait(final Duration theTimeout, final Duration thePollingInterval) {
    this.waitTimeout = theTimeout;
    this.waitPollingInterval = thePollingInterval;
  }

  /**
   * Runs an arbitrary List function with return type void. If the List is null, attempts to
   * populate it first.
   *
   * @param runnable - a lambda function with return type void
   */
  private void runLazily(final Runnable runnable) {
    // If the PageObjectOption is set to automatically refresh the list, then re-initialize before
    // every call
    if (!isInitialized() || context.getOptions().isAutoRefreshList()) {
      initialize();
    }
    runnable.run();
  }

  /**
   * Runs an arbitrary List function with non-void return type. If the List is null, attempts to
   * populate it first.
   *
   * @param supplier - a lambda function with non-void return type
   * @param <S> return type
   * @return The result of the supplier after the list is initialized
   */
  @SneakyThrows
  private <S> S runLazily(final Callable<S> supplier) {
    // If the PageObjectOption is set to automatically refresh the list, then re-initialize before
    // every call
    if (!isInitialized() || context.getOptions().isAutoRefreshList()) {
      initialize();
    }
    return supplier.call();
  }

  // ============= List methods =============

  /** Proxy to the underlying List.size() with lazy-loading. */
  @Override
  @SuppressWarnings(
      "PMD.LambdaCanBeMethodReference") // Method reference would make the call ambiguous due to
  // runLazily overloads
  public int size() {
    return runLazily(() -> underlying.size());
  }

  /** Proxy to the underlying List.isEmpty() with lazy-loading. */
  @Override
  @SuppressWarnings(
      "PMD.LambdaCanBeMethodReference") // Method reference would make the call ambiguous due to
  // runLazily overloads
  public boolean isEmpty() {
    return runLazily(() -> underlying.isEmpty());
  }

  /** Proxy to the underlying List.contains() with lazy-loading. */
  @Override
  public boolean contains(final Object o) {
    return runLazily(() -> underlying.contains(o));
  }

  /** Proxy to the underlying List.iterator() with lazy-loading. */
  @Override
  @SuppressWarnings(
      "PMD.LambdaCanBeMethodReference") // Method reference would make the call ambiguous due to
  // runLazily overloads
  public @NonNull Iterator<T> iterator() {
    return runLazily(() -> underlying.iterator());
  }

  /** Proxy to the underlying List.toArray() with lazy-loading. */
  @Override
  @SuppressWarnings(
      "PMD.LambdaCanBeMethodReference") // Method reference would make the call ambiguous due to
  // runLazily overloads
  public Object @NonNull [] toArray() {
    return runLazily(() -> underlying.toArray());
  }

  /** Proxy to the underlying List.toArray() with lazy-loading. */
  @Override
  public <Z> Z @NonNull [] toArray(final Z @NonNull [] a) {
    return runLazily(() -> underlying.toArray(a));
  }

  /** Proxy to the underlying List.addAll() with lazy-loading. */
  @Override
  public boolean addAll(final @Nonnull Collection<? extends T> c) {
    return runLazily(() -> underlying.addAll(c));
  }

  /** Proxy to the underlying List.addAll() with lazy-loading. */
  @Override
  public boolean addAll(int index, final @Nonnull Collection<? extends T> c) {
    return runLazily(() -> underlying.addAll(index, c));
  }

  /** Proxy to the underlying List.clear() with lazy-loading. */
  @Override
  @SuppressWarnings(
      "PMD.LambdaCanBeMethodReference") // Method reference would make the call ambiguous due to
  // runLazily overloads
  public void clear() {
    runLazily((Runnable) () -> underlying.clear());
  }

  /** Proxy to the underlying List.get() with lazy-loading. */
  @Override
  public T get(int index) {
    return runLazily(() -> underlying.get(index));
  }

  /** Proxy to the underlying List.set() with lazy-loading. */
  @Override
  public T set(int index, final T element) {
    return runLazily(() -> underlying.set(index, element));
  }

  /** Proxy to the underlying List.add() with lazy-loading. */
  @Override
  public void add(int index, final T element) {
    runLazily((Runnable) () -> underlying.add(index, element));
  }

  /** Proxy to the underlying List.add() with lazy-loading. */
  @Override
  public boolean add(final T o) {
    runLazily((Runnable) () -> underlying.add(o));
    return true;
  }

  /** Proxy to the underlying List.remove() with lazy-loading. */
  @Override
  public T remove(int index) {
    return runLazily(() -> underlying.remove(index));
  }

  /** Proxy to the underlying List.remove() with lazy-loading. */
  @Override
  public boolean remove(final Object o) {
    return runLazily(() -> underlying.remove(o));
  }

  /** Proxy to the underlying List.indexOf() with lazy-loading. */
  @Override
  public int indexOf(final Object o) {
    return runLazily(() -> underlying.indexOf(o));
  }

  /** Proxy to the underlying List.lastIndexOf() with lazy-loading. */
  @Override
  public int lastIndexOf(final Object o) {
    return runLazily(() -> underlying.lastIndexOf(o));
  }

  /** Proxy to the underlying List.listIterator() with lazy-loading. */
  @Override
  @SuppressWarnings(
      "PMD.LambdaCanBeMethodReference") // Method reference would make the call ambiguous due to
  // runLazily overloads
  public @Nonnull ListIterator<T> listIterator() {
    return runLazily(() -> underlying.listIterator());
  }

  /** Proxy to the underlying List.listIterator() with lazy-loading. */
  @Override
  public @Nonnull ListIterator<T> listIterator(int index) {
    return runLazily(() -> underlying.listIterator(index));
  }

  /** Proxy to the underlying List.subList() with lazy-loading. */
  @Override
  public @Nonnull List<T> subList(int fromIndex, int toIndex) {
    return runLazily(() -> underlying.subList(fromIndex, toIndex));
  }

  /** Proxy to the underlying List.retailAll() with lazy-loading. */
  @Override
  public boolean retainAll(final @Nonnull Collection<?> c) {
    return runLazily(() -> underlying.retainAll(c));
  }

  /** Proxy to the underlying List.removeAll() with lazy-loading. */
  @Override
  public boolean removeAll(final @Nonnull Collection<?> c) {
    return runLazily(() -> underlying.removeAll(c));
  }

  /** Proxy to the underlying List.containsAll() with lazy-loading. */
  @Override
  public boolean containsAll(final @NonNull Collection<?> c) {
    return runLazily(() -> new HashSet<>(underlying).containsAll(c));
  }
}
