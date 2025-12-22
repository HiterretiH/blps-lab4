package org.lab1.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvConfig {
  private static final String DEFAULT_APP_DIRECTORY = "/app";
  private static final Dotenv DOTENV = Dotenv.configure().directory(DEFAULT_APP_DIRECTORY).load();

  public static String get(String key) {
    String value = DOTENV.get(key);
    return value != null ? value : System.getenv(key);
  }

  public static String get(String key, String defaultValue) {
    String value = get(key);
    return value != null ? value : defaultValue;
  }
}
