package org.lab1.service;

import java.util.Optional;
import org.lab.logger.Logger;
import org.lab1.model.PaymentRequest;
import org.lab1.repository.PaymentRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PaymentRequestService {
  private static final String CREATE_REQUEST_LOG = "Creating PaymentRequest for application ID: ";
  private static final String AMOUNT_LOG = ", amount: ";
  private static final String CREATED_REQUEST_LOG = "PaymentRequest created with ID: ";
  private static final String FETCH_REQUEST_LOG = "Fetching PaymentRequest by application ID: ";
  private static final String FOUND_REQUEST_LOG = "PaymentRequest found for application ID: ";
  private static final String NOT_FOUND_REQUEST_LOG =
      "PaymentRequest not found for application ID: ";
  private static final String VALIDATE_CARD_LOG = "Validating card for PaymentRequest ID: ";
  private static final String VALIDATION_RESULT_LOG =
      "Card validation result for PaymentRequest ID: ";
  private static final String COLON_SEPARATOR = ": ";

  private final PaymentRequestRepository paymentRequestRepository;
  private final Logger logger;

  @Autowired
  public PaymentRequestService(
      final PaymentRequestRepository paymentRequestRepositoryParam,
      @Qualifier("correlationLogger") final Logger loggerParam) {
    this.paymentRequestRepository = paymentRequestRepositoryParam;
    this.logger = loggerParam;
  }

  public final PaymentRequest createPaymentRequest(final int applicationId, final double amount) {
    logger.info(CREATE_REQUEST_LOG + applicationId + AMOUNT_LOG + amount);
    PaymentRequest paymentRequest = new PaymentRequest(applicationId, amount);
    PaymentRequest savedRequest = paymentRequestRepository.save(paymentRequest);
    logger.info(
        CREATED_REQUEST_LOG
            + savedRequest.getApplicationId()
            + AMOUNT_LOG
            + savedRequest.getAmount());
    return savedRequest;
  }

  public final Optional<PaymentRequest> getPaymentRequestById(final int applicationId) {
    logger.info(FETCH_REQUEST_LOG + applicationId);
    Optional<PaymentRequest> paymentRequest = paymentRequestRepository.findById(applicationId);

    if (paymentRequest.isPresent()) {
      logger.info(
          FOUND_REQUEST_LOG + applicationId + AMOUNT_LOG + paymentRequest.get().getAmount());
    } else {
      logger.info(NOT_FOUND_REQUEST_LOG + applicationId);
    }

    return paymentRequest;
  }

  public final boolean validateCard(final PaymentRequest paymentRequest) {
    logger.info(VALIDATE_CARD_LOG + paymentRequest.getApplicationId());
    boolean isValid = paymentRequest.isCardValid();
    logger.info(
        VALIDATION_RESULT_LOG + paymentRequest.getApplicationId() + COLON_SEPARATOR + isValid);
    return isValid;
  }
}
