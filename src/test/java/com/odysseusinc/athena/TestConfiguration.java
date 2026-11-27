package com.odysseusinc.athena;


import jakarta.mail.internet.MimeMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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
    @Bean
    public JavaMailSender testJavaMailSender() {

        return new JavaMailSenderImpl() {
            @Override
            public void send(@NotNull MimeMessage... mimeMessages) {
                // Integration tests verify generation state and artifacts, not SMTP delivery.
            }
        };
    }

    @Primary
    @Bean(name = "bundleExecutor")
    public Executor bundleExecutor() {
        return sameThreadExecutor();
    }

    @Primary
    @Bean(name = "bundleDeltaExecutor")
    public Executor deltaExecutor() {
        return sameThreadExecutor();
    }

    private static Executor sameThreadExecutor() {
        return MoreExecutors.newDirectExecutorService();
    }

}
