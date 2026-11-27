package com.odysseusinc.athena.service.job;

import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.repositories.athena.DownloadBundleRepository;
import com.odysseusinc.athena.util.DownloadBundleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BundleGenerationQueueServiceTest {

    @Mock
    private DownloadBundleRepository repository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private BundleGenerationQueueService queue;

    @BeforeEach
    void setUp() {

        queue = new BundleGenerationQueueService(repository, jdbcTemplate);
        ReflectionTestUtils.setField(queue, "maxAttempts", 3);
        ReflectionTestUtils.setField(queue, "staleAfter", Duration.ofHours(6));
    }

    @Test
    void claimsOldestPendingBundleAndRecordsOwnership() {

        DownloadBundle bundle = bundle(42L, DownloadBundleStatus.PENDING);
        when(repository.findNextPendingForUpdate()).thenReturn(Optional.of(bundle));

        Optional<BundleGenerationQueueService.Claim> claimed = queue.claimNext("worker-a");

        assertEquals(Optional.of(new BundleGenerationQueueService.Claim(42L, "worker-a")), claimed);
        assertEquals(DownloadBundleStatus.GENERATING, bundle.getStatus());
        assertEquals("worker-a", bundle.getGenerationWorker());
        assertEquals(1, bundle.getGenerationAttempts());
        verify(repository).save(bundle);
    }

    @Test
    void doesNotClaimAnotherBundleWhileOneIsGenerating() {

        when(repository.existsByStatus(DownloadBundleStatus.GENERATING)).thenReturn(true);

        assertTrue(queue.claimNext("worker-b").isEmpty());

        verify(repository, never()).findNextPendingForUpdate();
    }

    @Test
    void claimsTheRequestedPendingBundle() {

        DownloadBundle bundle = bundle(42L, DownloadBundleStatus.PENDING);
        when(repository.findPendingByIdForUpdate(42L)).thenReturn(Optional.of(bundle));

        Optional<BundleGenerationQueueService.Claim> claimed = queue.claim(42L, "worker-a");

        assertEquals(Optional.of(new BundleGenerationQueueService.Claim(42L, "worker-a")), claimed);
        verify(repository, never()).findNextPendingForUpdate();
    }

    @Test
    void enqueueClearsPreviousFailureState() {

        DownloadBundle bundle = bundle(42L, DownloadBundleStatus.FAILED);
        bundle.setGenerationFailure("old failure");
        when(repository.findById(42L)).thenReturn(Optional.of(bundle));

        queue.enqueue(42L);

        assertEquals(DownloadBundleStatus.PENDING, bundle.getStatus());
        assertEquals(null, bundle.getGenerationFailure());
        verify(repository).save(bundle);
    }

    @Test
    void staleWorkerCannotHeartbeatAReassignedBundle() {

        assertThrows(IllegalStateException.class,
                () -> queue.heartbeat(42L, "stale-worker"));
    }

    private DownloadBundle bundle(long id, DownloadBundleStatus status) {

        DownloadBundle bundle = new DownloadBundle();
        bundle.setId(id);
        bundle.setStatus(status);
        return bundle;
    }
}
