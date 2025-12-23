package org.lab1.config;

import io.github.cdimascio.dotenv.Dotenv;

public final class EnvConfig {
  private static final String DEFAULT_APP_DIRECTORY = "/app";
  private static final Dotenv DOTENV = Dotenv.configure().directory(DEFAULT_APP_DIRECTORY).load();

  private EnvConfig() {
    throw new UnsupportedOperationException("🚫 Утилитарный класс! Не создавай инстансы! ~Killa");
  }

  public static String get(final String key) {
    String value = DOTENV.get(key);
    return value != null ? value : System.getenv(key);
  }

  public static String get(final String key, final String defaultValue) {
    String value = get(key);
    return value != null ? value : defaultValue;
  }
}
