package com.carlosalbertoxw.jqs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import com.carlosalbertoxw.jqs.config.AppConfig;
import com.carlosalbertoxw.jqs.config.RoutineConfig;
import com.carlosalbertoxw.jqs.jobs.RoutineJob;
import com.carlosalbertoxw.jqs.services.RoutineServices;

class MainTest {

    private static RoutineConfig routine() {
        RoutineConfig routine = new RoutineConfig();
        routine.setName("Job");
        routine.setGroup("Tests");
        routine.setService("Example");
        routine.setCronExpression("0 0 7 ? * MON-FRI");
        routine.setTimeZone("America/Mexico_City");
        return routine;
    }

    @Test
    @DisplayName("buildJob crea un RoutineJob con la identidad y el servicio de la rutina")
    void buildJobHasExpectedIdentityAndService() {
        JobDetail job = Main.buildJob(routine());

        assertEquals(RoutineJob.class, job.getJobClass());
        assertEquals("Job", job.getKey().getName());
        assertEquals("Tests", job.getKey().getGroup());
        assertEquals("Example", job.getJobDataMap().getString(RoutineJob.SERVICE_KEY));
    }

    @Test
    @DisplayName("buildTrigger crea un CronTrigger con cron, zona horaria y politica de misfire")
    void buildTriggerUsesRoutineSchedule() {
        Trigger trigger = Main.buildTrigger(routine());

        CronTrigger cron = assertInstanceOf(CronTrigger.class, trigger);
        assertEquals("0 0 7 ? * MON-FRI", cron.getCronExpression());
        assertEquals("America/Mexico_City", cron.getTimeZone().getID());
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING, cron.getMisfireInstruction());
        assertEquals("Job-trigger", trigger.getKey().getName());
        assertEquals("Tests", trigger.getKey().getGroup());
        assertEquals(Main.buildJob(routine()).getKey(), trigger.getJobKey());
        assertNotNull(trigger.getFireTimeAfter(new Date()));
    }

    @Test
    @DisplayName("El routines.yml incluido carga y es valido con los servicios registrados")
    void bundledConfigurationIsValid() throws Exception {
        AppConfig config = Main.loadConfig(new String[0]);
        RoutineServices services = Main.registerServices(config.getReports(), Clock.systemUTC());

        assertEquals(List.of("RoutineJob", "DailyReport", "ReportCleanup"),
                config.getRoutines().stream().map(RoutineConfig::getName).toList());
        assertEquals(List.of(), Main.validate(config, services));
    }

    @Test
    @DisplayName("Los errores de la seccion reports tambien detienen el arranque")
    void invalidReportsSectionFails() {
        AppConfig config = new AppConfig();
        config.getReports().setRetentionDays(0);

        List<String> errors = Main.validate(config, Main.registerServices(config.getReports(), Clock.systemUTC()));

        assertTrue(errors.stream().anyMatch(e -> e.contains("retentionDays")));
    }
}
