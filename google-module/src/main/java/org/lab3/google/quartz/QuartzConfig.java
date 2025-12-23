package org.lab3.google.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public final class QuartzConfig {

  @Bean
  public JobDetail updateAppsTopJobDetail() {
    return JobBuilder.newJob(UpdateAppsTopJob.class)
        .withIdentity("updateAppsTopJob")
        .storeDurably()
        .build();
  }

  @Bean
  public Trigger updateAppsTopJobTrigger() {
    return TriggerBuilder.newTrigger()
        .forJob(updateAppsTopJobDetail())
        .withIdentity("updateAppsTopTrigger")
        .withSchedule(CronScheduleBuilder.cronSchedule("0 0/2 * * * ?"))
        .build();
  }
}
