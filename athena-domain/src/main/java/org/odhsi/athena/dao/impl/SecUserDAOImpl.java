package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.SecUserDAO;
import org.odhsi.athena.entity.SecAction;
import org.odhsi.athena.entity.SecRole;
import org.odhsi.athena.entity.SecUser;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by GMalikov on 18.09.2015.
 */
public class SecUserDAOImpl extends BaseDAOImpl<SecUserDAOImpl> implements SecUserDAO {

    @Override
    public SecUser getById(Long id) {
        String sqlString = "SELECT * FROM SEC_USER WHERE id = :userId";
        Map<String, Object> params = new HashMap<>();
        params.put("userId", id);
        return namedParameterJdbcTemplate.queryForObject(sqlString, params, new UserMapper());
    }

    private static final class UserMapper implements RowMapper<SecUser> {

        @Override
        public SecUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            SecUser user = new SecUser();
            user.setId(rs.getLong("ID"));
            user.setUserName(rs.getString("USERNAME"));
            user.setPassword(rs.getString("PASSWORD"));
            user.setSalt(rs.getString("SALT"));
            user.setName(rs.getString("NAME"));
            user.setRoles(new HashSet<SecRole>());
            return user;
        }
    }

    @Override
    public SecUser getUserWithRolesById(Long id) {
        String sqlString = "SELECT u.id AS user_id, u.username, u.password, u.salt, u.name, r.ID AS role_id, r.name AS role_name "
                + "FROM SEC_USER u "
                + "LEFT JOIN SEC_USER_ROLE ur ON u.id = ur.USER_ID "
                + "LEFT JOIN SEC_ROLE r ON ur.ROLE_ID = r.ID "
                + "WHERE u.ID = :userId ";
        Map<String, Object> params = new HashMap<>();
        params.put("userId", id);
        return namedParameterJdbcTemplate.query(sqlString, params, new UserWithRolesExtractor());
    }

    private static final class UserWithRolesExtractor implements ResultSetExtractor<SecUser> {

        @Override
        public SecUser extractData(ResultSet rs) throws SQLException, DataAccessException {
            SecUser user = null;
            while (rs.next()) {
                if (user == null) {
                    user = new SecUser();
                    user.setId(rs.getLong("USER_ID"));
                    user.setUserName(rs.getString("USERNAME"));
                    user.setPassword(rs.getString("PASSWORD"));
                    user.setSalt(rs.getString("SALT"));
                    user.setName(rs.getString("NAME"));
                    user.setRoles(new HashSet<SecRole>());
                }
                Long roleId = rs.getLong("ROLE_ID");
                if (roleId > 0) {
                    SecRole role = new SecRole();
                    role.setId(roleId);
                    role.setName(rs.getString("ROLE_NAME"));
                    role.setActions(new HashSet<SecAction>());
                    user.getRoles().add(role);
                }
            }
            return user;
        }
    }

    @Override
    public SecUser getByUsername(String username) {
        String sqlString = "SELECT * FROM SEC_USER WHERE id = ':username'";
        Map<String,Object> params = new HashMap<>();
        params.put("username", username);
        return namedParameterJdbcTemplate.queryForObject(sqlString, params, new UserMapper());
    }
}
