package org.lab3.google;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GoogleConfig {
    private static final String SERVICE_ACCOUNT_JSON = """
            {
              "type": "service_account",
              "project_id": "",
              "private_key_id": "",
              "private_key": "",
              "client_email": "",
              "client_id": "",
              "auth_uri": "https://accounts.google.com/o/oauth2/auth",
              "token_uri": "https://oauth2.googleapis.com/token",
              "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
              "client_x509_cert_url": "",
              "universe_domain": "googleapis.com"
            }
        """;

    public static GoogleManagedConnection createConnection() throws IOException, GeneralSecurityException {
        return new GoogleManagedConnection(SERVICE_ACCOUNT_JSON);
    }
}