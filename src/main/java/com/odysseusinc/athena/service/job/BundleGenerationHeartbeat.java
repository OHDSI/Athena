package com.odysseusinc.athena.service.job;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

/** Renews and monitors one bundle-generation lease while CPU-heavy work is running. */
@Component
public class BundleGenerationHeartbeat {

    private final BundleGenerationQueueService queue;
    private final ThreadPoolTaskScheduler scheduler;
    private final Duration interval;

    public BundleGenerationHeartbeat(
            BundleGenerationQueueService queue,
            @Qualifier("bundleWorkerScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${bundle.worker.heartbeat-interval:PT30S}") Duration interval) {

        this.queue = queue;
        this.scheduler = scheduler;
        this.interval = interval;
    }

    public Lease start(long bundleId, String workerId) {

        queue.heartbeat(bundleId, workerId);
        AtomicReference<RuntimeException> failure = new AtomicReference<>();
        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
            try {
                queue.heartbeat(bundleId, workerId);
            } catch (RuntimeException exception) {
                failure.compareAndSet(null, exception);
            }
        }, interval);
        return new Lease(task, failure);
    }

    public static final class Lease implements AutoCloseable {

        private final ScheduledFuture<?> task;
        private final AtomicReference<RuntimeException> failure;

        private Lease(ScheduledFuture<?> task, AtomicReference<RuntimeException> failure) {

            this.task = task;
            this.failure = failure;
        }

        public void check() {

            RuntimeException exception = failure.get();
            if (exception != null) {
                throw exception;
            }
        }

        @Override
        public void close() {

            task.cancel(false);
        }
    }
}
