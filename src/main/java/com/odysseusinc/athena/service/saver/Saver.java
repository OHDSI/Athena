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

import static com.odysseusinc.athena.service.writer.ZipWriter.putEntry;

import com.odysseusinc.athena.exceptions.IORuntimeException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.SavedFile;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.impl.AthenaCSVWriter;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.util.CDMVersion;
import com.opencsv.CSVWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipOutputStream;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

public abstract class Saver implements ISaver {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Saver.class);
    private static final String QUESTION_MARK = "?";
    private static final String COMMA = ",";

    @Autowired
    protected DownloadBundleService bundleService;

    @Value("${csv.separator:;}")
    protected Character separator;

    @Autowired
    @Qualifier("athenaV5DataSource")
    private DataSource v5DataSource;

    @Autowired
    @Qualifier("athenaV4DataSource")
    private DataSource v4DataSource;

    @Autowired
    FileHelper fileHelper;

    public abstract String fileName();

    protected abstract String query();

    protected List filter(List ids) {

        return ids;
    }

    public boolean containCpt4(List ids) {

        return false;
    }

    public boolean includedInBundle(List ids) {

        return true;
    }

    public Character getSeparator() {
        return separator;
    }

    public List getIds() {

        return Collections.emptyList();
    }

    public void save(ZipOutputStream zos, DownloadBundle bundle, List ids) {

        if (!includedInBundle(ids)) {
            return;
        }
        List filteredIds = filter(ids);
        writeCSV(zos, bundle, filteredIds);
    }

    protected void writeCSV(ZipOutputStream zos, DownloadBundle bundle, List vocabularyIds) {

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

    public void writeContent(DownloadBundle bundle, CSVWriter csvWriter, List ids) throws Exception {

        if (ids.isEmpty()) {
            return;
        }
        CDMVersion currentVersion = bundle.getCdmVersion();
        DataSource dataSource = CDMVersion.V4_5 == currentVersion ? v4DataSource : v5DataSource;

        String query = query();

        int countParam = StringUtils.countMatches(query, QUESTION_MARK);
        int countVocabs = ids.size();

        String param = String.join(COMMA, Collections.nCopies(countVocabs, QUESTION_MARK));
        query = query.replace(QUESTION_MARK, param);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(query)) {

            conn.setAutoCommit(false);

            for (int i = 0; i < countParam; i++) {
                for (int j = 0; j < countVocabs; j++) {

                    if (CDMVersion.V4_5 == currentVersion) {
                        st.setLong(i * countVocabs + j + 1, (Long) ids.get(j));
                    } else {
                        st.setString(i * countVocabs + j + 1, (String) ids.get(j));
                    }
                }
            }
            st.setFetchSize(500);
            LOGGER.info("Preparing to execute (bundle with uuid {}): {}", bundle.getUuid(), st);
            ResultSet rs = st.executeQuery();
            csvWriter.writeAll(rs, true, false);
            rs.close();
        }
    }
}
