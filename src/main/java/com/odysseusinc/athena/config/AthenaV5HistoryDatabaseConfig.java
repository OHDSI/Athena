/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;


@Configuration
@EnableJpaRepositories(basePackages = "com.odysseusinc.athena.repositories.v5history",
        entityManagerFactoryRef = "athenaV5HistoryEntityManagerFactory",
        transactionManagerRef = "athenaV5HistoryTransactionManager")
@ConfigurationProperties("spring.datasource-v5-history")
public class AthenaV5HistoryDatabaseConfig extends HikariConfig{

    public final static String V5_HISTORY_PERSISTENCE_UNIT_NAME = "ATHENA_V5_HISTORY_UNIT";

    @Bean
    public HikariDataSource dataSourceAthenaV5History() {

        return new HikariDataSource(this);
    }

    @Bean(name = "athenaV5HistoryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean athenaV5HistoryEntityManagerFactory(final HikariDataSource dataSourceAthenaV5History) {

        return new LocalContainerEntityManagerFactoryBean() {{
            setDataSource(dataSourceAthenaV5History);
            setPersistenceProviderClass(HibernatePersistenceProvider.class);
            setPersistenceUnitName(V5_HISTORY_PERSISTENCE_UNIT_NAME);
            setPackagesToScan("com.odysseusinc.athena.model.athenav5history");
            setJpaProperties(HikariProps.JPA_PROPERTIES);
        }};
    }

    @Bean(name = "athenaV5HistoryTransactionManager")
    public PlatformTransactionManager transactionManagerGQL(EntityManagerFactory athenaV5HistoryEntityManagerFactory) {

        return new JpaTransactionManager(athenaV5HistoryEntityManagerFactory);
     }
}