package com.carlosalbertoxw.jqs.config;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Configuracion de una rutina: que servicio ejecutar y cuando. Se enlaza desde cada
 * elemento de la lista {@code routines} de {@code routines.yml}.
 */
public class RoutineConfig {

    /** Nombre logico del job (usado en la identidad de Quartz). */
    private String name = "";

    /** Grupo al que pertenecen el job y el trigger en Quartz. */
    private String group = "Routines";

    /** Clave del {@link com.carlosalbertoxw.jqs.services.RoutineService} registrado. */
    private String service = "";

    /**
     * Expresion cron de Quartz (6-7 campos: seg min hora diaMes mes diaSemana [anio]).
     * Ejemplo: "0 * * ? * *" = cada minuto.
     */
    private String cronExpression = "";

    /** Zona horaria IANA (p. ej. America/Mexico_City) para evaluar el cron. Vacio = UTC. */
    private String timeZone;

    /** Si esta deshabilitada, la rutina no se programa al arrancar. */
    private boolean enabled = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Zona horaria efectiva: la configurada o UTC si esta vacia.
     *
     * @throws java.time.DateTimeException si la zona no existe
     */
    public TimeZone resolveTimeZone() {
        return timeZone == null || timeZone.isBlank()
                ? TimeZone.getTimeZone(ZoneId.of("UTC"))
                : TimeZone.getTimeZone(ZoneId.of(timeZone));
    }
}
