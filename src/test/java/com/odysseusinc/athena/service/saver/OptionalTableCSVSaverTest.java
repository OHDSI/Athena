/*
 *
 * Copyright 2026 Odysseus Data Services, Inc. (EPAM Systems company)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Created: August 28, 2026
 *
 */

package com.odysseusinc.athena.service.saver;

import com.odysseusinc.athena.exceptions.IORuntimeException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.util.CDMVersion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OptionalTableCSVSaverTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement statement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private DownloadBundle bundle;
    @Mock
    private ZipOutputStream zipOutputStream;

    private TestSaver saver;

    @Before
    public void setUp() throws Exception {

        saver = new TestSaver();
        saver.v5DataSource = dataSource;
        when(bundle.getCdmVersion()).thenReturn(CDMVersion.V5);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT to_regclass(?)")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
    }

    @Test
    public void skipsTheFileWhenItsTableDoesNotExist() throws Exception {

        when(resultSet.getString(1)).thenReturn(null);

        saver.save(zipOutputStream, bundle, Collections.singletonList("RxNorm"));

        assertFalse(saver.writeCalled);
        verify(statement).setString(1, "concept_metadata");
    }

    @Test
    public void writesTheFileWhenItsTableExists() throws Exception {

        when(resultSet.getString(1)).thenReturn("concept_metadata");

        saver.save(zipOutputStream, bundle, Collections.singletonList("RxNorm"));

        assertTrue(saver.writeCalled);
    }

    @Test
    public void doesNotMistakeDatabaseFailuresForAnAbsentOptionalTable() throws Exception {

        when(statement.executeQuery()).thenThrow(new SQLException("connection lost"));

        assertThrows(IORuntimeException.class,
                () -> saver.save(zipOutputStream, bundle, Collections.singletonList("RxNorm")));
        assertFalse(saver.writeCalled);
    }

    private static class TestSaver extends CSVSaver {

        private boolean writeCalled;

        @Override
        protected String requiredTable() {

            return "concept_metadata";
        }

        @Override
        public String fileName() {

            return "CONCEPT_METADATA.csv";
        }

        @Override
        protected String query() {

            return "SELECT * FROM concept_metadata";
        }

        @Override
        protected <T> void writeCSVtoZIP(ZipOutputStream zos, DownloadBundle bundle,
                                         List<T> vocabularyIds) {

            writeCalled = true;
        }
    }
}
