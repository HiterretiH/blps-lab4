package org.lab3.google.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvConfig {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("/app")
            .load();

    public static String get(String key) {
        String value = dotenv.get(key);
        return (value != null) ? value : System.getenv(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value != null) ? value : defaultValue;
    }
}
