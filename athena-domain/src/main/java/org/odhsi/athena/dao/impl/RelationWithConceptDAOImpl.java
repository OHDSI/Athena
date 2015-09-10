package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.RelationWithConceptDAO;
import org.odhsi.athena.entity.RelationWithConcept;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GMalikov on 21.07.2015.
 */
public class RelationWithConceptDAOImpl extends BaseDAOImpl<RelationWithConceptDAOImpl> implements RelationWithConceptDAO, InitializingBean {

    @Override
    public List<RelationWithConcept> getRelationsForBrowserPagingResult(int start, int length, String sortOrder, String searchValue, Long conceptId) {
        StringBuilder sqlString = new StringBuilder();
        sqlString.append("SELECT CONCEPT_RELATIONSHIP.RELATIONSHIP_ID, CONCEPT.CONCEPT_NAME ");
        sqlString.append("FROM DEV_TIMUR.CONCEPT_RELATIONSHIP ");
        sqlString.append("INNER JOIN DEV_TIMUR.CONCEPT ");
        sqlString.append("ON DEV_TIMUR.CONCEPT_RELATIONSHIP.CONCEPT_ID_2=DEV_TIMUR.CONCEPT.CONCEPT_ID ");
        sqlString.append("WHERE (CONCEPT_ID_1 = :conceptId OR CONCEPT_ID_2 = :conceptId) ");
        if(!StringUtils.isEmpty(searchValue)){
            sqlString.append(" AND (RELATIONSHIP_ID LIKE '%");
            sqlString.append(searchValue);
            sqlString.append("%' OR CONCEPT_NAME LIKE '%");
            sqlString.append(searchValue);
            sqlString.append("%') ");
        }
        sqlString.append(" ORDER BY CONCEPT_RELATIONSHIP.RELATIONSHIP_ID ");
        sqlString.append(sortOrder.toUpperCase());
        sqlString.append(" OFFSET :offsetValue ROWS FETCH NEXT :nextValue ROWS ONLY");
        Map<String,Object> params = new HashMap<>();
        params.put("offsetValue", start);
        params.put("nextValue", length);
        params.put("conceptId", conceptId);
        return namedParameterJdbcTemplate.query(sqlString.toString(), params, new RelationWithConceptMapper());
    }

    private static final class RelationWithConceptMapper implements RowMapper<RelationWithConcept>{
        @Override
        public RelationWithConcept mapRow(ResultSet rs, int rowNum) throws SQLException {
            RelationWithConcept relationWithConcept = new RelationWithConcept();
            relationWithConcept.setId(rs.getString("RELATIONSHIP_ID"));
            relationWithConcept.setConceptName(rs.getString("CONCEPT_NAME"));
            return relationWithConcept;
        }
    }

    @Override
    public int getTotalRelationsWithConcepts() {
        String sql = "SELECT COUNT(RELATIONSHIP_ID) FROM DEV_TIMUR.CONCEPT_RELATIONSHIP";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Override
    public int getFilteredRelationsForBrowser(String searchValue, Long conceptId) {
        StringBuilder sqlString = new StringBuilder();
        sqlString.append("SELECT COUNT(*) ");
        sqlString.append("FROM DEV_TIMUR.CONCEPT_RELATIONSHIP ");
        sqlString.append("INNER JOIN DEV_TIMUR.CONCEPT ");
        sqlString.append("ON DEV_TIMUR.CONCEPT_RELATIONSHIP.CONCEPT_ID_2=DEV_TIMUR.CONCEPT.CONCEPT_ID ");
        sqlString.append("WHERE (CONCEPT_ID_1 = :conceptId OR CONCEPT_ID_2 = :conceptId) ");
        if(!StringUtils.isEmpty(searchValue)){
            sqlString.append("AND (RELATIONSHIP_ID LIKE '%");
            sqlString.append(conceptId);
            sqlString.append("%' OR CONCEPT_NAME LIKE '%");
            sqlString.append(conceptId);
            sqlString.append("%')");
        }
        sqlString.append("ORDER BY CONCEPT_RELATIONSHIP.RELATIONSHIP_ID DESC");
        Map<String, Object> params = new HashMap<>();
        params.put("conceptId", conceptId);

        return namedParameterJdbcTemplate.queryForObject(sqlString.toString(), params, Integer.class);
    }
}
