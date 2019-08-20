/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Authors: Yaroslav Molodkov
 *
 */

package com.odysseusinc.athena.service.concept;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;

/**
 * Solr have to be initialized only ONCE for all unit tests!!
 *
 * This initializer creates an instance of the EmbeddedSolrServer and populates it with data. The data from `concepts-from-import-query.csv`.
 * This CSV file represents the result from the Solr import query.
 */
public class SolrInitializer extends ExternalResource {

    public static final String CONCEPTS_CSV = "/testdata/concepts-from-import-query.csv";
    public static final String TEST_SOLR_RESOURCES = "src/test/resources/testdata/solr";

    public static EmbeddedSolrServer server;

    public static final TestRule INSTANCE = new SolrInitializer();
    private AtomicBoolean started = new AtomicBoolean();

    @Override
    protected void before() throws Throwable {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        this.initSolr();
    }

    private void initSolr() throws Exception {

        CoreContainer container = new CoreContainer(TEST_SOLR_RESOURCES);
        container.load();
        server = new EmbeddedSolrServer(container, "concepts");
        reindexTestConcepts();
    }


    private void reindexTestConcepts() throws Exception {

        server.deleteByQuery("*:*");
        server.add(getSolrDocsFromResource());
        server.commit();
    }

    private List<SolrInputDocument> getSolrDocsFromResource() throws Exception {

        try (CSVReader csvReader = new CSVReader(new FileReader(SolrConceptPhraseSearchTest.class.getResource(CONCEPTS_CSV).getPath()))) {
            return csvReader.readAll().stream()
                    .map(strings -> {
                        SolrInputDocument doc = new SolrInputDocument();
                        doc.addField("concept_id", strings[0]);
                        doc.addField("concept_code", strings[1]);
                        doc.addField("concept_name", strings[2]);
                        doc.addField("concept_class_id", strings[3]);
                        doc.addField("domain_id", strings[4]);
                        doc.addField("vocabulary_id", strings[5]);
                        doc.addField("standard_concept", strings[6]);
                        doc.addField("invalid_reason", strings[7]);
                        doc.addField("domain_name", strings[8]);
                        doc.addField("vocabulary_name", strings[9]);
                        doc.addField("concept_synonym_n", strings[10]);
                        return doc;

                    })
                    .collect(Collectors.toList());
        }
    }

}
