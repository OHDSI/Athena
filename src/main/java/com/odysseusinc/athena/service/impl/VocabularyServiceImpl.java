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

import com.odysseusinc.athena.api.v1.controller.converter.ConverterUtils;
import com.odysseusinc.athena.api.v1.controller.converter.vocabulary.VocabularyToUserVocabularyDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadBundleDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadShareDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.UserVocabularyDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyDTO;
import com.odysseusinc.athena.exceptions.LicenseException;
import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.DownloadItem;
import com.odysseusinc.athena.model.athena.DownloadShare;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.model.athena.Notification;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadBundleRepository;
import com.odysseusinc.athena.repositories.athena.DownloadItemRepository;
import com.odysseusinc.athena.repositories.athena.DownloadShareRepository;
import com.odysseusinc.athena.repositories.athena.LicenseRepository;
import com.odysseusinc.athena.repositories.athena.NotificationRepository;
import com.odysseusinc.athena.service.*;
import com.odysseusinc.athena.service.mail.EmailService;
import com.odysseusinc.athena.service.saver.v5.history.delta.CacheDeltaService;
import com.odysseusinc.athena.util.CDMVersion;
import com.odysseusinc.athena.util.DownloadBundleStatus;
import com.odysseusinc.athena.util.extractor.LicenseStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.odysseusinc.athena.util.extractor.LicenseStatus.APPROVED;
import static com.odysseusinc.athena.util.extractor.LicenseStatus.PENDING;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.ListUtils.intersection;
import static org.thymeleaf.util.ListUtils.isEmpty;

@Slf4j
@Service
@Transactional
public class VocabularyServiceImpl implements VocabularyService {
    private static final String DEFAULT_SORT_COLUMN = "idV4";
    public static final Integer CPT4_ID_V4 = 4;

    private final AsyncVocabularyService asyncVocabularyService;
    private final ConceptService conceptService;
    private final ConverterUtils converterUtils;
    private final DownloadBundleRepository downloadBundleRepository;
    private final DownloadItemRepository downloadItemRepository;
    private final DownloadShareRepository downloadShareRepository;
    private final EmailService emailService;
    private final GenericConversionService conversionService;
    private final LicenseRepository licenseRepository;
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final VocabularyConversionService vocabularyConversionService;

    protected final CacheDeltaService cacheDeltaService;

    private final DownloadBundleService downloadBundleService;

    @Autowired
    public VocabularyServiceImpl(AsyncVocabularyService asyncVocabularyService, ConceptService conceptService, ConverterUtils converterUtils, DownloadBundleRepository downloadBundleRepository, DownloadItemRepository downloadItemRepository, DownloadShareRepository downloadShareRepository, EmailService emailService, GenericConversionService conversionService, LicenseRepository licenseRepository, NotificationRepository notificationRepository, UserService userService, VocabularyConversionService vocabularyConversionService, CacheDeltaService cacheDeltaService, DownloadBundleService downloadBundleService) {

        this.asyncVocabularyService = asyncVocabularyService;
        this.conceptService = conceptService;
        this.converterUtils = converterUtils;
        this.downloadBundleRepository = downloadBundleRepository;
        this.downloadItemRepository = downloadItemRepository;
        this.downloadShareRepository = downloadShareRepository;
        this.emailService = emailService;
        this.conversionService = conversionService;
        this.licenseRepository = licenseRepository;
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.vocabularyConversionService = vocabularyConversionService;
        this.cacheDeltaService = cacheDeltaService;
        this.downloadBundleService = downloadBundleService;
    }

    @Override
    public List<UserVocabularyDTO> getAllForCurrentUser() {

        Sort sort = Sort.by(Sort.Direction.ASC, DEFAULT_SORT_COLUMN);
        AthenaUser user = userService.getCurrentUser();
        List<VocabularyDTO> vocabularyDTOs = converterUtils.convertList(
                vocabularyConversionService.findByOmopReqIsNull(sort), VocabularyDTO.class);

        return new VocabularyToUserVocabularyDTO(user.getLicenses()).convert(vocabularyDTOs);
    }

