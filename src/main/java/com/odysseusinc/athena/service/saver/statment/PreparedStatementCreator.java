package com.odysseusinc.athena.service.saver.statment;

import com.odysseusinc.athena.util.CDMVersion;
import org.apache.commons.lang.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO dev pls write here JDocs
public class PreparedStatementCreator extends SqlStatementCreator {

    private static final String QUESTION_MARK = "?";
    private static final String COMMA = ",";


    @Override
    public <T> QueryAndParams getQuery(String query, Params paramsTtt) {
        int countParam = StringUtils.countMatches(query, QUESTION_MARK);
        int countVocabs = paramsTtt.getIds().size();

        query = query.replace(
                QUESTION_MARK,
                String.join(COMMA, Collections.nCopies(countVocabs, QUESTION_MARK))
        );

        List<Object> params = Stream.generate(paramsTtt::getIds)
                .limit(countParam)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return new QueryAndParams(query, params);
    }

    @Override
    public void setParams(CDMVersion currentVersion, PreparedStatement st, List<Object> params) throws SQLException {
        for (int j = 0; j < params.size(); j++) {
            if (CDMVersion.V4_5 == currentVersion) {
                st.setLong(j + 1, (Long) params.get(j));
            } else {
                st.setString(j + 1, (String) params.get(j));
            }
        }
    }


}