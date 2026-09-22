package com.carlosalbertoxw.jqs.jobs;

import java.util.Objects;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import com.carlosalbertoxw.jqs.services.RoutineServices;

/**
 * {@link JobFactory} que crea los {@link RoutineJob} con el registro de servicios. Otros
 * tipos de job se crean con el constructor por defecto, como hace Quartz.
 */
public final class RoutineJobFactory implements JobFactory {

    private final RoutineServices services;
    private final JobFactory fallback = new SimpleJobFactory();

    public RoutineJobFactory(RoutineServices services) {
        this.services = Objects.requireNonNull(services, "services");
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        if (bundle.getJobDetail().getJobClass() == RoutineJob.class) {
            return new RoutineJob(services);
        }
        return fallback.newJob(bundle, scheduler);
    }
}
