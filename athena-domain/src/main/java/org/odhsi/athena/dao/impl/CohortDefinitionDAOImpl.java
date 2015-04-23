package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.CohortDefinitionDAO;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Created by GMalikov on 30.03.2015.
 */
public class CohortDefinitionDAOImpl implements CohortDefinitionDAO, InitializingBean{

    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (dataSource == null){
            throw new BeanCreationException("Must set dataSource on CohortDefinitionDAO");
        }
        if (jdbcTemplate == null){
            throw new BeanCreationException("Must set jdbcTemplate on CohortDefinitionDAO");
        }
    }
}
