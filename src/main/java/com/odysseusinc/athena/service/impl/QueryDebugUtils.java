package com.odysseusinc.athena.service.impl;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryDebugUtils {

    private static final Logger log = LoggerFactory.getLogger(QueryDebugUtils.class);

    private QueryDebugUtils() {
    }


    public static void addDebugAndScore(SolrQuery solrQuery) {
        solrQuery.set("debugQuery", "on");
        solrQuery.setParam("fl", "*,score");
    }

    public static String getDebug(String debug) {

        if (isBlank(debug)) {
            return StringUtils.EMPTY;
        }
        return Arrays.stream(debug.split("\\r?\\n"))
                .filter(str -> str.endsWith("=") || str.contains("sum of") || str.contains("weight") || str.contains("boost"))
                .collect(Collectors.joining("\n"));
    }

    public static String getQuery(String queryString) {

        if (isBlank(queryString)) {
            return StringUtils.EMPTY;
        }

        try {
            String newLines = queryString
                    .replace("OR", "OR\n")
                    .replace("AND", "AND\n")
                    .replace("(", "\n(\n")
                    .replace(")", "\n)\n");
            int level = 0;
            StringBuilder builder = new StringBuilder();
            for (String line : newLines.split("\\r?\\n")) {
                if (StringUtils.isBlank(line)) {
                    continue;
                }

                if (line.contains(")")) {
                    level--;
                }
                for (int i = 0; i < level * 3; i++) {
                    builder.append(" ");
                }
                builder.append(line.trim());
                builder.append("\n");
                if (line.contains("(")) {
                    level++;
                }
            }
            return builder.toString();
        } catch (Exception e) {
            log.info("Cannot decode solr query string{}", queryString, e);
        }
        return queryString;

    }

    /**
     * This is a super handy method to deep debugging of the queries. If you pass solr_query method as lambda here, then you get score explanation info for each found concept.
     * <pre>{@code
    *        queryResponse = QueryDebugUtils.debug(
    *               DEBUG_INFO_FOR_SEARCH_DIR_BASE, searchName,
    *               query, () -> SolrInitializer.server.query(query)
    *       );
     * }</pre>
     *
     * This info is going to be in the DEBUG_INFO_FOR_SEARCH_DIR_BASE/searchName directory, and a separate file will be created for each concept and named as conceptID.
     * Then you can use DIFF to see a difference between found results.
     */
    public static QueryResponse debug(String baseDir, String conceptQueryDir, SolrQuery query, ProducerWithException<QueryResponse> requestFunction) throws SolrServerException, IOException {

        QueryDebugUtils.addDebugAndScore(query);
        QueryResponse response = requestFunction.produce();

        String dir = String.format("%s/%s/", baseDir, conceptQueryDir);
        FileUtils.deleteDirectory(new File(dir));

        File fileWithQuery = new File(String.format("%s%s", dir,  "query"));
        FileUtils.writeStringToFile(fileWithQuery, QueryDebugUtils.getQuery(query.getQuery()), Charset.defaultCharset());

        for (Map.Entry<String, String> stringStringEntry : response.getExplainMap().entrySet()) {
            String k = stringStringEntry.getKey();
            String v = stringStringEntry.getValue();
            File file = new File(dir + org.apache.commons.lang3.StringUtils.substringBetween(v, "\n", "=") + "_" + k);
            FileUtils.writeStringToFile(file, v, Charset.defaultCharset());
        }
        return response;
    }

    public interface ProducerWithException<T> {
        T produce() throws SolrServerException, IOException;
    }

}
