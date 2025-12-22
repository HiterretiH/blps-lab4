package org.lab3.google.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.model.*;
import com.google.api.services.forms.v1.model.Request;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.lab.logger.Logger;
import org.lab3.google.json.MonetizationEvent;
import org.lab3.google.resource.GoogleManagedConnection;

public class GoogleConnectionImpl implements GoogleConnection {
  private final Drive driveService;
  private final Sheets sheetsService;
  private final GoogleManagedConnection managedConnection;
  private final MetricsManager metrics = MetricsManager.getInstance();
  private static final Logger logger = Logger.getInstance("google-module");

  public GoogleConnectionImpl(GoogleManagedConnection managedConnection) {
    this.managedConnection = managedConnection;
    this.driveService = managedConnection.getDriveService();
    this.sheetsService = managedConnection.getSheetsService();
  }

  @Override
  public String uploadFile(String fileName, byte[] fileData) throws IOException {
    com.google.api.services.drive.model.File fileMetadata =
        new com.google.api.services.drive.model.File().setName(fileName);

    return driveService
        .files()
        .create(
            fileMetadata,
            new com.google.api.client.http.ByteArrayContent("application/octet-stream", fileData))
        .execute()
        .getId();
  }

  @Override
  public String createGoogleSheet(String title) throws IOException {
    Spreadsheet spreadsheet =
        new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(title));

