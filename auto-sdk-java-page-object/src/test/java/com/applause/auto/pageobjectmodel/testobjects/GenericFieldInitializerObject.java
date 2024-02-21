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
package com.applause.auto.pageobjectmodel.testobjects;

import com.applause.auto.pageobjectmodel.annotation.Locate;
import com.applause.auto.pageobjectmodel.base.BaseComponent;
import com.applause.auto.pageobjectmodel.base.UIElement;
import com.applause.auto.pageobjectmodel.enums.Strategy;
import java.util.List;

public class GenericFieldInitializerObject<T extends UIElement> extends BaseComponent {

  @Locate(using = Strategy.ID, value = "id")
  public T wrappedElement;

  @Locate(using = Strategy.ID, value = "id")
  public List<T> wrappedElementList;
}
