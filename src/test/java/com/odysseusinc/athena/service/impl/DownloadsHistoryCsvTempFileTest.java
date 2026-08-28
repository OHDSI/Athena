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
 * Created: August 14, 2026
 *
 */

package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.api.v1.controller.dto.DownloadHistoryDTO;
import com.odysseusinc.athena.service.writer.FileHelper;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractCollection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * The temporary file {@code generateCSV} stages the export in must not survive a failure.
 * <p>
 * The copy is guarded — {@code try { Files.copy } finally { Files.deleteIfExists }} — but the
 * writing above it is not. When the try-with-resources block throws, control leaves the method
 * before either statement runs and the file stays in {@code files.store.path}. The comment on
 * that block says the restructuring was so "a failure cannot leak the file", which is the
 * property asserted here; the previous {@code finally { copy; delete; }} did delete on every
 * path. Every failed admin statistics export leaves a UUID-named file behind.
 * <p>
 * The failure is forced through the collection rather than through a record's contents, so the
 * test does not depend on which fields {@code DownloadHistoryExtractor} happens to read.
 * <p>
 * JUnit 4, matching the rest of the suite.
 */
public class DownloadsHistoryCsvTempFileTest {

    @Test
    public void doesNotLeaveTheTemporaryFileBehindWhenWritingFails() throws Exception {

        Path store = Files.createTempDirectory("athena-history-export");
        DownloadsHistoryServiceImpl service =
                new DownloadsHistoryServiceImpl(null, null, ';', storeAt(store));

        try {
            service.generateCSV(new FailingCollection(), new ByteArrayOutputStream());
            fail("the export should have propagated the failure raised while writing");
        } catch (RuntimeException expected) {
            // The point is what is left on disk afterwards, not which exception escaped.
        }

        String[] left = store.toFile().list();
        assertEquals("the export failed, so the staged file should have been removed; the store"
                        + " directory still holds " + String.join(", ", left),
                0, left.length);
    }

    /** The successful path must clean up too — otherwise the assertion above proves nothing. */
    @Test
    public void doesNotLeaveTheTemporaryFileBehindOnSuccess() throws Exception {

        Path store = Files.createTempDirectory("athena-history-export");
        DownloadsHistoryServiceImpl service =
                new DownloadsHistoryServiceImpl(null, null, ';', storeAt(store));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.generateCSV(new java.util.ArrayList<>(), out);

        assertEquals("the store directory should have been left empty",
                0, store.toFile().list().length);
        assertTrue("the header row should have been written", out.size() > 0);
    }

    /**
     * {@code getTempPath} is the only thing {@code generateCSV} needs from the helper, and the
     * repository and user service are untouched on this path.
     */
    private static FileHelper storeAt(Path store) {

        return new FileHelper() {
            @Override
            public String getTempPath(String uuid) {
                return new File(store.toFile(), uuid).getAbsolutePath();
            }
        };
    }

    /**
     * Throws as soon as the records are traversed — after the writer has created the file and
     * written the header, which is the window in which the leak occurs. Extending
     * {@link AbstractCollection} covers {@code stream()} too, since it spliterates the iterator.
     */
    private static final class FailingCollection extends AbstractCollection<DownloadHistoryDTO> {

        @Override
        public Iterator<DownloadHistoryDTO> iterator() {
            throw new IllegalStateException("the record source failed midway through the export");
        }

        @Override
        public int size() {
            return 1;
        }
    }
}
