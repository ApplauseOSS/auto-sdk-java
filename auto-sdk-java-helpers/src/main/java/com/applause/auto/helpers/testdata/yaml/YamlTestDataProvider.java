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
package com.applause.auto.helpers.testdata.yaml;

import com.applause.auto.helpers.testdata.TestDataProvider;
import io.github.yamlpath.YamlExpressionParser;
import io.github.yamlpath.YamlPath;
import java.io.FileInputStream;
import java.util.Set;
import lombok.Getter;
import lombok.SneakyThrows;

/** Yaml test data reader based on https://github.com/yaml-path/YamlPath */
public class YamlTestDataProvider implements TestDataProvider {

  @Getter private final String yamlTestDataFilePath;

  private YamlExpressionParser yamlExpressionParser;

  @SneakyThrows
  public YamlTestDataProvider(String yamlTestDataFilePath) {
    this.yamlTestDataFilePath = yamlTestDataFilePath;
    this.yamlExpressionParser = YamlPath.from(new FileInputStream(yamlTestDataFilePath));
  }

  @Override
  public <T> T readSingleValueFromTestDataFile(String yamlPathSyntax) {
    return yamlExpressionParser.readSingle(yamlPathSyntax);
  }

  @Override
  public Set readValuesFromTestDataFile(String yamlPathSyntax) {
    return yamlExpressionParser.read(yamlPathSyntax);
  }
}
