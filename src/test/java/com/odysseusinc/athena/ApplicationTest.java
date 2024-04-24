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

package com.odysseusinc.athena;

import com.odysseusinc.athena.glue.LocalEnvironmentInitializer;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.util.CDMVersion;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@ActiveProfiles("test")
@SpringBootTest(
        //TODO DEV don't like it, maybe it is better override configuration complitly
        properties = "spring.main.allow-bean-definition-overriding=true",
        classes = TestConfiguration.class)
@ContextConfiguration(initializers = LocalEnvironmentInitializer.class)
@Deprecated
public class ApplicationTest {


    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private FileHelper fileHelper;

    @Test
    void name() throws IOException {
        List<Integer> list =  Arrays.asList(18,125,21,78,71,72,66,2,127,1,0,90,70,34);
        new ArrayList<>();
        AthenaUser currentUser = new AthenaUser(1L);
        DownloadBundle downloadBundle = vocabularyService.saveBundle("test", list, currentUser, CDMVersion.V5, 19990502, true, 19990501);
        vocabularyService.saveContent(downloadBundle, currentUser);
        String zipPath = fileHelper.getZipPath(downloadBundle.getUuid());
        System.out.println(zipPath);

        Path tempDir = Files.createTempDirectory(UUID.randomUUID().toString());
        List<String> unzip = unzip(zipPath, tempDir.toString());
        Assertions.assertEquals(10, unzip.size());

    }

    public List<String>  unzip(String zipFilePath, String destDir) throws IOException {
        List<String> extractedFiles = new ArrayList<>();
        try (ZipArchiveInputStream zipIn = new ZipArchiveInputStream(Files.newInputStream(Paths.get(zipFilePath)))) {
            ArchiveEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDir + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    extractFile(zipIn, filePath);
                    extractedFiles.add(filePath);
                }
                entry = zipIn.getNextEntry();
            }
        }
        return extractedFiles;
    }

    public static void extractFile(InputStream input, String outputPath) throws IOException {
        try (OutputStream output = Files.newOutputStream(Paths.get(outputPath))) {
            IOUtils.copy(input, output);
        }
    }

}
