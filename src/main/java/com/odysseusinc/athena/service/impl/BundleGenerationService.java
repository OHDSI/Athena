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
import com.odysseusinc.athena.service.job.BundleGenerationHeartbeat;
import com.odysseusinc.athena.service.job.BundleGenerationQueueService;
import com.odysseusinc.athena.service.mail.EmailService;
import com.odysseusinc.athena.service.saver.*;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.service.writer.ZipWriter;
import com.odysseusinc.athena.util.CDMVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

@Service
public class BundleGenerationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BundleGenerationService.class);

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
    private final UserService userService;
    private final BundleGenerationQueueService queueService;
    private final BundleGenerationHeartbeat heartbeat;

    public BundleGenerationService(DownloadBundleRepository downloadBundleRepository, DownloadBundleService downloadBundleService, EmailService emailService, FileHelper fileHelper, List<SaverV4> saversV4, List<SaverV5> saversV5, List<SaverV5History> saverV5Histories, List<SaverV5Delta> saverV5Deltas, UrlBuilder urlBuilder, VocabularyConversionRepository vocabularyConversionRepository, ZipWriter zipWriter, UserService userService, BundleGenerationQueueService queueService, BundleGenerationHeartbeat heartbeat) {
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
        this.userService = userService;
        this.queueService = queueService;
        this.heartbeat = heartbeat;
    }

    public void generateBundle(long bundleId, String workerId) {

        DownloadBundle bundle = downloadBundleRepository.findById(bundleId)
                .orElseThrow(() -> new NotExistException(
                        "Cannot find bundle with id =" + bundleId, DownloadBundle.class));
        AthenaUser user = userService.get(bundle.getUserId());
        save(bundle, user, workerId);
    }

    private void save(DownloadBundle bundle, AthenaUser user, String workerId) {
        List<Integer> idV4s = bundle.getVocabularyV4Ids();
        long bundleId = bundle.getId();
        String partialZipPath = fileHelper.getPartialZipPath(
                bundle.getUuid(), UUID.randomUUID().toString());
        BundleGenerationHeartbeat.Lease lease = null;
        try {
            lease = heartbeat.start(bundleId, workerId);
            BundleGenerationHeartbeat.Lease activeLease = lease;
            try (FileOutputStream fout = new FileOutputStream(partialZipPath);
                 ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fout))) {

                List<?> ids = getIds(bundle, idV4s);

                BundleType type = downloadBundleService.getType(bundle);
                downloadBundleService.validate(bundle);
                List<? extends ISaver> savers = getSavers(type);

                SaverService saver = new SaverService(
                        downloadBundleService,
                        ids,
                        fileHelper,
                        activeLease::check);
                bundle = saver.save(zos, bundle, savers);
                zipWriter.addExtraFiles(zos, bundle);
            }

            activeLease.check();
            queueService.heartbeat(bundleId, workerId);
            fileHelper.publishZip(bundle.getUuid(), partialZipPath);
            queueService.markReady(bundleId, workerId);
            LOGGER.info("Vocabulary generation completed for bundle [{}]", bundleId);
        } catch (Exception ex) {
            fileHelper.deletePartialZip(partialZipPath);
            boolean failureRecorded = queueService.markFailed(bundleId, workerId, ex);
            LOGGER.error(ex.getMessage(), ex);
            if (failureRecorded) {
                emailService.sendFailedSaving(user);
            }
            return;
        } finally {
            if (lease != null) {
                lease.close();
            }
        }

        try {
            BundleType type = downloadBundleService.getType(bundle);
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
        } catch (Exception emailFailure) {
            // The archive is complete and downloadable; a notifier outage must not make it failed.
            LOGGER.error("Bundle [{}] is ready, but its completion email could not be queued",
                    bundleId, emailFailure);
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
}
