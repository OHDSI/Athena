package org.odhsi.athena.dao.impl;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Created by GMalikov on 10.09.2015.
 */
public class BaseDAOImpl<T> implements InitializingBean{

    protected DataSource dataSource;

    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected JdbcTemplate jdbcTemplate;

    private Class<T> type;

    @Resource(name = "dataSource")
    public void setDataSource(DataSource dataSource){
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(dataSource == null){
            throw new BeanCreationException("dataSource on " + type + " is not set.");
        }
        if (namedParameterJdbcTemplate == null){
            throw new BeanCreationException("namedParameterJdbcTemplate on" + type + " is not set.");
        }
        if (jdbcTemplate == null){
            throw new BeanCreationException("jdbcTemplate on " + type + " is not set.");
        }
    }
}
