/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package com.odysseusinc.athena.service.saver;

import com.odysseusinc.athena.exceptions.IORuntimeException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.SavedFile;
import com.odysseusinc.athena.service.impl.AthenaCSVWriter;
import com.odysseusinc.athena.util.CDMVersion;
import com.opencsv.CSVWriter;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.zip.ZipOutputStream;

import static com.odysseusinc.athena.service.writer.ZipWriter.putEntry;

public abstract class CSVSaver extends Saver implements ISaver {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CSVSaver.class);

    private final SqlStatementCreator sqlStatementCreator = new SqlStatementCreator();

    @Value("${csv.separator:;}")
    @Getter
    protected Character separator;

    @Override
    public <T> void save(ZipOutputStream zos, DownloadBundle bundle, List<T> ids) {

        if (!includedInBundle(ids)) {
            return;
        }
        List<T> vocabularyIds = filter(ids);
        writeCSVtoZIP(zos, bundle, vocabularyIds);
    }

    protected <T> void writeCSVtoZIP(ZipOutputStream zos, DownloadBundle bundle, List<T> vocabularyIds) {

        final String fileName = fileName();
        Path path = fileHelper.getPath(bundle.getUuid(), fileName);

        try (CSVWriter csvWriter = new AthenaCSVWriter(path.toString(), separator)) {

            writeContent(bundle, csvWriter, vocabularyIds);
            csvWriter.flush(true);
            putEntry(zos, fileName, path);
            LOGGER.info("Entry is added to archive {}, bundle uuid: {}", fileName, bundle.getUuid());
            Files.delete(path);
        } catch (Exception ex) {
            throw new IORuntimeException("", ex);
        }

        SavedFile savedFile = new SavedFile();
        savedFile.setRealName(fileName);
        savedFile.setDownloadBundle(bundle);
        bundleService.save(savedFile);
    }

    public <T> void writeContent(DownloadBundle bundle, CSVWriter csvWriter, List<T> ids) throws Exception {

        if (ids.isEmpty()) {
            return;
        }
        CDMVersion currentVersion = bundle.getCdmVersion();
        try (
                Connection conn = getDataSource(currentVersion).getConnection();
                PreparedStatement st = sqlStatementCreator.getStatement(conn, query(), ids, currentVersion)
        ) {
            LOGGER.info("Preparing to execute (bundle with uuid {}): {}", bundle.getUuid(), st);
            ResultSet rs = st.executeQuery();
            csvWriter.writeAll(rs, true, false);
            rs.close();
        }
    }


}
