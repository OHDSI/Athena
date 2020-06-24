package com.odysseusinc.athena.service.concept;

import static com.odysseusinc.athena.service.concept.SolrTestUtils.createConceptSearchDTO;

import com.odysseusinc.athena.api.v1.controller.converter.ConceptSearchDTOToSolrQuery;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.service.impl.ConceptSearchPhraseToSolrQueryService;
import com.odysseusinc.athena.service.impl.ConceptSearchQueryPartCreator;
import com.odysseusinc.athena.service.impl.QueryDebugUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;

public abstract class SolrConceptSearchAbstractTest {

    // This super handy flag to debug queries. In case of TRUE value, directory is created for each unit test. The query and concept score-explain-info for are placed there.
    // You can compare score-explain-info by diff tools and understand why you get such a result
    public static final boolean DEBUG_INFO_ENABLED = false;
    public static final String DEBUG_INFO_FOR_SEARCH_DIR_BASE = "search-query-debug-info/";

    @ClassRule
    public static final TestRule serviceInitializer = SolrInitializer.INSTANCE;
    @Rule
    public TestName testName = new TestName();


    private ConceptSearchDTOToSolrQuery conceptSearchDTOToSolrQuery;

    @Before
    public void setUp() {

        ConceptSearchQueryPartCreator conceptSearchQueryPartCreator = new ConceptSearchQueryPartCreator();
        ConceptSearchPhraseToSolrQueryService conceptSearchPhraseToSolrQueryService = new ConceptSearchPhraseToSolrQueryService(conceptSearchQueryPartCreator);

        conceptSearchDTOToSolrQuery = new ConceptSearchDTOToSolrQuery(
                conceptSearchPhraseToSolrQueryService,
                null,
                null,
                conceptSearchQueryPartCreator);
    }

    protected SolrDocumentList executeQuery(String queryString) throws SolrServerException, IOException {

        ConceptSearchDTO conceptSearchDTO = createConceptSearchDTO(queryString);
        SolrQuery query = conceptSearchDTOToSolrQuery.createQuery(conceptSearchDTO, Collections.emptyList());

        QueryResponse queryResponse = debug(testName.getMethodName(), query,
                () -> SolrInitializer.server.query(query));
        return queryResponse.getResults();
    }


    private QueryResponse debug(String dirName, SolrQuery query, ProducerWithException<QueryResponse> requestFunction) throws SolrServerException, IOException {
        if (!DEBUG_INFO_ENABLED) {
            return requestFunction.produce();
        }

        QueryDebugUtils.addDebugAndScore(query);
        QueryResponse response = requestFunction.produce();

        String dir = String.format("%s/%s/", DEBUG_INFO_FOR_SEARCH_DIR_BASE, dirName);
        FileUtils.deleteDirectory(new File(dir));

        File fileWithQuery = new File(String.format("%s%s", dir,  "query"));
        FileUtils.writeStringToFile(fileWithQuery, QueryDebugUtils.getQuery(query.getQuery()), Charset.defaultCharset());

        for (Map.Entry<String, String> stringStringEntry : response.getExplainMap().entrySet()) {
            String k = stringStringEntry.getKey();
            String v = stringStringEntry.getValue();
            File file = new File(dir + StringUtils.substringBetween(v, "\n", "=") + "_" + k);
            FileUtils.writeStringToFile(file, v, Charset.defaultCharset());
        }
        return response;
    }

    public interface ProducerWithException<T> {
        T produce() throws SolrServerException, IOException;
    }

}
