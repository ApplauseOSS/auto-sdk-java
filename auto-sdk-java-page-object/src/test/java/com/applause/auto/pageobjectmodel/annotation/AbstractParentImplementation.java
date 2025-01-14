/*
 *
 * Copyright © 2025 Applause App Quality, Inc.
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
import lombok.NoArgsConstructor;

@Implementation(is = AbstractParentImplementation.class, on = Platform.DEFAULT)
@Implementation(is = ChildAndroidImplementation.class, on = Platform.MOBILE_ANDROID)
@Implementation(is = ChildIOSImplementation.class, on = Platform.MOBILE_IOS)
public abstract class AbstractParentImplementation extends BaseComponent {

  public abstract String getImplementationName();
}

@NoArgsConstructor
class ChildAndroidImplementation extends AbstractParentImplementation {

  @Override
  public String getImplementationName() {
    return "AndroidImplementation";
  }
}

@NoArgsConstructor
class ChildIOSImplementation extends AbstractParentImplementation {

  @Override
  public String getImplementationName() {
    return "IOSImplementation";
  }
}
