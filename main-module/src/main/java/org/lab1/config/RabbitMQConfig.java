package org.lab1.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
  private static final String RABBITMQ_HOST = EnvConfig.get("RABBITMQ_HOST", "rabbit");
  private static final int RABBITMQ_PORT = Integer.parseInt(EnvConfig.get("RABBITMQ_PORT", "5672"));
  private static final String RABBITMQ_USERNAME = EnvConfig.get("RABBITMQ_USERNAME", "admin");
  private static final String RABBITMQ_PASSWORD = EnvConfig.get("RABBITMQ_PASSWORD", "password");
  private static final String CONNECTION_FACTORY_BEAN = "connectionFactory";
  private static final String RABBIT_ADMIN_BEAN = "rabbitAdmin";
  private static final String RABBIT_TEMPLATE_BEAN = "rabbitTemplate";

  @Bean(name = CONNECTION_FACTORY_BEAN)
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
    connectionFactory.setHost(RABBITMQ_HOST);
    connectionFactory.setPort(RABBITMQ_PORT);
    connectionFactory.setUsername(RABBITMQ_USERNAME);
    connectionFactory.setPassword(RABBITMQ_PASSWORD);
    return connectionFactory;
  }

  @Bean(name = RABBIT_ADMIN_BEAN)
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  @Bean(name = RABBIT_TEMPLATE_BEAN)
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    return new RabbitTemplate(connectionFactory);
  }
}
