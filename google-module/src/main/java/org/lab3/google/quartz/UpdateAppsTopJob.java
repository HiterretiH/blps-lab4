package org.lab3.google.quartz;

import org.lab.logger.Logger;
import org.lab3.google.model.GoogleOperationResult;
import org.lab3.google.repository.OperationResultRepository;
import org.lab3.google.service.GoogleConnection;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class UpdateAppsTopJob implements Job {
    private static final Logger LOGGER = Logger.getInstance("google-module-job");
    private static final String UPDATE_APPS_TOP_OPERATION = "updateAppsTop";
    private static final String ALL_SPREADSHEETS_TARGET = "All spreadsheets";
    private static final String SUCCESS_RESULT = "Apps top updated";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        GoogleConnection googleConnection = (GoogleConnection) context.getJobDetail()
                .getJobDataMap().get("googleConnection");
        OperationResultRepository repository = (OperationResultRepository) context.getJobDetail()
                .getJobDataMap().get("repository");

        try {
            LOGGER.info("Starting scheduled apps top update...");
            googleConnection.updateAppsTop();
            LOGGER.info("Scheduled apps top update completed");

            if (repository != null) {
                GoogleOperationResult result = new GoogleOperationResult(
                        null,
                        UPDATE_APPS_TOP_OPERATION,
                        ALL_SPREADSHEETS_TARGET,
                        SUCCESS_RESULT,
                        null
                );
                repository.save(result);
            }
        } catch (Exception exception) {
            LOGGER.error("Error in scheduled apps top update: " + exception.getMessage());

            if (repository != null) {
                GoogleOperationResult result = new GoogleOperationResult(
                        null,
                        UPDATE_APPS_TOP_OPERATION,
                        ALL_SPREADSHEETS_TARGET,
                        null,
                        exception.getMessage()
                );
                repository.save(result);
            }
            throw new JobExecutionException(exception);
        }
    }
}
