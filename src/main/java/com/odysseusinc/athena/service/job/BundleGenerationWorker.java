package com.odysseusinc.athena.service.job;

import com.odysseusinc.athena.service.impl.BundleGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.util.UUID;

/** Polls the durable queue on a scheduler dedicated to bundle generation. */
@Slf4j
@Component
@ConditionalOnProperty(name = "bundle.worker.enabled", havingValue = "true", matchIfMissing = true)
public class BundleGenerationWorker {

    private final BundleGenerationQueueService queue;
    private final BundleGenerationService generator;
    private final String workerId;

    public BundleGenerationWorker(
            BundleGenerationQueueService queue,
            BundleGenerationService generator,
            @Value("${bundle.worker.id:}") String configuredWorkerId) {

        this.queue = queue;
        this.generator = generator;
        String instanceId = ManagementFactory.getRuntimeMXBean().getName() + "-" + UUID.randomUUID();
        this.workerId = configuredWorkerId.isBlank()
                ? instanceId
                : configuredWorkerId + "-" + instanceId;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void recoverInterruptedJobs() {

        int recovered = queue.recoverStaleJobs();
        if (recovered > 0) {
            log.warn("Recovered [{}] stale bundle generation jobs", recovered);
        }
    }

    @Scheduled(fixedDelayString = "${bundle.worker.recovery-delay:PT1M}")
    public void recoverStaleJobs() {

        recoverInterruptedJobs();
    }

    @Scheduled(
            fixedDelayString = "${bundle.worker.poll-delay:PT1S}",
            scheduler = "bundleWorkerScheduler")
    public void processNext() {

        queue.claimNext(workerId).ifPresent(this::generate);
    }

    private void generate(BundleGenerationQueueService.Claim claim) {

        try {
            generator.generateBundle(claim.bundleId(), claim.workerId());
        } catch (Exception failure) {
            // Failures during initial entity/user loading happen before the generator can
            // handle them. Do not leave that claim stuck until stale-job recovery runs.
            try {
                queue.markFailed(claim.bundleId(), claim.workerId(), failure);
            } catch (Exception statusFailure) {
                failure.addSuppressed(statusFailure);
            }
            log.error("Bundle generation failed before the job could be processed", failure);
        }
    }
}
