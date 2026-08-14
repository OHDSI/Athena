package com.odysseusinc.athena.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class AsyncConfiguration implements AsyncConfigurer {

    /**
     * How many bundles may be generated concurrently. The rest queue.
     * <p>
     * This used to be paired with a {@code bundle.maxPoolSize} of 5, which never did
     * anything: {@code ThreadPoolTaskExecutor} defaults to an <em>unbounded</em> queue, and a
     * {@code ThreadPoolExecutor} only starts a thread beyond the core size once the queue is
     * full — which cannot happen. The pool has therefore always been exactly this many
     * threads. Rather than make the ceiling live (which would mean a small bounded queue, and
     * so rejecting bundle requests during a burst), the ceiling is set from this value and the
     * dead property is gone: capping concurrency and queueing the overflow is the behaviour
     * that was wanted, and the behaviour that was already happening.
     */
    @Value("${bundle.corePoolSize:3}")
    private int corePoolSize;

    /**
     * The delta executor uses only one thread, so that only one bundle can be generated at a
     * time and PostgreSQL is not overwhelmed.
     */
    @Value("${bundle.delta.corePoolSize:1}")
    private int deltaCorePoolSize;

    /**
     * How long shutdown waits for in-flight asynchronous work before giving up on it.
     */
    @Value("${bundle.awaitTerminationSeconds:60}")
    private int awaitTerminationSeconds;

    @Bean(name = "emailSenderExecutor")
    public Executor emailsExecutor() {

        return fixedSizePool(1, "email-sender-");
    }

    @Bean(name = "bundleExecutor")
    public Executor bundleExecutor() {

        return fixedSizePool(corePoolSize, "bundle-");
    }

    @Bean(name = "bundleDeltaExecutor")
    public Executor bundleDeltaExecutor() {

        return fixedSizePool(deltaCorePoolSize, "bundle-delta-");
    }

    /**
     * A pool of exactly {@code size} threads with an unbounded queue. Core and max are set
     * together on purpose — see the {@link #corePoolSize} javadoc for why a differing max
     * would be inert.
     */
    private ThreadPoolTaskExecutor fixedSizePool(int size, String threadNamePrefix) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(size);
        executor.setMaxPoolSize(size);
        executor.setThreadNamePrefix(threadNamePrefix);
        drainOnShutdown(executor);
        return executor;
    }

    /**
     * Without this an executor is only {@code shutdown()} on context close, which
     * does not wait: whatever is already running keeps running, against a context that is
     * being torn down. In production that means a redeploy silently abandons bundle
     * generation that is midway through and drops queued notification emails. In the test
     * suite it was the source of the intermittent
     * {@code GenericWebApplicationContext ... has been closed already} — {@code @DirtiesContext}
     * closes the Cucumber context after every scenario, so any task still finishing from the
     * scenario just ended raced the teardown.
     * <p>
     * The e-mail executor was worse than the other two: it was a raw
     * {@code Executors.newSingleThreadExecutor()} with no Spring lifecycle configuration at
     * all, and e-mail is sent at the <em>end</em> of bundle generation — exactly the work most
     * likely to still be queued when a scenario finishes.
     */
    private void drainOnShutdown(ThreadPoolTaskExecutor executor) {

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
    }
}