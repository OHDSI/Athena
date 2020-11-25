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
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.DownloadShare;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadShareRepository;
import com.odysseusinc.athena.service.DownloadShareService;
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

    private final DownloadShareRepository bundleShareRepository;
    private final EmailService emailService;
    private final UserService userService;

    @Autowired
    public DownloadShareServiceImpl(DownloadShareRepository bundleShareRepository, EmailService emailService, UserService userService) {
        this.bundleShareRepository = bundleShareRepository;
        this.emailService = emailService;
        this.userService = userService;
    }

    @Override
    public List<DownloadShare> getBundleShares(DownloadBundle downloadBundle) {
        return bundleShareRepository.findByBundle(downloadBundle);
    }

    @Override
    public void change(DownloadBundle bundle, String emails, AthenaUser user) {
        if (emails == null || emails.isEmpty()) {
            deleteByDownloadBundle(bundle);
            return;
        }

        // User emails are not validated to avoid the possibility
        // for the current user to get the real emails of other users registered in the system
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

    private void sendNotification(List<DownloadShare> newSharedBundles, DownloadBundle bundle, AthenaUser bundleOwner) {
        newSharedBundles.forEach(downloadShare -> {
            AthenaUser recepient = userService.getUser(downloadShare.getUserEmail());
            if (recepient != null) {
                emailService.sendVocabulariesWereSharedNotification(recepient, bundleOwner, bundle);
            }
        });
    }

    @Override
    public void deleteByDownloadBundle(DownloadBundle downloadBundle) {
        bundleShareRepository.deleteByBundle(downloadBundle);
    }
}
