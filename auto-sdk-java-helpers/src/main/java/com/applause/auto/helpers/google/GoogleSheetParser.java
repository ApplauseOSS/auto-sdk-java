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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for parsing Google Sheets. Requires configuration management on the Google Cloud
 * side.
 */
@SuppressWarnings("PMD.LooseCoupling")
public final class GoogleSheetParser {

  private static final Logger logger = LogManager.getLogger(GoogleSheetParser.class);
  private static final String KEY_PARAMETER = "key";

  @Getter private final String sheetId;
  @Getter private final String applicationName;
  private final Sheets service;

  /**
   * Constructs a GoogleSheetParser using an API key.
   *
   * @param apiKey The API key.
   * @param sheetId The Google Sheet ID.
   * @param applicationName The application name.
   */
  public GoogleSheetParser(
      @NonNull final String apiKey,
      @NonNull final String sheetId,
      @NonNull final String applicationName) {
    this.sheetId = sheetId;
    this.applicationName = applicationName;

    final HttpTransport transport = new NetHttpTransport.Builder().build();
    final HttpRequestInitializer httpRequestInitializer =
        request ->
            request.setInterceptor(intercepted -> intercepted.getUrl().set(KEY_PARAMETER, apiKey));

    this.service =
        new Sheets.Builder(transport, GsonFactory.getDefaultInstance(), httpRequestInitializer)
            .setApplicationName(applicationName)
            .build();
  }

  /**
   * Constructs a GoogleSheetParser using a service account JSON file.
   *
   * @param serviceAccountJsonFileInputStream The input stream for the service account JSON file.
   * @param sheetId The Google Sheet ID.
   * @param applicationName The application name.
   */
  @SuppressWarnings("deprecation")
  @SneakyThrows
  public GoogleSheetParser(
      @NonNull final InputStream serviceAccountJsonFileInputStream,
      @NonNull final String sheetId,
      @NonNull final String applicationName) {
    this.sheetId = sheetId;
    this.applicationName = applicationName;

    final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    final GoogleCredential credentials =
        GoogleCredential.fromStream(serviceAccountJsonFileInputStream)
            .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS));

    this.service =
        new Sheets.Builder(httpTransport, GsonFactory.getDefaultInstance(), credentials)
            .setApplicationName(applicationName)
            .build();
  }

  /**
   * Retrieves all cell records from a sheet.
   *
   * @param sheetTitle The title of the sheet.
   * @return A list of lists representing the cell values.
   * @throws IOException If an I/O error occurs.
   * @throws GoogleSheetParserException If the sheet is not found or no values are present.
   */
  public List<List<Object>> getAllSheetCellRecords(@NonNull final String sheetTitle)
      throws IOException {
    final var sheetProperties = getSheetObjectBySheetTitle(sheetTitle).getProperties();
    final var dataFilter = getAllSheetCellsDataFilter(sheetProperties);

    final var request = new BatchGetValuesByDataFilterRequest();
    request.setDataFilters(List.of(dataFilter));

    final var response =
        service.spreadsheets().values().batchGetByDataFilter(sheetId, request).execute();

    final var valueRanges = response.getValueRanges();
    final var values = getCellMatrixFromValueRanges(List.of(dataFilter), valueRanges);

    logger.info("Lines loaded from Sheet: {}", values.size());
    return values;
  }

  @SneakyThrows
  private Sheet getSheetObjectBySheetTitle(@NonNull final String sheetName) {
    final var spreadsheetDocument = service.spreadsheets().get(sheetId).execute();
    return spreadsheetDocument.getSheets().stream()
        .filter(sheet -> sheet.getProperties().getTitle().equals(sheetName))
        .findFirst()
        .orElseThrow(
            () -> new GoogleSheetParserException("Sheet with title: " + sheetName + " not found"));
  }

  private DataFilter getAllSheetCellsDataFilter(@NonNull final SheetProperties sheetProperties) {
    final var dataFilter = new DataFilter();
    final var gridRange = new GridRange();
    final var gridProperties = sheetProperties.getGridProperties();

    gridRange.setSheetId(sheetProperties.getSheetId());
    gridRange.setStartRowIndex(0);
    gridRange.setEndRowIndex(gridProperties.getRowCount() - 1);
    gridRange.setStartColumnIndex(0);
    gridRange.setEndColumnIndex(gridProperties.getColumnCount() - 1);

    logger.info(
        "Creating data filter for sheet: {} with row range [{}, {}] and column range [{}, {}]",
        sheetProperties.getTitle(),
        gridRange.getStartRowIndex(),
        gridRange.getEndRowIndex(),
        gridRange.getStartColumnIndex(),
        gridRange.getEndColumnIndex());

    dataFilter.setGridRange(gridRange);
    return dataFilter;
  }

  private List<List<Object>> getCellMatrixFromValueRanges(
      @NonNull final List<DataFilter> dataFilters,
      @NonNull final List<MatchedValueRange> matchedValueRanges) {

    if (matchedValueRanges.size() != dataFilters.size()) {
      final String filters = dataFilters.stream().map(DataFilter::getGridRange).toList().toString();
      throw new GoogleSheetParserException("No value ranges data present for " + filters);
    }

    return Collections.singletonList(
        matchedValueRanges.getFirst().getValueRange().getValues().stream()
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(
                () ->
                    new GoogleSheetParserException(
                        "No Collection value object found in value range result response")));
  }
}
