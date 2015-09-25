package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.SecActionDAO;
import org.odhsi.athena.entity.SecAction;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by GMalikov on 21.09.2015.
 */
public class SecActionDAOImpl extends BaseDAOImpl<SecActionDAOImpl> implements SecActionDAO{

    @Override
    public SecAction getById(Long id) {
        String sqlString = "SELECT * FROM SEC_ACTION WHERE id = :actionId";
        Map<String,Object> params = new HashMap<>();
        params.put("actionId", id);
        return namedParameterJdbcTemplate.queryForObject(sqlString,params,new ActionMapper());
    }

    private static final class ActionMapper implements RowMapper<SecAction>{

        @Override
        public SecAction mapRow(ResultSet rs, int rowNum) throws SQLException {
            SecAction action = new SecAction();
            action.setId(rs.getLong("ID"));
            action.setName(rs.getString("NAME"));
            return action;
        }
    }
}
