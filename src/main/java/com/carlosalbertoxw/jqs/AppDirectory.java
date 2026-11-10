package com.carlosalbertoxw.jqs;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Carpeta de la aplicacion: la que contiene el jar en ejecucion. Un servicio (WinSW,
 * systemd) puede arrancar en otro directorio de trabajo, asi que las rutas relativas
 * (logs, routines.yml) se resuelven contra esta carpeta para que queden junto al jar.
 *
 * <p>Al ejecutar desde el IDE o con Maven (clases sueltas, sin jar) se usa el directorio
 * de trabajo actual.
 */
public final class AppDirectory {

    /** Se calcula una sola vez: la ubicacion del jar no cambia durante la ejecucion. */
    private static final Path DIRECTORY = locate();

    private AppDirectory() {
    }

    public static Path get() {
        return DIRECTORY;
    }

    private static Path locate() {
        try {
            final Path location = Path.of(AppDirectory.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(location)) {
                return location.toAbsolutePath().getParent();
            }
        } catch (Exception e) {
            // Sin informacion de origen del codigo: se usa el directorio actual.
        }
        return Path.of("").toAbsolutePath();
    }
}
