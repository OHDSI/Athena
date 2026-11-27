package com.odysseusinc.athena.api.v1.controller.converter;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.exceptions.ValidationException;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConceptSearchSortingTest {

    private final ConceptSearchDTOToSolrQuery converter =
            new ConceptSearchDTOToSolrQuery(null, null, null, null);

    @Test
    void mapsPublicSortNamesToSolrSchemaFields() {

        ConceptSearchDTO source = sortedBy("relevance", "DESC");
        SolrQuery query = new SolrQuery();

        converter.setSorting(source, query);

        assertEquals("score desc", query.getSortField());
    }

    @Test
    void mapsLegacyUiFieldNames() {

        ConceptSearchDTO source = sortedBy("domainId", "asc");
        SolrQuery query = new SolrQuery();

        converter.setSorting(source, query);

        assertEquals("domain_id asc", query.getSortField());
    }

    @Test
    void rejectsUnknownFieldsBeforeCallingSolr() {

        assertThrows(ValidationException.class,
                () -> converter.setSorting(sortedBy("not_a_field", "asc"), new SolrQuery()));
    }

    @Test
    void rejectsUnknownDirectionsBeforeCallingSolr() {

        assertThrows(ValidationException.class,
                () -> converter.setSorting(sortedBy("name", "sideways"), new SolrQuery()));
    }

    private ConceptSearchDTO sortedBy(String field, String order) {

        ConceptSearchDTO source = new ConceptSearchDTO();
        source.setSort(field);
        source.setOrder(order);
        return source;
    }
}
