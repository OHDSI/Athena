package com.odysseusinc.athena.service.saver;

import com.odysseusinc.athena.util.CDMVersion;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//todo dev pls write here JDocs
public class SqlStatementCreator {

    private static final String QUESTION_MARK = "?";
    private static final String COMMA = ",";

    public <T> PreparedStatement getStatement(Connection conn, String query, List<T> ids, CDMVersion currentVersion) throws SQLException {
        conn.setAutoCommit(false);

        QueryAndParams queryAndParams = getQuery(query, ids);
        PreparedStatement st = conn.prepareStatement(queryAndParams.query);
        st.setFetchSize(500);
        setParams(currentVersion, st, queryAndParams.params);
        return st;
    }

    public <T> QueryAndParams getQuery(String query, List<T> ids) {
        int countParam = StringUtils.countMatches(query, QUESTION_MARK);
        int countVocabs = ids.size();

        query = query.replace(
                QUESTION_MARK,
                String.join(COMMA, Collections.nCopies(countVocabs, QUESTION_MARK))
        );

        List<Object> params = Stream.generate(() -> ids)
                .limit(countParam)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return new QueryAndParams(query, params);
    }

    public void setParams(CDMVersion currentVersion, PreparedStatement st, List<Object> params) throws SQLException {
        for (int j = 0; j < params.size(); j++) {
            if (CDMVersion.V4_5 == currentVersion) {
                st.setLong(j + 1, (Long) params.get(j));
            } else {
                st.setString(j + 1, (String) params.get(j));
            }
        }
    }

    @AllArgsConstructor
    private static class QueryAndParams {
        public final String query;
        public final List<Object> params;
    }
}