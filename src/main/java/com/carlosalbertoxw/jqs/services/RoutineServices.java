package com.carlosalbertoxw.jqs.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Registro de servicios de negocio por clave. Cada rutina de {@code routines.yml} indica
 * en {@code service} la clave del {@link RoutineService} que ejecuta. Se crea una
 * instancia nueva en cada ejecucion.
 */
public final class RoutineServices {

    private final Map<String, Supplier<? extends RoutineService>> factories = new HashMap<>();

    public RoutineServices register(String key, Supplier<? extends RoutineService> factory) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(factory, "factory");
        if (factories.putIfAbsent(key, factory) != null) {
            throw new IllegalArgumentException("Ya hay un servicio registrado con la clave '" + key + "'.");
        }
        return this;
    }

    public boolean isRegistered(String key) {
        return key != null && factories.containsKey(key);
    }

    /**
     * @throws IllegalArgumentException si no hay un servicio registrado con esa clave
     */
    public RoutineService create(String key) {
        final Supplier<? extends RoutineService> factory = key == null ? null : factories.get(key);
        if (factory == null) {
            throw new IllegalArgumentException("No hay un RoutineService registrado con la clave '" + key + "'.");
        }
        return factory.get();
    }
}
