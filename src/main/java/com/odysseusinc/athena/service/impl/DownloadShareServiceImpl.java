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

import com.google.common.base.Splitter;
import com.odysseusinc.athena.api.v1.controller.converter.UrlBuilder;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadShareChangeDTO;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.DownloadShare;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadShareRepository;
import com.odysseusinc.athena.service.DownloadShareService;
import com.odysseusinc.athena.service.mail.VocabulariesShareSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class DownloadShareServiceImpl implements DownloadShareService {
    @Autowired
    private DownloadShareRepository bundleShareRepository;

    @Autowired
    private VocabulariesShareSender vocabulariesShareSender;

    @Autowired
    private UserService userService;

    @Autowired
    private UrlBuilder urlBuilder;

    @Override
    public List<DownloadShare> getBundleShares(Long downloadBundleId) {
        return bundleShareRepository.findByDownloadShareIdBundleId(downloadBundleId);
    }

    @Override
    public List<DownloadShare> getBundleShares(String shareUserEmail) {
        return bundleShareRepository.findByDownloadShareIdUserEmail(shareUserEmail);
    }

    @Override
    public List<String> getUserEmails(Long downloadBundleId) {
        List<DownloadShare> downloadShares = bundleShareRepository.findByDownloadShareIdBundleId(downloadBundleId);
        if (downloadShares == null || downloadShares.isEmpty()) {
            return Collections.emptyList();
        }
        return downloadShares.stream()
                .map(bs -> bs.getUserEmail())
                .collect(toList());
    }

    @Override
    public List<DownloadShare> change(DownloadBundle bundle, DownloadShareChangeDTO changeDTO, AthenaUser user) {
        if (changeDTO == null) {
            return Collections.emptyList();
        }

        if (changeDTO.getEmailList() == null || changeDTO.getEmailList().isEmpty()) {
            deleteByDownloadBundleId(bundle.getId());
            return Collections.emptyList();
        }

        List<String> emails = Splitter.on(",").trimResults().splitToList(changeDTO.getEmailList());

        List<DownloadShare> sharedBundles = emails.stream()
                .map(email -> new DownloadShare(bundle.getId(), email, user))
                .collect(Collectors.toList());

        List<DownloadShare> existingSharedBundles = getBundleShares(bundle.getId());

        List<DownloadShare> newSharedBundles = sharedBundles.stream()
                .filter(bs -> !existingSharedBundles.contains(bs))
                .collect(toList());
        bundleShareRepository.save(newSharedBundles);
        newSharedBundles.stream()
                .forEach(downloadShare -> {
                    AthenaUser shareUser = userService.getUser(downloadShare.getUserEmail());
                    if (shareUser != null) {
                        try {
                            vocabulariesShareSender.send(shareUser, user,
                                    urlBuilder.downloadVocabulariesLink(bundle.getUuid()),
                                    bundle.getCdmVersion());
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                });

        List<DownloadShare> unusedSharedBundles = existingSharedBundles.stream()
                .filter(existingBS -> !sharedBundles.contains(existingBS))
                .collect(toList());
        bundleShareRepository.delete(unusedSharedBundles);

        return getBundleShares(bundle.getId());
    }

    @Override
    public void deleteByDownloadBundleId(Long downloadBundleId) {
        bundleShareRepository.deleteByDownloadShareIdBundleId(downloadBundleId);
    }
}
