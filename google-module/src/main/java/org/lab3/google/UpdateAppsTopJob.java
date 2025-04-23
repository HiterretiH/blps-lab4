package org.lab3.google;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class UpdateAppsTopJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            // Получаем GoogleConnection из JobDataMap
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            GoogleConnection googleConnection = (GoogleConnection) dataMap.get("googleConnection");

            if (googleConnection != null) {
                googleConnection.updateAppsTop();
                System.out.println("Successfully updated apps top from Quartz job");
            } else {
                System.err.println("GoogleConnection not found in job context");
            }
        } catch (Exception e) {
            throw new JobExecutionException("Failed to update apps top", e);
        }
    }
}