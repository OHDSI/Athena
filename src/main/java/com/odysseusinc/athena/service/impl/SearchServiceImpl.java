/*
 *
 * Copyright 2020 Odysseus Data Services, inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Alexandr Cumarav
 * Created: March 24, 2020
 *
 */

package com.odysseusinc.athena.service.impl;

import static java.util.stream.Collectors.toList;
import static org.apache.solr.common.params.CursorMarkParams.CURSOR_MARK_PARAM;
import static org.apache.solr.common.params.CursorMarkParams.CURSOR_MARK_START;

import com.odysseusinc.athena.api.v1.controller.converter.ConceptSearchDTOToSolrQuery;
import com.odysseusinc.athena.api.v1.controller.converter.ConceptSearchResultToDTO;
import com.odysseusinc.athena.api.v1.controller.converter.SolrDocumentToConceptDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchResultDTO;
import com.odysseusinc.athena.service.SearchService;
import com.odysseusinc.athena.service.SolrService;
import com.odysseusinc.athena.service.VocabularyConversionService;
import com.odysseusinc.athena.service.impl.solr.SearchResult;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.util.extractor.ConceptFieldsExtractor;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    private final Character separator;
    private final ConceptSearchDTOToSolrQuery converterToSolrQuery;
    private final ConceptSearchResultToDTO converter;
    private final FileHelper fileHelper;
    private final SolrService solrService;
    private final VocabularyConversionService conversionService;

    public SearchServiceImpl(SolrService solrService, ConceptSearchDTOToSolrQuery converterToSolrQuery, ConceptSearchResultToDTO converter, VocabularyConversionService conversionService, FileHelper fileHelper, @Value("${csv.separator:;}") Character separator) {

        this.solrService = solrService;
        this.converterToSolrQuery = converterToSolrQuery;
        this.converter = converter;
        this.conversionService = conversionService;
        this.fileHelper = fileHelper;
        this.separator = separator;
    }

    @Override
    public ConceptSearchResultDTO search(ConceptSearchDTO searchDTO, boolean debug) throws IOException, SolrServerException {

        List<String> v5Ids = conversionService.getUnavailableVocabularies();
        SolrQuery solrQuery = converterToSolrQuery.createQuery(searchDTO, v5Ids);

        if (debug) {
            solrQuery.setParam("fl", "*,score");
            solrQuery.set("debugQuery", "on");

            QueryResponse solrResponse = solrService.search(solrQuery);
            List<SolrDocument> solrDocumentList = solrResponse.getResults();

            return converter.convert(
                    new SearchResult<>(solrQuery, solrResponse, solrDocumentList), v5Ids,
                    QueryDebugUtils.getDebug(solrResponse.getExplainMap().toString()),
                    QueryDebugUtils.getQuery(solrQuery.toString())
            );
        }

        QueryResponse solrResponse = solrService.search(solrQuery);
        List<SolrDocument> solrDocumentList = solrResponse.getResults();
        return converter.convert(new SearchResult<>(solrQuery, solrResponse, solrDocumentList), v5Ids, null, null);
    }

    @Override
    public void generateCSV(ConceptSearchDTO searchDTO, OutputStream osw) throws IOException, SolrServerException {

        SolrQuery solrQuery = converterToSolrQuery.convertForCursor(searchDTO);
        String cursorMark = CURSOR_MARK_START;
        boolean done = false;
        boolean first = true;

        String name = fileHelper.getTempPath(UUID.randomUUID().toString());
        File temp = new File(name);
        while (!done) {

            try (CSVWriter csvWriter = new AthenaCSVWriter(name, separator)) {
                if (first) {
                    csvWriter.writeNext(new String[]{"Id", "Code", "Name", "Concept Class Id", "Domain", "Vocabulary",
                            "Invalid Reason", "Standard Concept"}, false);
                    first = false;
                }
                solrQuery.set(CURSOR_MARK_PARAM, cursorMark);
                QueryResponse solrResponse = solrService.search(solrQuery);
                List<SolrDocument> solrDocuments = solrResponse.getResults();
                List<ConceptDTO> concepts = solrDocuments.stream()
                        .map(SolrDocumentToConceptDTO::convert)
                        .collect(toList());
                writeAll(csvWriter, concepts);
                String nextCursorMark = solrResponse.getNextCursorMark();
                if (cursorMark.equals(nextCursorMark)) {
                    done = true;
                }
                cursorMark = nextCursorMark;
                csvWriter.flush(true);
            } finally {
                Files.copy(temp.toPath(), osw);
                Files.delete(temp.toPath());
            }
        }
    }

    private void writeAll(CSVWriter csvWriter, List<ConceptDTO> concepts) throws IOException {

        ConceptFieldsExtractor extractor = new ConceptFieldsExtractor();
        csvWriter.writeAll(new ArrayList<>(extractor.extractForAll(concepts)));
    }
}
