package org.lab1.controller;

import java.util.List;
import java.util.Optional;
import org.lab.logger.Logger;
import org.lab1.json.InAppPurchasesJson;
import org.lab1.model.InAppPurchase;
import org.lab1.service.InAppPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/in-app-purchases")
public class InAppPurchaseController {
  private static final String CREATE_REQUEST_LOG =
      "Received request to create InAppPurchases. Titles count: ";
  private static final String DESCRIPTIONS_COUNT_LOG = ", Descriptions count: ";
  private static final String PRICES_COUNT_LOG = ", Prices count: ";
  private static final String CREATE_SUCCESS_LOG = "Successfully created ";
  private static final String IN_APP_PURCHASES_LOG = " InAppPurchases.";
  private static final String CREATE_ERROR_LOG = "Failed to create InAppPurchases. Reason: ";
  private static final String LIST_REQUEST_LOG = "Received request to list all InAppPurchases.";
  private static final String LIST_FOUND_LOG = "Found ";
  private static final String GET_REQUEST_LOG = "Received request to get InAppPurchase by ID: ";
  private static final String GET_NOT_FOUND_LOG = "InAppPurchase not found with ID: ";
  private static final String LINK_REQUEST_LOG =
      "Received request to link InAppPurchases to MonetizedApplication ID: ";
  private static final String LINK_SUCCESS_LOG = "Successfully linked ";
  private static final String TO_APP_ID_LOG = " InAppPurchases to MonetizedApplication ID: ";
  private static final String LINK_ERROR_LOG =
      "Failed to link InAppPurchases to MonetizedApplication ID: ";
  private static final String REASON_LOG = ". Reason: ";

  private final InAppPurchaseService inAppPurchaseService;
  private final Logger logger;

  @Autowired
  public InAppPurchaseController(
      final InAppPurchaseService inAppPurchaseServiceParam, final Logger loggerParam) {
    this.inAppPurchaseService = inAppPurchaseServiceParam;
    this.logger = loggerParam;
  }

  @PreAuthorize("hasAuthority('in_app_purchase.manage')")
  @PostMapping("/create")
  public ResponseEntity<List<InAppPurchase>> createInAppPurchases(
      @RequestBody final InAppPurchasesJson inAppPurchases) {
    int titlesCount = inAppPurchases.getTitles() != null ? inAppPurchases.getTitles().size() : 0;
    int descriptionsCount =
        inAppPurchases.getDescriptions() != null ? inAppPurchases.getDescriptions().size() : 0;
    int pricesCount = inAppPurchases.getPrices() != null ? inAppPurchases.getPrices().size() : 0;

    logger.info(
        CREATE_REQUEST_LOG
            + titlesCount
            + DESCRIPTIONS_COUNT_LOG
            + descriptionsCount
            + PRICES_COUNT_LOG
            + pricesCount);

    try {
      List<InAppPurchase> purchases =
          inAppPurchaseService.createInAppPurchases(
              inAppPurchases.getTitles(),
              inAppPurchases.getDescriptions(),
              inAppPurchases.getPrices());
      logger.info(CREATE_SUCCESS_LOG + purchases.size() + IN_APP_PURCHASES_LOG);
      return ResponseEntity.ok(purchases);
    } catch (IllegalArgumentException exception) {
      logger.error(CREATE_ERROR_LOG + exception.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  @PreAuthorize("hasAuthority('in_app_purchase.read')")
  @GetMapping("/all")
  public ResponseEntity<List<InAppPurchase>> getAllInAppPurchases() {
    logger.info(LIST_REQUEST_LOG);
    List<InAppPurchase> purchases = inAppPurchaseService.getAllInAppPurchases();
    logger.info(LIST_FOUND_LOG + purchases.size() + IN_APP_PURCHASES_LOG);
    return ResponseEntity.ok(purchases);
  }

  @PreAuthorize("hasAuthority('in_app_purchase.read')")
  @GetMapping("/{id}")
  public ResponseEntity<InAppPurchase> getInAppPurchaseById(@PathVariable final int id) {
    logger.info(GET_REQUEST_LOG + id);
    Optional<InAppPurchase> purchase = inAppPurchaseService.getInAppPurchaseById(id);

    if (purchase.isPresent()) {
      return ResponseEntity.ok(purchase.get());
    }

    logger.info(GET_NOT_FOUND_LOG + id);
    return ResponseEntity.notFound().build();
  }

  @PreAuthorize("hasAuthority('in_app_purchase.manage')")
  @PostMapping("/link-to-monetized-app/{monetizedApplicationId}")
  public ResponseEntity<List<InAppPurchase>> linkMonetizedAppToPurchases(
      @PathVariable final int monetizedApplicationId) {
    logger.info(LINK_REQUEST_LOG + monetizedApplicationId);

    try {
      List<InAppPurchase> linkedPurchases =
          inAppPurchaseService.linkMonetizedAppToPurchases(monetizedApplicationId);
      logger.info(
          LINK_SUCCESS_LOG + linkedPurchases.size() + TO_APP_ID_LOG + monetizedApplicationId);
      return ResponseEntity.ok(linkedPurchases);
    } catch (RuntimeException exception) {
      logger.error(LINK_ERROR_LOG + monetizedApplicationId + REASON_LOG + exception.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }
}
