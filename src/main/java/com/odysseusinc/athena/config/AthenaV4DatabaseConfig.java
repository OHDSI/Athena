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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(basePackages = "com.odysseusinc.athena.repositories.v4",
        entityManagerFactoryRef = "athenav4EntityManagerFactory",
        transactionManagerRef = "athenav4TransactionManager")
@ConfigurationProperties("spring.datasource-v4")
public class AthenaV4DatabaseConfig extends HikariConfig {

    public final static String ATHENA_V4_PERSISTENCE_UNIT_NAME = "ATHENA_V4";

    public AthenaV4DatabaseConfig(@Qualifier("athenaV4HikariProps") HikariProps hikariProps) {

        hikariProps.setToConfig(this);
    }

    @Bean
    public HikariDataSource dataSourceAthenaV4() {

        return new HikariDataSource(this);
    }

    @Bean(name = "athenav4EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean athenav4EntityManagerFactory(final HikariDataSource dataSourceAthenaV4) {

        return new LocalContainerEntityManagerFactoryBean() {{
            setDataSource(dataSourceAthenaV4);
            setPersistenceProviderClass(HibernatePersistenceProvider.class);
            setPersistenceUnitName(ATHENA_V4_PERSISTENCE_UNIT_NAME);
            setPackagesToScan("com.odysseusinc.athena.model.athenav4", "com.odysseusinc.athena.model.common");
            setJpaProperties(HikariProps.JPA_PROPERTIES);
        }};
    }

    @Bean(name = "athenav4TransactionManager")
    public PlatformTransactionManager athenav4TransactionManager(EntityManagerFactory athenav4EntityManagerFactory) {

        return new JpaTransactionManager(athenav4EntityManagerFactory);
    }
}
