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
import lombok.NonNull;

public final class JiraAnnotationsScanner {

  private JiraAnnotationsScanner() {
    // utility class
  }

  /**
   * Scan test and get declared JiraId value Annotation is mandatory to be declared, otherwise
   * exception is thrown
   *
   * @param result the test result
   * @return JiraId identifier
   * @throws JiraAnnotationException JIRA library error
   */
  public static String getJiraIdentifier(@NonNull final Method result)
      throws JiraAnnotationException {
    return result.getAnnotation(JiraID.class).identifier();
  }

  /**
   * Scan test and get declared JiraDefect value
   *
   * @param result test result
   * @return JiraDefect identifier
   */
  public static String getJiraDefect(@NonNull final Method result) {
    return result.getAnnotation(JiraDefect.class).identifier();
  }
}
