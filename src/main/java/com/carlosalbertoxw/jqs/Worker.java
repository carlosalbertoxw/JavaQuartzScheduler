package com.carlosalbertoxw.jqs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tarea que ejecuta el scheduler en cada disparo del trigger. Aqui iria la logica
 * de negocio a automatizar; en este ejemplo simplemente registra un mensaje.
 *
 * @author Carlos
 */
public class Worker implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(Worker.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.info("Ejecutando tarea. Proximo disparo: {}", context.getNextFireTime());
    }
}
