package org.lab1.config;

import org.lab.logger.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private static final String ALL_PATHS = "/**";
  private static final String LOCAL_FRONTEND_URL = "http://localhost:4200";
  private static final String ALL_HEADERS = "*";
  private static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};
  private static final String PASSWORD_ENCODER_BEAN = "passwordEncoder";
  private static final String REST_TEMPLATE_BEAN = "restTemplate";
  private static final String LOGGER_BEAN = "logger";
  private static final String APP_LOGGER_NAME = "app";
  private static final String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping(ALL_PATHS)
        .allowedOrigins(LOCAL_FRONTEND_URL)
        .allowedMethods(ALLOWED_METHODS)
        .allowedHeaders(ALL_HEADERS);
  }

  @Bean(name = PASSWORD_ENCODER_BEAN)
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean(name = REST_TEMPLATE_BEAN)
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean(name = LOGGER_BEAN)
  @Scope(value = SCOPE_SINGLETON)
  public Logger logger() {
    return Logger.getInstance(APP_LOGGER_NAME);
  }
}
