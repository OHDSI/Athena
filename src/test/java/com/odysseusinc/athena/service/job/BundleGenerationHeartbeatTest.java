package com.odysseusinc.athena.service.job;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BundleGenerationHeartbeatTest {

    @Mock
    private BundleGenerationQueueService queue;

    @Mock
    private ThreadPoolTaskScheduler scheduler;

    @Mock
    private ScheduledFuture<?> scheduledTask;

    @Test
    void renewsUntilTheLeaseIsClosed() {

        Duration interval = Duration.ofSeconds(30);
        ArgumentCaptor<Runnable> heartbeatTask = ArgumentCaptor.forClass(Runnable.class);
        doReturn(scheduledTask).when(scheduler)
                .scheduleAtFixedRate(heartbeatTask.capture(), eq(interval));
        BundleGenerationHeartbeat heartbeat =
                new BundleGenerationHeartbeat(queue, scheduler, interval);

        BundleGenerationHeartbeat.Lease lease = heartbeat.start(42L, "worker-a");
        heartbeatTask.getValue().run();
        lease.check();
        lease.close();

        verify(queue, times(2)).heartbeat(42L, "worker-a");
        verify(scheduledTask).cancel(false);
    }

    @Test
    void exposesAHeartbeatFailureToTheGenerationThread() {

        Duration interval = Duration.ofSeconds(30);
        ArgumentCaptor<Runnable> heartbeatTask = ArgumentCaptor.forClass(Runnable.class);
        doReturn(scheduledTask).when(scheduler)
                .scheduleAtFixedRate(heartbeatTask.capture(), eq(interval));
        doNothing().doThrow(new IllegalStateException("lease lost"))
                .when(queue).heartbeat(42L, "worker-a");
        BundleGenerationHeartbeat heartbeat =
                new BundleGenerationHeartbeat(queue, scheduler, interval);

        BundleGenerationHeartbeat.Lease lease = heartbeat.start(42L, "worker-a");
        heartbeatTask.getValue().run();

        assertThrows(IllegalStateException.class, lease::check);
        lease.close();
    }
}
