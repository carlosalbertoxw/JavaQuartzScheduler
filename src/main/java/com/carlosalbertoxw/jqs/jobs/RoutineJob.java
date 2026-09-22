package com.carlosalbertoxw.jqs.jobs;

import java.util.Objects;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carlosalbertoxw.jqs.services.RoutineService;
import com.carlosalbertoxw.jqs.services.RoutineServices;

/**
 * Job de Quartz generico: resuelve el {@link RoutineService} indicado en su JobDataMap y le
 * delega el trabajo real.
 *
 * <ul>
 *   <li>{@link DisallowConcurrentExecution} evita que una ejecucion lenta se solape con la
 *       siguiente (por job, no entre rutinas distintas).</li>
 *   <li>Las excepciones se relanzan como {@link JobExecutionException} para que Quartz
 *       aplique su politica de registro; el siguiente disparo del cron la reintenta.</li>
 *   <li>Es {@link InterruptableJob}: en el apagado se interrumpe el hilo de la rutina.</li>
 * </ul>
 */
@DisallowConcurrentExecution
public final class RoutineJob implements InterruptableJob {

    private static final Logger LOG = LoggerFactory.getLogger(RoutineJob.class);

    /** Clave del JobDataMap con la clave del servicio a ejecutar. */
    public static final String SERVICE_KEY = "routineService";

    private final RoutineServices services;

    private volatile Thread executingThread;
    private volatile boolean interrupted;

    public RoutineJob(RoutineServices services) {
        this.services = Objects.requireNonNull(services, "services");
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobKey jobKey = context.getJobDetail().getKey();
        executingThread = Thread.currentThread();
        try {
            final Object serviceKey = context.getMergedJobDataMap().get(SERVICE_KEY);
            if (!(serviceKey instanceof String key)) {
                throw new IllegalStateException("El job " + jobKey + " no define '" + SERVICE_KEY + "'.");
            }

            services.create(key).run();
        } catch (InterruptedException e) {
            if (!interrupted) {
                throw fail(jobKey, e);
            }
            // Apagado ordenado: no es un error, no reintentar.
            LOG.warn("Job {} interrumpido por apagado de la aplicacion.", jobKey);
        } catch (Exception e) {
            throw fail(jobKey, e);
        } finally {
            executingThread = null;
            // Limpia la marca de interrupcion para no afectar al hilo del pool de Quartz.
            Thread.interrupted();
        }
    }

    @Override
    public void interrupt() {
        interrupted = true;
        final Thread thread = executingThread;
        if (thread != null) {
            thread.interrupt();
        }
    }

    private static JobExecutionException fail(JobKey jobKey, Exception e) {
        LOG.error("Fallo el job {}.", jobKey, e);
        // refireImmediately = false: el siguiente disparo del cron lo reintenta.
        return new JobExecutionException(e, false);
    }
}
