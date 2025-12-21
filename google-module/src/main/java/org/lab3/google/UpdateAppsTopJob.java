package org.lab3.google;

import org.lab.logger.Logger;
import org.lab3.google.model.GoogleOperationResult;
import org.lab3.google.repository.OperationResultRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class UpdateAppsTopJob implements Job {
    private static final Logger logger = Logger.getInstance("google-module-job");

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        GoogleConnection googleConnection = (GoogleConnection) context.getJobDetail()
                .getJobDataMap().get("googleConnection");
        OperationResultRepository repository = (OperationResultRepository) context.getJobDetail()
                .getJobDataMap().get("repository");

        try {
            logger.info("Starting scheduled apps top update...");
            googleConnection.updateAppsTop();
            logger.info("Scheduled apps top update completed");

            if (repository != null) {
                GoogleOperationResult result = new GoogleOperationResult(
                        null,
                        "updateAppsTop",
                        "All spreadsheets",
                        "Apps top updated successfully",
                        null
                );
                repository.save(result);
            }
        } catch (Exception e) {
            logger.error("Error in scheduled apps top update: " + e.getMessage());

            if (repository != null) {
                GoogleOperationResult result = new GoogleOperationResult(
                        null,
                        "updateAppsTop",
                        "All spreadsheets",
                        null,
                        "Error: " + e.getMessage()
                );
                repository.save(result);
            }
            throw new JobExecutionException(e);
        }
    }
}
