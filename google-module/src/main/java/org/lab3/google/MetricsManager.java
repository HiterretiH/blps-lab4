package org.lab3.google;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.HTTPServer;
import org.lab.logger.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MetricsManager {
    private static final int METRICS_PORT = 8080;
    private static MetricsManager instance;
    private static final Logger logger = Logger.getInstance("google-module");

    private final Counter totalRequests;
    private final Counter successfulRequests;
    private final Counter failedRequests;
    private final Map<String, Counter> operationCounters;
    private final Histogram requestDuration;
    private final Counter formsCreated;
    private final Counter sheetsCreated;
    private final Counter tabsCreated;
    private final Counter revenueUpdates;
    private final Counter topUpdates;

    private HTTPServer metricsServer;

    private MetricsManager() {
        // Инициализация метрик
        totalRequests = Counter.build()
                .name("google_module_requests_total")
                .help("Total number of requests received")
                .register();
        logger.info("Prometheus counter 'google_module_requests_total' registered.");

        successfulRequests = Counter.build()
                .name("google_module_requests_success_total")
                .help("Total number of successful requests")
                .register();
        logger.info("Prometheus counter 'google_module_requests_success_total' registered.");

        failedRequests = Counter.build()
                .name("google_module_requests_failed_total")
                .help("Total number of failed requests")
                .register();
        logger.info("Prometheus counter 'google_module_requests_failed_total' registered.");

        operationCounters = new HashMap<>();
        initializeOperationCounters();

        requestDuration = Histogram.build()
                .name("google_module_request_duration_seconds")
                .help("Request duration in seconds")
                .register();
        logger.info("Prometheus histogram 'google_module_request_duration_seconds' registered.");

        formsCreated = Counter.build()
                .name("google_module_forms_created_total")
                .help("Total number of Google Forms created")
                .register();
        logger.info("Prometheus counter 'google_module_forms_created_total' registered.");

        sheetsCreated = Counter.build()
                .name("google_module_sheets_created_total")
                .help("Total number of Google Sheets created")
                .register();
        logger.info("Prometheus counter 'google_module_sheets_created_total' registered.");

        tabsCreated = Counter.build()
                .name("google_module_sheet_tabs_created_total")
                .help("Total number of tabs created in sheets")
                .register();
        logger.info("Prometheus counter 'google_module_sheet_tabs_created_total' registered.");

        revenueUpdates = Counter.build()
                .name("google_module_revenue_updates_total")
                .help("Total number of revenue updates")
                .register();
        logger.info("Prometheus counter 'google_module_revenue_updates_total' registered.");

        topUpdates = Counter.build()
                .name("google_module_top_updates_total")
                .help("Total number of top updates")
                .register();
        logger.info("Prometheus counter 'google_module_top_updates_total' registered.");
    }

    public void recordFormCreated() {
        formsCreated.inc();
        logger.info("Recorded a new Google Form creation.");
    }

    public void recordSheetCreated() {
        sheetsCreated.inc();
        logger.info("Recorded a new Google Sheet creation.");
    }

    public void recordTabsCreated(int count) {
        tabsCreated.inc(count);
        logger.info("Recorded creation of " + count + " tab(s) in a Google Sheet.");
    }

    public void recordRevenueUpdate() {
        revenueUpdates.inc();
        logger.info("Recorded a revenue update.");
    }

    public void recordTopUpdate() {
        topUpdates.inc();
        logger.info("Recorded an update of the top apps.");
    }

    private void initializeOperationCounters() {
        String[] operations = {
                "createForm", "createSheetWithData", "addAppSheets",
                "updateMonetization", "updateAppsTop", "healthcheck"
        };

        for (String op : operations) {
            operationCounters.put(op, Counter.build()
                    .name("google_module_operation_" + op + "_total")
                    .help("Total number of " + op + " operations")
                    .register());
            logger.info("Prometheus counter 'google_module_operation_" + op + "_total' registered.");
        }
    }

    public static synchronized MetricsManager getInstance() {
        if (instance == null) {
            instance = new MetricsManager();
            logger.info("MetricsManager instance created.");
        }
        return instance;
    }

    public void start() throws IOException {
        if (metricsServer == null) {
            metricsServer = new HTTPServer(METRICS_PORT);
            logger.info("Prometheus HTTP server started on port: " + METRICS_PORT);
        }
    }

    public void stop() {
        if (metricsServer != null) {
            metricsServer.stop();
            logger.info("Prometheus HTTP server stopped.");
        }
    }

    public void recordRequest(String operation, boolean success) {
        totalRequests.inc();
        logger.info("Total requests count incremented. Current total: " + totalRequests.get());
        if (success) {
            successfulRequests.inc();
            logger.info("Successful requests count incremented. Current successful: " + successfulRequests.get());
        } else {
            failedRequests.inc();
            logger.error("Failed requests count incremented. Current failed: " + failedRequests.get());
        }

        Counter opCounter = operationCounters.get(operation);
        if (opCounter != null) {
            opCounter.inc();
            logger.info("Operation '" + operation + "' count incremented. Current count: " + opCounter.get());
        } else {
            logger.error("No counter found for operation: " + operation);
        }
    }

    public Histogram.Timer startRequestTimer() {
        Histogram.Timer timer = requestDuration.startTimer();
        logger.info("Started request timer.");
        return timer;
    }
}