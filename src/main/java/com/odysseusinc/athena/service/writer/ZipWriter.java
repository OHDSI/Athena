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

package com.odysseusinc.athena.service.writer;

import static com.odysseusinc.athena.util.CDMVersion.V4_5;

import com.odysseusinc.athena.model.athena.DownloadBundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.odysseusinc.athena.service.impl.AthenaCSVWriter;
import com.odysseusinc.athena.service.saver.Saver;
import com.odysseusinc.athena.service.saver.v4.InvalidConceptCPT4V4Saver;
import com.odysseusinc.athena.service.saver.v5.InvalidConceptCPT4V5Saver;
import com.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ZipWriter {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ZipWriter.class);

    @Value("${cpt4.dir.v4}")
    private String cpt4V4Files;

    @Value("${cpt4.dir.v5}")
    private String cpt4V5Files;

    @Autowired
    FileHelper fileHelper;

    @Autowired
    InvalidConceptCPT4V4Saver cpt4V4DeprecatedSaver;

    @Autowired
    InvalidConceptCPT4V5Saver cpt4V5DeprecatedSaver;

    public synchronized void addCPT4Utility(ZipOutputStream zos, DownloadBundle bundle) throws Exception {

        if (bundle.isCpt4()) {
            File filesStoreDir = V4_5 == bundle.getCdmVersion() ? new File(cpt4V4Files) : new File(cpt4V5Files);
            updateCPT4Utility(bundle, filesStoreDir.getAbsolutePath());
            addFolderToZip(filesStoreDir, zos, filesStoreDir.getAbsolutePath());
        }
    }

    private void addFolderToZip(File folder, ZipOutputStream zip, String baseName) throws IOException {

        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                addFolderToZip(file, zip, baseName);
            } else {
                String name = file.getAbsolutePath().substring(baseName.length() + 1);
                LOGGER.info("Adding zip entry : absolute path file {}, baseName {}, baseName length {}, entry name {}",
                        file.getAbsolutePath(), baseName, baseName.length(), name);
                ZipEntry zipEntry = new ZipEntry(name);
                zip.putNextEntry(zipEntry);
                IOUtils.copy(new FileInputStream(file), zip);
                zip.closeEntry();
            }
        }
    }

    public static void putEntry(ZipOutputStream zos, String fileName, Path path) throws IOException {

        zos.putNextEntry(new ZipEntry(fileName));
        Files.copy(path, zos);
        zos.closeEntry();
    }

    private void updateCPT4Utility(DownloadBundle bundle, String filePath) throws Exception {
        Saver saver = V4_5 == bundle.getCdmVersion() ? cpt4V4DeprecatedSaver : cpt4V5DeprecatedSaver;
        Path path = fileHelper.getPath(bundle.getUuid(), saver.fileName());
        try (CSVWriter csvWriter = new AthenaCSVWriter(path.toString(), saver.getSeparator())) {
            saver.writeContent(bundle, csvWriter, saver.getIds());
        }

        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        URI uri = URI.create(fileHelper.getCPT4UtilityJarFilePath(filePath));

        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            Path externalTxtFile = Paths.get(path.toString());
            Path pathInZipfile = zipfs.getPath(saver.fileName());
            Files.copy(externalTxtFile, pathInZipfile,
                    StandardCopyOption.REPLACE_EXISTING);
        }
        Files.delete(path);
    }
}
