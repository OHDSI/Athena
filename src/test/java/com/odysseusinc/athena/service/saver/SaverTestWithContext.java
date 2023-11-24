package com.odysseusinc.athena.service.saver;

import com.odysseusinc.athena.config.WebApplicationStarter;
import com.odysseusinc.athena.service.saver.v5.ConceptClassV5Saver;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApplicationStarter.class)
@ContextConfiguration(initializers = {SaverTestWithContext.Initializer.class})
public class SaverTestWithContext {

    @Autowired
    private ConceptClassV5Saver conceptClassV5Saver;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11.1")
            .withDatabaseName("athenadb")
            .withUsername("ohdsi")
            .withPassword("ohdsi");


    @Test
    @Transactional
    public void doSomething() {
        conceptClassV5Saver.save(null, null, null);
        System.out.println("ttt");

    }

    public static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}