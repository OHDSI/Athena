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

package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.api.v1.controller.converter.UrlBuilder;
import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadBundleRepository;
import com.odysseusinc.athena.repositories.athena.VocabularyConversionRepository;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.mail.FailedSavingSender;
import com.odysseusinc.athena.service.mail.VocabulariesSender;
import com.odysseusinc.athena.service.mail.VocabulariesShareSender;
import com.odysseusinc.athena.service.saver.ISaver;
import com.odysseusinc.athena.service.saver.SaverV4;
import com.odysseusinc.athena.service.saver.SaverV5;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.service.writer.ZipWriter;
import com.odysseusinc.athena.util.CDMVersion;
import com.odysseusinc.athena.util.DownloadBundleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
public class AsyncVocabularyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncVocabularyService.class);

    private VocabularyConversionRepository vocabularyConversionRepository;
    private DownloadBundleRepository downloadBundleRepository;
    private List<SaverV4> saversV4;
    private List<SaverV5> saversV5;
    private ZipWriter zipWriter;
    private DownloadBundleService downloadBundleService;
    private UrlBuilder urlBuilder;
    private VocabulariesSender vocabulariesSender;
    private VocabulariesShareSender vocabulariesShareSender;
    private FailedSavingSender failedSavingSender;
    private FileHelper fileHelper;

    @Autowired
    public AsyncVocabularyService(VocabularyConversionRepository vocabularyConversionRepository,
                                  DownloadBundleRepository downloadBundleRepository,
                                  List<SaverV4> saversV4, List<SaverV5> saversV5,
                                  ZipWriter zipWriter,
                                  DownloadBundleService downloadBundleService,
                                  UrlBuilder urlBuilder,
                                  VocabulariesSender vocabulariesSender,
                                  VocabulariesShareSender vocabulariesShareSender,
                                  FailedSavingSender failedSavingSender,
                                  FileHelper fileHelper) {

        this.vocabularyConversionRepository = vocabularyConversionRepository;
        this.downloadBundleRepository = downloadBundleRepository;
        this.saversV4 = saversV4;
        this.saversV5 = saversV5;
        this.zipWriter = zipWriter;
        this.downloadBundleService = downloadBundleService;
        this.urlBuilder = urlBuilder;
        this.vocabulariesSender = vocabulariesSender;
        this.vocabulariesShareSender = vocabulariesShareSender;
        this.failedSavingSender = failedSavingSender;
        this.fileHelper = fileHelper;
    }

    @Async
    public void saveContent(DownloadBundle bundle, AthenaUser user) {

        List<Long> idV4s = bundle.getVocabularyV4Ids();
        try (FileOutputStream fout = new FileOutputStream(fileHelper.getZipPath(bundle.getUuid()));
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fout))) {

            List ids;
            List<? extends ISaver> savers;
            switch (bundle.getCdmVersion()) {
                case V4_5:
                    ids = idV4s;
                    savers = saversV4;
                    break;
                case V5:
                    ids = vocabularyConversionRepository.findIdsV5ByIdsV4(idV4s);
                    savers = saversV5;
                    break;
                default:
                    throw new NotExistException("No savers for version " + bundle.getCdmVersion(), CDMVersion.class);
            }
            SaverService saver = new SaverService(downloadBundleService, ids, fileHelper);
            bundle = saver.save(zos, bundle, savers);
            zipWriter.addCPT4Utility(zos, bundle);

            LOGGER.info("Bundle is saved in zip: {}", bundle.toString());
            updateStatus(bundle, DownloadBundleStatus.READY);
            vocabulariesSender.send(user, urlBuilder.downloadVocabulariesLink(bundle.getUuid()),
                    bundle.getCdmVersion());

        } catch (Exception ex) {
            updateStatus(bundle, DownloadBundleStatus.FAILED);
            LOGGER.error(ex.getMessage(), ex);
            failedSavingSender.send(user, Collections.emptyMap());
        }
    }

    protected void updateStatus(DownloadBundle bundle, DownloadBundleStatus status) {

        bundle.setStatus(status);
        downloadBundleRepository.save(bundle);
    }

}