    @Override
    public DownloadBundle saveBundle(String bundleName, List<Integer> idV4s, AthenaUser currentUser, CDMVersion version, Integer vocabularyVersion, boolean delta, Integer deltaVersion) {

        DownloadBundle bundle = downloadBundleService.initBundle(bundleName, currentUser, version, vocabularyVersion, delta, deltaVersion);
        downloadBundleService.validate(bundle);
        log.info("Ready for save download items for bundle with name: [{}] and uuid: [{}], user id: [{}]",
                bundleName, bundle.getUuid(), bundle.getUserId());

        List<Integer> withOmopReqIdV4s = vocabularyConversionService.findByOmopReqIsNotNull()
                .stream()
                .map(VocabularyConversion::getIdV4)
                .collect(toList());
        withOmopReqIdV4s.addAll(idV4s);
        checkBundleVocabularies(withOmopReqIdV4s, currentUser.getId());


        bundle = saveDownloadItems(bundle, withOmopReqIdV4s);
        log.info("Download items are added, bundle: [{}]", bundle);
        return bundle;
    }

    @Override
    public void checkBundleVocabularies(long bundleId, Long userId) {

        DownloadBundle bundle = downloadBundleRepository.getOne(bundleId);
        List<Integer> bundleVocabularyV4Ids = bundle.getVocabularies().stream()
                .map(e -> e.getVocabularyConversion().getIdV4())
                .collect(toList());
        checkBundleVocabularies(bundleVocabularyV4Ids, userId);
    }

    @Override
    public void saveContent(DownloadBundle bundle, AthenaUser user) {
        if (bundle.isDelta() && !cacheDeltaService.isDeltaVersionCached(bundle.getVocabularyVersion(), bundle.getDeltaVersion())) {
            asyncVocabularyService.saveSlowExecutableContent(bundle, user);
        } else {
            asyncVocabularyService.saveContent(bundle, user);
        }
    }

    @Override
    public DownloadBundle saveDownloadItems(DownloadBundle bundle, List<Integer> idV4s) {

        final DownloadBundle result = downloadBundleRepository.save(bundle);
        List<DownloadItem> items = idV4s.stream()
                .map(id -> new DownloadItem(result, new VocabularyConversion(id)))
                .collect(Collectors.toList());

        result.setVocabularies(downloadItemRepository.saveAll(items));
        return result;
    }

    @Override
    public List<DownloadBundleDTO> getDownloadHistory(AthenaUser user) {

        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<DownloadBundle> history = downloadBundleRepository.findByUserId(user.getId(), sort);

        List<DownloadShare> shares = downloadShareRepository.findByUserEmail(user.getEmail());
        List<DownloadBundleDTO> sharedDTOs = new ArrayList<>();
        // add shared bundles to list of available downloads
        if (!shares.isEmpty()) {
            for(DownloadShare share: shares) {
                DownloadBundleDTO bundleDTO = conversionService.convert(share.getBundle(), DownloadBundleDTO.class);
                // remove from shares all references to shares with other users
                List<DownloadShareDTO> filteredShares = bundleDTO.getDownloadShareDTOs().stream()
                        .filter(s -> s.getEmail().equals(user.getEmail()))
                        .collect(toList());
                bundleDTO.setDownloadShareDTOs(filteredShares);
                try {
                    checkBundleVocabularies(share.getBundle().getId(), user.getId());
                } catch (LicenseException e) {
                    // if some vocabularies require licence and current user does not have it -
                    // clear link to zip file
                    bundleDTO.setLink(StringUtils.EMPTY);
                }
                sharedDTOs.add(bundleDTO);
            }
        }

        List<DownloadBundleDTO> dtos = converterUtils.convertList(history, DownloadBundleDTO.class);
        dtos.addAll(sharedDTOs);
        return dtos;
    }

    @Override
    public DownloadBundle getDownloadBundle(String uuid) {

        DownloadBundle result = downloadBundleRepository.findByUuid(uuid);
        if (result == null) {
            throw new NotExistException("No bundle with uuid " + uuid, DownloadBundle.class);
        }
        return result;
    }

    @Override
    public void restoreDownloadBundle(long bundleId) {

        DownloadBundle downloadBundle = downloadBundleRepository.getOne(bundleId);
        AthenaUser currentUser = userService.getCurrentUser();
        checkBundleUser(currentUser, downloadBundle);
        if (!downloadBundle.isArchived()) {
            return;
        }
        downloadBundleService.validate(downloadBundle);
        checkBundleVocabularies(downloadBundle.getId(), currentUser.getId());
        asyncVocabularyService.updateStatus(downloadBundle, DownloadBundleStatus.PENDING);
        saveContent(downloadBundle, currentUser);
        log.info("Vocabulary restoring is started, bundle id: {}, user id: {}", downloadBundle.getId(),
                currentUser.getId());
    }

