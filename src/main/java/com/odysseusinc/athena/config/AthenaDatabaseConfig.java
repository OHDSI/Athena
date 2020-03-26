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

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(basePackages = "com.odysseusinc.athena.repositories.athena",
        entityManagerFactoryRef = "athenaEntityManagerFactory",
        transactionManagerRef = "athenaTransactionManager")
@EnableConfigurationProperties(DataSourceProperties.class)
public class AthenaDatabaseConfig {

    @Autowired
    DataSourceProperties properties;

    @Bean(name = "athenaTransactionManager")
    @Primary
    PlatformTransactionManager athenaTransactionManager(@Qualifier("athenaEntityManagerFactory")
                                                                EntityManagerFactory entityManagerFactory) {

        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean(name = "athenaEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean athenaEntityManagerFactory(@Qualifier("athenaDataSource")
                                                                              DataSource dataSource) {

        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(false);

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(dataSource);
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPackagesToScan("com.odysseusinc.athena.model.athena",
                "com.odysseusinc.athena.model.common",
                "com.odysseusinc.athena.model.security");
        return factoryBean;
    }

    @Bean(name = "athenaJdbcTemplate")
    public JdbcTemplate athenaJdbcTemplate(@Qualifier("athenaDataSource") DataSource dataSource) {

        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "athenaDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    DataSource athenaDataSource() {

        return DataSourceBuilder.create().build();
    }
}