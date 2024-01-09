
package com.odysseusinc.athena.service.saver.statment;

import java.sql.JDBCType;
import java.util.*;

/**
 * NamedStatementCreator is a utility class designed to implement named SQL statements,
 * providing a convenient layer over the standard PreparedStatement functionality.
 * In reality, it acts as a wrapper that produces a standard PreparedStatement.
 * It offers basic functionality and is limited to working only with placeholders found in the Params class.
 *
 * While an alternative, NamedParameterJdbcTemplate, could be employed, adopting it would necessitate
 * a rewrite of the saver code and CSV save-to-file functionality. This, in turn, would require additional testing
 * and debugging efforts to address any resulting issues.
 */
public class NamedStatementCreator extends SqlStatementCreator {

    @Override
    public <T> SqlStatementCreator.QueryAndParams getQuery(String query, Params params) {
        List<Params.PARAM_TYPE> types = findPlaceholdersAndThereOrder(query, Arrays.asList(Params.PARAM_TYPE.values()));
        return createQueryAndParamsForStatement(types, params, query);
    }

    private List<Params.PARAM_TYPE> findPlaceholdersAndThereOrder(String sql, List<Params.PARAM_TYPE> params) {
        Map<Integer, Params.PARAM_TYPE> foundPlaceholders = new TreeMap<>();
        for (Params.PARAM_TYPE param : params) {
            String placeholder = param.getPlaceholder();
            int index = sql.indexOf(placeholder);
            while (index != -1) {
                foundPlaceholders.put(index, param);
                index = sql.indexOf(placeholder, index + 1);
            }
        }
        return new ArrayList<>(foundPlaceholders.values());
    }

    private QueryAndParams createQueryAndParamsForStatement(List<Params.PARAM_TYPE> types, Params params, String query) {
        List<Object> values = new ArrayList<>();
        for (Params.PARAM_TYPE type : types) {
            String placeholderValue = type.getPlaceholder();
            if (type.getType() == JDBCType.ARRAY) {
                List<Object> listValue = params.getList(type);
                query = query.replaceFirst(
                        placeholderValue,
                        String.join(",", Collections.nCopies(listValue.size(), "?"))
                );
                values.addAll(listValue);
            } else {
                query = query.replaceFirst(placeholderValue, "?");
                values.add(params.getValue(type));
            }
        }
        return new QueryAndParams(query, values);
    }
}