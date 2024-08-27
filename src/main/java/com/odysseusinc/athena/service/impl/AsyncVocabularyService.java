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
import com.odysseusinc.athena.service.DownloadBundleService.BundleType;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import org.springframework.scheduling.annotation.Async;


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
    private final UrlBuilder urlBuilder;
    private final VocabularyConversionRepository vocabularyConversionRepository;
    private final ZipWriter zipWriter;

    public AsyncVocabularyService(DownloadBundleRepository downloadBundleRepository, DownloadBundleService downloadBundleService, EmailService emailService, FileHelper fileHelper, List<SaverV4> saversV4, List<SaverV5> saversV5, List<SaverV5History> saverV5Histories, List<SaverV5Delta> saverV5Deltas, UrlBuilder urlBuilder, VocabularyConversionRepository vocabularyConversionRepository, ZipWriter zipWriter) {
        this.downloadBundleRepository = downloadBundleRepository;
        this.downloadBundleService = downloadBundleService;
        this.emailService = emailService;
        this.fileHelper = fileHelper;
        this.saversV4 = saversV4;
        this.saversV5 = saversV5;
        this.saverV5Histories = saverV5Histories;
        this.saverV5Deltas = saverV5Deltas;
        this.urlBuilder = urlBuilder;
        this.vocabularyConversionRepository = vocabularyConversionRepository;
        this.zipWriter = zipWriter;
    }

    @Async("bundleExecutor")
    public void generateBundle(DownloadBundle bundle, AthenaUser user) {

        save(bundle, user);
    }

    @Async("bundleDeltaExecutor")
    public void generateSlowExecutableBundle(DownloadBundle bundle, AthenaUser user) {

        save(bundle, user);
    }

    private void save(DownloadBundle bundle, AthenaUser user) {
        List<Integer> idV4s = bundle.getVocabularyV4Ids();
        try (FileOutputStream fout = new FileOutputStream(fileHelper.getZipPath(bundle.getUuid()));
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fout))) {

            List<?> ids = getIds(bundle, idV4s);

            BundleType type = downloadBundleService.getType(bundle);
            downloadBundleService.validate(bundle, idV4s);
            List<? extends ISaver> savers = getSavers(type);

            SaverService saver = new SaverService(downloadBundleService, ids, fileHelper);
            bundle = saver.save(zos, bundle, savers);
            zipWriter.addExtraFiles(zos, bundle);

            LOGGER.info("Bundle is saved in zip: {}", bundle);
            updateStatus(bundle, DownloadBundleStatus.READY);

            final Map<String, String> includedVocabularies = bundle.getVocabularies().stream()
                    .map(DownloadItem::getVocabularyConversion)
                    .filter(vocab -> !vocab.getOmopReqValue())
                    .sorted(Comparator.comparing(VocabularyConversion::getIdV4))
                    .collect(Collectors.toMap(
                            VocabularyConversion::getIdV5,
                            VocabularyConversion::getName,
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));

            sendEmail(bundle, user, type, includedVocabularies);

        } catch (Exception ex) {
            updateStatus(bundle, DownloadBundleStatus.FAILED);
            LOGGER.error(ex.getMessage(), ex);
            emailService.sendFailedSaving(user);
        }
    }

    private List<? extends ISaver> getSavers(BundleType type) {
        switch (type) {
            case V4_5:
                return saversV4;
            case V5_HISTORIES:
                return saverV5Histories;
            case V5_DELTAS:
                return saverV5Deltas;
            default:
                return saversV5;
        }
    }

    private void sendEmail(DownloadBundle bundle, AthenaUser user, BundleType type, Map<String, String> includedVocabularies) {
        switch (type) {
            case V5_DELTAS:
                emailService.sendDeltaDownloadLink(user, bundle.getName(), urlBuilder.downloadVocabulariesLink(bundle.getUuid()), bundle.getCdmVersion(),
                        includedVocabularies, bundle.formattedVocabularyVersion(), bundle.formattedDeltaVersion()
                );
                break;
            case V5_HISTORIES:
                emailService.sendVocabularyDownloadLink(user, bundle.getName(), urlBuilder.downloadVocabulariesLink(bundle.getUuid()),
                        bundle.getCdmVersion(), includedVocabularies, bundle.formattedVocabularyVersion()
                );
                break;
            default:
                emailService.sendVocabularyDownloadLink(user, bundle.getName(), urlBuilder.downloadVocabulariesLink(bundle.getUuid()),
                        bundle.getCdmVersion(), includedVocabularies, bundle.formattedReleaseVersion()
                );
                break;
        }
    }




    // TODO: Generics needs proper handling. We plan to eliminate the v4 version.
    private List getIds(DownloadBundle bundle, List<Integer> idV4s) {
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
