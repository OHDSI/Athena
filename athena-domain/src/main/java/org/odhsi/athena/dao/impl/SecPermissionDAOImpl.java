package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.SecPermissionDAO;
import org.odhsi.athena.entity.SecAction;
import org.odhsi.athena.entity.SecDomain;
import org.odhsi.athena.entity.SecPermission;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GMalikov on 21.09.2015.
 */
public class SecPermissionDAOImpl extends BaseDAOImpl<SecPermissionDAOImpl> implements SecPermissionDAO{

    @Override
    public SecPermission getById(Long id) {
        String sqlString = "SELECT p.id, p.user_id, p.instance_id, " +
                "a.id as ACTION_ID, a.name as ACTION_NAME, " +
                "d.id as DOMAIN_ID, d.name as DOMAIN_NAME " +
                "FROM SEC_PERMISSION p " +
                "LEFT JOIN SEC_ACTION a on p.action_id = a.ID " +
                "LEFT JOIN SEC_DOMAIN d on p.domain_id = d.ID " +
                "WHERE p.id = :permissionId";
        Map<String, Object> params = new HashMap<>();
        params.put("permissionId", id);
        return namedParameterJdbcTemplate.query(sqlString, params, new SecPermissionExtractor());
    }

    private static final class SecPermissionExtractor implements ResultSetExtractor<SecPermission>{

        @Override
        public SecPermission extractData(ResultSet rs) throws SQLException, DataAccessException {
            SecPermission permission = null;
            while(rs.next()){
                if (permission == null) {
                    permission = new SecPermission();
                    permission.setId(rs.getLong("ID"));
                    permission.setInstanceId(rs.getString("INSTANCE_ID"));
                }
                Long action_id = rs.getLong("ACTION_ID");
                if(action_id > 0){
                    SecAction action = new SecAction();
                    action.setId(rs.getLong("ACTION_ID"));
                    action.setName(rs.getString("ACTION_NAME"));
                    permission.setAction(action);
                }
                Long domain_id = rs.getLong("DOMAIN_ID");
                if (domain_id > 0){
                    SecDomain domain = new SecDomain();
                    domain.setId(rs.getLong("DOMAIN_ID"));
                    domain.setName(rs.getString("DOMAIN_NAME"));
                }
            }
            return permission;
        }
    }

    @Override
    public List<SecPermission> getUserPermissions(Long userId) {
        String sqlString = "SELECT p.id, p.user_id, p.instance_id, " +
                "a.id as ACTION_ID, a.name as ACTION_NAME, " +
                "d.id as DOMAIN_ID, d.name as DOMAIN_NAME " +
                "FROM SEC_PERMISSION p " +
                "LEFT JOIN SEC_ACTION a on p.action_id = a.ID " +
                "LEFT JOIN SEC_DOMAIN d on p.domain_id = d.ID " +
                "WHERE p.USER_ID = :userId";
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return namedParameterJdbcTemplate.query(sqlString, params, new SecPermissionListExtractor());
    }

    private static final class SecPermissionListExtractor implements ResultSetExtractor<List<SecPermission>>{

        @Override
        public List<SecPermission> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, SecPermission> map = new HashMap<>();
            SecPermission permission = null;
            while(rs.next()){
                Long id = rs.getLong("ID");
                permission = map.get(id);
                if (permission == null){
                    permission = new SecPermission();
                    permission.setId(id);
                    permission.setInstanceId(rs.getString("INSTANCE_ID"));
                    map.put(id, permission);
                }
                Long action_id = rs.getLong("ACTION_ID");
                if(action_id > 0){
                    SecAction action = new SecAction();
                    action.setId(action_id);
                    action.setName(rs.getString("ACTION_NAME"));
                    permission.setAction(action);
                }
                Long domain_id = rs.getLong("DOMAIN_ID");
                if(domain_id > 0){
                    SecDomain domain = new SecDomain();
                    domain.setId(domain_id);
                    domain.setName(rs.getString("DOMAIN_NAME"));
                }
            }

            return new ArrayList<SecPermission>(map.values());
        }
    }
}
