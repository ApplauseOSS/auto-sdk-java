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
package com.applause.auto.data.enums;

import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.Point;

/** Enum defining the valid swipe directions */
public enum SwipeDirection {
  /** Swipe Down */
  DOWN,
  /** Swipe Up */
  UP,
  /** Swipe Left */
  LEFT,
  /** Swipe Right */
  RIGHT;

  /**
   * Returns a pair of points representing a vector pointing across the middle 70% of an arbitrary
   * box. This is used by various swipe methods.
   *
   * @param width of the arbitrary box
   * @param height of the arbitrary box
   * @return a {@code Pair<Point, Point>} containing the start and end points for the vector
   * @throws IllegalArgumentException if swipe direction is invalid
   */
  public Pair<Point, Point> getSwipeVector(final int width, final int height) {
    Point start;
    Point end =
        switch (this) {
          case UP -> {
            start = new Point(width / 2, (int) (height * 0.85));
            yield new Point(width / 2, (int) (height * 0.15));
          }
          case DOWN -> {
            start = new Point(width / 2, (int) (height * 0.15));
            yield new Point(width / 2, (int) (height * 0.85));
          }
          case LEFT -> {
            start = new Point((int) (width * 0.85), height / 2);
            yield new Point((int) (width * 0.15), height / 2);
          }
          case RIGHT -> {
            start = new Point((int) (width * 0.15), height / 2);
            yield new Point((int) (width * 0.85), height / 2);
          }
          default ->
              throw new IllegalArgumentException(
                  "Invalid SwipeDirection value specified, somehow.");
        };
    return Pair.of(start, end);
  }
}
