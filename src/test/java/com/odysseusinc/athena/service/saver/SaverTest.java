package com.odysseusinc.athena.service.saver;

import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.util.CDMVersion;
import com.opencsv.CSVWriter;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SaverTest {

    @Mock
    private DownloadBundle bundle;

    @Mock
    private CSVWriter csvWriter;

    @Mock
    private DataSource v5DataSource;

    @Mock
    private DataSource v4DataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private SaverDummy saver;

    @Before
    public void setUp() throws Exception {

        when(v4DataSource.getConnection()).thenReturn(connection);
        when(v5DataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    public void shouldPutAllVocabularyIdsToOnePlaceForV5() throws Exception {
        when(bundle.getCdmVersion()).thenReturn(CDMVersion.V5);
        List<String> ids = asList("1", "2", "3");
        saver.setQuery("SELECT * FROM concept WHERE id IN (?)");

        saver.writeContent(bundle, csvWriter, ids);

        verify(connection).prepareStatement(eq("SELECT * FROM concept WHERE id IN (?,?,?)"));
        verifySetStringValues(preparedStatement, asList("1", "2", "3"));
    }

    @Test
    public void shouldPutAllVocabularyIdsToSeveralPlaceForV5() throws Exception {
        when(bundle.getCdmVersion()).thenReturn(CDMVersion.V5);
        List<String> ids = asList("1", "2", "3");
        saver.setQuery("SELECT * FROM concept WHERE id IN (?,?)");

        saver.writeContent(bundle, csvWriter, ids);

        verify(connection).prepareStatement(eq("SELECT * FROM concept WHERE id IN (?,?,?,?,?,?)"));
        verifySetStringValues(preparedStatement, asList("1", "2", "3", "1", "2", "3"));
    }

    @Test
    public void shouldPutAllVocabularyIdsToOnePlaceForV4_5() throws Exception {
        when(bundle.getCdmVersion()).thenReturn(CDMVersion.V4_5);
        List<Long> ids = asList(1L, 2L, 3L);
        saver.setQuery("SELECT * FROM concept WHERE id IN (?)");

        saver.writeContent(bundle, csvWriter, ids);

        verify(connection).prepareStatement(eq("SELECT * FROM concept WHERE id IN (?,?,?)"));
        verifySetLongValues(preparedStatement, asList(1L, 2L, 3L));
    }

    @Test
    public void shouldPutAllVocabularyIdsToSeveralPlaceForV4_5() throws Exception {
        when(bundle.getCdmVersion()).thenReturn(CDMVersion.V4_5);
        List<Long> ids = asList(1L, 2L, 3L);
        saver.setQuery("SELECT * FROM concept WHERE id IN (?,?)");

        saver.writeContent(bundle, csvWriter, ids);

        verify(connection).prepareStatement(eq("SELECT * FROM concept WHERE id IN (?,?,?,?,?,?)"));
        verifySetLongValues(preparedStatement, asList(1L, 2L, 3L, 1L, 2L, 3L));
    }

    private void verifySetStringValues(PreparedStatement preparedStatement, List<String> expectedValues) throws SQLException {
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(preparedStatement, times(expectedValues.size())).setString(anyInt(), valueCaptor.capture());
        assertEquals(expectedValues, valueCaptor.getAllValues());
    }

    private void verifySetLongValues(PreparedStatement preparedStatement, List<Long> expectedValues) throws SQLException {
        ArgumentCaptor<Long> valueCaptor = ArgumentCaptor.forClass(Long.class);
        verify(preparedStatement, times(expectedValues.size())).setLong(anyInt(), valueCaptor.capture());
        assertEquals(expectedValues, valueCaptor.getAllValues());
    }

    @Setter
    @Getter
    public static class SaverDummy extends CSVSaver {

        private String query;

        @Override
        public String fileName() {
            return "somefile.csv";
        }

        @Override
        public String query() {
            return query;
        }
    }

}