package com.odysseusinc.athena.service.saver.statment;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class NamedStatementCreatorTest {

    @Test
    public void getQuery_ShouldGenerateCorrectQueryAndParams() {
        // Given
        NamedStatementCreator creator = new NamedStatementCreator();
        String sql = "select * from where param1=:version and param2 in (:vocabularyIds) and param3=:versionDelta and paramA=:version";
        Params params = new Params(Arrays.asList("100", "200", "300"), 1, 2);

        // When
        SqlStatementCreator.QueryAndParams result = creator.getQuery(sql, params);

        // Then
        String expectedQuery = "select * from where param1=? and param2 in (?,?,?) and param3=? and paramA=?";
        assertEquals(expectedQuery, result.getQuery());
        assertEquals(Arrays.asList(1, "100", "200", "300", 2, 1), result.getParams());
    }

}