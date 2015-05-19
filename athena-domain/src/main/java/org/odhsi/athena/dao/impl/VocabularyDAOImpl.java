package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.VocabularyDAO;
import org.odhsi.athena.db_stored.SfGetCurrentStatus;
import org.odhsi.athena.entity.Vocabulary;
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
 * Created by GMalikov on 26.03.2015.
 */

@Repository("vocabularyDAO")
public class VocabularyDAOImpl implements VocabularyDAO, InitializingBean{

    private DataSource dataSource;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private JdbcTemplate jdbcTemplate;

    private SfGetCurrentStatus sfGetCurrentStatus;

    @Override
    public Vocabulary getVocabularyById(String id) {
        String sql = "SELECT * FROM DEV_TIMUR.VOCABULARY WHERE id = :vocabularyId";
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
            vocabulary.setName(resultSet.getString("VOCABULARY_ID"));
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

    @Resource(name = "dataSource")
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        sfGetCurrentStatus = new SfGetCurrentStatus(dataSource);
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
}
