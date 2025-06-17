package com.autenticacao.api.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresSingletonContainer extends PostgreSQLContainer<PostgresSingletonContainer> {

  private static final String IMAGE_VERSION = "postgres:15.3";
  private static PostgresSingletonContainer container;

  private PostgresSingletonContainer() {
    super(IMAGE_VERSION);
    withDatabaseName("testdb");
    withUsername("testuser");
    withPassword("testpass");
    withInitScript("db/init_test.sql");
  }

  public static PostgresSingletonContainer getInstance() {
    if (container == null) {
      container = new PostgresSingletonContainer();
      container.start();
      Runtime.getRuntime().addShutdownHook(new Thread(container::stop));
    }
    return container;
  }
}
