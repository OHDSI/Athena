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
import com.odysseusinc.athena.exceptions.LicenseException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.DownloadShare;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadBundleRepository;
import com.odysseusinc.athena.repositories.athena.DownloadShareRepository;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.DownloadShareService;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.mail.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Transactional
@Service
public class DownloadShareServiceImpl implements DownloadShareService {

    private final DownloadBundleService downloadBundleService;
    private final DownloadBundleRepository downloadBundleRepository;
    private final DownloadShareRepository downloadShareRepository;
    private final EmailService emailService;
    private final UserService userService;
    private final UrlBuilder urlBuilder;
    private final VocabularyService vocabularyService;
    @Autowired
    public DownloadShareServiceImpl(DownloadBundleService downloadBundleService, DownloadBundleRepository downloadBundleRepository, DownloadShareRepository downloadShareRepository, EmailService emailService, UserService userService, UrlBuilder urlBuilder, VocabularyService vocabularyService) {
        this.downloadBundleService = downloadBundleService;
        this.downloadBundleRepository = downloadBundleRepository;
        this.downloadShareRepository = downloadShareRepository;
        this.emailService = emailService;
        this.userService = userService;
        this.urlBuilder = urlBuilder;
        this.vocabularyService = vocabularyService;
    }

    @Override
    public List<DownloadShare> getBundleShares(DownloadBundle downloadBundle) {
        return downloadShareRepository.findByBundle(downloadBundle);
    }

    @Override
    public void change(long bundleId, List<String> emails, AthenaUser user) {

        DownloadBundle bundle = downloadBundleRepository.getOne(bundleId);
        if (emails.isEmpty()) {
            deleteByDownloadBundle(bundle);
            return;
        }

        List<DownloadShare> sharedBundles = emails.stream().distinct()
                .map(email -> new DownloadShare(bundle, email, user))
                .collect(toList());

        List<DownloadShare> existingSharedBundles = getBundleShares(bundle);

        List<DownloadShare> newSharedBundles = sharedBundles.stream()
                .filter(bs -> !existingSharedBundles.contains(bs))
                .collect(toList());
        downloadShareRepository.saveAll(newSharedBundles);

        List<DownloadShare> unusedSharedBundles = existingSharedBundles.stream()
                .filter(existingBS -> !sharedBundles.contains(existingBS))
                .collect(toList());
        downloadShareRepository.deleteAll(unusedSharedBundles);

        sendNotification(newSharedBundles, bundle, user);
    }

    private void sendNotification(List<DownloadShare> newSharedBundles, DownloadBundle bundle, AthenaUser bundleOwner) {
        newSharedBundles.forEach(downloadShare -> {
            AthenaUser recipient = userService.getUser(downloadShare.getUserEmail());
            if (recipient != null) {
                final String bundleUrl = bundleUrlFor(recipient, bundle);
                switch (this.downloadBundleService.getType(bundle)) {
                    case V5_DELTAS:
                        emailService.sendDeltaWereSharedNotification(recipient, bundleOwner, bundleUrl, bundle.getCdmVersion(),
                                bundle.formattedVocabularyVersion(), bundle.formattedDeltaVersion());
                        break;
                    case V5_HISTORIES:
                        emailService.sendVocabulariesWereSharedNotification(recipient, bundleOwner, bundleUrl, bundle.getCdmVersion(),
                                bundle.formattedVocabularyVersion());
                        break;
                    default:
                        emailService.sendVocabulariesWereSharedNotification(recipient, bundleOwner, bundleUrl, bundle.getCdmVersion(),
                                bundle.formattedReleaseVersion());
                        break;
                }
            }
        });
    }

    /**
     * The direct archive link is a bearer URL: {@code /zip/{uuid}} is public and, without a
     * session, is checked against the bundle owner's licences. It must therefore only be sent
     * to recipients who hold every licence the bundle needs, mirroring
     * {@code getDownloadHistory}, which already hides the link from those who do not. Anyone
     * else is sent to the download page, where they can request the missing licences.
     */
    private String bundleUrlFor(AthenaUser recipient, DownloadBundle bundle) {

        try {
            vocabularyService.checkBundleVocabularies(bundle.getId(), recipient.getId());
            return urlBuilder.downloadVocabulariesLink(bundle.getUuid());
        } catch (LicenseException e) {
            return urlBuilder.downloadVocabulariesPageUrl();
        }
    }

    @Override
    public void deleteByDownloadBundle(DownloadBundle downloadBundle) {
        downloadShareRepository.deleteByBundle(downloadBundle);
    }
}
