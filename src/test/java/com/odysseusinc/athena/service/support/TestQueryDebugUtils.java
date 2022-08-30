package com.odysseusinc.athena.service.support;

import com.odysseusinc.athena.service.impl.QueryDebugUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

public class TestQueryDebugUtils {

    public static final String DEBUG_INFO_FOR_SEARCH_DIR_BASE = "search-query-debug-info/";

    public static QueryResponse debug(String dirName, SolrQuery query, ProducerWithException<QueryResponse> function) throws Exception {
        QueryDebugUtils.addDebugAndScore(query);
        QueryResponse response = function.produce();
        System.out.println(QueryDebugUtils.getQuery(query.getQuery()));
        String dir = String.format("%s/%s/",DEBUG_INFO_FOR_SEARCH_DIR_BASE, dirName);
        FileUtils.deleteDirectory(new File(dir));
        for (Map.Entry<String, Object> stringStringEntry : response.getExplainMap().entrySet()) {
            String k = stringStringEntry.getKey();
            String v = stringStringEntry.getValue().toString();
            File file = new File(dir + StringUtils.substringBetween(v, "\n", "=") + "_" + k);
            FileUtils.writeStringToFile(file, v, Charset.defaultCharset());
        }
        return response;
    }

    public interface ProducerWithException<T> {
        T produce() throws Exception;
    }


}
