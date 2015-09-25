package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.SecRoleDAO;
import org.odhsi.athena.entity.SecAction;
import org.odhsi.athena.entity.SecRole;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by GMalikov on 21.09.2015.
 */
public class SecRoleDAOImpl extends BaseDAOImpl<SecRoleDAOImpl> implements SecRoleDAO{

    @Override
    public SecRole getById(Long id) {
        String sqlString = "SELECT * FROM SEC_ROLE WHERE id = :roleId";
        Map<String,Object> params = new HashMap<>();
        params.put("roleId", id);
        return namedParameterJdbcTemplate.queryForObject(sqlString, params, new RoleMapper());
    }

    private static final class RoleMapper implements RowMapper<SecRole>{

        @Override
        public SecRole mapRow(ResultSet rs, int rowNum) throws SQLException {
            SecRole role = new SecRole();
            role.setId(rs.getLong("ID"));
            role.setName(rs.getString("NAME"));
            role.setActions(new HashSet<SecAction>());
            return role;
        }
    }

    @Override
    public SecRole getRoleWithActionsById(Long id) {
        String sqlString = "SELECT r.id as ROLE_ID, r.name as ROLE_NAME, a.ID as ACTION_ID, a.NAME as ACTION_NAME "+
                "FROM SEC_ROLE r " +
                "LEFT JOIN SEC_ROLE_ACTION ar on r.id = ar.ROLE_ID " +
                "LEFT JOIN SEC_ACTION a on ar.ACTION_ID = a.id " +
                "WHERE r.id = :roleId";
        Map<String, Object> params = new HashMap<>();
        params.put("roleId", id);
        return namedParameterJdbcTemplate.query(sqlString, params, new RolesWithActionsExtractor());
    }

    private static final class RolesWithActionsExtractor implements ResultSetExtractor<SecRole>{

        @Override
        public SecRole extractData(ResultSet rs) throws SQLException, DataAccessException {
            SecRole role = null;
            while (rs.next()){
                if (role == null){
                    role = new SecRole();
                    role.setId(rs.getLong("ROLE_ID"));
                    role.setName(rs.getString("ROLE_NAME"));
                }
                Long actionId = rs.getLong("ACTION_ID");
                if(actionId > 0){
                    SecAction action = new SecAction();
                    action.setId(actionId);
                    action.setName(rs.getString("ACTION_NAME"));
                    role.getActions().add(action);
                }
            }
            return role;
        }
    }

    @Override
    public List<SecRole> getUserRoles(Long userId) {
        String sqlString = "SELECT r.ID, r.NAME FROM SEC_ROLE r " +
                "LEFT JOIN SEC_USER_ROLE sur on r.ID = sur.ROLE_ID " +
                "LEFT JOIN SEC_USER u on sur.USER_ID = u.ID "+
                "WHERE u.ID = :userId ";
        Map<String, Object> params = new HashMap<>();
        return namedParameterJdbcTemplate.query(sqlString, params, new RoleMapper());
    }
}