    @Override
    public void checkBundleUser(AthenaUser user, DownloadBundle bundle){

        if (ObjectUtils.notEqual(user.getId(), bundle.getUserId())) {
            throw new PermissionDeniedException();
        }
    }

    @Override
    public void checkBundleAndSharedUser(AthenaUser user, DownloadBundle bundle){
        if (ObjectUtils.notEqual(user.getId(), bundle.getUserId())) {
            // check whether this bundle was shared with the current user
            List<DownloadShare> shares = downloadShareRepository.findByBundle(bundle);
            shares.stream()
                    .filter(s -> user.getEmail().equals(s.getUserEmail()))
                    .findAny()
                    .orElseThrow(() -> new PermissionDeniedException());
        }
    }

    @Override
    public List<License> grantLicenses(AthenaUser user, List<Integer> vocabularyV4Ids) {

        final List<License> newLicenses = vocabularyV4Ids.stream()
                .map(v4Id -> buildLicense(user, v4Id, APPROVED))
                .collect(toList());

        final List<License> savedLicenses = licenseRepository.saveAll(newLicenses);
        conceptService.invalidateGraphCache(user.getId());
        return savedLicenses;
    }

    @Override
    public Long requestLicense(AthenaUser user, Integer vocabularyV4Id) {

        final License requestedLicense = buildLicense(user, vocabularyV4Id, PENDING);
        requestedLicense.setRequestDate(new Date());
        final License savedLicense = licenseRepository.save(requestedLicense);
        conceptService.invalidateGraphCache(user.getId());
        return savedLicense.getId();
    }

    @Override
    public void deleteLicense(Long licenseId) {

        License userLicense = licenseRepository.getOne(licenseId);
        licenseRepository.deleteById(licenseId);
        conceptService.invalidateGraphCache(userLicense.getUser().getId());
    }

    @Override
    public void acceptLicense(Long id, boolean accepted) {

        License userLicense = licenseRepository.getOne(id);
        String vocabularyName = userLicense.getVocabularyConversion().getName();
        AthenaUser user = userLicense.getUser();
        if (accepted) {
            userLicense.setStatus(LicenseStatus.APPROVED);
            licenseRepository.save(userLicense);
        } else {
            licenseRepository.deleteById(id);
        }
        conceptService.invalidateGraphCache(user.getId());
        emailService.sendLicenseAcceptance(user, accepted, vocabularyName);
    }

    @Override
    public License get(AthenaUser user, Integer vocabularyId) {

        return licenseRepository.findByUserIdAndVocabularyIdV4(user.getId(), vocabularyId);
    }

    @Override
    public Optional<License> get(Long licenseId) {

        return licenseRepository.findById(licenseId);
    }

    @Override
    public License get(Long licenseId, String token) {

        return licenseRepository.findByIdAndToken(licenseId, token);
    }

    @Override
    public List<Notification> getNotifications(Long userId) {

        return notificationRepository.findByUserId(userId);
    }


    private License buildLicense(AthenaUser user,  Integer vocabularyV4Id, LicenseStatus status) {

        VocabularyConversion vocabularyConversion = vocabularyConversionService.findByVocabularyV4Id(vocabularyV4Id);
        return new License(user, vocabularyConversion, status);
    }

    private void checkBundleVocabularies(List<Integer> bundleVocabularyIdV4s, Long userId) {

        //PENDING licenses are not active
        List<Integer> allUnavailableVocabularyIds = vocabularyConversionService.getUnavailableVocabularies(userId, false)
                .stream()
                .map(VocabularyDTO::getId)
                .collect(Collectors.toList());

        List<Integer> unavailableIdsFromBundle = intersection(allUnavailableVocabularyIds, bundleVocabularyIdV4s);
        if (!isEmpty(unavailableIdsFromBundle)) {
            throw new LicenseException(
                    format("User must have licenses for the bundle vocabularies %s", unavailableIdsFromBundle.toString()), unavailableIdsFromBundle);
        }
    }
}
