package com.odysseusinc.athena.service.saver.statment;

import com.odysseusinc.athena.util.CDMVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;


public abstract class SqlStatementCreator {

    public static final int FETCH_SIZE = 500;

    public PreparedStatement getStatement(Connection conn, String query, Placeholders placeholders, CDMVersion currentVersion) throws SQLException {
        conn.setAutoCommit(false);
        QueryAndParams queryAndParams = getQuery(query, placeholders);
        PreparedStatement st = conn.prepareStatement(queryAndParams.query);
        setParams(currentVersion, st, queryAndParams.params, conn);
        st.setFetchSize(FETCH_SIZE);
        return st;
    }

    protected void setParams(CDMVersion currentVersion, PreparedStatement st, List<QueryAndParams.Param> params, Connection conn) throws SQLException {
        for (int j = 0; j < params.size(); j++) {
            st.setObject(j + 1, params.get(j).getValue());
        }
    }

    protected abstract <T> QueryAndParams getQuery(String query, Placeholders placeholders);

    @AllArgsConstructor
    @Getter
    public static class QueryAndParams {
        public final String query;
        public final List<Param> params;

        public static QueryAndParams of(String query, List<Object> params) {
            return new QueryAndParams(
                    query,params.stream().map(v -> new Param(v, TYPE.UNDEFINED)).collect(Collectors.toList())
            );
        }

        @AllArgsConstructor
        @Getter
        public static class Param {
            private Object value;
            private TYPE type;
        }
    }

    public enum TYPE {
        IN_VALUES,
        VARCHAR_ARRAY,
        VARCHAR,
        UNDEFINED
    }
}