package com.carlosalbertoxw.jqs.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.carlosalbertoxw.jqs.config.ReportsConfig;

class ReportServicesTest {

    private static final Instant NOW = Instant.parse("2026-09-22T07:00:00Z");

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @TempDir
    Path tempDir;

    private Path directory;
    private ReportsConfig config;

    @BeforeEach
    void setUp() {
        directory = tempDir.resolve("reports");
        config = new ReportsConfig();
        config.setDirectory(directory.toString());
        config.setRetentionDays(7);
    }

    @AfterEach
    void clearInterrupt() {
        Thread.interrupted();
    }

    @Test
    @DisplayName("DailyReport escribe el archivo de reporte con la fecha en el nombre")
    void dailyReportWritesReportFile() throws Exception {
        new DailyReportService(config, clock).run();

        Path file = directory.resolve("report-20260922-070000.txt");
        assertTrue(Files.exists(file));
        assertTrue(Files.readString(file).contains("Reporte generado: 2026-09-22T07:00:00"));
    }

    @Test
    @DisplayName("La limpieza borra solo los reportes vencidos")
    void cleanupDeletesOnlyExpiredReports() throws Exception {
        Files.createDirectories(directory);
        Path expired = createFile("report-old.txt", NOW.minus(Duration.ofDays(8)));
        Path recent = createFile("report-new.txt", NOW.minus(Duration.ofDays(6)));
        Path unrelated = createFile("otro-archivo.txt", NOW.minus(Duration.ofDays(30)));

        new ReportCleanupService(config, clock).run();

        assertFalse(Files.exists(expired));
        assertTrue(Files.exists(recent));
        assertTrue(Files.exists(unrelated));
    }

    @Test
    @DisplayName("La limpieza no hace nada si la carpeta no existe")
    void cleanupMissingDirectoryDoesNothing() throws Exception {
        new ReportCleanupService(config, clock).run();

        assertFalse(Files.exists(directory));
    }

    @Test
    @DisplayName("La limpieza se detiene si la rutina fue interrumpida")
    void cleanupStopsWhenInterrupted() throws Exception {
        Files.createDirectories(directory);
        Path expired = createFile("report-old.txt", NOW.minus(Duration.ofDays(8)));
        Thread.currentThread().interrupt();

        assertThrows(InterruptedException.class, () -> new ReportCleanupService(config, clock).run());
        assertTrue(Files.exists(expired));
    }

    private Path createFile(String name, Instant lastModified) throws IOException {
        Path path = Files.writeString(directory.resolve(name), "x");
        Files.setLastModifiedTime(path, FileTime.from(lastModified));
        return path;
    }
}
