package com.carlosalbertoxw.jqs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;

@ExtendWith(MockitoExtension.class)
class WorkerTest {

    @Mock
    private JobExecutionContext context;

    @Test
    @DisplayName("execute se ejecuta sin lanzar excepciones")
    void executeDoesNotThrow() {
        when(context.getNextFireTime()).thenReturn(new Date());

        Worker worker = new Worker();

        assertDoesNotThrow(() -> worker.execute(context));
        verify(context).getNextFireTime();
    }
}
