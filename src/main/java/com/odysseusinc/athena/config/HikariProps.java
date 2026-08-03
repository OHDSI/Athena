package com.odysseusinc.athena.config;



import com.zaxxer.hikari.HikariConfig;

import java.util.Properties;

public class HikariProps {

    public static final Properties JPA_PROPERTIES = new Properties() {{
        // Hibernate 6 removed the version-specific dialects (PostgreSQL95Dialect and
        // friends); PostgreSQLDialect now derives the server version at runtime.
        put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        put("hibernate.hbm2ddl.auto", "none");
        put("hibernate.ddl-auto", "none");
        put("show-sql", "true");
        put("hibernate.temp.use_jdbc_metadata_defaults", "false");
    }};

    private Integer idleTimeout;
    private Integer maximumPoolSize;
    private Integer minimumIdle;
    private String poolName;

    public Integer getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public Integer getMinimumIdle() {
        return minimumIdle;
    }

    public void setMinimumIdle(Integer minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    protected void setToConfig(HikariConfig config) {
        config.setPoolName(this.getPoolName());
        config.setMinimumIdle(this.getMinimumIdle());
        config.setMaximumPoolSize(this.getMaximumPoolSize());
        config.setIdleTimeout(this.getIdleTimeout());
        config.setConnectionTestQuery("SELECT 1");
    }

}