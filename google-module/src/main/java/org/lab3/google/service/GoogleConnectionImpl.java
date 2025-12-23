package org.lab3.google.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.model.BatchUpdateFormRequest;
import com.google.api.services.forms.v1.model.CreateItemRequest;
import com.google.api.services.forms.v1.model.Form;
import com.google.api.services.forms.v1.model.Info;
import com.google.api.services.forms.v1.model.Item;
import com.google.api.services.forms.v1.model.Location;
import com.google.api.services.forms.v1.model.Question;
import com.google.api.services.forms.v1.model.QuestionItem;
import com.google.api.services.forms.v1.model.TextQuestion;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.RepeatCellRequest;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.lab.logger.Logger;
import org.lab3.google.json.MonetizationEvent;
import org.lab3.google.resource.GoogleManagedConnection;

/**
 * Implementation of GoogleConnection interface. Provides methods for interacting with Google Drive,
 * Sheets, and Forms APIs. This class is not designed for extension.
 */
@SuppressWarnings("checkstyle:DesignForExtension")
public final class GoogleConnectionImpl implements GoogleConnection {
  private final Drive driveService;
  private final Sheets sheetsService;
  private final GoogleManagedConnection managedConnection;
  private final MetricsManager metrics = MetricsManager.getInstance();
  private static final Logger LOGGER = Logger.getInstance("google-module");

  private static final float LIGHT_GRAY_RED = 0.9f;
  private static final float LIGHT_GRAY_GREEN = 0.9f;
  private static final float LIGHT_GRAY_BLUE = 0.9f;
  private static final float LIGHTER_GRAY_RED = 0.95f;
  private static final float LIGHTER_GRAY_GREEN = 0.95f;
  private static final float LIGHTER_GRAY_BLUE = 0.95f;
  private static final int THREE = 3;
  private static final int FIVE = 5;
  private static final int FOUR = 4;
  private static final int THOUSAND = 1000;
  private static final int TWO = 2;
  private static final int ZERO = 0;
  private static final int ONE = 1;
  private static final int APP_NAME_COLUMN = 1;
  private static final int TOTAL_REVENUE_COLUMN = 4;
  private static final int ROW_OFFSET = 2;

  public GoogleConnectionImpl(final GoogleManagedConnection managedConnectionParam) {
    this.managedConnection = managedConnectionParam;
    this.driveService = managedConnectionParam.getDriveService();
    this.sheetsService = managedConnectionParam.getSheetsService();
  }

  @Override
  public String uploadFile(final String fileName, final byte[] fileData) throws IOException {
    com.google.api.services.drive.model.File fileMetadata =
        new com.google.api.services.drive.model.File().setName(fileName);

    return driveService
        .files()
        .create(
            fileMetadata,
            new com.google.api.client.http.ByteArrayContent(
                "application" + "/octet-stream", fileData))
        .execute()
        .getId();
  }

  @Override
  public String createGoogleSheet(final String title) throws IOException {
    Spreadsheet spreadsheet =
        new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(title));

