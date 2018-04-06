/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package com.odysseusinc.athena.config;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CDMFlywayConfig {

    @Value("#{'${flyway.v5.locations}'.split(',')}")
    private String[] locationsV5;

    @Value("#{'${flyway.v4.locations}'.split(',')}")
    private String[] locationsV4;

    @Autowired
    @Qualifier("athenaV5DataSource")
    private DataSource v5DataSource;

    @Autowired
    @Qualifier("athenaV4DataSource")
    private DataSource v4DataSource;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    @FlywayDataSource
    public DataSource dataSource() {

        return DataSourceBuilder.create().build();
    }

    @PostConstruct
    public void migrateFlyway() {

        Flyway flywayCDM = new Flyway();
        flywayCDM.setDataSource(v5DataSource);
        flywayCDM.setLocations(locationsV5);
        flywayCDM.migrate();

        Flyway flywayAthenaV4 = new Flyway();
        flywayAthenaV4.setDataSource(v4DataSource);
        flywayAthenaV4.setLocations(locationsV4);
        flywayAthenaV4.migrate();

    }
}
