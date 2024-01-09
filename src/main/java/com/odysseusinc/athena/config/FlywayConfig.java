/*
 *
 * Copyright 2020 Odysseus Data Services, inc.
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
 * Authors: Alex Cumarav
 * Created: March 6, 2020
 *
 */

package com.odysseusinc.athena.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    private static final String FLYWAY_MIGRATIONS_HISTORY_TABLE = "schema_version";
    private final DataSource athenaDataSource;
    private final DataSource v5DataSource;
    private final DataSource v4DataSource;
    private final DataSource v5DataSourceHistory;
    @Value("#{'${athena-db.flyway.locations}'.split(',')}")
    private String[] locations;
    @Value("#{'${athena-v5.flyway.locations}'.split(',')}")
    private String[] locationsV5;
    @Value("#{'${athena-v4.flyway.locations}'.split(',')}")
    private String[] locationsV4;
    @Value("#{'${athena-v5-history.flyway.locations}'.split(',')}")
    private String[] locationsV5History;
    @Value("#{'${spring.datasource-v5-history.vocabulary.schema}'}")
    private String vocabularySchema;

    @Autowired
    public FlywayConfig(@Qualifier("dataSourceAthenaDB") DataSource athenaDataSource, @Qualifier("dataSourceAthenaV5") DataSource v5DataSource, @Qualifier("dataSourceAthenaV5History") DataSource v5DataSourceHistory,
                        @Qualifier("dataSourceAthenaV4") DataSource v4DataSource) {

        this.athenaDataSource = athenaDataSource;
        this.v5DataSource = v5DataSource;
        this.v4DataSource = v4DataSource;
        this.v5DataSourceHistory = v5DataSourceHistory;
    }


    @PostConstruct
    public void migrateFlyway() {

        Flyway.configure()
                .dataSource(athenaDataSource)
                .locations(locations)
                .table(FLYWAY_MIGRATIONS_HISTORY_TABLE)
                .load()
                .migrate();

        Flyway.configure()
                .dataSource(v5DataSource)
                .table(FLYWAY_MIGRATIONS_HISTORY_TABLE)
                .locations(locationsV5)
                .load()
                .migrate();

        Flyway.configure()
                .dataSource(v4DataSource)
                .locations(locationsV4)
                .table(FLYWAY_MIGRATIONS_HISTORY_TABLE)
                .load()
                .migrate();

        Flyway.configure()
                .dataSource(v5DataSourceHistory)
                .locations(locationsV5History)
                .table(FLYWAY_MIGRATIONS_HISTORY_TABLE)
                .load()
                .migrate();

        Flyway.configure()
                .dataSource(v5DataSourceHistory)
                .schemas(vocabularySchema)
                .locations(locationsV5)
                .table(FLYWAY_MIGRATIONS_HISTORY_TABLE)
                .load()
                .migrate();

    }
}