    Spreadsheet created = sheetsService.spreadsheets().create(spreadsheet).execute();
    metrics.recordSheetCreated();
    return created.getSpreadsheetId();
  }

  @Override
  public void updateGoogleSheet(final String sheetId, final List<List<Object>> data)
      throws IOException {
    ValueRange body = new ValueRange().setValues(data);
    sheetsService
        .spreadsheets()
        .values()
        .update(sheetId, "A1", body)
        .setValueInputOption("RAW")
        .execute();
  }

  @Override
  public String createGoogleForm(final String title, final Map<String, String> fields)
      throws IOException {
    Forms formsService = managedConnection.getFormsService();

    Form form = new Form().setInfo(new Info().setTitle(title).setDocumentTitle(title));
    Form createdForm = formsService.forms().create(form).execute();
    String formId = createdForm.getFormId();

    List<com.google.api.services.forms.v1.model.Request> requests = new ArrayList<>();
    for (String entry : fields.keySet()) {
      com.google.api.services.forms.v1.model.Request request =
          new com.google.api.services.forms.v1.model.Request()
              .setCreateItem(
                  new CreateItemRequest()
                      .setItem(
                          new Item()
                              .setTitle(entry)
                              .setQuestionItem(
                                  new QuestionItem()
                                      .setQuestion(
                                          new Question()
                                              .setRequired(true)
                                              .setTextQuestion(
                                                  new TextQuestion().setParagraph(false)))))
                      .setLocation(new Location().setIndex(ZERO)));
      requests.add(request);
    }

    if (!requests.isEmpty()) {
      BatchUpdateFormRequest batchUpdateRequest =
          new BatchUpdateFormRequest().setRequests(requests);
      formsService.forms().batchUpdate(formId, batchUpdateRequest).execute();
    }

    metrics.recordFormCreated();
    return "https://docs.google.com/forms/d/" + formId + "/edit";
  }

  /**
   * Creates a revenue spreadsheet with data.
   *
   * @param title the title of the spreadsheet
   * @param headers the headers for the spreadsheet
   * @param data the data to populate
   * @return the URL of the created spreadsheet
   * @throws IOException if there's an error creating the spreadsheet
   */
  public String createRevenueSpreadsheetWithData(
      final String title, final List<String> headers, final List<List<Object>> data)
      throws IOException {
    Spreadsheet spreadsheet =
        new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(title));

    SheetProperties sheetProperties =
        new SheetProperties().setTitle("ApplicationsRevenue").setSheetId(ZERO);

    spreadsheet.setSheets(List.of(new Sheet().setProperties(sheetProperties)));
    Spreadsheet created = sheetsService.spreadsheets().create(spreadsheet).execute();
    String spreadsheetId = created.getSpreadsheetId();

    List<List<Object>> allData = new ArrayList<>();
    allData.add(headers.stream().map(Object.class::cast).collect(Collectors.toList()));
    allData.addAll(data);

    ValueRange body = new ValueRange().setValues(allData).setMajorDimension("ROWS");

    sheetsService
        .spreadsheets()
        .values()
        .update(spreadsheetId, "A1", body)
        .setValueInputOption("USER_ENTERED")
        .execute();

    driveService
        .permissions()
        .create(spreadsheetId, new Permission().setType("anyone").setRole("reader"))
        .execute();

    return "https://docs.google.com/spreadsheets/d/" + spreadsheetId + "/edit";
  }

  @Override
  public void addAppSheets(
      final String googleEmail, final String spreadsheetTitle, final String appName)
      throws IOException {
    String query =
        String.format(
            "name='%s' and mimeType='application/vnd" + ".google-apps.spreadsheet'",
            spreadsheetTitle);
    FileList files =
        driveService
            .files()
            .list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute();

    if (files.getFiles().isEmpty()) {
      throw new IOException("Spreadsheet not found: " + spreadsheetTitle);
    }

    String spreadsheetId = files.getFiles().get(0).getId();

    LOGGER.info("Spreadsheet ID: " + spreadsheetId);

    Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
    int nextSheetId =
        spreadsheet.getSheets().stream()
                .mapToInt(sheet -> sheet.getProperties().getSheetId())
                .max()
                .orElse(ZERO)
            + ONE;

    String[] sheetNames = {
      appName + " Revenue",
      appName + " Watched Adds",
      appName + " Microtransactions",
      appName + " Downloads"
    };

    List<com.google.api.services.sheets.v4.model.Request> deleteRequests = new ArrayList<>();
    for (Sheet sheet : spreadsheet.getSheets()) {
      String title = sheet.getProperties().getTitle();
      for (String sheetName : sheetNames) {
        if (title.equals(sheetName)) {
          deleteRequests.add(
              new com.google.api.services.sheets.v4.model.Request()
                  .setDeleteSheet(
                      new com.google.api.services.sheets.v4.model.DeleteSheetRequest()
                          .setSheetId(sheet.getProperties().getSheetId())));
          break;
        }
      }
    }

    if (!deleteRequests.isEmpty()) {
      BatchUpdateSpreadsheetRequest deleteBatchRequest =
          new BatchUpdateSpreadsheetRequest().setRequests(deleteRequests);
      sheetsService.spreadsheets().batchUpdate(spreadsheetId, deleteBatchRequest).execute();
    }

    List<com.google.api.services.sheets.v4.model.Request> createRequests = new ArrayList<>();
    for (String sheetName : sheetNames) {
      createRequests.add(
          new com.google.api.services.sheets.v4.model.Request()
              .setAddSheet(
                  new AddSheetRequest()
                      .setProperties(
                          new SheetProperties().setTitle(sheetName).setSheetId(nextSheetId++))));
    }

    BatchUpdateSpreadsheetRequest createBatchRequest =
        new BatchUpdateSpreadsheetRequest().setRequests(createRequests);
    BatchUpdateSpreadsheetResponse response =
        sheetsService.spreadsheets().batchUpdate(spreadsheetId, createBatchRequest).execute();

    metrics.recordTabsCreated(sheetNames.length);

    List<ValueRange> dataList = new ArrayList<>();
    dataList.add(
        new ValueRange()
            .setRange(appName + " Revenue!A1")
            .setValues(
                List.of(
                    List.of(
                        "applicationId",
                        "revenueFromAdds",
                        "revenueFromDownloads",
                        "revenueFromMicrotransactions",
                        "totalRevenue"))));
    dataList.add(
        new ValueRange()
            .setRange(appName + " Watched Adds!A1")
            .setValues(List.of(List.of("userId", "addId"))));
    dataList.add(
        new ValueRange()
            .setRange(appName + " Microtransactions!A1")
            .setValues(List.of(List.of("userId", "microtransactionId"))));
    dataList.add(
        new ValueRange()
            .setRange(appName + " Downloads!A1")
            .setValues(List.of(List.of("userId", "appId"))));

    BatchUpdateValuesRequest batchDataRequest =
        new BatchUpdateValuesRequest().setValueInputOption("RAW").setData(dataList);
    sheetsService.spreadsheets().values().batchUpdate(spreadsheetId, batchDataRequest).execute();

    List<com.google.api.services.sheets.v4.model.Request> formatRequests = new ArrayList<>();
    for (SheetProperties sheet :
        response.getReplies().stream()
            .map(reply -> reply.getAddSheet().getProperties())
            .collect(Collectors.toList())) {

      formatRequests.add(
          new com.google.api.services.sheets.v4.model.Request()
              .setRepeatCell(
                  new RepeatCellRequest()
                      .setRange(
                          new GridRange()
                              .setSheetId(sheet.getSheetId())
                              .setStartRowIndex(ZERO)
                              .setEndRowIndex(ONE))
                      .setCell(
                          new CellData()
                              .setUserEnteredFormat(
                                  new CellFormat()
                                      .setTextFormat(
                                          new com.google.api.services.sheets.v4.model.TextFormat()
                                              .setBold(true))))
                      .setFields("userEnteredFormat.textFormat.bold")));
    }

    if (!formatRequests.isEmpty()) {
      BatchUpdateSpreadsheetRequest formatBatchRequest =
          new BatchUpdateSpreadsheetRequest().setRequests(formatRequests);
      sheetsService.spreadsheets().batchUpdate(spreadsheetId, formatBatchRequest).execute();
    }

    LOGGER.info("Added sheets for app '" + appName + "' to spreadsheet: " + spreadsheetTitle);
  }

  @Override
  public void updateMonetizationSheets(final MonetizationEvent event) throws IOException {
    String developerEmail = getDeveloperEmail(event.getApplicationId());
    String spreadsheetTitle = String.format("Revenue Statistics - %s - 52", developerEmail);

    String spreadsheetId = findSpreadsheetId(spreadsheetTitle);
    if (spreadsheetId == null) {
      throw new IOException("Spreadsheet not found: " + spreadsheetTitle);
    }

    String appName = getApplicationName(event.getApplicationId());

    switch (event.getEventType()) {
      case DOWNLOAD:
        handleDownloadEvent(spreadsheetId, appName, event);
        break;
      case PURCHASE:
        handlePurchaseEvent(spreadsheetId, appName, event);
        break;
      case AD_VIEW:
        handleAdViewEvent(spreadsheetId, appName, event);
        break;
      default:
        LOGGER.error("Unknown event type: " + event.getEventType());
        break;
    }

    updateApplicationsRevenue(spreadsheetId, appName, event.getEventType(), event.getAmount());

    LOGGER.info(
        "Monetization updated for app '"
            + appName
            + "': "
            + event.getEventType()
            + ", amount: "
            + event.getAmount());
  }

  private void handleDownloadEvent(
      final String spreadsheetId, final String appName, final MonetizationEvent event)
      throws IOException {
    appendToSheet(
        spreadsheetId, appName + " Downloads", List.of(event.getUserId(), event.getItemId()));

    updateRevenueColumn(
        spreadsheetId, appName + " Revenue", "revenueFromDownloads", event.getAmount());

    metrics.recordRevenueUpdate();
  }

  private void handlePurchaseEvent(
      final String spreadsheetId, final String appName, final MonetizationEvent event)
      throws IOException {
    appendToSheet(
        spreadsheetId,
        appName + " Microtransactions",
        List.of(event.getUserId(), event.getItemId()));

    updateRevenueColumn(
        spreadsheetId, appName + " Revenue", "revenueFromMicrotransactions", event.getAmount());

    metrics.recordRevenueUpdate();
  }

  private void handleAdViewEvent(
      final String spreadsheetId, final String appName, final MonetizationEvent event)
      throws IOException {
    appendToSheet(
        spreadsheetId, appName + " Watched Adds", List.of(event.getUserId(), event.getItemId()));

    updateRevenueColumn(spreadsheetId, appName + " Revenue", "revenueFromAdds", event.getAmount());

    metrics.recordRevenueUpdate();
  }

  private void appendToSheet(
      final String spreadsheetId, final String sheetName, final List<Object> rowData)
      throws IOException {
    ValueRange body = new ValueRange().setValues(List.of(rowData)).setMajorDimension("ROWS");

    sheetsService
        .spreadsheets()
        .values()
        .append(spreadsheetId, sheetName + "!A1", body)
        .setValueInputOption("USER_ENTERED")
        .setInsertDataOption("INSERT_ROWS")
        .execute();
  }

  private void updateRevenueColumn(
      final String spreadsheetId, final String sheetName, final String column, final double amount)
      throws IOException {
    String range;
    switch (column) {
      case "revenueFromAdds":
        range = "B2";
        break;
      case "revenueFromDownloads":
        range = "C2";
        break;
      case "revenueFromMicrotransactions":
        range = "D2";
        break;
      default:
        throw new IllegalArgumentException("Invalid column: " + column);
    }

    ValueRange currentValue =
        sheetsService.spreadsheets().values().get(spreadsheetId, sheetName + "!" + range).execute();

    double currentAmount = 0;
    if (currentValue.getValues() != null && !currentValue.getValues().isEmpty()) {
      currentAmount = Double.parseDouble(currentValue.getValues().get(0).get(0).toString());
    }

    ValueRange newValue =
        new ValueRange().setValues(List.of(List.of(Double.toString(currentAmount + amount))));

    sheetsService
        .spreadsheets()
        .values()
        .update(spreadsheetId, sheetName + "!" + range, newValue)
        .setValueInputOption("USER_ENTERED")
        .execute();

    updateTotalRevenue(spreadsheetId, sheetName);
  }

  private void updateTotalRevenue(final String spreadsheetId, final String sheetName)
      throws IOException {
    ValueRange revenues =
        sheetsService.spreadsheets().values().get(spreadsheetId, sheetName + "!B2:D2").execute();

    double total = 0;
    if (revenues.getValues() != null) {
      for (Object value : revenues.getValues().get(0)) {
        if (value.toString().isEmpty()) {
          value = "0";
        }
        total += Double.parseDouble(value.toString());
      }
    }

    ValueRange totalValue = new ValueRange().setValues(List.of(List.of(total)));

    sheetsService
        .spreadsheets()
        .values()
        .update(spreadsheetId, sheetName + "!E2", totalValue)
        .setValueInputOption("USER_ENTERED")
        .execute();
  }

  private void updateApplicationsRevenue(
      final String spreadsheetId,
      final String appName,
      final MonetizationEvent.EventType eventType,
      final double amount)
      throws IOException {
    ValueRange appRevenue =
        sheetsService
            .spreadsheets()
            .values()
            .get(spreadsheetId, appName + " " + "Revenue!B2:E2")
            .execute();

    if (appRevenue.getValues() == null || appRevenue.getValues().isEmpty()) {
      return;
    }

    List<Object> revenueData = appRevenue.getValues().get(0);

    double adsRevenue =
        revenueData.get(0) == null || revenueData.get(0).toString().trim().isEmpty()
            ? 0.0
            : Double.parseDouble(revenueData.get(0).toString());

    double downloadRevenue =
        revenueData.get(1) == null || revenueData.get(1).toString().trim().isEmpty()
            ? 0.0
            : Double.parseDouble(revenueData.get(1).toString());

    double purchasesRevenue =
        revenueData.get(TWO) == null || revenueData.get(TWO).toString().trim().isEmpty()
            ? 0.0
            : Double.parseDouble(revenueData.get(TWO).toString());

    double totalRevenue =
        revenueData.get(THREE) == null || revenueData.get(THREE).toString().trim().isEmpty()
            ? 0.0
            : Double.parseDouble(revenueData.get(THREE).toString());

    ValueRange apps =
        sheetsService
            .spreadsheets()
            .values()
            .get(spreadsheetId, "ApplicationsRevenue!A2:B")
            .execute();

    int rowNumber = -1;
    String appId = null;
    if (apps.getValues() != null) {
      for (int i = 0; i < apps.getValues().size(); i++) {
        if (apps.getValues().get(i).size() >= TWO
            && apps.getValues().get(i).get(APP_NAME_COLUMN).toString().equals(appName)) {
          rowNumber = i + ROW_OFFSET;
          appId = apps.getValues().get(i).get(0).toString();
          break;
        }
      }
    }

    if (rowNumber > 0 && appId != null) {
      ValueRange updateData =
          new ValueRange()
              .setValues(
                  List.of(List.of(adsRevenue, downloadRevenue, purchasesRevenue, totalRevenue)));

      sheetsService
          .spreadsheets()
          .values()
          .update(spreadsheetId, "ApplicationsRevenue!C" + rowNumber + ":F" + rowNumber, updateData)
          .setValueInputOption("USER_ENTERED")
          .execute();
    } else {
      LOGGER.error("Application " + appName + " not found in " + "ApplicationsRevenue to update.");
    }
  }

  private String findSpreadsheetId(final String title) throws IOException {
    FileList files =
        driveService
            .files()
            .list()
            .setQ(
                "name='"
                    + title
                    + "' "
                    + "and "
                    + "mimeType='application/vnd.google-apps.spreadsheet'")
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute();

    return files.getFiles().isEmpty() ? null : files.getFiles().get(0).getId();
  }

  private String getDeveloperEmail(final int applicationId) {
    return "yee.eey.yeeeey.yee.eey@gmail.com";
  }

  private String getApplicationName(final int applicationId) {
    return "My App";
  }

  private void updateSingleTop(final String spreadsheetId, final List<AppRevenue> allAppsRevenue)
      throws IOException {
    String topSheetName = "ApplicationsRevenueTop";
    List<List<Object>> values = new ArrayList<>();
    values.add(List.of("Rank", "Application", "Total Revenue"));

    for (int i = 0; i < allAppsRevenue.size(); i++) {
      AppRevenue app = allAppsRevenue.get(i);
      values.add(List.of(i + ONE, app.getAppName(), app.getTotalRevenue()));
    }

    ValueRange body = new ValueRange().setValues(values).setMajorDimension("ROWS");

    try {
      sheetsService
          .spreadsheets()
          .values()
          .update(spreadsheetId, topSheetName + "!A1", body)
          .setValueInputOption("USER_ENTERED")
          .execute();
      formatTopSheet(spreadsheetId);
    } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
      LOGGER.error("Error updating sheet " + topSheetName + ": " + e.getDetails().getMessage());
      throw e;
    }
  }

  @Override
  public void updateAppsTop() throws IOException {
    FileList spreadsheets =
        driveService
            .files()
            .list()
            .setQ(
                "mimeType"
                    + "='application/vnd.google-apps.spreadsheet'"
                    + " and name "
                    + "contains 'Revenue Statistics'")
            .setFields("files(id, name)")
            .execute();

    List<AppRevenue> allAppsRevenue = new ArrayList<>();

    for (File file : spreadsheets.getFiles()) {
      String spreadsheetId = file.getId();
      ValueRange response =
          sheetsService
              .spreadsheets()
              .values()
              .get(spreadsheetId, "ApplicationsRevenue!B2:F")
              .execute();

      if (response.getValues() != null) {
        for (List<Object> row : response.getValues()) {
          if (row.size() >= FIVE) {
            String appName = row.get(0).toString();
            double totalRevenue = Double.parseDouble(row.get(TOTAL_REVENUE_COLUMN).toString());
            allAppsRevenue.add(
                new AppRevenue(appName, totalRevenue, spreadsheetId, "ApplicationsRevenue"));
          }
        }
      }
    }

    allAppsRevenue.sort((a1, a2) -> Double.compare(a2.getTotalRevenue(), a1.getTotalRevenue()));
    metrics.recordTopUpdate();

    for (File file : spreadsheets.getFiles()) {
      String spreadsheetId = file.getId();
      updateSingleTop(spreadsheetId, allAppsRevenue);
    }

    LOGGER.info("Apps top updated for " + spreadsheets.getFiles().size() + " " + "spreadsheets");
  }

  private void formatTopSheet(final String spreadsheetId) throws IOException {
    List<com.google.api.services.sheets.v4.model.Request> requests = new ArrayList<>();

    requests.add(
        new com.google.api.services.sheets.v4.model.Request()
            .setRepeatCell(
                new RepeatCellRequest()
                    .setRange(
                        new GridRange()
                            .setSheetId(ZERO)
                            .setStartRowIndex(ZERO)
                            .setEndRowIndex(ONE)
                            .setStartColumnIndex(ZERO)
                            .setEndColumnIndex(THREE))
                    .setCell(
                        new CellData()
                            .setUserEnteredFormat(
                                new CellFormat()
                                    .setTextFormat(
                                        new com.google.api.services.sheets.v4.model.TextFormat()
                                            .setBold(true))
                                    .setBackgroundColor(
                                        new Color()
                                            .setRed(LIGHT_GRAY_RED)
                                            .setGreen(LIGHT_GRAY_GREEN)
                                            .setBlue(LIGHT_GRAY_BLUE))))
                    .setFields("userEnteredFormat")));

    requests.add(
        new com.google.api.services.sheets.v4.model.Request()
            .setAddConditionalFormatRule(
                new com.google.api.services.sheets.v4.model.AddConditionalFormatRuleRequest()
                    .setRule(
                        new com.google.api.services.sheets.v4.model.ConditionalFormatRule()
                            .setRanges(
                                List.of(
                                    new GridRange()
                                        .setSheetId(ZERO)
                                        .setStartRowIndex(ONE)
                                        .setEndRowIndex(THOUSAND)
                                        .setStartColumnIndex(ZERO)
                                        .setEndColumnIndex(THREE)))
                            .setBooleanRule(
                                new com.google.api.services.sheets.v4.model.BooleanRule()
                                    .setCondition(
                                        new com.google.api.services.sheets.v4.model
                                                .BooleanCondition()
                                            .setType("MOD")
                                            .setValues(
                                                List.of(
                                                    new com.google.api.services.sheets.v4.model
                                                            .ConditionValue()
                                                        .setUserEnteredValue("2"),
                                                    new com.google.api.services.sheets.v4.model
                                                            .ConditionValue()
                                                        .setUserEnteredValue("0"))))
                                    .setFormat(
                                        new CellFormat()
                                            .setBackgroundColor(
                                                new Color()
                                                    .setRed(LIGHTER_GRAY_RED)
                                                    .setGreen(LIGHTER_GRAY_GREEN)
                                                    .setBlue(LIGHTER_GRAY_BLUE)))))));

    BatchUpdateSpreadsheetRequest batchUpdateRequest =
        new BatchUpdateSpreadsheetRequest().setRequests(requests);

    sheetsService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
  }

  private static final class AppRevenue {
    private final String appName;
    private final double totalRevenue;
    private final String spreadsheetId;
    private final String revenueSheetName;

    AppRevenue(
        final String appNameParam,
        final double totalRevenueParam,
        final String spreadsheetIdParam,
        final String revenueSheetNameParam) {
      this.appName = appNameParam;
      this.totalRevenue = totalRevenueParam;
      this.spreadsheetId = spreadsheetIdParam;
      this.revenueSheetName = revenueSheetNameParam;
    }

    public String getAppName() {
      return appName;
    }

    public double getTotalRevenue() {
      return totalRevenue;
    }

    public String getSpreadsheetId() {
      return spreadsheetId;
    }

    public String getRevenueSheetName() {
      return revenueSheetName;
    }
  }

  @Override
  public void close() {
    managedConnection.cleanup();
  }
}
