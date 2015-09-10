package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.SynonymWithLanguageDAO;
import org.odhsi.athena.entity.SynonymWithLanguage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GMalikov on 22.07.2015.
 */
public class SynonymWithLanguageDAOImpl extends BaseDAOImpl<SynonymWithLanguageDAOImpl> implements SynonymWithLanguageDAO, InitializingBean {

    @Override
    public List<SynonymWithLanguage> getConceptSynonymsForBrowser(int start, int length, String searchValue, String sortOrder, Long conceptId) {
        StringBuilder sqlString = new StringBuilder();
        sqlString.append("SELECT CONCEPT_SYNONYM.CONCEPT_SYNONYM_NAME, CONCEPT.CONCEPT_NAME ");
        sqlString.append("FROM DEV_TIMUR.CONCEPT_SYNONYM ");
        sqlString.append("INNER JOIN DEV_TIMUR.CONCEPT ");
        sqlString.append("ON CONCEPT_SYNONYM.LANGUAGE_CONCEPT_ID=CONCEPT.CONCEPT_ID ");
        sqlString.append("WHERE CONCEPT_SYNONYM.CONCEPT_ID = :conceptId ");
        if (!StringUtils.isEmpty(searchValue)){
            sqlString.append(" AND CONCEPT_SYNONYM_NAME LIKE'%");
            sqlString.append(searchValue);
            sqlString.append("%' ");
        }
        sqlString.append("ORDER BY CONCEPT_SYNONYM_NAME ");
        sqlString.append(sortOrder.toUpperCase());
        sqlString.append(" OFFSET :offsetValue ROWS FETCH NEXT :nextValue ROWS ONLY");
        Map<String,Object> params = new HashMap<>();
        params.put("conceptId", conceptId);
        params.put("offsetValue", start);
        params.put("nextValue", length);
        return namedParameterJdbcTemplate.query(sqlString.toString(), params, new SynonymWithLanguageMapper());
    }

    private static final class SynonymWithLanguageMapper implements RowMapper<SynonymWithLanguage>{
        @Override
        public SynonymWithLanguage mapRow(ResultSet rs, int rowNum) throws SQLException {
            SynonymWithLanguage synonymWithLanguage = new SynonymWithLanguage();
            synonymWithLanguage.setName(rs.getString("CONCEPT_SYNONYM_NAME"));
            synonymWithLanguage.setLanguage(rs.getString("CONCEPT_NAME"));
            return synonymWithLanguage;
        }
    }

    @Override
    public int getTotalSynonyms() {
        String sql = "SELECT COUNT(*) " +
                "FROM DEV_TIMUR.CONCEPT_SYNONYM " +
                "INNER JOIN DEV_TIMUR.CONCEPT " +
                "ON CONCEPT_SYNONYM.LANGUAGE_CONCEPT_ID=CONCEPT.CONCEPT_ID ";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Override
    public int getFilteredSynonymsForBrowser(String searchValue, Long conceptId) {
        StringBuilder sqlString = new StringBuilder();
        sqlString.append("SELECT COUNT(*) ");
        sqlString.append("FROM DEV_TIMUR.CONCEPT_SYNONYM ");
        sqlString.append("INNER JOIN DEV_TIMUR.CONCEPT ");
        sqlString.append("ON CONCEPT_SYNONYM.LANGUAGE_CONCEPT_ID=CONCEPT.CONCEPT_ID ");
        sqlString.append("WHERE CONCEPT_SYNONYM.CONCEPT_ID = :conceptId ");
        if(!StringUtils.isEmpty(searchValue)){
            sqlString.append(" AND CONCEPT_SYNONYM.CONCEPT_SYNONYM_NAME LIKE'%");
            sqlString.append(searchValue);
            sqlString.append("%' ");
        }
        Map<String,Object> params = new HashMap<>();
        params.put("conceptId", conceptId);

        return namedParameterJdbcTemplate.queryForObject(sqlString.toString(), params, Integer.class);
    }
}
