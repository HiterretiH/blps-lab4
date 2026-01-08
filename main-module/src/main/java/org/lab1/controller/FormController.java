package org.lab1.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Map;
import org.lab.logger.Logger;
import org.lab1.exception.ValidationException;
import org.lab1.model.User;
import org.lab1.service.FormGenerationService;
import org.lab1.service.UserQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forms")
public class FormController {
  private static final String GENERATE_FORM_REQUEST_LOG =
      "Received request to generate Google Form.";
  private static final String USER_NOT_FOUND_LOG = "User not found: ";
  private static final String GENERATE_FORM_SUCCESS_LOG =
      "Google Form generation initiated for user ID: ";
  private static final String RESULT_LOG = ". Result: ";
  private static final String GENERATE_FORM_ERROR_LOG =
      "Error generating Google Form for user ID: ";
  private static final String GENERATE_FIELDS_REQUEST_LOG =
      "Received request to generate form fields.";
  private static final String GENERATED_FIELDS_LOG = "Generated ";
  private static final String FORM_FIELDS_LOG = " form fields.";
  private static final String ADD_FIELD_REQUEST_LOG = "Received request to add field: ";
  private static final String FIELD_NAME_REQUIRED_LOG = "Field name is required.";
  private static final String FIELD_ADDED_SUCCESS_LOG = "Field '";
  private static final String ADDED_SUCCESSFULLY_LOG = "' added successfully.";
  private static final String ADD_FIELDS_REQUEST_LOG = "Received request to add multiple fields.";
  private static final String FIELDS_REQUIRED_LOG = "Field names are required.";
  private static final String NO_VALID_FIELDS_LOG = "No valid field names provided.";
  private static final String FIELDS_ADDED_SUCCESS_LOG = "Added ";
  private static final String FIELDS_SUCCESSFULLY_LOG = " fields successfully.";
  private static final String FIELD_NAME_KEY = "fieldName";
  private static final String FORMS_GENERATED_METRIC = "forms.generated.total";
  private static final String FIELDS_ADDED_METRIC = "fields.added.total";
  private static final String FORMS_GENERATED_DESCRIPTION = "Total number of forms generated";
  private static final String FIELDS_ADDED_DESCRIPTION = "Total number of fields added";
  private static final String FIELD_NAME_REQUIRED_MESSAGE = "Field name is required";
  private static final String NO_VALID_FIELDS_MESSAGE = "No valid field names provided";
  private static final String FIELDS_ADDED_MESSAGE = "Fields added successfully";
  private static final String FIELD_ADDED_MESSAGE = "Field added successfully";
  private static final String REASON_LOG = ". Reason: ";

  private final FormGenerationService formGenerationService;
  private final UserQueryService userQueryService;
  private final Counter formsGeneratedCounter;
  private final Counter fieldsAddedCounter;
  private final Logger logger;

  @Autowired
  public FormController(
      final FormGenerationService formGenerationServiceParam,
      final UserQueryService userQueryServiceParam,
      final MeterRegistry meterRegistry,
      final Logger loggerParam) {
    this.formGenerationService = formGenerationServiceParam;
    this.userQueryService = userQueryServiceParam;
    this.formsGeneratedCounter =
        Counter.builder(FORMS_GENERATED_METRIC)
            .description(FORMS_GENERATED_DESCRIPTION)
            .register(meterRegistry);
    this.fieldsAddedCounter =
        Counter.builder(FIELDS_ADDED_METRIC)
            .description(FIELDS_ADDED_DESCRIPTION)
            .register(meterRegistry);
    this.logger = loggerParam;
  }

  @PreAuthorize("hasAuthority('form.create')")
  @PostMapping("/create")
  public ResponseEntity<String> generateGoogleForm() {
    logger.info(GENERATE_FORM_REQUEST_LOG);

    try {
      User user = userQueryService.getCurrentAuthenticatedUser();
      int userId = user.getId();

      String result = formGenerationService.generateAndSendGoogleForm(userId);
      formsGeneratedCounter.increment();
      logger.info(GENERATE_FORM_SUCCESS_LOG + userId + RESULT_LOG + result);
      return ResponseEntity.ok(result);
    } catch (Exception exception) {
      logger.error(GENERATE_FORM_ERROR_LOG + exception.getMessage());
      throw new RuntimeException("Error generating form: " + exception.getMessage());
    }
  }

  @PreAuthorize("hasAuthority('form.read')")
  @GetMapping("/generate")
  public ResponseEntity<Map<String, String>> generateForm() {
    logger.info(GENERATE_FIELDS_REQUEST_LOG);
    Map<String, String> formFields = formGenerationService.generateFormFields();
    logger.info(GENERATED_FIELDS_LOG + formFields.size() + FORM_FIELDS_LOG);
    return ResponseEntity.ok(formFields);
  }

  @PreAuthorize("hasAuthority('form.manage')")
  @PostMapping("/addField")
  public ResponseEntity<String> addField(@RequestBody final Map<String, String> fieldRequest) {
    String fieldName = fieldRequest.get(FIELD_NAME_KEY);
    logger.info(ADD_FIELD_REQUEST_LOG + fieldName);

    if (fieldName == null || fieldName.trim().isEmpty()) {
      logger.error(FIELD_NAME_REQUIRED_LOG);
      throw new ValidationException(FIELD_NAME_REQUIRED_MESSAGE);
    }

    formGenerationService.addField(fieldName);
    fieldsAddedCounter.increment();
    logger.info(FIELD_ADDED_SUCCESS_LOG + fieldName + ADDED_SUCCESSFULLY_LOG);
    return ResponseEntity.ok(FIELD_ADDED_MESSAGE);
  }

  @PreAuthorize("hasAuthority('form.manage')")
  @PostMapping("/addFields")
  public ResponseEntity<String> addFields(
      @RequestBody final List<Map<String, String>> fieldsRequest) {
    logger.info(ADD_FIELDS_REQUEST_LOG);

    if (fieldsRequest == null || fieldsRequest.isEmpty()) {
      logger.error(FIELDS_REQUIRED_LOG);
      throw new ValidationException("Field names are required");
    }

    List<String> fieldNames =
        fieldsRequest.stream()
            .map(fieldMap -> fieldMap.get(FIELD_NAME_KEY))
            .filter(fieldName -> fieldName != null && !fieldName.trim().isEmpty())
            .toList();

    if (fieldNames.isEmpty()) {
      logger.error(NO_VALID_FIELDS_LOG);
      throw new ValidationException(NO_VALID_FIELDS_MESSAGE);
    }

    formGenerationService.addFields(fieldNames);
    fieldsAddedCounter.increment(fieldNames.size());
    logger.info(FIELDS_ADDED_SUCCESS_LOG + fieldNames.size() + FIELDS_SUCCESSFULLY_LOG);
    return ResponseEntity.ok(FIELDS_ADDED_MESSAGE);
  }
}
