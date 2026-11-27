package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.api.v1.controller.converter.ConceptSearchDTOToSolrQuery;
import com.odysseusinc.athena.api.v1.controller.converter.ConceptSearchResultToDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.service.SolrService;
import com.odysseusinc.athena.service.VocabularyConversionService;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code debug=true} on the public concept search is honoured for administrators only.
 */
class SearchDebugAccessTest {

    private SolrService solrService;
    private SearchServiceImpl searchService;
    private ConceptSearchDTO search;

    @BeforeEach
    void setUp() throws Exception {

        solrService = mock(SolrService.class);
        ConceptSearchDTOToSolrQuery toSolrQuery = mock(ConceptSearchDTOToSolrQuery.class);
        VocabularyConversionService conversionService = mock(VocabularyConversionService.class);

        search = new ConceptSearchDTO();
        when(conversionService.getUnavailableVocabularies()).thenReturn(Collections.emptyList());
        when(toSolrQuery.createQuery(any(ConceptSearchDTO.class), anyList())).thenAnswer(i -> new SolrQuery("*:*"));

        QueryResponse response = mock(QueryResponse.class);
        when(response.getResults()).thenReturn(new SolrDocumentList());
        when(response.getExplainMap()).thenReturn(Collections.emptyMap());
        when(solrService.search(any(SolrQuery.class))).thenReturn(response);

        searchService = new SearchServiceImpl(solrService, toSolrQuery, mock(ConceptSearchResultToDTO.class),
                conversionService, null, ';');
    }

    @AfterEach
    void clearContext() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void anonymousDebugRequestIsIgnored() throws Exception {

        searchService.search(search, true);

        assertNull(sentQuery().get("debugQuery"));
    }

    @Test
    void nonAdminDebugRequestIsIgnored() throws Exception {

        authenticateAs("ROLE_USER");

        searchService.search(search, true);

        assertNull(sentQuery().get("debugQuery"));
    }

    @Test
    void adminDebugRequestIsHonoured() throws Exception {

        authenticateAs("ROLE_ADMIN");

        searchService.search(search, true);

        assertEquals("on", sentQuery().get("debugQuery"));
    }

    private void authenticateAs(String role) {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", null, role));
    }

    private SolrQuery sentQuery() throws Exception {

        ArgumentCaptor<SolrQuery> captor = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrService).search(captor.capture());
        return captor.getValue();
    }
}
