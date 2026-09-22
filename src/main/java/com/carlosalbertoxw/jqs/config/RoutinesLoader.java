package com.carlosalbertoxw.jqs.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * Lee la configuracion de un archivo YAML con la forma:
 *
 * <pre>
 * routines:
 *   - name: RoutineJob
 *     service: Example
 *     cronExpression: "0 * * ? * *"
 * reports:
 *   directory: reports
 *   retentionDays: 7
 * </pre>
 *
 * Las claves desconocidas (p. ej. un error de escritura) se reportan como error.
 */
public final class RoutinesLoader {

    private static final ObjectMapper MAPPER = new YAMLMapper();

    private RoutinesLoader() {
    }

    public static AppConfig load(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return load(in);
        }
    }

    public static AppConfig load(InputStream in) throws IOException {
        final AppConfig config = MAPPER.readValue(in, AppConfig.class);
        return config == null ? new AppConfig() : config;
    }
}
