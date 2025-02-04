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
package com.applause.auto.helpers.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Google Sheet parser (from Google Drive) [Requires some configuration management from Google Cloud
 * side] Some common tutorials:
 */
public class GoogleSheetParser {

  private static final Logger logger = LogManager.getLogger(GoogleSheetParser.class);

  @Getter private String sheetId;

  @Getter private String googleSheetParsingCloudConfiguredApplicationName;

  private Sheets service;

  /**
   * Create sheet parser using API key access That will work once Google sheet document is available
   * for sharing with "Everyone with a link" option
   *
   * @param apiKey
   * @param sheetId
   */
  public GoogleSheetParser(
      String apiKey, String sheetId, String googleSheetParsingCloudConfiguredApplicationName) {
    this.sheetId = sheetId;
    this.googleSheetParsingCloudConfiguredApplicationName =
        googleSheetParsingCloudConfiguredApplicationName;
    NetHttpTransport transport = new NetHttpTransport.Builder().build();
    HttpRequestInitializer httpRequestInitializer =
        request -> {
          request.setInterceptor(intercepted -> intercepted.getUrl().set("key", apiKey));
        };
    this.service =
        new Sheets.Builder(transport, GsonFactory.getDefaultInstance(), httpRequestInitializer)
            .setApplicationName(googleSheetParsingCloudConfiguredApplicationName)
            .build();
  }

  /**
   * Create parser using service account .json file
   *
   * @param serviceAccountJsonFileInputStream input stream for .json file with configs
   * @param sheetId Google sheet id value (unique part of access link)
   * @throws IOException
   * @throws GeneralSecurityException
   */
  @SneakyThrows
  public GoogleSheetParser(
      InputStream serviceAccountJsonFileInputStream,
      String sheetId,
      String googleSheetParsingCloudConfiguredApplicationName) {
    this.sheetId = sheetId;
    this.googleSheetParsingCloudConfiguredApplicationName =
        googleSheetParsingCloudConfiguredApplicationName;
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    GoogleCredential googleCredentials =
        GoogleCredential.fromStream(serviceAccountJsonFileInputStream)
            .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS));

    this.service =
        new Sheets.Builder(httpTransport, GsonFactory.getDefaultInstance(), googleCredentials)
            .setApplicationName(googleSheetParsingCloudConfiguredApplicationName)
            .build();
  }

  /**
   * Get all sheet cell records
   *
   * @param sheetTitle sheet title
   * @return kind of a matrix values from Google sheet file per sheet title from a document
   * @throws IOException
   */
  public List<List<Object>> getAllSheetCellRecords(String sheetTitle) throws IOException {
    SheetProperties sheetProperties = getSheetObjectBySheetTitle(sheetTitle).getProperties();
    List<DataFilter> dataFilters = new ArrayList<>();
    DataFilter dataFilterForWholeDocumentCells = getAllSheetCellsDataFilter(sheetProperties);
    dataFilters.add(dataFilterForWholeDocumentCells);

    BatchGetValuesByDataFilterRequest batchGetValuesByDataFilterRequest =
        getBatchFilterRequest(dataFilters);
    BatchGetValuesByDataFilterResponse batchGetValuesByDataFilterResponse =
        service
            .spreadsheets()
            .values()
            .batchGetByDataFilter(sheetId, batchGetValuesByDataFilterRequest)
            .execute();
    List<MatchedValueRange> matchedValueRanges =
        batchGetValuesByDataFilterResponse.getValueRanges();
    List<List<Object>> values = getCellMatrixFromValueRanges(dataFilters, matchedValueRanges);
    logger.info("Lines loaded from Sheet: " + values.size());
    return values;
  }

  @SneakyThrows
  private Sheet getSheetObjectBySheetTitle(String sheetName) {
    Spreadsheet spreadsheetDocument = service.spreadsheets().get(sheetId).execute();
    return spreadsheetDocument.getSheets().stream()
        .filter(sheet -> sheet.getProperties().getTitle().equals(sheetName))
        .findFirst()
        .orElseThrow(
            () ->
                new GoogleSheetParserException(
                    "Sheet with title: " + sheetName + " not " + "found"));
  }

  private DataFilter getAllSheetCellsDataFilter(SheetProperties sheetProperties) {
    DataFilter dataFilter = new DataFilter();
    GridRange gridRange = new GridRange();
    GridProperties gridProperties = sheetProperties.getGridProperties();
    gridRange.setSheetId(sheetProperties.getSheetId());
    int startIndex = 0;
    int endRowIndex = gridProperties.getRowCount() - 1;
    int startColumnIndex = 0;
    int endColumnIndex = gridProperties.getColumnCount() - 1;
    logger.info(
        "Creating data filter for sheet: {} with row range [{} , {}] and column range [{} , {}]",
        sheetProperties.getTitle(),
        startIndex,
        endRowIndex,
        startColumnIndex,
        endColumnIndex);
    gridRange.setStartRowIndex(startIndex);
    gridRange.setEndRowIndex(endRowIndex);
    gridRange.setStartColumnIndex(startColumnIndex);
    gridRange.setEndColumnIndex(endColumnIndex);

    dataFilter.setGridRange(gridRange);
    return dataFilter;
  }

  private BatchGetValuesByDataFilterRequest getBatchFilterRequest(List<DataFilter> dataFilters) {
    BatchGetValuesByDataFilterRequest batchGetValuesByDataFilterRequest =
        new BatchGetValuesByDataFilterRequest();
    batchGetValuesByDataFilterRequest.setDataFilters(dataFilters);
    return batchGetValuesByDataFilterRequest;
  }

  private List<List<Object>> getCellMatrixFromValueRanges(
      List<DataFilter> dataFilters, List<MatchedValueRange> matchedValueRanges) {
    int dataFiltersCount = dataFilters.size();
    if (matchedValueRanges.size() != dataFiltersCount) {
      throw new GoogleSheetParserException(
          "No value ranges data present for "
              + dataFilters.stream()
                  .map(filter -> filter.getGridRange())
                  .collect(Collectors.toList()));
    }

    List<List<Object>> values =
        (List<List<Object>>)
            matchedValueRanges
                // TODO Implement dynamic filtering approach with passing List<DataFilter> as param
                .get(dataFiltersCount - 1)
                .getValueRange()
                .values()
                .stream()
                .filter(valueObject -> Collection.class.isAssignableFrom(valueObject.getClass()))
                .findFirst()
                .orElseThrow(
                    () ->
                        new GoogleSheetParserException(
                            "No Collection value object found in value range result response"));
    if (Objects.nonNull(values) && !values.isEmpty()) {
      return values;
    } else {
      logger.error("No data 'matrix' found in range");
      return List.of(Collections.emptyList());
    }
  }
}
