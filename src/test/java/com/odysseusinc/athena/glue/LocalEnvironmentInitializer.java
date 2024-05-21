package com.odysseusinc.athena.glue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.UUID;

import static org.testcontainers.containers.PostgreSQLContainer.IMAGE;


/**
 * This initializer captures logs with NOTICE level from the 'athena_cdm_v5_history' database.
 * For debugging PostgreSQL functions, developers can insert RAISE NOTICE alerts into any store function in any Liquibase migration.
 * This enables viewing of result values in the logs.
 */
@Slf4j
public class LocalEnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse(IMAGE).withTag("16.2");

    private static final Map<String, PostgreSQLContainer<?>> containers = ImmutableMap.of(
            "athena_db", createPostgreSQLContainer("athena_db"),
            "athena_cdm_v4_5", createPostgreSQLContainer("athena_cdm_v4_5"),
            "athena_cdm_v5", createPostgreSQLContainer("athena_cdm_v5"),
            "athena_cdm_v5_history", createPostgreSQLContainer("athena_cdm_v5_history")
    );

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        containers.forEach((name, container) -> {
            if (name.equals("athena_cdm_v5_history")) {
                container.setCommand("postgres", "-c", "fsync=off", "-c", "log_statement=all", "-c", "log_min_messages=NOTICE");
                container.start();
                container.followOutput(this::processLog);
            } else {
                container.start();
            }
        });

        Map<String, Object> properties = ImmutableMap.<String, Object>builder()
                .put("spring.datasource-db.jdbc-url", containers.get("athena_db").getJdbcUrl())
                .put("spring.datasource-db.username", containers.get("athena_db").getUsername())
                .put("spring.datasource-db.password", containers.get("athena_db").getPassword())
                .put("spring.datasource-v4.jdbc-url", containers.get("athena_cdm_v4_5").getJdbcUrl())
                .put("spring.datasource-v4.username", containers.get("athena_cdm_v4_5").getUsername())
                .put("spring.datasource-v4.password", containers.get("athena_cdm_v4_5").getPassword())
                .put("spring.datasource-v5.jdbc-url", containers.get("athena_cdm_v5").getJdbcUrl())
                .put("spring.datasource-v5.username", containers.get("athena_cdm_v5").getUsername())
                .put("spring.datasource-v5.password", containers.get("athena_cdm_v5").getPassword())
                .put("spring.datasource-v5-history.jdbc-url", containers.get("athena_cdm_v5_history").getJdbcUrl())
                .put("spring.datasource-v5-history.username", containers.get("athena_cdm_v5_history").getUsername())
                .put("spring.datasource-v5-history.password", containers.get("athena_cdm_v5_history").getPassword())
                .build();

        properties.forEach((key, value) -> log.info("Configuration - {}: {}", key, value));

        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("testcontainers", properties));

        ApplicationListener<ContextClosedEvent> onClose =
                event -> containers.values().forEach(container -> container.stop());
        context.addApplicationListener(onClose);
    }

    private void processLog(OutputFrame frame) {
        String logMessage = frame.getUtf8String();
        if (logMessage.contains("NOTICE:") && !logMessage.contains("RAISE NOTICE")) {
            // Print each log message on a new line, removing trailing line break.
            log.debug("\n   {}", logMessage.replaceFirst("\\s+$", ""));
        }
    }

    private static PostgreSQLContainer<?> createPostgreSQLContainer(String databaseName) {
        return new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withDatabaseName(databaseName)
                .withUsername("ohdsi").withPassword("ohdsi")
                .withCreateContainerCmdModifier(cmd -> cmd.withName(databaseName + "_" + UUID.randomUUID()));
    }
}
