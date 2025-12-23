package org.lab1.controller;

import java.util.List;
import java.util.Optional;
import org.lab.logger.Logger;
import org.lab1.json.InAppAddJson;
import org.lab1.model.InAppAdd;
import org.lab1.service.InAppAddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/in-app-ads")
public final class InAppAddController {
  private static final String CREATE_REQUEST_LOG =
      "Received request to create InAppAdd for MonetizedApplication ID: ";
  private static final String CREATE_SUCCESS_LOG = "InAppAdd created with ID: ";
  private static final String FOR_MONETIZED_APP_LOG = " for MonetizedApplication ID: ";
  private static final String BULK_CREATE_REQUEST_LOG =
      "Received request to create multiple InAppAdds. Count: ";
  private static final String BULK_CREATE_SUCCESS_LOG = "Successfully created ";
  private static final String IN_APP_ADDS_LOG = " InAppAdds.";
  private static final String BULK_CREATE_ERROR_LOG =
      "Failed to create multiple InAppAdds. Reason: ";
  private static final String LIST_REQUEST_LOG = "Received request to list all InAppAdds.";
  private static final String LIST_FOUND_LOG = "Found ";
  private static final String GET_REQUEST_LOG = "Received request to get InAppAdd by ID: ";
  private static final String GET_NOT_FOUND_LOG = "InAppAdd not found with ID: ";
  private static final String GET_BY_APP_REQUEST_LOG =
      "Received request to get InAppAdds by MonetizedApplication ID: ";
  private static final String GET_BY_APP_FOUND_LOG = "Found ";
  private static final String FOR_APP_ID_LOG = " for MonetizedApplication ID: ";

  private final InAppAddService inAppAddService;
  private final Logger logger;

  @Autowired
  public InAppAddController(final InAppAddService inAppAddServiceParam, final Logger loggerParam) {
    this.inAppAddService = inAppAddServiceParam;
    this.logger = loggerParam;
  }

  @PreAuthorize("hasAuthority('in_app_add.manage')")
  @PostMapping("/create")
  public ResponseEntity<InAppAdd> createInAppAdd(@RequestBody final InAppAddJson inAppAddJson) {
    logger.info(CREATE_REQUEST_LOG + inAppAddJson.getMonetizedApplicationId());
    InAppAdd inAppAdd = inAppAddService.createInAppAdd(inAppAddJson);
    logger.info(
        CREATE_SUCCESS_LOG
            + inAppAdd.getId()
            + FOR_MONETIZED_APP_LOG
            + inAppAdd.getMonetizedApplication().getId());
    return ResponseEntity.ok(inAppAdd);
  }

  @PreAuthorize("hasAuthority('in_app_add.manage')")
  @PostMapping("/bulk")
  public ResponseEntity<List<InAppAdd>> createMultipleInAppAdds(
      @RequestBody final List<InAppAddJson> inAppAddJsons) {
    int count = inAppAddJsons != null ? inAppAddJsons.size() : 0;
    logger.info(BULK_CREATE_REQUEST_LOG + count);

    try {
      List<InAppAdd> inAppAdds = inAppAddService.createMultipleInAppAdds(inAppAddJsons);
      logger.info(BULK_CREATE_SUCCESS_LOG + inAppAdds.size() + IN_APP_ADDS_LOG);
      return ResponseEntity.status(HttpStatus.CREATED).body(inAppAdds);
    } catch (IllegalArgumentException exception) {
      logger.error(BULK_CREATE_ERROR_LOG + exception.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }
  }

  @PreAuthorize("hasAuthority('in_app_add.read')")
  @GetMapping("/list")
  public ResponseEntity<List<InAppAdd>> getAllInAppAds() {
    logger.info(LIST_REQUEST_LOG);
    List<InAppAdd> inAppAdds = inAppAddService.getAllInAppAds();
    logger.info(LIST_FOUND_LOG + inAppAdds.size() + IN_APP_ADDS_LOG);
    return ResponseEntity.ok(inAppAdds);
  }

  @PreAuthorize("hasAuthority('in_app_add.read')")
  @GetMapping("get/{id}")
  public ResponseEntity<InAppAdd> getInAppAddById(@PathVariable final int id) {
    logger.info(GET_REQUEST_LOG + id);
    Optional<InAppAdd> inAppAdd = inAppAddService.getInAppAddById(id);

    if (inAppAdd.isPresent()) {
      return ResponseEntity.ok(inAppAdd.get());
    }

    logger.info(GET_NOT_FOUND_LOG + id);
    return ResponseEntity.notFound().build();
  }

  @PreAuthorize("hasAuthority('in_app_add.read')")
  @GetMapping("/monetized/{monetizedApplicationId}")
  public ResponseEntity<List<InAppAdd>> getInAppAdsByMonetizedApplication(
      @PathVariable final int monetizedApplicationId) {
    logger.info(GET_BY_APP_REQUEST_LOG + monetizedApplicationId);
    List<InAppAdd> inAppAdds =
        inAppAddService.getInAppAddByMonetizedApplication(monetizedApplicationId);
    logger.info(GET_BY_APP_FOUND_LOG + inAppAdds.size() + FOR_APP_ID_LOG + monetizedApplicationId);
    return ResponseEntity.ok(inAppAdds);
  }
}
