package org.odhsi.athena.dao.impl;

import org.odhsi.athena.dao.DomainWithConceptCountDAO;
import org.odhsi.athena.entity.DomainWithConceptCount;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GMalikov on 17.07.2015.
 */
public class DomainWithConceptCountDAOImpl implements DomainWithConceptCountDAO, InitializingBean {

    private DataSource dataSource;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    @Override
    public void afterPropertiesSet() throws Exception {
        if (dataSource == null){
            throw new BeanCreationException("Must set dataSource on DomainWithConceptCountDAO");
        }
        if (namedParameterJdbcTemplate == null){
            throw new BeanCreationException("Must set namedParameterJdbcTemplate on DomainWithConceptCountDAO");
        }
    }

    @Resource(name = "dataSource")
    public void setDataSource(DataSource dataSource){
        this.dataSource = dataSource;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public List<DomainWithConceptCount> getDomainsWithConceptCountForVocabulary(String vocabularyId) {
        String sql = "SELECT DOMAIN_ID, COUNT(CONCEPT_ID) AS CONCEPT_COUNT FROM DEV_TIMUR.CONCEPT" +
                " WHERE VOCABULARY_ID = :vocabularyId GROUP BY DOMAIN_ID ORDER BY DOMAIN_ID";
        Map<String, Object> params = new HashMap<>();
        params.put("vocabularyId", vocabularyId);
        return namedParameterJdbcTemplate.query(sql, params, new DomainWithConceptCountMapper());
    }

    private static final class DomainWithConceptCountMapper implements RowMapper<DomainWithConceptCount>{

        @Override
        public DomainWithConceptCount mapRow(ResultSet rs, int rowNum) throws SQLException {
            DomainWithConceptCount domainWithConceptCount = new DomainWithConceptCount();
            domainWithConceptCount.setId(rs.getString("DOMAIN_ID"));
            domainWithConceptCount.setConceptCount(rs.getLong("CONCEPT_COUNT"));
            return domainWithConceptCount;
        }
    }
}
