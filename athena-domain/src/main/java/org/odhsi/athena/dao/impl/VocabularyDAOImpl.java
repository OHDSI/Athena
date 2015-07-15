package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.VocabularyDAO;
import org.odhsi.athena.db_stored.SfGetCurrentStatus;
import org.odhsi.athena.entity.Vocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by GMalikov on 26.03.2015.
 */

@Repository("vocabularyDAO")
public class VocabularyDAOImpl implements VocabularyDAO, InitializingBean{

    private DataSource dataSource;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private JdbcTemplate jdbcTemplate;

    private SfGetCurrentStatus sfGetCurrentStatus;

    private SimpleJdbcCall procBuildVocabulary;


    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyDAOImpl.class);

    @Override
    public Vocabulary getVocabularyById(String id) {
        String sql = "SELECT * FROM DEV_TIMUR.VOCABULARY WHERE VOCABULARY_ID = :vocabularyId";
        Map<String,Object> params = new HashMap<>();
        params.put("vocabularyId", id);
        return namedParameterJdbcTemplate.queryForObject(sql,params,new VocabularyMapper());
    }

    @Override
    public List<Vocabulary> getAllVocabularies() {
        String sql = "SELECT * FROM DEV_TIMUR.VOCABULARY";
        return jdbcTemplate.query(sql, new VocabularyMapper());
    }

    private static final class VocabularyMapper implements RowMapper<Vocabulary>{
        @Override
        public Vocabulary mapRow(ResultSet resultSet, int i) throws SQLException {
            Vocabulary vocabulary = new Vocabulary();
            vocabulary.setId(resultSet.getString("VOCABULARY_ID"));
            vocabulary.setName(resultSet.getString("VOCABULARY_NAME"));
            vocabulary.setConcept(null);
            vocabulary.setConceptSet(null);
            vocabulary.setReference(resultSet.getString("VOCABULARY_REFERENCE"));
            vocabulary.setVersion(resultSet.getString("VOCABULARY_VERSION"));
            return vocabulary;
        }
    }

    @Override
    public String getVocabularyStatus(String id) {
        List<String> result = sfGetCurrentStatus.execute(id);
        return result.get(0);
    }

    @Override
    public Date getLatestUpdateFromConversion(String id) {
        String sql = "SELECT LATEST_UPDATE FROM DEV_TIMUR.VOCABULARY_CONVERSION WHERE VOCABULARY_ID_V5 = :vocabularyId";
        Map<String, Object> params = new HashMap<>();
        params.put("vocabularyId", id);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Date.class);
    }

    @Override
    public void buildVocabulary(String id, String version, Date latestUpdate) {
        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("p_VocabularyID", id)
                .addValue("p_LatestUpdate", latestUpdate)
                .addValue("p_vocabularyVersion", version);
        procBuildVocabulary.execute(in);
    }

    @Resource(name = "dataSource")
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        sfGetCurrentStatus = new SfGetCurrentStatus(dataSource);
        this.procBuildVocabulary = new SimpleJdbcCall(dataSource)
                .withSchemaName("DEV_TIMUR")
                .withCatalogName("PKG_VOCABULARY")
                .withProcedureName("BuildVocabulary");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (dataSource == null){
            throw new BeanCreationException("Must set dataSource on VocabularyDAO");
        }
        if (namedParameterJdbcTemplate == null){
            throw new BeanCreationException("Must set namedParameterJdbcTemplate on VocabularyDAO");
        }
    }

    @Override
    public long getRecordsTotalForVocabulary(String vocabularyId) {
        String sql = "SELECT COUNT(CONCEPT_ID) FROM DEV_TIMUR.CONCEPT WHERE VOCABULARY_ID = :vocabularyId";
        Map<String,Object> params = new HashMap<>();
        params.put("vocabularyId", vocabularyId);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    @Override
    public long getDomainsCountForVocabulary(String vocabularyId) {
        String sql = "SELECT COUNT(DISTINCT DOMAIN_ID) " +
                "FROM DEV_TIMUR.CONCEPT WHERE VOCABULARY_ID = :vocabularyId";
        Map<String,Object> params = new HashMap<>();
        params.put("vocabularyId", vocabularyId);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    @Override
    public long getConceptsCountForVocabulary(String vocabularyId) {
        String sql = "SELECT COUNT(DISTINCT DOMAIN_ID) " +
                "FROM DEV_TIMUR.CONCEPT " +
                "WHERE VOCABULARY_ID = :vocabularyId and (STANDARD_CONCEPT = 'S' or STANDARD_CONCEPT is null) " +
                "and INVALID_REASON is null and VALID_END_DATE > :endDate ";
        Map<String,Object> params = new HashMap<>();
        params.put("vocabularyId", vocabularyId);
        params.put("endDate", new Date());
        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    @Override
    public long getRelationsCountForVocabulary(String vocabularyId) {
        String sql = "SELECT COUNT(*) " +
                "FROM DEV_TIMUR.CONCEPT " +
                "INNER JOIN DEV_TIMUR.CONCEPT_RELATIONSHIP " +
                "ON CONCEPT.CONCEPT_ID = CONCEPT_RELATIONSHIP.CONCEPT_ID_1 " +
                "WHERE CONCEPT.VOCABULARY_ID = :vocabularyId";
        Map<String,Object> params = new HashMap<>();
        params.put("vocabularyId", vocabularyId);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    @Override
    public List<Vocabulary> getVocabulariesForBrowserTable(Integer start, Integer length, String sortOrder, String searchVal) {
//        String sql = "SELECT * FROM DEV_TIMUR.VOCABULARY " + " ORDER BY VOCABULARY_ID " + sortOrder.toUpperCase() + " OFFSET :offsetValue ROWS FETCH NEXT :nextValue ROWS ONLY";
        StringBuilder sqlString = new StringBuilder();
        sqlString.append("SELECT * FROM DEV_TIMUR.VOCABULARY ");
        if(!StringUtils.isEmpty(searchVal)){
            sqlString.append(" WHERE VOCABULARY_ID LIKE '");
            sqlString.append(searchVal);
            sqlString.append("%' ");
            sqlString.append(" OR VOCABULARY_NAME LIKE '");
            sqlString.append(searchVal);
            sqlString.append("%' ");
        }
        sqlString.append(" ORDER BY VOCABULARY_ID ");
        sqlString.append(sortOrder.toUpperCase());
        sqlString.append(" OFFSET :offsetValue ROWS FETCH NEXT :nextValue ROWS ONLY");
        Map<String, Object> params = new HashMap<>();
        params.put("offsetValue", start);
        params.put("nextValue", length);
        return namedParameterJdbcTemplate.query(sqlString.toString(),params, new VocabularyMapper());
    }

    @Override
    public int getFilteredVocabulariesCountForBrowserTable(String searchVal) {
//        String sql = "SELECT COUNT(VOCABULARY_ID) FROM DEV_TIMUR.VOCABULARY";
        StringBuilder sqlString = new StringBuilder();
        sqlString.append("SELECT COUNT(VOCABULARY_ID) FROM DEV_TIMUR.VOCABULARY ");
        if (!StringUtils.isEmpty(searchVal)){
            sqlString.append(" WHERE VOCABULARY_ID LIKE '");
            sqlString.append(searchVal);
            sqlString.append("%' ");
            sqlString.append(" OR VOCABULARY_NAME LIKE '");
            sqlString.append(searchVal);
            sqlString.append("%' ");
        }
        return jdbcTemplate.queryForObject(sqlString.toString(), Integer.class);
    }

    @Override
    public int getTotalVocabulariesCountForBrowserTable() {
        String sql = "SELECT COUNT(VOCABULARY_ID) FROM DEV_TIMUR.VOCABULARY";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
