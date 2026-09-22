package com.carlosalbertoxw.jqs.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoutinesLoaderTest {

    private static AppConfig loadConfig(String yaml) throws IOException {
        return RoutinesLoader.load(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }

    private static List<RoutineConfig> load(String yaml) throws IOException {
        return loadConfig(yaml).getRoutines();
    }

    @Test
    @DisplayName("Lee las rutinas y aplica los valores por defecto")
    void readsRoutinesWithDefaults() throws IOException {
        List<RoutineConfig> routines = load("""
                routines:
                  - name: A
                    service: Example
                    cronExpression: "0 * * ? * *"
                  - name: B
                    group: Reportes
                    service: Example
                    cronExpression: "0 0 7 ? * MON-FRI"
                    timeZone: America/Mexico_City
                    enabled: false
                """);

        assertEquals(2, routines.size());

        RoutineConfig a = routines.get(0);
        assertEquals("A", a.getName());
        assertEquals("Routines", a.getGroup());
        assertTrue(a.isEnabled());

        RoutineConfig b = routines.get(1);
        assertEquals("Reportes", b.getGroup());
        assertEquals("America/Mexico_City", b.getTimeZone());
        assertFalse(b.isEnabled());
    }

    @Test
    @DisplayName("Una clave desconocida se reporta como error")
    void unknownKeyFails() {
        assertThrows(IOException.class, () -> load("""
                routines:
                  - name: A
                    cron: "0 * * ? * *"
                """));
    }

    @Test
    @DisplayName("Lee la seccion reports y usa sus valores por defecto si falta")
    void readsReportsSection() throws IOException {
        AppConfig config = loadConfig("""
                reports:
                  directory: C:/reportes
                  retentionDays: 30
                """);

        assertEquals("C:/reportes", config.getReports().getDirectory());
        assertEquals(30, config.getReports().getRetentionDays());
        assertEquals(List.of(), config.getRoutines());

        ReportsConfig defaults = loadConfig("routines: []\n").getReports();
        assertEquals("reports", defaults.getDirectory());
        assertEquals(7, defaults.getRetentionDays());
    }

    @Test
    @DisplayName("Un archivo sin la lista routines no tiene rutinas")
    void missingRoutinesIsEmpty() throws IOException {
        assertEquals(List.of(), load("routines:\n"));
    }
}
