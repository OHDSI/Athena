package com.odysseusinc.athena.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryDebugUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryDebugUtils.class);

    public static String getDebug(String debug) {

        if (StringUtils.isEmpty(debug)) {
            return StringUtils.EMPTY;
        }
        return Arrays.stream(debug.split("\\r?\\n"))
                .filter(str -> str.endsWith("=") || str.contains("sum of") || str.contains("weight") || str.contains("boost"))
                .collect(Collectors.joining("\n"));
    }

    public static String getQuery(String queryString) {

        if (StringUtils.isEmpty(queryString)) {
            return StringUtils.EMPTY;
        }

        String query = decode(queryString);
        query = StringUtils.substringBetween(query, "=", "&");
        try {
            String newLines = query
                    .replace("OR", "OR\n")
                    .replace("AND", "AND\n")
                    .replace("(", "\n(\n")
                    .replace(")", "\n)\n");
            int level = 0;
            StringBuffer builder = new StringBuffer();
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
                return builder.toString();
            }
        } catch (Exception e) {
            LOGGER.info("Cannot decode solr query string{}", queryString, e);
        }
        return query;

    }

    private static String decode(String value) {

        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            LOGGER.info("Cannot decode solr query string{}", value, e);
        }
        return value;
    }

}
