package com.carlosalbertoxw.jqs.config;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.quartz.CronExpression;

import com.carlosalbertoxw.jqs.services.RoutineServices;

/**
 * Valida al arrancar que cada rutina tenga un cron valido, una zona horaria existente, un
 * servicio registrado y una identidad unica. Asi un error de configuracion detiene la
 * aplicacion con un mensaje claro en lugar de fallar dentro de Quartz.
 */
public final class RoutinesValidator {

    private final RoutineServices services;

    public RoutinesValidator(RoutineServices services) {
        this.services = Objects.requireNonNull(services, "services");
    }

    /** @return la lista de errores encontrados; vacia si la configuracion es valida */
    public List<String> validate(List<RoutineConfig> routines) {
        final List<String> errors = new ArrayList<>();
        final Set<String> identities = new HashSet<>();

        for (int i = 0; i < routines.size(); i++) {
            final RoutineConfig routine = routines.get(i);
            if (routine == null) {
                errors.add("routines[" + i + "]: la entrada esta vacia.");
                continue;
            }
            final String prefix = "routines[" + i + "] (" + routine.getName() + ")";

            if (isBlank(routine.getName())) {
                errors.add(prefix + ": 'name' es obligatorio.");
            }

            if (isBlank(routine.getGroup())) {
                errors.add(prefix + ": 'group' es obligatorio.");
            }

            if (!identities.add(routine.getGroup() + "/" + routine.getName())) {
                errors.add(prefix + ": ya existe otra rutina con el mismo group/name.");
            }

            if (isBlank(routine.getService())) {
                errors.add(prefix + ": 'service' es obligatorio.");
            } else if (!services.isRegistered(routine.getService())) {
                errors.add(prefix + ": no hay un RoutineService registrado con la clave '"
                        + routine.getService() + "'.");
            }

            if (isBlank(routine.getCronExpression())
                    || !CronExpression.isValidExpression(routine.getCronExpression())) {
                errors.add(prefix + ": la expresion cron '" + routine.getCronExpression() + "' no es valida.");
            }

            if (!isBlank(routine.getTimeZone()) && !isValidZone(routine.getTimeZone())) {
                errors.add(prefix + ": la zona horaria '" + routine.getTimeZone() + "' no existe.");
            }
        }

        return errors;
    }

    private static boolean isValidZone(String zone) {
        try {
            ZoneId.of(zone);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
