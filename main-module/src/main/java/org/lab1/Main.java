package org.lab1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "org.lab1")
@EnableJpaRepositories("org.lab1.repository")
@EntityScan("org.lab1.model")
public class Main {
  public static void main(final String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
