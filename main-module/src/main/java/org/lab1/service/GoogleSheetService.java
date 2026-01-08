package org.lab1.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.lab.logger.Logger;
import org.lab1.exception.ForbiddenException;
import org.lab1.exception.NotFoundException;
import org.lab1.exception.OAuthException;
import org.lab1.exception.UnauthorizedException;
import org.lab1.json.GoogleSheetRequestWithData;
import org.lab1.model.Application;
import org.lab1.model.MonetizedApplication;
import org.lab1.model.Role;
import org.lab1.model.User;
import org.lab1.repository.ApplicationRepository;
import org.lab1.repository.MonetizedApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleSheetService {
  private static final String CREATE_REVENUE_SHEET_LOG = "Creating revenue sheet for user ID: ";
  private static final String NOT_CONNECTED_LOG = "User ID ";
  private static final String NOT_CONNECTED_GOOGLE_LOG = " has not connected Google account.";
  private static final String NOT_CONNECTED_MSG = "User has not connected Google account";
  private static final String REVENUE_SHEET_TITLE_PREFIX = "Revenue Statistics - ";
  private static final String REVENUE_SHEET_TITLE_SUFFIX = " - 52";
  private static final String ID_COLUMN = "ID";
  private static final String APPLICATION_COLUMN = "Application";
  private static final String ADS_REVENUE_COLUMN = "Ads Revenue";
  private static final String DOWNLOAD_REVENUE_COLUMN = "Download Revenue";
  private static final String PURCHASES_REVENUE_COLUMN = "Purchases Revenue";
  private static final String TOTAL_REVENUE_COLUMN = "Total Revenue";
  private static final String REVENUE_SHEET_CREATION_LOG =
      "Revenue sheet creation request sent for user ID: ";
  private static final String TO_EMAIL_LOG = " to email: ";
  private static final String TITLE_LOG = ", title: ";
  private static final String ADD_APP_SHEETS_LOG = "Adding app sheets for user ID: ";
  private static final String APP_ID_LOG = ", app ID: ";
  private static final String NOT_DEVELOPER_LOG = "User ID ";
  private static final String NOT_DEVELOPER_MSG = " is not a developer.";
  private static final String DEVELOPER_ONLY_MSG = "Only developers can add app sheets";
  private static final String APP_NOT_FOUND_LOG = "Application not found with ID: ";
  private static final String APP_NOT_FOUND_MSG = "Application not found";
  private static final String APP_NOT_BELONG_LOG = "Application ID ";
  private static final String NOT_BELONG_TO_DEV_LOG = " does not belong to developer ID ";
  private static final String NOT_BELONG_MSG = "Application doesn't belong to this developer";
  private static final String GOOGLE_CONNECTION_FAILED_LOG =
      "Google connection failed for user ID: ";
  private static final String REASON_LOG = ". Reason: ";
  private static final String GOOGLE_CONNECTION_FAILED_MSG = "Google connection failed";
  private static final String APP_SHEETS_CREATION_LOG =
      "App sheets creation request sent for app: ";
  private static final String USER_ID_LOG = ", user ID: ";
  private static final String TRIGGER_UPDATE_LOG = "Triggering update apps top request.";
  private static final String UPDATE_REQUEST_SENT_LOG = "Update apps top request sent.";

  private final GoogleTaskSender googleTaskSender;
  private final GoogleOAuthQueryService googleOAuthQueryService;
  private final UserQueryService userQueryService;
  private final MonetizedApplicationRepository monetizedApplicationRepository;
  private final ApplicationRepository applicationRepository;
  private final Logger logger;

  @Autowired
  public GoogleSheetService(
      final GoogleTaskSender googleTaskSenderParam,
      final GoogleOAuthQueryService googleOAuthQueryServiceParam,
      final UserQueryService userQueryServiceParam,
      final MonetizedApplicationRepository monetizedApplicationRepositoryParam,
      final ApplicationRepository applicationRepositoryParam,
      final Logger loggerParam) {
    this.googleTaskSender = googleTaskSenderParam;
    this.googleOAuthQueryService = googleOAuthQueryServiceParam;
    this.userQueryService = userQueryServiceParam;
    this.monetizedApplicationRepository = monetizedApplicationRepositoryParam;
    this.applicationRepository = applicationRepositoryParam;
    this.logger = loggerParam;
  }

  public final String createRevenueSheet(final int userId) throws OAuthException {
    logger.info(CREATE_REVENUE_SHEET_LOG + userId);

    if (!googleOAuthQueryService.isGoogleConnected(userId)) {
      logger.error(NOT_CONNECTED_LOG + userId + NOT_CONNECTED_GOOGLE_LOG);
      throw new UnauthorizedException(NOT_CONNECTED_MSG);
    }

    String googleEmail = googleOAuthQueryService.getUserGoogleEmail(userId);
    String sheetTitle = REVENUE_SHEET_TITLE_PREFIX + googleEmail + REVENUE_SHEET_TITLE_SUFFIX;
    List<MonetizedApplication> apps = monetizedApplicationRepository.findByDeveloperUserId(userId);

    List<List<Object>> appData =
        apps.stream()
            .map(
                app -> {
                  List<Object> row = new ArrayList<>();
                  row.add(app.getApplication().getId());
                  row.add(app.getApplication().getName());
                  row.add(app.getAdsRevenue());
                  row.add(app.getDownloadRevenue());
                  row.add(app.getPurchasesRevenue());
                  row.add(app.getRevenue());
                  return row;
                })
            .collect(Collectors.toList());

    GoogleSheetRequestWithData request =
        new GoogleSheetRequestWithData(
            googleEmail,
            sheetTitle,
            List.of(
                ID_COLUMN,
                APPLICATION_COLUMN,
                ADS_REVENUE_COLUMN,
                DOWNLOAD_REVENUE_COLUMN,
                PURCHASES_REVENUE_COLUMN,
                TOTAL_REVENUE_COLUMN),
            appData);

    googleTaskSender.sendSheetCreationRequest(userId, request);
    logger.info(
        REVENUE_SHEET_CREATION_LOG + userId + TO_EMAIL_LOG + googleEmail + TITLE_LOG + sheetTitle);
    return "Revenue sheet creation with data request sent for user: " + userId;
  }

  @Transactional
  public final String addAppSheets(final int userId, final int appId) {
    logger.info(ADD_APP_SHEETS_LOG + userId + APP_ID_LOG + appId);

    try {
      User user = userQueryService.getUserById(userId);
      if (user.getRole() != Role.DEVELOPER) {
        logger.error(NOT_CONNECTED_LOG + userId + NOT_DEVELOPER_MSG);
        throw new ForbiddenException(DEVELOPER_ONLY_MSG);
      }

      Application app =
          applicationRepository
              .findById(appId)
              .orElseThrow(
                  () -> {
                    logger.error(APP_NOT_FOUND_LOG + appId);
                    return new NotFoundException("Application not found with ID: " + appId);
                  });

      if (app.getDeveloper().getUser().getId() != userId) {
        logger.error(APP_NOT_BELONG_LOG + appId + NOT_BELONG_TO_DEV_LOG + userId);
        throw new ForbiddenException(NOT_BELONG_MSG);
      }

      if (!googleOAuthQueryService.isGoogleConnected(userId)) {
        logger.error(NOT_CONNECTED_LOG + userId + NOT_CONNECTED_GOOGLE_LOG);
        throw new UnauthorizedException(NOT_CONNECTED_MSG);
      }

      googleTaskSender.sendAddAppSheetsRequest(userId, app.getName());
      logger.info(APP_SHEETS_CREATION_LOG + app.getName() + USER_ID_LOG + userId);
      return "App sheets creation request sent for app: " + appId;

    } catch (OAuthException oAuthException) {
      logger.error(
          GOOGLE_CONNECTION_FAILED_LOG + userId + REASON_LOG + oAuthException.getMessage());
      throw new UnauthorizedException(GOOGLE_CONNECTION_FAILED_MSG);
    }
  }

  public final void triggerUpdateAppsTop() {
    logger.info(TRIGGER_UPDATE_LOG);
    googleTaskSender.sendUpdateAppsTopRequest();
    logger.info(UPDATE_REQUEST_SENT_LOG);
  }
}
