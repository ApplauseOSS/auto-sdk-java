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
package com.applause.auto.helpers.util;

import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Random dat utils */
public class RandomUtils {

  private static final Logger logger = LogManager.getLogger(RandomUtils.class);

  /**
   * Get Faker object for further fake data generation * /
   *
   * @return Faker - faker object from Faker library https://github.com/DiUS/java-faker
   */
  public static Faker getFaker() {
    return new Faker();
  }

  /**
   * Get random password
   *
   * @param upperCaseSymbolsCount - count of upper case symbols
   * @param lowCaseSymbolsCount - count of lower case symbols
   * @param numericSymbolsCount - numeric symbols count
   * @param withSpecialCharacter - should include special character or not
   * @return generated password
   */
  public static String getRandomValidUserAccountPassword(
      int upperCaseSymbolsCount,
      int lowCaseSymbolsCount,
      int numericSymbolsCount,
      boolean withSpecialCharacter) {
    StringBuilder randomPasswordStringBuilder =
        new StringBuilder()
            .append(RandomStringUtils.randomAlphabetic(upperCaseSymbolsCount).toUpperCase())
            .append(RandomStringUtils.randomAlphabetic(lowCaseSymbolsCount).toLowerCase())
            .append(RandomStringUtils.randomNumeric(numericSymbolsCount));
    if (withSpecialCharacter) {
      randomPasswordStringBuilder.append("$");
    }
    String randomPassword = randomPasswordStringBuilder.toString();
    logger.info("Newly generated random password is: " + randomPassword);
    return randomPassword;
  }
}
