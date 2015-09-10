package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.VocabularyBuildLogDAO;
import org.odhsi.athena.entity.VocabularyBuildLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GMalikov on 28.05.2015.
 */

@Repository("vocabularyBuildLogDAO")
public class VocabularyBuildLogDAOImpl extends BaseDAOImpl<VocabularyBuildLogDAOImpl> implements VocabularyBuildLogDAO, InitializingBean {


    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyBuildLogDAOImpl.class);

    private static final String VOCABULARY_ID = "vocabularyId";

    @Override
    public List<VocabularyBuildLog> getAllLogsForVocabulary(String vocabularyId) {
        String sql = "SELECT * FROM DEV_TIMUR.VOCABULARY_LOG WHERE VOCABULARY_ID = :vocabularyId ORDER BY LOG_ID DESC";
        Map<String, Object> params = new HashMap<>();
        params.put(VOCABULARY_ID, vocabularyId);
        return namedParameterJdbcTemplate.query(sql,params,new VocabularyBuildLogMapper());
    }

    @Override
    public List<VocabularyBuildLog> getErrorLogsForVocabulary(String vocabularyId) {
        String sql = "SELECT * FROM DEV_TIMUR.VOCABULARY_LOG WHERE VOCABULARY_ID = :vocabularyId AND OP_STATUS = 3 ORDER BY LOG_ID DESC";
        Map<String, Object> params = new HashMap<>();
        params.put(VOCABULARY_ID, vocabularyId);
        return namedParameterJdbcTemplate.query(sql,params,new VocabularyBuildLogMapper());
    }

    @Override
    public List<VocabularyBuildLog> getSuccessLogsForVocabulary(String vocabularyId) {
        String sql = "SELECT * FROM DEV_TIMUR.VOCABULARY_LOG WHERE VOCABULARY_ID = :vocabularyId AND (OP_STATUS = 1 OR OP_STATUS = 2) ORDER BY LOG_ID DESC";
        Map<String, Object> params = new HashMap<>();
        params.put(VOCABULARY_ID, vocabularyId);
        return namedParameterJdbcTemplate.query(sql,params,new VocabularyBuildLogMapper());
    }

    private static final class VocabularyBuildLogMapper implements RowMapper<VocabularyBuildLog> {
        @Override
        public VocabularyBuildLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            VocabularyBuildLog vocabularyBuildLog = new VocabularyBuildLog();
            vocabularyBuildLog.setId(rs.getLong("LOG_ID"));
            vocabularyBuildLog.setOpStart(rs.getTimestamp("OP_START"));
            vocabularyBuildLog.setVocabulary(null);
            vocabularyBuildLog.setOpNumber(rs.getLong("OP_NUMBER"));
            vocabularyBuildLog.setOpDescription(rs.getString("OP_DESCRIPTION"));
            vocabularyBuildLog.setOpEnd(rs.getTimestamp("OP_END"));
            vocabularyBuildLog.setOpStatus(rs.getLong("OP_STATUS"));
            vocabularyBuildLog.setOpDetail(rs.getString("OP_DETAIL"));
            return vocabularyBuildLog;
        }
    }

    @Override
    public String getVocabularyStatus(String vocabularyId) {
        String sql = "SELECT OP_STATUS FROM DEV_TIMUR.VOCABULARY_LOG " +
                "WHERE LOG_ID = (SELECT MAX(LOG_ID) FROM DEV_TIMUR.VOCABULARY_LOG WHERE VOCABULARY_ID = :vocabularyId)";
        Map<String, Object> params = new HashMap<>();
        params.put(VOCABULARY_ID, vocabularyId);
        try{
            return namedParameterJdbcTemplate.queryForObject(sql, params,String.class);
        } catch (EmptyResultDataAccessException ex){
            LOGGER.info("Status not found for " + vocabularyId, ex);
            return null;
        }
    }
}

