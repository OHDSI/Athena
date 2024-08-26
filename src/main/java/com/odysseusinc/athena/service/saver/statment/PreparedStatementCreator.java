package com.odysseusinc.athena.service.saver.statment;

import com.odysseusinc.athena.util.CDMVersion;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class  provides functionality for preparing SQL statements with placeholders using vocabulary-based parameters.
 * It is designed to work ONLY with vocabularies  as placeholders in the SQL query.
 */
public class PreparedStatementCreator extends SqlStatementCreator {

    private static final String QUESTION_MARK = "?";
    private static final String COMMA = ",";

    @Override
    public QueryAndParams getQuery(String query, Placeholders placeholders) {
        int countParam = StringUtils.countMatches(query, QUESTION_MARK);
        int countVocabs = placeholders.getIds().size();

        query = query.replace(
                QUESTION_MARK,
                String.join(COMMA, Collections.nCopies(countVocabs, QUESTION_MARK))
        );

        List<Object> params = Stream.generate(placeholders::getIds)
                .limit(countParam)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return QueryAndParams.of(query, params);
    }

    @Override
    public void setParams(CDMVersion currentVersion, PreparedStatement st, List<QueryAndParams.Param> params, Connection conn) throws SQLException {
        for (int j = 0; j < params.size(); j++) {
            Object value = params.get(j).getValue();
            if (CDMVersion.V4_5 == currentVersion) {
                st.setLong(j + 1, (Long) value);
            } else {
                st.setString(j + 1, (String) value);
            }
        }
    }
}