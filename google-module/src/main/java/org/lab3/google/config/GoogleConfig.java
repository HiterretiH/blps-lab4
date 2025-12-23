package org.lab3.google.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import org.lab3.google.resource.GoogleManagedConnection;

public final class GoogleConfig {
  private static final String GOOGLE_CREDENTIALS_PATH_KEY =
      "GOOGLE_CREDENTIALS_PATH";
  private static final String DEFAULT_GOOGLE_CREDENTIALS_PATH =
      "/app/credentials.json";

  private GoogleConfig() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static GoogleManagedConnection createConnection()
      throws IOException, GeneralSecurityException {
    String credentialsPath = EnvConfig.get(
        GOOGLE_CREDENTIALS_PATH_KEY, DEFAULT_GOOGLE_CREDENTIALS_PATH);
    Path path = Paths.get(credentialsPath);

    if (!Files.exists(path)) {
      throw new IOException(
          "Google credentials file not found at: " + credentialsPath);
    }

    String credentialsJson = Files.readString(path);
    return new GoogleManagedConnection(credentialsJson);
  }
}
