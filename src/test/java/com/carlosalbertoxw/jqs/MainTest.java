package com.carlosalbertoxw.jqs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Trigger;

class MainTest {

    @Test
    @DisplayName("buildJob crea un JobDetail para Worker con la identidad esperada")
    void buildJobHasExpectedIdentity() {
        JobDetail job = Main.buildJob();

        assertNotNull(job);
        assertEquals(Worker.class, job.getJobClass());
        assertEquals(Main.JOB_NAME, job.getKey().getName());
        assertEquals(Main.GROUP, job.getKey().getGroup());
    }

    @Test
    @DisplayName("buildTrigger crea un CronTrigger con la expresion e identidad indicadas")
    void buildTriggerUsesGivenCron() {
        Trigger trigger = Main.buildTrigger(Main.DEFAULT_CRON);

        assertNotNull(trigger);
        assertInstanceOf(CronTrigger.class, trigger);
        assertEquals(Main.DEFAULT_CRON, ((CronTrigger) trigger).getCronExpression());
        assertEquals(Main.TRIGGER_NAME, trigger.getKey().getName());
        assertEquals(Main.GROUP, trigger.getKey().getGroup());
    }

    @Test
    @DisplayName("La expresion cron por defecto es valida y produce disparos futuros")
    void defaultCronProducesFutureFireTime() {
        Trigger trigger = Main.buildTrigger(Main.DEFAULT_CRON);

        assertNotNull(trigger.getFireTimeAfter(new java.util.Date()));
    }

    @Test
    @DisplayName("buildTrigger lanza excepcion con una expresion cron invalida")
    void buildTriggerRejectsInvalidCron() {
        assertThrows(RuntimeException.class, () -> Main.buildTrigger("esto-no-es-cron"));
    }
}
