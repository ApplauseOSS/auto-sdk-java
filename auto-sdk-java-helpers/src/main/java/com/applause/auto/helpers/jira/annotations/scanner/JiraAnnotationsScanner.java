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
package com.applause.auto.helpers.jira.annotations.scanner;

import com.applause.auto.helpers.jira.annotations.JiraDefect;
import com.applause.auto.helpers.jira.annotations.JiraID;
import com.applause.auto.helpers.jira.exceptions.JiraAnnotationException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import org.testng.ITestResult;

public class JiraAnnotationsScanner {

  /**
   * Scan test and get declared JiraId value Annotation is mandatory to be declared, otherwise
   * exception is thrown
   *
   * @param result
   * @return JiraId identifier
   * @throws JiraAnnotationException
   */
  public static String getJiraIdentifier(ITestResult result) throws JiraAnnotationException {
    Method[] methods = result.getTestClass().getRealClass().getMethods();
    try {
      return Objects.requireNonNull(
          Arrays.stream(methods)
              .filter(method -> method.getName().equals(result.getMethod().getMethodName()))
              .findFirst()
              .get()
              .getAnnotation(JiraID.class)
              .identifier());
    } catch (NullPointerException nullPointerException) {
      throw new JiraAnnotationException(
          String.format("Missing JiraId annotation for test [%s]", result.getName()));
    }
  }

  /**
   * Scan test and get declared JiraDefect value
   *
   * @param result
   * @return JiraDefect identifier
   */
  public static String getJiraDefect(ITestResult result) {
    Method[] methods = result.getTestClass().getRealClass().getMethods();
    return Objects.requireNonNull(
            Arrays.stream(methods)
                .filter(method -> method.getName().equals(result.getMethod().getMethodName()))
                .findFirst()
                .orElse(null))
        .getAnnotation(JiraDefect.class)
        .identifier();
  }
}
