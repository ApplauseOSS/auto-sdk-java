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

import java.lang.annotation.*;

/**
 * This annotation can be added to methods in page object classes to wrap those methods with
 * analytics assertions. The AnalyticsHelper will check if any network requests were made during the
 * execution of the annotated method. These requests can be further filtered down by
 * setting @AnalyticsCall parameters - for instance, if url is set to "applause.com",
 * AnalyticsHelper will look for requests to applause.com. All resulting requests will then be
 * checked to ensure that a response was received.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AnalyticsCall {

  /**
   * This is a shortcut to filter URL by "google-analytics.com". Note that Google Analytics sends
   * their metadata in query params, so that would be a good filter to use in tandem with this one.
   *
   * @return whether we're using Google Analytics
   */
  boolean googleAnalytics() default false;

  /**
   * This is a shortcut to filter URL by "omtrdc.net". Note that Adobe Omniture sends their metadata
   * in query params, so that would be a good filter to use in tandem with this one.
   *
   * @return whether we're using omniture
   */
  boolean omniture() default false;

  /**
   * timestamp at beginning of call. 0 means empty
   *
   * @return the timestamp (unix time)
   */
  long timestampBefore() default 0;

  /**
   * timestamp at end of call. 0 means empty
   *
   * @return the timestamp (unix time)
   */
  long timestampAfter() default 0;

  /**
   * This is the Chrome performance logging method - for instance, Network.requestWillBeSent.
   *
   * @return logging method
   */
  String method() default "";

  /**
   * Filters by the request's URL, but works with partial matches - "applause.com" will catch
   * requests to "applause.com/test_automation".
   *
   * @return the url or empty string
   */
  String url() default "";

  /**
   * If params() is specified and paramVals() is not, AnalyticsHelper will simply check for the
   * existence of each param.
   *
   * @return the params
   */
  String[] params() default {};

  /**
   * If both params() and paramVals() are specified, AnalyticsHelper will check that each param has
   * a particular value, matched in order.
   *
   * @return param vals
   */
  String[] paramVals() default {};

  /**
   * This matches arbitrary text contained anywhere in the log entry. If you aren't having success
   * with other filters, this would be a good fallback.
   *
   * @return the whole log entry text
   */
  String text() default "";

  /**
   * If initiatorParams() is specified and initiatorParamVals() is not, AnalyticsHelper will simply
   * check for the existence of each initiator param.
   *
   * @return the initiator params
   */
  String[] initiatorParams() default {};

  /**
   * If both initiatorParams() and initiatorParamVals() are specified, AnalyticsHelper will check
   * that each initiator param has a particular value, matched in order.
   *
   * @return init param vals
   */
  String[] initiatorParamVals() default {};

  /**
   * If requestParams() is specified and requestParamVals() is not, AnalyticsHelper will simply
   * check for the existence of each request param.
   *
   * @return request parameters
   */
  String[] requestParams() default {};

  /**
   * If both requestParams() and requestParamVals() are specified, AnalyticsHelper will check that
   * each request param has a particular value, matched in order.
   *
   * @return request param vals
   */
  String[] requestParamVals() default {};

  /**
   * If responseParams() is specified and responseParamVals() is not, AnalyticsHelper will simply
   * check for the existence of each response param.
   *
   * @return response params
   */
  String[] responseParams() default {};

  /**
   * If both responseParams() and responseParamVals() are specified, AnalyticsHelper will check that
   * each response param has a particular value, matched in order.
   *
   * @return response param values
   */
  String[] responseParamVals() default {};

  /**
   * If headers() is specified and headerVals() is not, AnalyticsHelper will simply check for the
   * existence of each header.
   *
   * @return the headers
   */
  String[] headers() default {};

  /**
   * If both headers() and headerVals() are specified, AnalyticsHelper will check that each header
   * has a particular value, matched in order.
   *
   * @return the header values
   */
  String[] headerVals() default {};

  /**
   * the request ID
   *
   * @return request id
   */
  String requestId() default "";

  /**
   * If queryParams() is specified and queryParamVals() is not, AnalyticsHelper will simply check
   * for the existence of each query param.
   *
   * @return query params
   */
  String[] queryParams() default {};

  /**
   * If both queryParams() and queryParamVals() are specified, AnalyticsHelper will check that each
   * query param has a particular value, matched in order.
   *
   * @return query param values
   */
  String[] queryParamVals() default {};
}
