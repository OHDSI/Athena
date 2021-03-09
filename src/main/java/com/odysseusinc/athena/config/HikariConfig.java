package com.odysseusinc.athena.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class HikariConfig {

    @Bean
    @ConfigurationProperties("spring.datasource-db.hikari")
    protected HikariProps athenaDBHikariProps() {
        return new HikariProps();
    }

    @Bean
    @ConfigurationProperties("spring.datasource-v4.hikari")
    protected HikariProps athenaV4HikariProps() {
        return new HikariProps();
    }

    @Bean
    @ConfigurationProperties("spring.datasource-v5.hikari")
    protected HikariProps athenaV5HikariProps() {
        return new HikariProps();
    }

}