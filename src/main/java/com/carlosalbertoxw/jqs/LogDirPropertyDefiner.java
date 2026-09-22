package com.carlosalbertoxw.jqs;

import ch.qos.logback.core.PropertyDefinerBase;

/**
 * Define la carpeta de logs para {@code logback.xml}: la propiedad del sistema
 * {@code log.dir} si se indico, o {@code logs/} junto al jar ({@link AppDirectory}).
 */
public class LogDirPropertyDefiner extends PropertyDefinerBase {

    static final String LOG_DIR_PROPERTY = "log.dir";

    @Override
    public String getPropertyValue() {
        final String configured = System.getProperty(LOG_DIR_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return AppDirectory.get().resolve("logs").toString();
    }
}
