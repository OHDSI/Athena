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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class AsyncConfiguration implements AsyncConfigurer {

    /**
     * The delta executor uses only one thread for both core and max pool size.
     * This way we ensure that only one bundle can be generated at a time, preventing PostgreSQL from being overwhelmed.
     */
    @Value("${corePoolSize:1}")
    private int corePoolSize;
    @Value("${maxPoolSize:1}")
    private int maxPoolSize;

    @Bean(name = "emailSenderExecutor")
    public Executor emailsExecutor() {

        return Executors.newSingleThreadExecutor();
    }


    @Bean(name = "deltaExecutor")
    public Executor deltaExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        return executor;
    }
}