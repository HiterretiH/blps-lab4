package org.lab3.google.resource;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ConfigProperty;
import jakarta.resource.spi.Connector;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.TransactionSupport;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import java.util.logging.Logger;
import javax.transaction.xa.XAResource;

@Connector(
    displayName = "Google JCA Adapter",
    vendorName = "Google Inc.",
    version = "1.0",
    transactionSupport = TransactionSupport
        .TransactionSupportLevel.NoTransaction
)
public final class GoogleResourceAdapter implements ResourceAdapter {
  private static final Logger LOG =
      Logger.getLogger(GoogleResourceAdapter.class.getName());

  private String googleClientId;
  private String googleClientSecret;

  @ConfigProperty(type = String.class, description = "Google API Client ID")
  public void setGoogleClientId(final String googleClientIdParam) {
    this.googleClientId = googleClientIdParam;
  }

  @ConfigProperty(type = String.class,
      description = "Google API Client Secret")
  public void setGoogleClientSecret(
      final String googleClientSecretParam) {
    this.googleClientSecret = googleClientSecretParam;
  }

  @Override
  public void start(final BootstrapContext bootstrapContext)
      throws ResourceAdapterInternalException {
    LOG.info("Google JCA Adapter started with clientId: "
        + googleClientId);
  }

  @Override
  public void stop() {
    LOG.info("Google JCA Adapter stopped");
  }

  @Override
  public void endpointActivation(
      final MessageEndpointFactory messageEndpointFactory,
      final jakarta.resource.spi.ActivationSpec activationSpec)
      throws ResourceException {
    LOG.info("Endpoint activated: " + messageEndpointFactory);
  }

  @Override
  public void endpointDeactivation(
      final MessageEndpointFactory messageEndpointFactory,
      final jakarta.resource.spi.ActivationSpec activationSpec) {
    LOG.info("Endpoint deactivated: " + messageEndpointFactory);
  }

  @Override
  public XAResource[] getXAResources(
      final jakarta.resource.spi.ActivationSpec[] activationSpecs) {
    return new XAResource[0];
  }
}
