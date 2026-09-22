package com.carlosalbertoxw.jqs.jobs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.carlosalbertoxw.jqs.services.RoutineService;
import com.carlosalbertoxw.jqs.services.RoutineServices;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoutineJobTest {

    private static final String SERVICE_KEY = "Test";

    @Mock
    private JobExecutionContext context;

    @Mock
    private JobDetail jobDetail;

    private final JobDataMap dataMap = new JobDataMap();

    @BeforeEach
    void setUp() {
        dataMap.put(RoutineJob.SERVICE_KEY, SERVICE_KEY);
        when(jobDetail.getKey()).thenReturn(new JobKey("Job", "Tests"));
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(context.getMergedJobDataMap()).thenReturn(dataMap);
    }

    private static RoutineJob jobFor(RoutineService service) {
        return new RoutineJob(new RoutineServices().register(SERVICE_KEY, () -> service));
    }

    @Test
    @DisplayName("Ejecuta el servicio configurado en el JobDataMap")
    void runsConfiguredService() throws Exception {
        AtomicInteger runs = new AtomicInteger();

        jobFor(runs::incrementAndGet).execute(context);

        assertEquals(1, runs.get());
    }

    @Test
    @DisplayName("Envuelve los fallos en JobExecutionException sin reintento inmediato")
    void wrapsFailuresInJobExecutionException() {
        IllegalStateException failure = new IllegalStateException("boom");

        JobExecutionException ex = assertThrows(JobExecutionException.class,
                () -> jobFor(() -> { throw failure; }).execute(context));

        assertSame(failure, ex.getCause());
        assertFalse(ex.refireImmediately());
    }

    @Test
    @DisplayName("Una interrupcion por apagado no se considera error")
    void swallowsInterruptionDuringShutdown() {
        AtomicReference<RoutineJob> job = new AtomicReference<>();
        job.set(jobFor(() -> {
            job.get().interrupt();
            Thread.sleep(10_000);
        }));

        assertDoesNotThrow(() -> job.get().execute(context));
        assertFalse(Thread.currentThread().isInterrupted());
    }

    @Test
    @DisplayName("Una InterruptedException ajena al apagado si es un error")
    void unexpectedInterruptionFails() {
        assertThrows(JobExecutionException.class,
                () -> jobFor(() -> { throw new InterruptedException(); }).execute(context));
    }

    @Test
    @DisplayName("Falla si el JobDataMap no define el servicio")
    void failsWhenServiceKeyIsMissing() {
        dataMap.remove(RoutineJob.SERVICE_KEY);

        JobExecutionException ex = assertThrows(JobExecutionException.class,
                () -> jobFor(() -> { }).execute(context));

        assertInstanceOf(IllegalStateException.class, ex.getCause());
    }
}
