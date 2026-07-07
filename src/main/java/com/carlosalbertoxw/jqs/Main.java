package com.carlosalbertoxw.jqs;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Punto de entrada de la aplicacion. Programa una tarea recurrente ({@link Worker})
 * mediante una expresion cron de Quartz.
 *
 * <p>La expresion cron puede indicarse como primer argumento del programa; si no se
 * indica, se usa {@link #DEFAULT_CRON} (cada minuto). Para generar expresiones cron
 * puede usarse una herramienta como
 * https://www.freeformatter.com/cron-expression-generator-quartz.html
 *
 * @author Carlos
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    /** Cada minuto, en el segundo 0. */
    static final String DEFAULT_CRON = "0 0/1 * 1/1 * ? *";

    static final String JOB_NAME = "worker";
    static final String GROUP = "jqs";
    static final String TRIGGER_NAME = "worker-trigger";

    public static void main(String[] args) {
        final String cron = args.length > 0 ? args[0] : DEFAULT_CRON;

        try {
            final Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.scheduleJob(buildJob(), buildTrigger(cron));
            scheduler.start();
            LOG.info("Scheduler iniciado. Tarea programada con cron: {}", cron);

            registerShutdownHook(scheduler);
        } catch (SchedulerException e) {
            LOG.error("No se pudo iniciar el scheduler", e);
            System.exit(1);
        }
    }

    /** Construye el {@link JobDetail} para la tarea {@link Worker}. */
    static JobDetail buildJob() {
        return JobBuilder.newJob(Worker.class)
                .withIdentity(JOB_NAME, GROUP)
                .build();
    }

    /**
     * Construye el {@link Trigger} a partir de una expresion cron.
     *
     * @param cron expresion cron valida de Quartz
     * @return el trigger configurado
     * @throws RuntimeException si la expresion cron no es valida
     */
    static Trigger buildTrigger(String cron) {
        return TriggerBuilder.newTrigger()
                .withIdentity(TRIGGER_NAME, GROUP)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .startNow()
                .build();
    }

    /**
     * Registra un hook para detener el scheduler de forma ordenada cuando la JVM
     * recibe una senal de terminacion (Ctrl+C, kill, etc.).
     */
    private static void registerShutdownHook(Scheduler scheduler) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOG.info("Deteniendo scheduler...");
                scheduler.shutdown(true);
                LOG.info("Scheduler detenido correctamente.");
            } catch (SchedulerException e) {
                LOG.error("Error al detener el scheduler", e);
            }
        }, "jqs-shutdown"));
    }
}
