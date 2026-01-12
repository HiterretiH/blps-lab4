package org.lab.logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public abstract class AbstractLogger implements Logger {
  protected static final int DEFAULT_BUFFER_SIZE = 50;
  protected static final String DEFAULT_LOG_DIRECTORY = "logs";
  protected static final String DEFAULT_BASE_FILE_NAME = "app";
  protected static final String TIME_FORMAT_PATTERN = "HH:mm:ss";
  protected static final String DATE_TIME_FORMAT_PATTERN = "yyyyMMdd_HHmmss";
  protected static final int UUID_SUBSTRING_LENGTH = 8;
  protected static final String LOG_MESSAGE_FORMAT = "[%s] [%s] %s%n";
  protected static final String LOG_FILE_NAME_FORMAT = "%s_%s_%s.log";

  private final int bufferSize;
  private final String logDirectory;
  private final String baseFileName;
  private final StringBuilder buffer = new StringBuilder();
  private int currentBufferSize = 0;
  private File logFile;
  private boolean shutdownHookAdded = false;

  protected static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN);
  protected static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);

  protected AbstractLogger() {
    this(DEFAULT_BUFFER_SIZE, DEFAULT_LOG_DIRECTORY, DEFAULT_BASE_FILE_NAME);
  }

  protected AbstractLogger(
      final int bufferSizeParam, final String logDirectoryParam, final String baseFileNameParam) {
    this.bufferSize = bufferSizeParam;
    this.logDirectory = logDirectoryParam;
    this.baseFileName = baseFileNameParam;
    createLogDirectory();
    addShutdownHook();
  }

  protected int getBufferSize() {
    return bufferSize;
  }

  protected String getLogDirectory() {
    return logDirectory;
  }

  protected String getBaseFileName() {
    return baseFileName;
  }

  protected StringBuilder getBuffer() {
    return buffer;
  }

  protected int getCurrentBufferSize() {
    return currentBufferSize;
  }

  protected void setCurrentBufferSize(final int newCurrentBufferSize) {
    this.currentBufferSize = newCurrentBufferSize;
  }

  protected File getLogFile() {
    return logFile;
  }

  protected void setLogFile(final File newLogFile) {
    this.logFile = newLogFile;
  }

  protected boolean isShutdownHookAdded() {
    return shutdownHookAdded;
  }

  protected void setShutdownHookAdded(final boolean newShutdownHookAdded) {
    this.shutdownHookAdded = newShutdownHookAdded;
  }

  private void createLogDirectory() {
    File directory = new File(logDirectory);
    if (!directory.exists()) {
      directory.mkdirs();
    }
  }

  private synchronized void addShutdownHook() {
    if (!shutdownHookAdded) {
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    if (currentBufferSize > 0) {
                      System.out.println("Shutdown hook: flushing logs...");
                      flush();
                    }
                  }));
      shutdownHookAdded = true;
    }
  }

  protected synchronized void logInternal(final Severity severity, final String message) {
    LocalTime currentLocalTime = LocalTime.now();
    String formattedTime = currentLocalTime.format(TIME_FORMATTER);
    String logMessage = String.format(LOG_MESSAGE_FORMAT, formattedTime, severity.name(), message);

    if (logFile == null) {
      logFile = createLogFile();
    }

    System.out.print(logMessage);
    buffer.append(logMessage);
    currentBufferSize++;

    if (currentBufferSize >= bufferSize) {
      saveToFile();
      logFile = createLogFile();
    }
  }

  protected File createLogFile() {
    String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
    String uuid = UUID.randomUUID().toString().substring(0, UUID_SUBSTRING_LENGTH);

    String fileName = String.format(LOG_FILE_NAME_FORMAT, baseFileName, timestamp, uuid);
    File file = new File(logDirectory, fileName);
    try {
      file.createNewFile();
    } catch (IOException ioException) {
      System.err.println("Failed to create log file: " + ioException.getMessage());
    }
    return file;
  }

  protected synchronized void saveToFile() {
    try {
      if (logFile != null && !buffer.isEmpty()) {
        Files.write(logFile.toPath(), buffer.toString().getBytes(), StandardOpenOption.APPEND);
        clearBuffer();
      }
    } catch (Exception exception) {
      System.err.println("Failed to write logs to file: " + exception.getMessage());
    }
  }

  private void clearBuffer() {
    buffer.setLength(0);
    currentBufferSize = 0;
  }

  @Override
  public synchronized void flush() {
    if (currentBufferSize > 0) {
      saveToFile();
    }
  }

  @Override
  public void close() {
    flush();
  }
}
