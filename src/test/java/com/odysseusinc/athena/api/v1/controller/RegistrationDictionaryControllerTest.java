package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.repositories.athena.RegistrationDictionaryRepository;
import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RegistrationDictionaryControllerTest {

    @ClassRule
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2");
    private static MockMvc mvc;

    @BeforeClass
    public static void setUp() {
        var dataSource = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        Flyway.configure().dataSource(dataSource).locations("db/migration/share", "db/migration/athena").load().migrate();
        JdbcClient.create(dataSource).sql("INSERT INTO countries (name, alfa2, alfa3) VALUES (?, 'XX', 'XXX')")
                .param("Test%_\\Country").update();
        var repository = new RegistrationDictionaryRepository(dataSource);
        mvc = MockMvcBuilders.standaloneSetup(new RegistrationDictionaryController(repository))
                .setControllerAdvice(new ExceptionHandlingController()).build();
    }

    @Test
    public void registrationGetsProfessionalTypeIdsAndNames() throws Exception {
        mvc.perform(get("/api/v1/users/professional-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value(0))
                .andExpect(jsonPath("$.result[?(@.id == 1)].name").value(containsInAnyOrder("IT Professional")));
    }

    @Test
    public void countriesKeepSelectedIsoCodeOrIdInAdditionToTheLimitedPage() throws Exception {
        for (String selected : new String[]{"US", "840"}) {
            mvc.perform(get("/api/v1/users/countries")
                            .param("query", "").param("limit", "1").param("includeId", selected))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result[*].isoCode").value(containsInAnyOrder("AF", "US")));
        }
        mvc.perform(get("/api/v1/users/countries")
                        .param("query", "fGhAn").param("limit", "10").param("includeId", "AF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(1)))
                .andExpect(jsonPath("$.result[0].id").value(4))
                .andExpect(jsonPath("$.result[0].name").value("Afghanistan"));
    }

    @Test
    public void emptyCountryQueryWithoutSelectionReturnsTheRequestedPage() throws Exception {
        mvc.perform(get("/api/v1/users/countries").param("query", "").param("limit", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result", hasSize(5)));
    }

    @Test
    public void wildcardCharactersMatchLiterally() throws Exception {
        for (String query : new String[]{"%", "_", "\\", "%_\\"}) {
            mvc.perform(get("/api/v1/users/countries").param("query", query).param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result[*].isoCode").value(containsInAnyOrder("XX")));
        }
    }

    @Test
    public void bothDictionariesEnforceTheSamePageBounds() throws Exception {
        for (String path : new String[]{"/api/v1/users/countries", "/api/v1/users/provinces"}) {
            for (String limit : new String[]{"-1", "251"}) {
                mvc.perform(get(path).param("countryId", "US").param("query", "").param("limit", limit))
                        .andExpect(status().isBadRequest());
            }
            mvc.perform(get(path).param("countryId", "US").param("query", "").param("limit", "0"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.result", hasSize(0)));
            mvc.perform(get(path).param("countryId", "US").param("query", "").param("limit", "250"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    public void provincesSupportNumericAndIsoKeysAndRespectCountryScope() throws Exception {
        for (String country : new String[]{"US", "840"}) {
            mvc.perform(get("/api/v1/users/provinces")
                            .param("countryId", country).param("query", "liFor").param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result[*].isoCode").value(containsInAnyOrder("US-CA")));
        }
        for (String selected : new String[]{"US-CA", "5"}) {
            mvc.perform(get("/api/v1/users/provinces")
                            .param("countryId", "US").param("query", "Alaska").param("limit", "1")
                            .param("includeId", selected))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result[*].isoCode").value(containsInAnyOrder("US-AK", "US-CA")));
        }
        mvc.perform(get("/api/v1/users/provinces")
                        .param("countryId", "PT").param("query", "").param("limit", "10").param("includeId", "US-CA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(0)));
    }
}
