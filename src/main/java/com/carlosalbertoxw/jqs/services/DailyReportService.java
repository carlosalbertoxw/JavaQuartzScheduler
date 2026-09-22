package com.carlosalbertoxw.jqs.services;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carlosalbertoxw.jqs.config.ReportsConfig;

/**
 * Rutina de ejemplo que genera un reporte de texto con un resumen del sistema.
 * Muestra como una rutina recibe su propia configuracion ({@link ReportsConfig}) y
 * dependencias ({@link Clock}, que permite fijar la hora en las pruebas).
 */
public class DailyReportService implements RoutineService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyReportService.class);

    /** Clave con la que se registra el servicio y se referencia en {@code service}. */
    public static final String KEY = "DailyReport";

    /** Prefijo de los archivos de reporte; la limpieza solo borra archivos con este patron. */
    public static final String FILE_PREFIX = "report-";

    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ReportsConfig config;
    private final Clock clock;

    public DailyReportService(ReportsConfig config, Clock clock) {
        this.config = Objects.requireNonNull(config, "config");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public void run() throws IOException {
        final OffsetDateTime now = OffsetDateTime.now(clock.withZone(ZoneOffset.UTC));
        final Path directory = Files.createDirectories(config.resolveDirectory());
        final Path path = directory.resolve(FILE_PREFIX + FILE_STAMP.format(now) + ".txt");

        final Runtime runtime = Runtime.getRuntime();
        final String content = String.join(System.lineSeparator(),
                "Reporte generado: " + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now),
                "Equipo:           " + hostName(),
                "Sistema:          " + System.getProperty("os.name") + " " + System.getProperty("os.version"),
                "Procesadores:     " + runtime.availableProcessors(),
                "Memoria en uso:   " + (runtime.totalMemory() - runtime.freeMemory()) / 1024 + " KB",
                "");

        Files.writeString(path, content, StandardCharsets.UTF_8);

        LOG.info("Reporte diario generado en {}", path);
    }

    private static String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            return "desconocido";
        }
    }
}
