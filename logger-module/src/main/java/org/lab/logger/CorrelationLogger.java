package org.lab.logger;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public final class CorrelationLogger extends AbstractLogger {
  private static final String LOG_MESSAGE_FORMAT_WITH_CONTEXT = "[%s] [%s] [traceId=%s] %s%n";

  private static final Map<String, CorrelationLogger> LOGGER_INSTANCES = new HashMap<>();

  public static CorrelationLogger getInstance() {
    return getInstance(DEFAULT_BASE_FILE_NAME);
  }

  public static CorrelationLogger getInstance(final String baseFileNameParam) {
    return LOGGER_INSTANCES.computeIfAbsent(baseFileNameParam, CorrelationLogger::new);
  }

  private CorrelationLogger(final String baseFileNameParam) {
    super(DEFAULT_BUFFER_SIZE, DEFAULT_LOG_DIRECTORY, baseFileNameParam);
  }

  private CorrelationLogger(
      final int bufferSizeParam, final String logDirectoryParam, final String baseFileNameParam) {
    super(bufferSizeParam, logDirectoryParam, baseFileNameParam);
  }

  @Override
  public void info(final String message) {
    logInternalWithContext(Severity.INF, message);
  }

  @Override
  public void error(final String message) {
    logInternalWithContext(Severity.ERR, message);
  }

  private synchronized void logInternalWithContext(final Severity severity, final String message) {
    Span currentSpan = Span.current();
    SpanContext spanContext = currentSpan.getSpanContext();

    String traceId = spanContext.isValid() ? spanContext.getTraceId() : "no-trace";
    String correlationId = traceId;

    String logMessage = createLogMessage(severity, message, traceId, correlationId);
    writeLog(severity, logMessage);
  }

  private String createLogMessage(
      final Severity severity,
      final String message,
      final String traceId,
      final String correlationId) {
    LocalTime currentLocalTime = LocalTime.now();
    String formattedTime = currentLocalTime.format(TIME_FORMATTER);

    if (correlationId != null && !correlationId.equals(traceId)) {
      return String.format(
          "[%s] [%s] [traceId=%s, correlationId=%s] %s%n",
          formattedTime, severity.name(), traceId, correlationId, message);
    }

    return String.format(
        LOG_MESSAGE_FORMAT_WITH_CONTEXT, formattedTime, severity.name(), traceId, message);
  }

  private void writeLog(final Severity severity, final String logMessage) {
    System.out.print(logMessage);

    if (getLogFile() == null) {
      setLogFile(createLogFile());
    }

    getBuffer().append(logMessage);
    setCurrentBufferSize(getCurrentBufferSize() + 1);

    if (getCurrentBufferSize() >= getBufferSize()) {
      saveToFile();
      setLogFile(createLogFile());
    }
  }
}
