package com.carlosalbertoxw.jqs.services;

import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rutina minima de ejemplo: solo registra inicio y fin. Usala como plantilla para logica
 * real (llamadas a API, consultas a BD, etc.); para un ejemplo con configuracion y
 * dependencias propias, ver {@link DailyReportService}.
 */
public class ExampleRoutineService implements RoutineService {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleRoutineService.class);

    /** Clave con la que se registra el servicio y se referencia en {@code service}. */
    public static final String KEY = "Example";

    @Override
    public void run() throws InterruptedException {
        LOG.info("Rutina iniciada a las {}", OffsetDateTime.now());

        // Simula trabajo respetando la interrupcion (apagado ordenado).
        Thread.sleep(200);

        LOG.info("Rutina finalizada correctamente.");
    }
}
