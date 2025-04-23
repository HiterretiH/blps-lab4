package org.lab3.google;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GoogleConfig {
    private static final String SERVICE_ACCOUNT_JSON = """
            {
              # json with secret key for service account
            }
        """;

    public static GoogleManagedConnection createConnection() throws IOException, GeneralSecurityException {
        return new GoogleManagedConnection(SERVICE_ACCOUNT_JSON);
    }
}