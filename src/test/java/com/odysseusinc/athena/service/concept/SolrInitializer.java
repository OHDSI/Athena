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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.NodeConfig;
import org.apache.solr.core.SolrXmlConfig;
import org.assertj.core.util.Files;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Solr have to be initialized only ONCE for all unit tests!!
 *
 * This initializer creates an instance of the EmbeddedSolrServer and populates it with data. The data from `concepts-from-import-query.csv`.
 * This CSV file represents the result from the Solr import query.
 */
public class SolrInitializer extends ExternalResource {

    private static final String CONCEPTS_CSV = "/testdata/concepts-from-import-query.csv";

    private static final String TEST_SOLR_RESOURCES = "src/test/resources/testdata/solr";

    private static final String MAIN_SOLR_CONF_RESOURCES = "src/main/resources/solr";
    private static final String TEST_SOLR_CONF_RESOURCES = TEST_SOLR_RESOURCES  + "/concepts/conf";


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

        this.copySolrConfigurationToTestResources();
        this.removeUnnecessarySolrConfigurations();
        this.runEmbeddedSolr();
    }

    private void runEmbeddedSolr() throws Exception {

        Path tempSolrRoot = Files.newTemporaryFolder().toPath();
        Path baseConfigs = Paths.get(TEST_SOLR_RESOURCES);
        FileUtils.copyDirectory(baseConfigs.toFile(), tempSolrRoot.toFile());
        NodeConfig cfg = SolrXmlConfig.fromSolrHome(tempSolrRoot, new Properties());
        server = new EmbeddedSolrServer(cfg, "concepts");
        reindexTestConcepts();
    }

    private void removeUnnecessarySolrConfigurations() throws ParserConfigurationException, SAXException, IOException, TransformerException {

        final String solrConfig = TEST_SOLR_CONF_RESOURCES + "/solrconfig.xml";

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(solrConfig));
        final Node config = doc.getElementsByTagName("config").item(0);
        this.removeElementByAttributeValue(config, "name", "/dataimport");

        TransformerFactory.newInstance().newTransformer()
                .transform(
                        new DOMSource(doc),
                        new StreamResult(new File(solrConfig)));
    }

    private void removeElementByAttributeValue(Node config, String attribute, String attributeValue) {

        final NodeList configNodes = config.getChildNodes();
        IntStream.range(0, configNodes.getLength()).mapToObj(configNodes::item)
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .filter(element -> StringUtils.equals(element.getAttribute(attribute), attributeValue))
                .findAny()
                .ifPresent(config::removeChild);
    }

    private void copySolrConfigurationToTestResources() throws IOException {
        FileUtils.copyDirectory(new File(MAIN_SOLR_CONF_RESOURCES), new File(TEST_SOLR_CONF_RESOURCES));
    }

    private void reindexTestConcepts() throws Exception {

        server.deleteByQuery("*:*");
        server.add(getSolrDocsFromResource());
        server.commit();
    }

    private List<SolrInputDocument> getSolrDocsFromResource() throws Exception {

        try (CSVReader csvReader = new CSVReader(new FileReader(SolrConceptPhraseSearchTest.class.getResource(CONCEPTS_CSV).getPath()), ';')) {
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
