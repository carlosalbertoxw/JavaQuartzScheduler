package com.carlosalbertoxw.jqs.services;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carlosalbertoxw.jqs.config.ReportsConfig;

/**
 * Rutina de ejemplo que borra los reportes de {@link DailyReportService} mas antiguos que
 * {@link ReportsConfig#getRetentionDays()}. Solo toca archivos con el prefijo de reporte.
 */
public class ReportCleanupService implements RoutineService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportCleanupService.class);

    /** Clave con la que se registra el servicio y se referencia en {@code service}. */
    public static final String KEY = "ReportCleanup";

    private final ReportsConfig config;
    private final Clock clock;

    public ReportCleanupService(ReportsConfig config, Clock clock) {
        this.config = Objects.requireNonNull(config, "config");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public void run() throws IOException, InterruptedException {
        final Path directory = config.resolveDirectory();
        if (!Files.isDirectory(directory)) {
            LOG.info("No existe la carpeta de reportes {}; nada que limpiar.", directory);
            return;
        }

        final Instant cutoff = clock.instant().minus(Duration.ofDays(config.getRetentionDays()));
        int deleted = 0;

        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory,
                DailyReportService.FILE_PREFIX + "*.txt")) {
            for (Path file : files) {
                // Apagado ordenado: se detiene entre archivos si se interrumpe la rutina.
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Limpieza de reportes interrumpida.");
                }

                if (Files.isRegularFile(file) && Files.getLastModifiedTime(file).toInstant().isBefore(cutoff)) {
                    Files.delete(file);
                    deleted++;
                }
            }
        }

        LOG.info("Limpieza de reportes: {} archivo(s) con mas de {} dias eliminados.",
                deleted, config.getRetentionDays());
    }
}
