package com.odysseusinc.athena.glue;

import com.odysseusinc.athena.TestConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(initializers = LocalEnvironmentInitializer.class)
@CucumberContextConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestConfiguration.class,
        properties = "spring.main.allow-bean-definition-overriding=true" //TODO DEV don't like it, maybe it is better override configuration complitly
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class CucumberStepDefinitions {
}
