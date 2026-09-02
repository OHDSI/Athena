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
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class AsyncConfiguration implements AsyncConfigurer {

    /**
     * How long shutdown waits for in-flight asynchronous work before giving up on it.
     */
    @Value("${bundle.awaitTerminationSeconds:60}")
    private int awaitTerminationSeconds;

    @Bean(name = "emailSenderExecutor")
    public Executor emailsExecutor() {

        return fixedSizePool(1, "email-sender-");
    }

    @Bean(name = "bundleWorkerScheduler")
    public ThreadPoolTaskScheduler bundleWorkerScheduler() {

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // One thread runs archive generation; the other renews its database lease.
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("bundle-worker-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(awaitTerminationSeconds);
        return scheduler;
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
