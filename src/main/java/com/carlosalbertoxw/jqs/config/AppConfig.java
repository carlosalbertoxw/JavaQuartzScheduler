package com.carlosalbertoxw.jqs.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Raiz de {@code routines.yml}: la lista de rutinas y la configuracion de reportes.
 */
public class AppConfig {

    private List<RoutineConfig> routines = new ArrayList<>();

    private ReportsConfig reports = new ReportsConfig();

    public List<RoutineConfig> getRoutines() {
        return routines;
    }

    public void setRoutines(List<RoutineConfig> routines) {
        this.routines = routines == null ? new ArrayList<>() : routines;
    }

    public ReportsConfig getReports() {
        return reports;
    }

    public void setReports(ReportsConfig reports) {
        this.reports = reports == null ? new ReportsConfig() : reports;
    }
}
