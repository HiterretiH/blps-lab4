package org.lab.logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Logger implements AutoCloseable  {
    private final int bufferSize;
    private final String logDirectory;
    private final String baseFileName;
    private final StringBuilder buffer = new StringBuilder();
    private int currentSize = 0;
    private File logFile;
    private boolean shutdownHookAdded = false;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Map<String, Logger> instances = new HashMap<>();

    public static Logger getInstance() {
        return getInstance("app");
    }

    public static Logger getInstance(String baseFileName) {
        if (instances.containsKey(baseFileName)) {
            return instances.get(baseFileName);
        }
        Logger logger = new Logger(baseFileName);
        instances.put(baseFileName, logger);
        return logger;
    }

    private Logger(String baseFileName) {
        this(50, "logs", baseFileName);
    }

    private Logger(int bufferSize, String logDirectory, String baseFileName) {
        this.bufferSize = bufferSize;
        this.logDirectory = logDirectory;
        this.baseFileName = baseFileName;

        File dir = new File(logDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        addShutdownHook();
    }

    private synchronized void addShutdownHook() {
        if (!shutdownHookAdded) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (currentSize > 0) {
                    System.out.println("Shutdown hook: flushing logs...");
                    flush();
                }
            }));
            shutdownHookAdded = true;
        }
    }

    public void log(Severity severity, String message) {
        logInternal(severity, message);
    }

    public void info(String message) {
        logInternal(Severity.INF, message);
    }

    public void error(String message) {
        logInternal(Severity.ERR, message);
    }

    private synchronized void logInternal(Severity severity, String message) {
        String logMessage = String.format("[%s] [%s] %s%n", LocalTime.now().format(formatter), severity.name(), message);

        if (logFile == null) {
            logFile = createLogFile();
        }

        buffer.append(logMessage);
        currentSize++;

        if (currentSize >= bufferSize) {
            saveToFile();
            logFile = createLogFile();
        }
    }

    private File createLogFile() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        String fileName = String.format("%s_%s_%s.log", baseFileName, timestamp, uuid);
        File file = new File(logDirectory, fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("Failed to create log file: " + e.getMessage());
        }
        return file;
    }

    private synchronized void saveToFile() {
        try {
            if (logFile != null) {
                Files.write(
                        logFile.toPath(),
                        buffer.toString().getBytes(),
                        StandardOpenOption.APPEND
                );
                buffer.setLength(0);
                currentSize = 0;
            }
        } catch (Exception e) {
            System.err.println("Failed to write logs to file: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        flush();
    }

    public synchronized void flush() {
        if (currentSize > 0) {
            saveToFile();
        }
    }
}
