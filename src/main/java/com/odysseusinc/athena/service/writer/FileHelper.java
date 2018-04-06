/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
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

import com.odysseusinc.athena.exceptions.IORuntimeException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileHelper {

    @Value("${files.store.path}")
    private String fileStorePath;

    public String getZipPath(@NotNull String uuid) {

        return getStoreFilesPath() + File.separator + uuid + ".zip";
    }

    public String getPath(@NotNull String uuid) {

        return getStoreFilesPath() + File.separator + uuid;
    }

    private String getStoreFilesPath() {

        return fileStorePath.replace('/', File.separatorChar);
    }

    public String getTempPath(@NotNull String uuid) {

        return getStoreFilesPath() + File.separator + "temp-" + uuid + ".csv";
    }

    public Path getPath(@NotNull String uuid, @NotNull String fileName) {

        return Paths.get(getFolder(uuid).toString() + File.separator + fileName);
    }

    public Path getFolder(@NotNull String uuid) {

        String filesPath = getStoreFilesPath();
        File filesStoreDir = new File(filesPath);
        makeDirectory(filesStoreDir);

        File storeDir = new File(filesPath, uuid);
        makeDirectory(storeDir);
        return Paths.get(getStoreFilesPath(), uuid);
    }

    public void makeDirectory(File file) {

        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new IORuntimeException("Cann't create folder:" + file);
            }
        }
    }

    public void deleteTempDirectory(@NotNull String uuid) {

        new File(getPath(uuid)).delete();
    }

    public String getCPT4UtilityJarFilePath(@NotNull String filePath) {
        return "jar:" + new File(filePath + "/cpt4.jar").toURI();
    }
}
