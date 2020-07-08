package com.odysseusinc.athena.service.impl;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
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

}
