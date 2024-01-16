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
import com.odysseusinc.athena.model.athena.DownloadItem;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadBundleRepository;
import com.odysseusinc.athena.repositories.athena.VocabularyConversionRepository;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.VocabularyReleaseVersionService;
import com.odysseusinc.athena.service.mail.EmailService;
import com.odysseusinc.athena.service.saver.*;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.service.writer.ZipWriter;
import com.odysseusinc.athena.util.CDMVersion;
import com.odysseusinc.athena.util.DownloadBundleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.springframework.scheduling.annotation.Async;

import static java.util.stream.Collectors.toMap;


@Service
@Transactional
public class AsyncVocabularyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncVocabularyService.class);

    private final DownloadBundleRepository downloadBundleRepository;
    private final DownloadBundleService downloadBundleService;
    private final EmailService emailService;
    private final FileHelper fileHelper;
    private final List<SaverV4> saversV4;
    private final List<SaverV5> saversV5;
    private final List<SaverV5History> saverV5Histories;
    private final List<SaverV5Delta> saverV5Deltas;
    private final VocabularyReleaseVersionService versionService;
    private final UrlBuilder urlBuilder;
    private final VocabularyConversionRepository vocabularyConversionRepository;
    private final ZipWriter zipWriter;

    public AsyncVocabularyService(DownloadBundleRepository downloadBundleRepository, DownloadBundleService downloadBundleService, EmailService emailService, FileHelper fileHelper, List<SaverV4> saversV4, List<SaverV5> saversV5, List<SaverV5History> saverV5Histories, List<SaverV5Delta> saverV5Deltas, VocabularyReleaseVersionService versionService, UrlBuilder urlBuilder, VocabularyConversionRepository vocabularyConversionRepository, ZipWriter zipWriter) {
        this.downloadBundleRepository = downloadBundleRepository;
        this.downloadBundleService = downloadBundleService;
        this.emailService = emailService;
        this.fileHelper = fileHelper;
        this.saversV4 = saversV4;
        this.saversV5 = saversV5;
        this.saverV5Histories = saverV5Histories;
        this.saverV5Deltas = saverV5Deltas;
        this.versionService = versionService;
        this.urlBuilder = urlBuilder;
        this.vocabularyConversionRepository = vocabularyConversionRepository;
        this.zipWriter = zipWriter;
    }

    @Async
    public void saveContent(DownloadBundle bundle, AthenaUser user) {

        List<Long> idV4s = bundle.getVocabularyV4Ids();
        try (FileOutputStream fout = new FileOutputStream(fileHelper.getZipPath(bundle.getUuid()));
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fout))) {

            List<?> ids = getIds(bundle, idV4s);
            List<? extends ISaver> savers = getSavers(bundle);

            SaverService saver = new SaverService(downloadBundleService, ids, fileHelper);
            bundle = saver.save(zos, bundle, savers);
            zipWriter.addExtraFiles(zos, bundle);

            LOGGER.info("Bundle is saved in zip: {}", bundle);
            updateStatus(bundle, DownloadBundleStatus.READY);

            final Map<String, String> includedVocabularies = bundle.getVocabularies().stream()
                    .map(DownloadItem::getVocabularyConversion)
                    .filter(vocab -> !vocab.getOmopReqValue())
                    .collect(toMap(VocabularyConversion::getIdV5, VocabularyConversion::getName));

            emailService.sendVocabularyDownloadLink(user, urlBuilder.downloadVocabulariesLink(bundle.getUuid()),
                    bundle.getCdmVersion(), bundle.getReleaseVersion(), bundle.getName(), includedVocabularies);

        } catch (Exception ex) {
            updateStatus(bundle, DownloadBundleStatus.FAILED);
            LOGGER.error(ex.getMessage(), ex);
            emailService.sendFailedSaving(user);
        }
    }

    private List<? extends ISaver> getSavers(DownloadBundle bundle) {
        if (bundle.getCdmVersion() == CDMVersion.V4_5) {
            return saversV4;
        }
        if (bundle.getCdmVersion() == CDMVersion.V5 && bundle.isDelta()) {
            return saverV5Deltas;
        }
        if (bundle.getCdmVersion() == CDMVersion.V5 && versionService.isCurrent(bundle.getVocabularyVersion()) && !bundle.isDelta()) {
            return saversV5;
        }
        if (bundle.getCdmVersion() == CDMVersion.V5 && !versionService.isCurrent(bundle.getVocabularyVersion()) && !bundle.isDelta()) {
            return saverV5Histories;
        }

        throw new NotExistException("No savers for version " + bundle.getCdmVersion(), CDMVersion.class);
    }


    // TODO: Generics needs proper handling. We plan to eliminate the v4 version.
    private List getIds(DownloadBundle bundle, List<Long> idV4s) {
        switch (bundle.getCdmVersion()) {
            case V4_5:
                return idV4s;
            case V5:
                return vocabularyConversionRepository.findIdsV5ByIdsV4(idV4s);
        }
        throw new NotExistException("Unsupported CDM version: " + bundle.getCdmVersion(), CDMVersion.class);
    }


    protected void updateStatus(DownloadBundle bundle, DownloadBundleStatus status) {

        bundle.setStatus(status);
        downloadBundleRepository.save(bundle);
    }
}
