package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.VocabularyBuildLogDAO;
import org.odhsi.athena.entity.VocabularyBuildLog;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GMalikov on 28.05.2015.
 */

@Repository("vocabularyBuildLogDAO")
public class VocabularyBuildLogDAOImpl implements VocabularyBuildLogDAO, InitializingBean {
    private DataSource dataSource;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private JdbcTemplate jdbcTemplate;

    @Override
    public List<VocabularyBuildLog> getLogForVocabulary(String vocabularyId) {
        String sql = "SELECT * FROM DEV_TIMUR.VOCABULARY_LOG WHERE VOCABULARY_ID = :vocabularyId ORDER BY OP_START ASC";
        Map<String, Object> params = new HashMap<>();
        params.put("vocabularyId", vocabularyId);
        return namedParameterJdbcTemplate.query(sql,params,new VocabularyBuildLogMapper());
    }

    private static final class VocabularyBuildLogMapper implements RowMapper<VocabularyBuildLog> {
        @Override
        public VocabularyBuildLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            VocabularyBuildLog vocabularyBuildLog = new VocabularyBuildLog();
            vocabularyBuildLog.setId(rs.getLong("LOG_ID"));
            vocabularyBuildLog.setOpStart(rs.getDate("OP_START"));
            vocabularyBuildLog.setVocabulary(null);
            vocabularyBuildLog.setOpNumber(rs.getLong("OP_NUMBER"));
            vocabularyBuildLog.setOpDescription(rs.getString("OP_DESCRIPTION"));
            vocabularyBuildLog.setOpEnd(rs.getDate("OP_END"));
            vocabularyBuildLog.setOpStatus(rs.getLong("OP_STATUS"));
            vocabularyBuildLog.setOpDetail(rs.getString("OP_DETAIL"));
            return vocabularyBuildLog;
        }
    }

    @Resource(name = "dataSource")
    public void setDataSource(DataSource dataSource){
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(dataSource == null){
            throw new BeanCreationException("dataSource on VocabularyBuildLogDAO is not set.");
        }
        if (namedParameterJdbcTemplate == null){
            throw new BeanCreationException("namedParameterJdbcTemplate on VocabularyBuildLogDAO is not set.");
        }
        if (jdbcTemplate == null){
            throw new BeanCreationException("jdbcTemplate on VocabularyBuildLogDAO is not set.");
        }
    }
}

