package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.ConceptDAO;
import org.odhsi.athena.entity.Concept;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GMalikov on 25.03.2015.
 */
public class ConceptDAOImpl implements ConceptDAO, InitializingBean{

    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (dataSource == null){
            throw new BeanCreationException("Must set dataSource on DomainDAO");
        }
        if (jdbcTemplate == null){
            throw new BeanCreationException("Must set jdbcTemplate on DomainDAO");
        }
    }

    @Override
    public List<Concept> getPagingConceptsForBrowser(int start, int length, String searchValue, String sortOrder, String vocabularyId, String domainId) {
//        String sql = "SELECT * FROM DEV_TIMUR.CONCEPT " +
//                "WHERE VOCABULARY_ID = 'CPT4' AND DOMAIN_ID = 'Measurement' " +
//                "ORDER BY CONCEPT_ID OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY";

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
