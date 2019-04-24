/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.carlosalbertoxw.jqs;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author Carlos
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class);

    /**
     * Para crear formatos de cron ir a la pagina http://www.cronmaker.com/
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            PropertyConfigurator.configure("D:\\log4j.properties");
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            JobDetail job = JobBuilder.newJob(Worker.class).build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("Worker Trigger")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * 1/1 * ? *"))
                    .startNow()
                    .build();
            scheduler.scheduleJob(job, trigger);
            scheduler.start();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }
}
