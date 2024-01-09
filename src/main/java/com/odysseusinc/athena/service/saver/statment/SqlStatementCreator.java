package com.odysseusinc.athena.service.saver.statment;

import com.odysseusinc.athena.util.CDMVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


public abstract class SqlStatementCreator {

    public static final int FETCH_SIZE = 500;

    public PreparedStatement getStatement(Connection conn, String query, Params params, CDMVersion currentVersion) throws SQLException {
        conn.setAutoCommit(false);
        QueryAndParams queryAndParams = getQuery(query, params);
        PreparedStatement st = conn.prepareStatement(queryAndParams.query);
        setParams(currentVersion, st, queryAndParams.params);
        st.setFetchSize(FETCH_SIZE);
        return st;
    }

    protected void setParams(CDMVersion currentVersion, PreparedStatement st, List<Object> params) throws SQLException {
        for (int j = 0; j < params.size(); j++) {
            st.setObject(j + 1, params.get(j));
        }
    }

    protected abstract <T> QueryAndParams getQuery(String query, Params paramsTtt);

    @AllArgsConstructor
    @Getter
    public static class QueryAndParams {
        public final String query;
        public final List<Object> params;
    }

}