    Spreadsheet created = sheetsService.spreadsheets().create(spreadsheet).execute();
    metrics.recordSheetCreated();
    return created.getSpreadsheetId();
  }

  @Override
  public void updateGoogleSheet(String sheetId, List<List<Object>> data) throws IOException {
    ValueRange body = new ValueRange().setValues(data);
    sheetsService
        .spreadsheets()
        .values()
        .update(sheetId, "A1", body)
        .setValueInputOption("RAW")
        .execute();
  }

  @Override
  public String createGoogleForm(String title, Map<String, String> fields) throws IOException {
    Forms formsService = managedConnection.getFormsService();

    // 1. Создаем базовую форму только с заголовком
    Form form = new Form().setInfo(new Info().setTitle(title).setDocumentTitle(title));

    // 2. Создаем пустую форму
    Form createdForm = formsService.forms().create(form).execute();
    String formId = createdForm.getFormId();

    // 3. Подготавливаем запросы для добавления вопросов
    List<Request> requests = new ArrayList<>();
    for (String entry : fields.keySet()) {
      Request request =
          new Request()
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
                      .setLocation(new Location().setIndex(0))); // Добавляем вопросы в начало формы
      requests.add(request);
    }

    // 4. Если есть вопросы, добавляем их через batchUpdate
    if (!requests.isEmpty()) {
      BatchUpdateFormRequest batchUpdateRequest =
          new BatchUpdateFormRequest().setRequests(requests);
      formsService.forms().batchUpdate(formId, batchUpdateRequest).execute();
    }

    metrics.recordFormCreated();
    // 5. Возвращаем URL формы
    return "https://docs.google.com/forms/d/" + formId + "/edit";
  }

  public String createRevenueSpreadsheetWithData(
      String title, List<String> headers, List<List<Object>> data) throws IOException {
    Spreadsheet spreadsheet =
        new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(title));

    SheetProperties sheetProperties =
        new SheetProperties().setTitle("ApplicationsRevenue").setSheetId(0);

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
  public void addAppSheets(String googleEmail, String spreadsheetTitle, String appName)
      throws IOException {
    // 1. Ищем таблицу по названию
    String query =
        String.format(
            "name='%s' and mimeType='application/vnd.google-apps.spreadsheet'", spreadsheetTitle);
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

    logger.info("Spreadsheet ID: " + spreadsheetId);

    // 2. Получаем информацию о существующих листах
    Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
    int nextSheetId =
        spreadsheet.getSheets().stream()
                .mapToInt(sheet -> sheet.getProperties().getSheetId())
                .max()
                .orElse(0)
            + 1;

    // 3. Подготавливаем названия листов
    String[] sheetNames = {
      appName + " Revenue",
      appName + " Watched Adds",
      appName + " Microtransactions",
      appName + " Downloads"
    };

    // 4. Сначала удаляем существующие листы (если есть)
    List<com.google.api.services.sheets.v4.model.Request> deleteRequests = new ArrayList<>();
    for (Sheet sheet : spreadsheet.getSheets()) {
      String title = sheet.getProperties().getTitle();
      for (String sheetName : sheetNames) {
        if (title.equals(sheetName)) {
          deleteRequests.add(
              new com.google.api.services.sheets.v4.model.Request()
                  .setDeleteSheet(
                      new DeleteSheetRequest().setSheetId(sheet.getProperties().getSheetId())));
          break;
        }
      }
    }

    if (!deleteRequests.isEmpty()) {
      BatchUpdateSpreadsheetRequest deleteBatchRequest =
          new BatchUpdateSpreadsheetRequest().setRequests(deleteRequests);
      sheetsService.spreadsheets().batchUpdate(spreadsheetId, deleteBatchRequest).execute();
    }

    // 5. Создаем новые листы
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

    // 6. Добавляем данные в листы
    List<ValueRange> data = new ArrayList<>();
    // Revenue sheet
    data.add(
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
    // Watched Adds sheet
    data.add(
        new ValueRange()
            .setRange(appName + " Watched Adds!A1")
            .setValues(List.of(List.of("userId", "addId"))));
    // Microtransactions sheet
    data.add(
        new ValueRange()
            .setRange(appName + " Microtransactions!A1")
            .setValues(List.of(List.of("userId", "microtransactionId"))));
    // Downloads sheet
    data.add(
        new ValueRange()
            .setRange(appName + " Downloads!A1")
            .setValues(List.of(List.of("userId", "appId"))));

    BatchUpdateValuesRequest batchDataRequest =
        new BatchUpdateValuesRequest().setValueInputOption("RAW").setData(data);
    sheetsService.spreadsheets().values().batchUpdate(spreadsheetId, batchDataRequest).execute();

    // 7. Форматируем заголовки
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
                              .setStartRowIndex(0)
                              .setEndRowIndex(1))
                      .setCell(
                          new CellData()
                              .setUserEnteredFormat(
                                  new CellFormat().setTextFormat(new TextFormat().setBold(true))))
                      .setFields("userEnteredFormat.textFormat.bold")));
    }

    if (!formatRequests.isEmpty()) {
      BatchUpdateSpreadsheetRequest formatBatchRequest =
          new BatchUpdateSpreadsheetRequest().setRequests(formatRequests);
      sheetsService.spreadsheets().batchUpdate(spreadsheetId, formatBatchRequest).execute();
    }

    logger.info("Added sheets for app '" + appName + "' to spreadsheet: " + spreadsheetTitle);
  }

  @Override
  public void updateMonetizationSheets(MonetizationEvent event) throws IOException {
    // 1. Формируем название таблицы
    String developerEmail = getDeveloperEmail(event.getApplicationId());
    String spreadsheetTitle = String.format("Revenue Statistics - %s - 52", developerEmail);

    // 2. Находим таблицу
    String spreadsheetId = findSpreadsheetId(spreadsheetTitle);
    if (spreadsheetId == null) {
      throw new IOException("Spreadsheet not found: " + spreadsheetTitle);
    }

    // 3. Получаем название приложения
    String appName = getApplicationName(event.getApplicationId());

    // 4. Обновляем данные в зависимости от типа события
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
    }

    // 5. Обновляем топ приложений
    updateApplicationsRevenue(spreadsheetId, appName, event.getEventType(), event.getAmount());

    logger.info(
        "Monetization updated for app '"
            + appName
            + "': "
            + event.getEventType()
            + ", amount: "
            + event.getAmount());
  }

  private void handleDownloadEvent(String spreadsheetId, String appName, MonetizationEvent event)
      throws IOException {
    // 1. Добавляем запись в лист Downloads
    appendToSheet(
        spreadsheetId, appName + " Downloads", List.of(event.getUserId(), event.getItemId()));

    // 2. Обновляем доход от загрузок
    updateRevenueColumn(
        spreadsheetId, appName + " Revenue", "revenueFromDownloads", event.getAmount());

    metrics.recordRevenueUpdate();
  }

  private void handlePurchaseEvent(String spreadsheetId, String appName, MonetizationEvent event)
      throws IOException {
    // 1. Добавляем запись в лист Microtransactions
    appendToSheet(
        spreadsheetId,
        appName + " Microtransactions",
        List.of(event.getUserId(), event.getItemId()));

    // 2. Обновляем доход от покупок
    updateRevenueColumn(
        spreadsheetId, appName + " Revenue", "revenueFromMicrotransactions", event.getAmount());

    metrics.recordRevenueUpdate();
  }

  private void handleAdViewEvent(String spreadsheetId, String appName, MonetizationEvent event)
      throws IOException {
    // 1. Добавляем запись в лист Watched Adds
    appendToSheet(
        spreadsheetId, appName + " Watched Adds", List.of(event.getUserId(), event.getItemId()));

    // 2. Обновляем доход от рекламы
    updateRevenueColumn(spreadsheetId, appName + " Revenue", "revenueFromAdds", event.getAmount());

    metrics.recordRevenueUpdate();
  }

  private void appendToSheet(String spreadsheetId, String sheetName, List<Object> rowData)
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
      String spreadsheetId, String sheetName, String column, double amount) throws IOException {
    // 1. Определяем столбец для обновления
    String range =
        switch (column) {
          case "revenueFromAdds" -> "B2";
          case "revenueFromDownloads" -> "C2";
          case "revenueFromMicrotransactions" -> "D2";
          default -> throw new IllegalArgumentException("Invalid column: " + column);
        };

    // 2. Получаем текущее значение
    ValueRange currentValue =
        sheetsService.spreadsheets().values().get(spreadsheetId, sheetName + "!" + range).execute();

    double currentAmount = 0;
    if (currentValue.getValues() != null && !currentValue.getValues().isEmpty()) {
      currentAmount = Double.parseDouble(currentValue.getValues().get(0).get(0).toString());
    }

    // 3. Обновляем значение
    ValueRange newValue =
        new ValueRange().setValues(List.of(List.of(Double.toString(currentAmount + amount))));

    sheetsService
        .spreadsheets()
        .values()
        .update(spreadsheetId, sheetName + "!" + range, newValue)
        .setValueInputOption("USER_ENTERED")
        .execute();

    // 4. Обновляем общий доход
    updateTotalRevenue(spreadsheetId, sheetName);
  }

  private void updateTotalRevenue(String spreadsheetId, String sheetName) throws IOException {
    // 1. Получаем все доходы
    ValueRange revenues =
        sheetsService.spreadsheets().values().get(spreadsheetId, sheetName + "!B2:D2").execute();

    double total = 0;
    if (revenues.getValues() != null) {
      for (Object value : revenues.getValues().get(0)) {
        if (value.toString().isEmpty()) value = "0";
        total += Double.parseDouble(value.toString());
      }
    }

    // 2. Обновляем общий доход
    ValueRange totalValue = new ValueRange().setValues(List.of(List.of(total)));

    sheetsService
        .spreadsheets()
        .values()
        .update(spreadsheetId, sheetName + "!E2", totalValue)
        .setValueInputOption("USER_ENTERED")
        .execute();
  }

  private void updateApplicationsRevenue(
      String spreadsheetId, String appName, MonetizationEvent.EventType eventType, double amount)
      throws IOException {
    // 1. Получаем текущие данные из листа приложения
    ValueRange appRevenue =
        sheetsService
            .spreadsheets()
            .values()
            .get(spreadsheetId, appName + " Revenue!B2:E2")
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
        revenueData.get(2) == null || revenueData.get(2).toString().trim().isEmpty()
            ? 0.0
            : Double.parseDouble(revenueData.get(2).toString());

    double totalRevenue =
        revenueData.get(3) == null || revenueData.get(3).toString().trim().isEmpty()
            ? 0.0
            : Double.parseDouble(revenueData.get(3).toString());

    // 2. Находим строку приложения в ApplicationsRevenue и получаем ID
    ValueRange apps =
        sheetsService
            .spreadsheets()
            .values()
            .get(spreadsheetId, "ApplicationsRevenue!A2:B") // Получаем ID и название приложений
            .execute();

    int rowNumber = -1;
    String appId = null;
    if (apps.getValues() != null) {
      for (int i = 0; i < apps.getValues().size(); i++) {
        if (apps.getValues().get(i).size() >= 2
            && apps.getValues().get(i).get(1).toString().equals(appName)) {
          rowNumber = i + 2; // +2 потому что A2 это первая строка данных
          appId = apps.getValues().get(i).get(0).toString();
          break;
        }
      }
    }

    // 3. Обновляем данные в строке приложения
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
      logger.error("Application " + appName + " not found in ApplicationsRevenue to update.");
    }

    // 4. Сортируем таблицу по Total Revenue (Quartz job сделает это позже)
  }

  private String findSpreadsheetId(String title) throws IOException {
    FileList files =
        driveService
            .files()
            .list()
            .setQ("name='" + title + "' and mimeType='application/vnd.google-apps.spreadsheet'")
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute();

    return files.getFiles().isEmpty() ? null : files.getFiles().get(0).getId();
  }

  private String getDeveloperEmail(int applicationId) {
    // Здесь должна быть логика получения email разработчика приложения
    // В реальной реализации нужно использовать репозиторий
    return "yee.eey.yeeeey.yee.eey@gmail.com";
  }

  private String getApplicationName(int applicationId) {
    // Здесь должна быть логика получения названия приложения
    // В реальной реализации нужно использовать репозиторий
    return "My App";
  }

  private void updateSingleTop(String spreadsheetId, List<AppRevenue> allAppsRevenue)
      throws IOException {
    String topSheetName = "ApplicationsRevenueTop";
    List<List<Object>> values = new ArrayList<>();
    values.add(List.of("Rank", "Application", "Total Revenue"));

    for (int i = 0; i < allAppsRevenue.size(); i++) {
      AppRevenue app = allAppsRevenue.get(i);
      values.add(List.of(i + 1, app.getAppName(), app.getTotalRevenue()));
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
      logger.error("Error updating sheet " + topSheetName + ": " + e.getDetails().getMessage());
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
                "mimeType='application/vnd.google-apps.spreadsheet' and name contains 'Revenue Statistics'")
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
          if (row.size() >= 5) {
            String appName = row.get(0).toString();
            double totalRevenue = Double.parseDouble(row.get(4).toString());
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

    logger.info("Apps top updated for " + spreadsheets.getFiles().size() + " spreadsheets");
  }

  private void formatTopSheet(String spreadsheetId) throws IOException {
    List<com.google.api.services.sheets.v4.model.Request> requests = new ArrayList<>();

    requests.add(
        new com.google.api.services.sheets.v4.model.Request()
            .setRepeatCell(
                new RepeatCellRequest()
                    .setRange(
                        new GridRange()
                            .setSheetId(0)
                            .setStartRowIndex(0)
                            .setEndRowIndex(1)
                            .setStartColumnIndex(0)
                            .setEndColumnIndex(3))
                    .setCell(
                        new CellData()
                            .setUserEnteredFormat(
                                new CellFormat()
                                    .setTextFormat(new TextFormat().setBold(true))
                                    .setBackgroundColor(
                                        new Color().setRed(0.9f).setGreen(0.9f).setBlue(0.9f))))
                    .setFields("userEnteredFormat")));

    requests.add(
        new com.google.api.services.sheets.v4.model.Request()
            .setAddConditionalFormatRule(
                new AddConditionalFormatRuleRequest()
                    .setRule(
                        new ConditionalFormatRule()
                            .setRanges(
                                List.of(
                                    new GridRange()
                                        .setSheetId(0)
                                        .setStartRowIndex(1)
                                        .setEndRowIndex(1000)
                                        .setStartColumnIndex(0)
                                        .setEndColumnIndex(3)))
                            .setBooleanRule(
                                new BooleanRule()
                                    .setCondition(
                                        new BooleanCondition()
                                            .setType("MOD")
                                            .setValues(
                                                List.of(
                                                    new ConditionValue().setUserEnteredValue("2"),
                                                    new ConditionValue().setUserEnteredValue("0"))))
                                    .setFormat(
                                        new CellFormat()
                                            .setBackgroundColor(
                                                new Color()
                                                    .setRed(0.95f)
                                                    .setGreen(0.95f)
                                                    .setBlue(0.95f)))))));

    BatchUpdateSpreadsheetRequest batchUpdateRequest =
        new BatchUpdateSpreadsheetRequest().setRequests(requests);

    sheetsService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
  }

  private static class AppRevenue {
    private final String appName;
    private final double totalRevenue;
    private final String spreadsheetId;
    private final String revenueSheetName;

    public AppRevenue(
        String appName, double totalRevenue, String spreadsheetId, String revenueSheetName) {
      this.appName = appName;
      this.totalRevenue = totalRevenue;
      this.spreadsheetId = spreadsheetId;
      this.revenueSheetName = revenueSheetName;
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
