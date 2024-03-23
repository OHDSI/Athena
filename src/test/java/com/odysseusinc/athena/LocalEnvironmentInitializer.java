package com.odysseusinc.athena;

import com.sun.tools.javac.util.List;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.testcontainers.containers.PostgreSQLContainer.IMAGE;

public class LocalEnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse(IMAGE).withTag("16.2");
    private static final PostgreSQLContainer<?> athenaDBContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("athena_db")
            .withUsername("ohdsi").withPassword("ohdsi");

    private static final PostgreSQLContainer<?> athenaCDMv4_5Container = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("athena_cdm_v4_5")
            .withUsername("ohdsi").withPassword("ohdsi");

    private static final PostgreSQLContainer<?> athenaCDMv5Container = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("athena_cdm_v5")
            .withUsername("ohdsi").withPassword("ohdsi");

    private static final PostgreSQLContainer<?> athenaCDMv5HistoryContainer = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("athena_cdm_v5_history")
            .withUsername("ohdsi").withPassword("ohdsi");
    private static final List<PostgreSQLContainer<?>> containers = List.of(athenaDBContainer, athenaCDMv4_5Container, athenaCDMv5Container, athenaCDMv5HistoryContainer);

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        containers.forEach(GenericContainer::start);
        Map<String, Object> properties = ImmutableMap.<String, Object>builder()
                .put("spring.datasource-db.jdbc-url", athenaDBContainer.getJdbcUrl())
                .put("spring.datasource-db.username", athenaDBContainer.getUsername())
                .put("spring.datasource-db.password", athenaDBContainer.getPassword())
                .put("spring.datasource-v4.jdbc-url", athenaCDMv4_5Container.getJdbcUrl())
                .put("spring.datasource-v4.username", athenaCDMv4_5Container.getUsername())
                .put("spring.datasource-v4.password", athenaCDMv4_5Container.getPassword())
                .put("spring.datasource-v5.jdbc-url", athenaCDMv5Container.getJdbcUrl())
                .put("spring.datasource-v5.username", athenaCDMv5Container.getUsername())
                .put("spring.datasource-v5.password", athenaCDMv5Container.getPassword())
                .put("spring.datasource-v5-history.jdbc-url", athenaCDMv5HistoryContainer.getJdbcUrl())
                .put("spring.datasource-v5-history.username", athenaCDMv5HistoryContainer.getUsername())
                .put("spring.datasource-v5-history.password", athenaCDMv5HistoryContainer.getPassword())
                .build();

        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("testcontainers", properties));

        ApplicationListener<ContextClosedEvent> onClose =
                event -> containers.forEach(GenericContainer::stop);
        context.addApplicationListener(onClose);
    }
}
