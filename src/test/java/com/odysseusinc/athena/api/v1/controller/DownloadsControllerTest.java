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
 * Created: August 30, 2026
 *
 */

package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.api.v1.controller.dto.DownloadHistoryDTO;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.service.DownloadsHistoryService;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.impl.UserService;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.util.CDMVersion;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/** Regression coverage for streamed vocabulary archives. */
public class DownloadsControllerTest {

    @Test
    public void missingArchiveIsNotFoundBeforeStatisticsOrHeadersAreWritten() throws Exception {

        Path missing = Files.createTempDirectory("athena-missing-archive").resolve("missing.zip");
        Fixture fixture = fixture(missing);
        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            fixture.controller.getZippedBundle(fixture.bundle.getUuid(), response);
            fail("a database record without its ZIP must not be reported as a server error");
        } catch (NotExistException expected) {
            assertEquals("Download archive is no longer available", expected.getMessage());
        }

        assertEquals(0, fixture.historyService.updateCount);
        assertNull(response.getContentType());
        assertNull(response.getHeader("Content-Disposition"));
        assertEquals(0, response.getContentAsByteArray().length);
    }

    @Test
    public void existingArchiveUsesOneValidContentTypeAndRecordsTheDownload() throws Exception {

        byte[] archive = {0x50, 0x4b, 0x03, 0x04};
        Path zip = Files.createTempFile("athena-archive", ".zip");
        Files.write(zip, archive);
        Fixture fixture = fixture(zip);
        MockHttpServletResponse response = new MockHttpServletResponse();

        fixture.controller.getZippedBundle(fixture.bundle.getUuid(), response);

        assertEquals("application/zip", response.getContentType());
        assertArrayEquals(archive, response.getContentAsByteArray());
        assertEquals(1, fixture.historyService.updateCount);
    }

    private static Fixture fixture(Path zip) {

        DownloadBundle bundle = new DownloadBundle();
        bundle.setId(17L);
        bundle.setUuid("archive-uuid");
        bundle.setUserId(42L);
        bundle.setCdmVersion(CDMVersion.V5);

        RecordingHistoryService historyService = new RecordingHistoryService();
        UserService userService = new UserService() {
            @Override
            public Long getCurrentUserId() {
                return null;
            }
        };
        VocabularyService vocabularyService = (VocabularyService) Proxy.newProxyInstance(
                VocabularyService.class.getClassLoader(),
                new Class<?>[]{VocabularyService.class},
                (proxy, method, args) -> {
                    if ("getDownloadBundle".equals(method.getName())) {
                        return bundle;
                    }
                    if ("checkBundleVocabularies".equals(method.getName())) {
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        FileHelper fileHelper = new FileHelper() {
            @Override
            public String getZipPath(String uuid) {
                return zip.toString();
            }
        };

        DownloadsController controller = new DownloadsController(
                historyService, fileHelper, userService, vocabularyService);
        return new Fixture(controller, historyService, bundle);
    }

    private static final class Fixture {
        private final DownloadsController controller;
        private final RecordingHistoryService historyService;
        private final DownloadBundle bundle;

        private Fixture(DownloadsController controller,
                        RecordingHistoryService historyService,
                        DownloadBundle bundle) {
            this.controller = controller;
            this.historyService = historyService;
            this.bundle = bundle;
        }
    }

    private static final class RecordingHistoryService implements DownloadsHistoryService {
        private int updateCount;

        @Override
        public void updateStatistics(DownloadBundle bundle, Long userId) {
            updateCount++;
        }

        @Override
        public Collection<DownloadHistoryDTO> retrieveStatistics(LocalDateTime from,
                                                                  LocalDateTime to,
                                                                  Boolean licensedOnly,
                                                                  String[] keywords) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void generateCSV(Collection<DownloadHistoryDTO> records, OutputStream osw)
                throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<DownloadHistoryDTO> sort(Collection<DownloadHistoryDTO> dtos,
                                                   String sortBy,
                                                   boolean sortAsc) {
            throw new UnsupportedOperationException();
        }
    }
}
