package com.carlosalbertoxw.jqs.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoutineServicesTest {

    @Test
    @DisplayName("Crea el servicio registrado con la clave indicada")
    void createsRegisteredService() {
        RoutineServices services = new RoutineServices().register(ExampleRoutineService.KEY, ExampleRoutineService::new);

        assertTrue(services.isRegistered(ExampleRoutineService.KEY));
        assertFalse(services.isRegistered("NoExiste"));
        assertTrue(services.create(ExampleRoutineService.KEY) instanceof ExampleRoutineService);
    }

    @Test
    @DisplayName("No se puede registrar dos veces la misma clave")
    void duplicateKeyIsRejected() {
        RoutineServices services = new RoutineServices().register("A", ExampleRoutineService::new);

        assertThrows(IllegalArgumentException.class, () -> services.register("A", ExampleRoutineService::new));
    }

    @Test
    @DisplayName("Crear un servicio no registrado falla")
    void unknownKeyFails() {
        assertThrows(IllegalArgumentException.class, () -> new RoutineServices().create("NoExiste"));
    }
}
