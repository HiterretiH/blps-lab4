package org.lab3.google.resource;

import jakarta.resource.spi.*;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;

import javax.transaction.xa.XAResource;
import java.util.logging.Logger;

@Connector(
        displayName = "Google JCA Adapter",
        vendorName = "Google Inc.",
        version = "1.0"
)
public class GoogleResourceAdapter implements ResourceAdapter {
    private static final Logger LOG = Logger.getLogger(GoogleResourceAdapter.class.getName());

    private String googleClientId;
    private String googleClientSecret;

    @ConfigProperty(
            type = String.class,
            description = "Google API Client ID"
    )
    public void setGoogleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
    }

    @ConfigProperty(
            type = String.class,
            description = "Google API Client Secret"
    )
    public void setGoogleClientSecret(String googleClientSecret) {
        this.googleClientSecret = googleClientSecret;
    }

    @Override
    public void start(BootstrapContext bootstrapContext) {
        LOG.info("Google JCA Adapter started with clientId: " + googleClientId);
    }

    @Override
    public void stop() {
        LOG.info("Google JCA Adapter stopped");
    }

    @Override
    public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        LOG.info("Endpoint activated: " + messageEndpointFactory);
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        LOG.info("Endpoint deactivated: " + messageEndpointFactory);
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) {
        return new XAResource[0];
    }
}