package org.lab.logger;

import java.util.HashMap;
import java.util.Map;

public final class FileLogger extends AbstractLogger {
  private static final Map<String, FileLogger> LOGGER_INSTANCES = new HashMap<>();

  public static FileLogger getInstance() {
    return getInstance(DEFAULT_BASE_FILE_NAME);
  }

  public static FileLogger getInstance(final String baseFileNameParam) {
    return LOGGER_INSTANCES.computeIfAbsent(baseFileNameParam, FileLogger::new);
  }

  private FileLogger(final String baseFileNameParam) {
    super(DEFAULT_BUFFER_SIZE, DEFAULT_LOG_DIRECTORY, baseFileNameParam);
  }

  private FileLogger(
      final int bufferSizeParam, final String logDirectoryParam, final String baseFileNameParam) {
    super(bufferSizeParam, logDirectoryParam, baseFileNameParam);
  }

  @Override
  public void info(final String message) {
    logInternal(Severity.INF, message);
  }

  @Override
  public void error(final String message) {
    logInternal(Severity.ERR, message);
  }
}
