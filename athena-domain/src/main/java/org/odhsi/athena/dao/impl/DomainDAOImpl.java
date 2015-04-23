package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.DomainDAO;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Created by GMalikov on 23.04.2015.
 */
public class DomainDAOImpl implements DomainDAO, InitializingBean {

    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource){
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (dataSource == null){
            throw new BeanCreationException("Must set dataSource on DomainDAO");
        }
        if (jdbcTemplate == null){
            throw new BeanCreationException("Must set jdbcTemplate on DomainDAO");
        }
    }
}
