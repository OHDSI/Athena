package com.odysseusinc.athena;


import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
import org.testcontainers.shaded.com.google.common.util.concurrent.ListeningExecutorService;
import org.testcontainers.shaded.com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executor;


@Configuration
@SpringBootConfiguration
@EnableAutoConfiguration
@Profile("test")
@ComponentScan(
        basePackageClasses = TestConfiguration.class
)
public class TestConfiguration {

    @Primary
    @Bean(name = "bundleExecutor")
    public Executor bundleExecutor() {
        return sameThreadExecutor();
    }

    @Primary
    @Bean(name = "deltaExecutor")
    public Executor deltaExecutor() {
        return sameThreadExecutor();
    }

    private static Executor sameThreadExecutor() {
        return MoreExecutors.newDirectExecutorService();
    }

}
