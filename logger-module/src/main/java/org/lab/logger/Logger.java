package org.lab.logger;

public interface Logger extends AutoCloseable {
  void info(String message);

  void error(String message);

  void flush();
}
