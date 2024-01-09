
package com.odysseusinc.athena.service.saver.statment;

import com.odysseusinc.athena.util.CDMVersion;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * NamedStatementCreator is a utility class designed to implement named SQL statements,
 * providing a convenient layer over the standard PreparedStatement functionality.
 * In reality, it acts as a wrapper that produces a standard PreparedStatement.
 * It offers basic functionality and is limited to working only with placeholders found in the Params class.
 * While an alternative, NamedParameterJdbcTemplate, could be employed, adopting it would necessitate
 * a rewrite of the saver code and CSV save-to-file functionality. This, in turn, would require additional testing
 * and debugging efforts to address any resulting issues.
 */
public class NamedStatementCreator extends SqlStatementCreator {

    @Override
    public SqlStatementCreator.QueryAndParams getQuery(String query, Placeholders placeholders) {
        List<Placeholders.PLACEHOLDER_TYPE> types = findPlaceholdersAndThereOrder(query, Arrays.asList(Placeholders.PLACEHOLDER_TYPE.values()));
        return createQueryAndParamsForStatement(types, placeholders, query);
    }

    private List<Placeholders.PLACEHOLDER_TYPE> findPlaceholdersAndThereOrder(String sql, List<Placeholders.PLACEHOLDER_TYPE> params) {
        Map<Integer, Placeholders.PLACEHOLDER_TYPE> foundPlaceholders = new TreeMap<>();
        for (Placeholders.PLACEHOLDER_TYPE param : params) {
            String placeholder = param.getPlaceholder();
            int index = sql.indexOf(placeholder);
            while (index != -1) {
                foundPlaceholders.put(index, param);
                index = sql.indexOf(placeholder, index + 1);
            }
        }
        return new ArrayList<>(foundPlaceholders.values());
    }

    @Override
    protected void setParams(CDMVersion currentVersion, PreparedStatement st, List<QueryAndParams.Param> params, Connection conn) throws SQLException {
        for (int j = 0; j < params.size(); j++) {
            QueryAndParams.Param param = params.get(j);
            if (param.getType() == TYPE.VARCHAR_ARRAY) {
                Array sqlArray = conn.createArrayOf("VARCHAR", (Object[]) param.getValue());
                st.setArray(j + 1, sqlArray );
            } else {
                st.setObject(j + 1, param.getValue());
            }
        }
    }

    private QueryAndParams createQueryAndParamsForStatement(List<Placeholders.PLACEHOLDER_TYPE> types, Placeholders placeholders, String query) {
        List<QueryAndParams.Param> values = new ArrayList<>();
        for (Placeholders.PLACEHOLDER_TYPE type : types) {
            String placeholderValue = type.getPlaceholder();
            if (type.getType() == TYPE.IN_VALUES) {
                List<Object> listValue = placeholders.getList(type);
                query = query.replaceFirst(
                        placeholderValue,
                        String.join(",", Collections.nCopies(listValue.size(), "?"))
                );
                values.addAll(listValue.stream().map(v -> new QueryAndParams.Param(v, type.getType())).collect(Collectors.toList()));
            } else {
                query = query.replaceFirst(placeholderValue, "?");
                values.add(new QueryAndParams.Param(placeholders.getValue(type), type.getType()));
            }
        }
        return new QueryAndParams(query, values);
    }
}