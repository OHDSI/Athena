package com.odysseusinc.athena.service.job;

import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.repositories.athena.DownloadBundleRepository;
import com.odysseusinc.athena.util.DownloadBundleStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static com.odysseusinc.athena.util.DownloadBundleStatus.GENERATING;
import static com.odysseusinc.athena.util.DownloadBundleStatus.PENDING;

/**
 * Durable queue state for CPU-heavy bundle packaging.
 *
 * <p>The PostgreSQL advisory lock serializes claims across every application instance. The
 * row lock prevents another claimant from selecting the same bundle and the GENERATING check
 * deliberately limits the whole deployment to one active archive at a time.</p>
 */
@Service
public class BundleGenerationQueueService {

    private static final long CLAIM_LOCK_ID = 0x415448454e41424cL;
    private static final int MAX_FAILURE_LENGTH = 1000;

    private final DownloadBundleRepository repository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${bundle.worker.max-attempts:3}")
    private int maxAttempts;

    @Value("${bundle.worker.stale-after:PT5M}")
    private Duration staleAfter;

    @Autowired
    public BundleGenerationQueueService(
            DownloadBundleRepository repository,
            @Qualifier("dataSourceAthenaDB") DataSource dataSource) {

        this(repository, new JdbcTemplate(dataSource));
    }

    BundleGenerationQueueService(
            DownloadBundleRepository repository,
            JdbcTemplate jdbcTemplate) {

        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(transactionManager = "athenaTransactionManager")
    public void enqueue(long bundleId) {

        DownloadBundle bundle = get(bundleId);
        if (bundle.getStatus() == GENERATING) {
            return;
        }
        bundle.setStatus(PENDING);
        bundle.setGenerationStartedAt(null);
        bundle.setGenerationHeartbeatAt(null);
        bundle.setGenerationWorker(null);
        bundle.setGenerationFailure(null);
        bundle.setGenerationAttempts(0);
        repository.save(bundle);
    }

    @Transactional(transactionManager = "athenaTransactionManager")
    public Optional<Claim> claimNext(String workerId) {

        if (!acquireClaimLock()) {
            return Optional.empty();
        }

        return repository.findNextPendingForUpdate().map(bundle -> claim(bundle, workerId));
    }

    @Transactional(transactionManager = "athenaTransactionManager")
    public Optional<Claim> claim(long bundleId, String workerId) {

        if (!acquireClaimLock()) {
            return Optional.empty();
        }

        return repository.findPendingByIdForUpdate(bundleId)
                .map(bundle -> claim(bundle, workerId));
    }

    @Transactional(transactionManager = "athenaTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void heartbeat(long bundleId, String workerId) {

        if (repository.heartbeatGenerating(bundleId, workerId, new Date()) != 1) {
            throw new IllegalStateException("Bundle generation lease is no longer owned by this worker");
        }
    }

    @Transactional(transactionManager = "athenaTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void markReady(long bundleId, String workerId) {

        if (repository.markReadyGenerating(bundleId, workerId, new Date()) != 1) {
            throw new IllegalStateException("Bundle generation lease is no longer owned by this worker");
        }
    }

    @Transactional(transactionManager = "athenaTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public boolean markFailed(long bundleId, String workerId, Throwable failure) {

        return repository.markFailedGenerating(
                bundleId, workerId, new Date(), summarize(failure)) == 1;
    }

    @Transactional(transactionManager = "athenaTransactionManager")
    public int recoverStaleJobs() {

        Date staleBefore = Date.from(Instant.now().minus(staleAfter));
        int failed = repository.failExhaustedGenerating(staleBefore, maxAttempts);
        int requeued = repository.requeueStaleGenerating(staleBefore, maxAttempts);
        return failed + requeued;
    }

    private DownloadBundle get(long bundleId) {

        return repository.findById(bundleId)
                .orElseThrow(() -> new NotExistException(
                        "Cannot find bundle with id =" + bundleId, DownloadBundle.class));
    }

    private boolean acquireClaimLock() {

        jdbcTemplate.queryForList("SELECT pg_advisory_xact_lock(?)", CLAIM_LOCK_ID);
        return !repository.existsByStatus(GENERATING);
    }

    private Claim claim(DownloadBundle bundle, String workerId) {

        Date now = new Date();
        bundle.setStatus(GENERATING);
        bundle.setGenerationStartedAt(now);
        bundle.setGenerationHeartbeatAt(now);
        bundle.setGenerationWorker(workerId);
        bundle.setGenerationAttempts(bundle.getGenerationAttempts() + 1);
        bundle.setGenerationFailure(null);
        repository.save(bundle);
        return new Claim(bundle.getId(), workerId);
    }

    private String summarize(Throwable failure) {

        String message = failure.getClass().getSimpleName() + ": "
                + Optional.ofNullable(failure.getMessage()).orElse("No detail");
        message = message.replace('\n', ' ').replace('\r', ' ');
        return message.substring(0, Math.min(message.length(), MAX_FAILURE_LENGTH));
    }

    public record Claim(long bundleId, String workerId) {
    }
}
