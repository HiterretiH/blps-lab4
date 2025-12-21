package org.lab3.google.service;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.HTTPServer;
import org.lab.logger.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MetricsManager {
    private static final int METRICS_PORT = 8080;
    private static MetricsManager instance;
    private static final Logger LOGGER = Logger.getInstance("google-module");

    private static final String TOTAL_REQUESTS_NAME = "google_module_requests_total";
    private static final String SUCCESSFUL_REQUESTS_NAME = "google_module_requests_success_total";
    private static final String FAILED_REQUESTS_NAME = "google_module_requests_failed_total";
    private static final String REQUEST_DURATION_NAME = "google_module_request_duration_seconds";
    private static final String FORMS_CREATED_NAME = "google_module_forms_created_total";
    private static final String SHEETS_CREATED_NAME = "google_module_sheets_created_total";
    private static final String TABS_CREATED_NAME = "google_module_sheet_tabs_created_total";
    private static final String REVENUE_UPDATES_NAME = "google_module_revenue_updates_total";
    private static final String TOP_UPDATES_NAME = "google_module_top_updates_total";

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
        totalRequests = Counter.build()
                .name(TOTAL_REQUESTS_NAME)
                .help("Total number of requests received")
                .register();
        LOGGER.info("Prometheus counter '" + TOTAL_REQUESTS_NAME + "' registered.");

        successfulRequests = Counter.build()
                .name(SUCCESSFUL_REQUESTS_NAME)
                .help("Total number of successful requests")
                .register();
        LOGGER.info("Prometheus counter '" + SUCCESSFUL_REQUESTS_NAME + "' registered.");

        failedRequests = Counter.build()
                .name(FAILED_REQUESTS_NAME)
                .help("Total number of failed requests")
                .register();
        LOGGER.info("Prometheus counter '" + FAILED_REQUESTS_NAME + "' registered.");

        operationCounters = new HashMap<>();
        initializeOperationCounters();

        requestDuration = Histogram.build()
                .name(REQUEST_DURATION_NAME)
                .help("Request duration in seconds")
                .register();
        LOGGER.info("Prometheus histogram '" + REQUEST_DURATION_NAME + "' registered.");

        formsCreated = Counter.build()
                .name(FORMS_CREATED_NAME)
                .help("Total number of Google Forms created")
                .register();
        LOGGER.info("Prometheus counter '" + FORMS_CREATED_NAME + "' registered.");

        sheetsCreated = Counter.build()
                .name(SHEETS_CREATED_NAME)
                .help("Total number of Google Sheets created")
                .register();
        LOGGER.info("Prometheus counter '" + SHEETS_CREATED_NAME + "' registered.");

        tabsCreated = Counter.build()
                .name(TABS_CREATED_NAME)
                .help("Total number of tabs created in sheets")
                .register();
        LOGGER.info("Prometheus counter '" + TABS_CREATED_NAME + "' registered.");

        revenueUpdates = Counter.build()
                .name(REVENUE_UPDATES_NAME)
                .help("Total number of revenue updates")
                .register();
        LOGGER.info("Prometheus counter '" + REVENUE_UPDATES_NAME + "' registered.");

        topUpdates = Counter.build()
                .name(TOP_UPDATES_NAME)
                .help("Total number of top updates")
                .register();
        LOGGER.info("Prometheus counter '" + TOP_UPDATES_NAME + "' registered.");
    }

    public void recordFormCreated() {
        formsCreated.inc();
        LOGGER.info("Recorded a new Google Form creation.");
    }

    public void recordSheetCreated() {
        sheetsCreated.inc();
        LOGGER.info("Recorded a new Google Sheet creation.");
    }

    public void recordTabsCreated(int count) {
        tabsCreated.inc(count);
        LOGGER.info("Recorded creation of " + count + " tab(s) in a Google Sheet.");
    }

    public void recordRevenueUpdate() {
        revenueUpdates.inc();
        LOGGER.info("Recorded a revenue update.");
    }

    public void recordTopUpdate() {
        topUpdates.inc();
        LOGGER.info("Recorded an update of the top apps.");
    }

    private void initializeOperationCounters() {
        String[] operations = {
                "createForm", "createSheetWithData", "addAppSheets",
                "updateMonetization", "updateAppsTop", "healthcheck"
        };

        for (String operation : operations) {
            operationCounters.put(operation, Counter.build()
                    .name("google_module_operation_" + operation + "_total")
                    .help("Total number of " + operation + " operations")
                    .register());
            LOGGER.info("Prometheus counter 'google_module_operation_" + operation + "_total' registered.");
        }
    }

    public static synchronized MetricsManager getInstance() {
        if (instance == null) {
            instance = new MetricsManager();
            LOGGER.info("MetricsManager instance created.");
        }
        return instance;
    }

    public void start() throws IOException {
        if (metricsServer == null) {
            metricsServer = new HTTPServer(METRICS_PORT);
            LOGGER.info("Prometheus HTTP server started on port: " + METRICS_PORT);
        }
    }

    public void stop() {
        if (metricsServer != null) {
            metricsServer.stop();
            LOGGER.info("Prometheus HTTP server stopped.");
        }
    }

    public void recordRequest(String operation, boolean success) {
        totalRequests.inc();
        LOGGER.info("Total requests count incremented. Current total: " + totalRequests.get());
        if (success) {
            successfulRequests.inc();
            LOGGER.info("Successful requests count incremented. Current successful: " + successfulRequests.get());
        } else {
            failedRequests.inc();
            LOGGER.error("Failed requests count incremented. Current failed: " + failedRequests.get());
        }

        Counter operationCounter = operationCounters.get(operation);
        if (operationCounter != null) {
            operationCounter.inc();
            LOGGER.info("Operation '" + operation + "' count incremented. Current count: " + operationCounter.get());
        } else {
            LOGGER.error("No counter found for operation: " + operation);
        }
    }

    public Histogram.Timer startRequestTimer() {
        Histogram.Timer timer = requestDuration.startTimer();
        LOGGER.info("Started request timer.");
        return timer;
    }
}
