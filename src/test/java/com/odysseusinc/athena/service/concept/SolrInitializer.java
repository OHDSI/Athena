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

import java.io.BufferedReader;
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

    /** Read-only inputs: nothing is written back to either. See {@link #initSolr()}. */
    private static final String TEST_SOLR_RESOURCES = "src/test/resources/testdata/solr";

    private static final String MAIN_SOLR_CONF_RESOURCES = "src/main/resources/solr";


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

    /**
     * This used to stage the configuration inside {@code src/test/resources} — it
     * copied {@code src/main/resources/solr} over the committed test tree and then rewrote
     * {@code solrconfig.xml} in place — before copying the result to a temp directory to
     * actually run from. So every test run mutated the working tree, and only converged back
     * to the committed bytes by coincidence.
     * <p>
     * The staging now happens in the temp directory instead. The two source trees are read
     * only, and the layering is unchanged: the committed test tree supplies {@code solr.xml},
     * {@code core.properties} and the analysis resources ({@code lang/}, {@code stopwords.txt},
     * {@code synonyms.txt}, {@code protwords.txt}), then the main configuration is copied over
     * the top of it, then {@code /dataimport} is stripped.
     */
    private void initSolr() throws Exception {

        Path tempSolrRoot = Files.newTemporaryFolder().toPath();
        FileUtils.copyDirectory(Paths.get(TEST_SOLR_RESOURCES).toFile(), tempSolrRoot.toFile());
        FileUtils.copyDirectory(new File(MAIN_SOLR_CONF_RESOURCES),
                tempSolrRoot.resolve("concepts").resolve("conf").toFile());

        this.removeUnnecessarySolrConfigurations(tempSolrRoot);
        this.runEmbeddedSolr(tempSolrRoot);
    }

    private void runEmbeddedSolr(Path tempSolrRoot) throws Exception {

        NodeConfig cfg = SolrXmlConfig.fromSolrHome(tempSolrRoot, new Properties());
        server = new EmbeddedSolrServer(cfg, "concepts");
        reindexTestConcepts();
    }

    private void removeUnnecessarySolrConfigurations(Path tempSolrRoot) throws ParserConfigurationException, SAXException, IOException, TransformerException {

        final File solrConfig = tempSolrRoot.resolve("concepts").resolve("conf")
                .resolve("solrconfig.xml").toFile();

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(solrConfig);
        final Node config = doc.getElementsByTagName("config").item(0);
        this.removeElementByAttributeValue(config, "name", "/dataimport");

        TransformerFactory.newInstance().newTransformer()
                .transform(
                        new DOMSource(doc),
                        new StreamResult(solrConfig));
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

    private void reindexTestConcepts() throws Exception {

        server.deleteByQuery("*:*");
        server.add(getSolrDocsFromResource());
        server.commit();
    }

    private List<SolrInputDocument> getSolrDocsFromResource() throws Exception {

        // The fixture is plain ';'-delimited with no quoting or escaping, so splitting is
        // equivalent to a CSV parse here and avoids a dependency purely for tests. The
        // vendored opencsv it used to rely on cannot run on Java 9+.
        try (BufferedReader reader = new BufferedReader(
                new FileReader(SolrConceptPhraseSearchTest.class.getResource(CONCEPTS_CSV).getPath()))) {
            return reader.lines()
                    .filter(line -> !line.trim().isEmpty())
                    .map(line -> line.split(";", -1))
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
