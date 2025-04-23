package org.lab3.google;

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
    private static final Logger log = Logger.getLogger(GoogleResourceAdapter.class.getName());

    private String googleClientId;
    private String googleClientSecret;

    @ConfigProperty(
            type = String.class,
            defaultValue = "",
            description = "Google API Client ID"
    )
    public void setGoogleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
    }

    @ConfigProperty(
            type = String.class,
            defaultValue = "",
            description = "Google API Client Secret"
    )
    public void setGoogleClientSecret(String googleClientSecret) {
        this.googleClientSecret = googleClientSecret;
    }

    @Override
    public void start(BootstrapContext ctx) {
        log.info("Google JCA Adapter started with clientId: " + googleClientId);
    }

    @Override
    public void stop() {
        log.info("Google JCA Adapter stopped");
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        log.info("Endpoint activated: " + endpointFactory);
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        log.info("Endpoint deactivated: " + endpointFactory);
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) {
        return new XAResource[0];
    }
}