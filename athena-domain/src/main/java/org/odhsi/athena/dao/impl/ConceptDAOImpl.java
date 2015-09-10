package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.ConceptDAO;
import org.odhsi.athena.entity.Concept;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GMalikov on 25.03.2015.
 */
public class ConceptDAOImpl extends BaseDAOImpl<ConceptDAOImpl> implements ConceptDAO, InitializingBean{

    @Override
    public List<Concept> getPagingConceptsForBrowser(int start, int length, String searchValue, String sortOrder, String vocabularyId, String domainId) {
        StringBuilder sqlString = new StringBuilder();
        sqlString.append("SELECT * FROM DEV_TIMUR.CONCEPT WHERE VOCABULARY_ID = :vocabularyId AND DOMAIN_ID = :domainId");
        if(!StringUtils.isEmpty(searchValue)){
            sqlString.append(" AND CONCEPT_NAME LIKE'%");
            sqlString.append(searchValue);
            sqlString.append("%' ");
        }
        sqlString.append(" ORDER BY CONCEPT_NAME ");
        sqlString.append(sortOrder.toUpperCase());
        sqlString.append(" OFFSET :offsetValue ROWS FETCH NEXT :nextValue ROWS ONLY");
        Map<String, Object> params = new HashMap<>();
        params.put("offsetValue", start);
        params.put("nextValue", length);
        params.put("vocabularyId", vocabularyId);
        params.put("domainId", domainId);
        return namedParameterJdbcTemplate.query(sqlString.toString(), params, new ConceptMapperForBrowserTable());
    }

    private static final class ConceptMapperForBrowserTable implements RowMapper<Concept>{
        @Override
        public Concept mapRow(ResultSet rs, int rowNum) throws SQLException {
            Concept concept = new Concept();
            concept.setId(rs.getLong("CONCEPT_ID"));
            concept.setName(rs.getString("CONCEPT_NAME"));
            return concept;
        }
    }

    @Override
    public int getTotalConceptsForBrowser() {
        String sql = "SELECT COUNT(CONCEPT_ID) FROM DEV_TIMUR.CONCEPT";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Override
    public int getFilteredConceptsCountForBrowser(String vocabularyId, String domainId, String searchValue) {
        StringBuilder sqlString = new StringBuilder();
        sqlString.append("SELECT COUNT(CONCEPT_ID) FROM DEV_TIMUR.CONCEPT ");
        sqlString.append(" WHERE VOCABULARY_ID = :vocabularyId AND DOMAIN_ID = :domainId ");
        if(!StringUtils.isEmpty(searchValue)){
            sqlString.append(" AND CONCEPT_NAME LIKE'");
            sqlString.append(searchValue);
            sqlString.append("%' ");
        }
        sqlString.append(" ORDER BY CONCEPT_ID");
        Map<String, Object> params = new HashMap<>();
        params.put("vocabularyId", vocabularyId);
        params.put("domainId", domainId);

        return namedParameterJdbcTemplate.queryForObject(sqlString.toString(), params, Integer.class);
    }
}
