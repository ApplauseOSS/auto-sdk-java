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
package com.applause.auto.helpers.jira.dto.responsemappers;

import com.applause.auto.helpers.jira.dto.responsemappers.iteration.Iteration;
import com.applause.auto.helpers.jira.dto.responsemappers.steps.Step;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class XrayTestRunDetails {
  private int id;
  private String status;
  private String color;
  private String testKey;
  private String testExecKey;
  private String executedBy;
  private String startedOn;
  private String finishedOn;
  private String startedOnIso;
  private String finishedOnIso;
  private int duration;
  private List<Iteration> iterations;
  private List<Object> defects;
  private List<Object> evidences;
  private List<Object> testEnvironments;
  private List<Object> fixVersions;
  private ArrayList<Step> steps;
}
