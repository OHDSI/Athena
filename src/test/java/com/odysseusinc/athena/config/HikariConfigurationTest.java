package com.odysseusinc.athena.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HikariConfigurationTest {

    @Test
    void v5AppliesNestedHikariProperties() {

        HikariProps properties = properties("AthenaV5-HikariPool", 7);
        AthenaV5DatabaseConfig configuration = new AthenaV5DatabaseConfig(properties);

        assertEquals("AthenaV5-HikariPool", configuration.getPoolName());
        assertEquals(7, configuration.getMaximumPoolSize());
    }

    @Test
    void historyAppliesItsOwnNestedHikariProperties() {

        HikariProps properties = properties("AthenaV5History-HikariPool", 4);
        AthenaV5HistoryDatabaseConfig configuration =
                new AthenaV5HistoryDatabaseConfig(properties);

        assertEquals("AthenaV5History-HikariPool", configuration.getPoolName());
        assertEquals(4, configuration.getMaximumPoolSize());
    }

    private HikariProps properties(String name, int maximumPoolSize) {

        HikariProps properties = new HikariProps();
        properties.setPoolName(name);
        properties.setMaximumPoolSize(maximumPoolSize);
        properties.setMinimumIdle(1);
        properties.setIdleTimeout(10_000);
        return properties;
    }
}
