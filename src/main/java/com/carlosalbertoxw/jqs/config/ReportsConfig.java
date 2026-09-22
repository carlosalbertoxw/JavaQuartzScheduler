package com.carlosalbertoxw.jqs.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.carlosalbertoxw.jqs.AppDirectory;

/**
 * Configuracion compartida por las rutinas de reportes (seccion {@code reports} de
 * {@code routines.yml}).
 */
public class ReportsConfig {

    static final int MAX_RETENTION_DAYS = 3650;

    /** Carpeta donde se escriben los reportes. Relativa = junto al jar. */
    private String directory = "reports";

    /** Dias que se conservan los reportes antes de que la limpieza los borre. */
    private int retentionDays = 7;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    /** Carpeta efectiva: absoluta tal cual, o relativa a la carpeta del jar. */
    public Path resolveDirectory() {
        return AppDirectory.get().resolve(directory).normalize();
    }

    /** @return la lista de errores encontrados; vacia si la configuracion es valida */
    public List<String> validate() {
        final List<String> errors = new ArrayList<>();
        if (directory == null || directory.isBlank()) {
            errors.add("reports: 'directory' es obligatorio.");
        }
        if (retentionDays < 1 || retentionDays > MAX_RETENTION_DAYS) {
            errors.add("reports: 'retentionDays' debe estar entre 1 y " + MAX_RETENTION_DAYS
                    + " (valor: " + retentionDays + ").");
        }
        return errors;
    }
}
