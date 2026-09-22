package com.carlosalbertoxw.jqs.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.carlosalbertoxw.jqs.services.RoutineServices;

class RoutinesValidatorTest {

    private final RoutinesValidator validator =
            new RoutinesValidator(new RoutineServices().register("Example", () -> () -> { }));

    private static RoutineConfig validRoutine(String name) {
        RoutineConfig routine = new RoutineConfig();
        routine.setName(name);
        routine.setService("Example");
        routine.setCronExpression("0 * * ? * *");
        return routine;
    }

    private List<String> validate(RoutineConfig... routines) {
        return validator.validate(Arrays.asList(routines));
    }

    private static boolean anyContains(List<String> errors, String text) {
        return errors.stream().anyMatch(e -> e.contains(text));
    }

    @Test
    @DisplayName("Una configuracion valida no reporta errores")
    void validConfigurationPasses() {
        assertEquals(List.of(), validate(validRoutine("A"), validRoutine("B")));
    }

    @Test
    @DisplayName("Un cron invalido se reporta")
    void invalidCronFails() {
        RoutineConfig routine = validRoutine("Job");
        routine.setCronExpression("0 61 * ? * *");

        assertTrue(anyContains(validate(routine), "cron"));
    }

    @Test
    @DisplayName("Una zona horaria inexistente se reporta")
    void unknownTimeZoneFails() {
        RoutineConfig routine = validRoutine("Job");
        routine.setTimeZone("Marte/Olympus");

        assertTrue(anyContains(validate(routine), "zona horaria"));
    }

    @Test
    @DisplayName("Un servicio no registrado se reporta")
    void unregisteredServiceFails() {
        RoutineConfig routine = validRoutine("Job");
        routine.setService("NoExiste");

        assertTrue(anyContains(validate(routine), "NoExiste"));
    }

    @Test
    @DisplayName("Dos rutinas con el mismo group/name se reportan")
    void duplicateIdentityFails() {
        assertTrue(anyContains(validate(validRoutine("A"), validRoutine("A")), "group/name"));
    }

    @Test
    @DisplayName("Los campos obligatorios vacios se reportan todos juntos")
    void blankRequiredFieldsFail() {
        RoutineConfig routine = new RoutineConfig();

        List<String> errors = validate(routine);

        assertTrue(anyContains(errors, "'name'"));
        assertTrue(anyContains(errors, "'service'"));
        assertTrue(anyContains(errors, "cron"));
    }

    @Test
    @DisplayName("Una zona horaria vacia se resuelve a UTC")
    void emptyTimeZoneResolvesToUtc() {
        assertEquals(TimeZone.getTimeZone("UTC"), validRoutine("Job").resolveTimeZone());
    }
}
