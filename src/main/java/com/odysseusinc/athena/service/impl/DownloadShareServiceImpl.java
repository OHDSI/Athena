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
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.DownloadShare;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadShareRepository;
import com.odysseusinc.athena.service.DownloadShareService;
import com.odysseusinc.athena.service.mail.VocabulariesShareSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<DownloadShare> getBundleShares(DownloadBundle downloadBundle) {
        return bundleShareRepository.findByBundle(downloadBundle);
    }

    @Override
    @Transactional
    public void change(DownloadBundle bundle, String emails, AthenaUser user) {
        if (emails == null || emails.isEmpty()) {
            deleteByDownloadBundle(bundle);
            return;
        }

        List<String> emailsList = Splitter.on(",").trimResults().splitToList(emails)
                .stream().distinct().collect(Collectors.toList());

        List<DownloadShare> sharedBundles = emailsList.stream()
                .map(email -> new DownloadShare(bundle, email, user))
                .collect(Collectors.toList());

        List<DownloadShare> existingSharedBundles = getBundleShares(bundle);

        List<DownloadShare> newSharedBundles = sharedBundles.stream()
                .filter(bs -> !existingSharedBundles.contains(bs))
                .collect(toList());
        bundleShareRepository.save(newSharedBundles);

        List<DownloadShare> unusedSharedBundles = existingSharedBundles.stream()
                .filter(existingBS -> !sharedBundles.contains(existingBS))
                .collect(toList());
        bundleShareRepository.delete(unusedSharedBundles);

        sendNotification(newSharedBundles, bundle, user);
    }

    private void sendNotification(List<DownloadShare> newSharedBundles, DownloadBundle bundle, AthenaUser user) {
        newSharedBundles.forEach(downloadShare -> {
                    AthenaUser shareUser = userService.getUser(downloadShare.getUserEmail());
                    if (shareUser != null) {
                        try {
                            vocabulariesShareSender.send(shareUser, user,
                                    urlBuilder.downloadVocabulariesLink(bundle.getUuid()),
                                    bundle.getCdmVersion(), bundle.getReleaseVersion());
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                });
    }

    @Override
    @Transactional
    public void deleteByDownloadBundle(DownloadBundle downloadBundle) {
        bundleShareRepository.deleteByBundle(downloadBundle);
    }
}
