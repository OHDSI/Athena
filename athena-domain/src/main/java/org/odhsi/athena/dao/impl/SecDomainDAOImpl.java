package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.SecDomainDAO;
import org.odhsi.athena.entity.SecDomain;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by GMalikov on 21.09.2015.
 */
public class SecDomainDAOImpl extends BaseDAOImpl<SecDomainDAOImpl> implements SecDomainDAO{

    @Override
    public SecDomain getById(Long id) {
        String sqlString = "SELECT * FROM SEC_DOMAIN WHERE id = :domainId";
        Map<String, Object> params = new HashMap<>();
        return namedParameterJdbcTemplate.queryForObject(sqlString, params, new DomainMapper());
    }

    private static final class DomainMapper implements RowMapper<SecDomain>{

        @Override
        public SecDomain mapRow(ResultSet rs, int rowNum) throws SQLException {
            SecDomain domain = new SecDomain();
            domain.setId(rs.getLong("ID"));
            domain.setName(rs.getString("NAME"));
            return domain;
        }
    }
}
