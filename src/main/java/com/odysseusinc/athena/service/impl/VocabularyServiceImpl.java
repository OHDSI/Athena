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
import com.odysseusinc.athena.service.ConceptService;
import com.odysseusinc.athena.service.VocabularyConversionService;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.mail.LicenseAcceptanceSender;
import com.odysseusinc.athena.service.mail.LicenseRequestSender;
import com.odysseusinc.athena.util.CDMVersion;
import com.odysseusinc.athena.util.DownloadBundleStatus;
import com.odysseusinc.athena.util.DownloadShareStatus;
import com.odysseusinc.athena.util.extractor.LicenseStatus;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.odysseusinc.athena.util.extractor.LicenseStatus.PENDING;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.ListUtils.intersection;
import static org.thymeleaf.util.ListUtils.isEmpty;

@Service
@Transactional
public class VocabularyServiceImpl implements VocabularyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyServiceImpl.class);

    private static final String DEFAULT_SORT_COLUMN = "idV4";
    public static final Integer CPT4_ID_V4 = 4;

    private VocabularyConversionService vocabularyConversionService;
    private DownloadBundleRepository downloadBundleRepository;
    private DownloadShareRepository downloadShareRepository;
    private DownloadItemRepository downloadItemRepository;
    private LicenseRepository licenseRepository;
    private LicenseRequestSender licenseRequestSender;
    private UserService userService;
    private ConverterUtils converterUtils;
    private AsyncVocabularyService asyncVocabularyService;
    private NotificationRepository notificationRepository;
    private LicenseAcceptanceSender licenseAcceptanceSender;
    private ConceptService conceptService;
    private GenericConversionService conversionService;

    @Autowired
    public VocabularyServiceImpl(VocabularyConversionService vocabularyConversionService,
                                 DownloadBundleRepository downloadBundleRepository,
                                 DownloadShareRepository downloadShareRepository,
                                 DownloadItemRepository downloadItemRepository,
                                 LicenseRepository licenseRepository,
                                 LicenseRequestSender licenseRequestSender,
                                 UserService userService,
                                 ConverterUtils converterUtils,
                                 AsyncVocabularyService asyncVocabularyService,
                                 NotificationRepository notificationRepository,
                                 ConceptService conceptService,
                                 LicenseAcceptanceSender licenseAcceptanceSender,
                                 GenericConversionService conversionService) {

        this.vocabularyConversionService = vocabularyConversionService;
        this.downloadBundleRepository = downloadBundleRepository;
        this.downloadItemRepository = downloadItemRepository;
        this.licenseRepository = licenseRepository;
        this.licenseRequestSender = licenseRequestSender;
        this.userService = userService;
        this.converterUtils = converterUtils;
        this.asyncVocabularyService = asyncVocabularyService;
        this.notificationRepository = notificationRepository;
        this.licenseAcceptanceSender = licenseAcceptanceSender;
        this.conceptService = conceptService;
        this.downloadShareRepository = downloadShareRepository;
        this.conversionService = conversionService;
    }

    @Override
    public List<UserVocabularyDTO> getAllForCurrentUser() throws PermissionDeniedException {

        Sort sort = new Sort(Sort.Direction.ASC, DEFAULT_SORT_COLUMN);
        AthenaUser user = userService.getCurrentUser();
        List<VocabularyDTO> vocabularyDTOs = converterUtils.convertList(
                vocabularyConversionService.findByOmopReqIsNull(sort), VocabularyDTO.class);

        return new VocabularyToUserVocabularyDTO(user.getLicenses()).convert(vocabularyDTOs);
    }

    @Override
    public DownloadBundle saveBundle(String bundleName, List<Integer> idV4s, AthenaUser currentUser, CDMVersion version) {

        String uuid = UUID.randomUUID().toString();
        LOGGER.info("Ready for save download items for bundle with name: [{}] and uuid: [{}], user id: [{}]",
                bundleName, uuid, currentUser.getId());

        List<Integer> withOmopReqIdV4s = vocabularyConversionService.findByOmopReqIsNotNull()
                .stream()
                .map(VocabularyConversion::getIdV4)
                .collect(toList());
        withOmopReqIdV4s.addAll(idV4s);
        checkBundleVocabularies(withOmopReqIdV4s, currentUser.getId());

        DownloadBundle bundle = buildDownloadBundle(version, uuid, bundleName, currentUser);
        bundle = saveDownloadItems(bundle, withOmopReqIdV4s);
        LOGGER.info("Download items are added, bundle: [{}]", bundle.toString());
        return bundle;
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

    public void checkBundleVocabularies(DownloadBundle bundle, Long userId) {

        List<Integer> bundleVocabularyV4Ids = bundle.getVocabularies().stream()
                .map(e -> e.getVocabularyConversion().getIdV4())
                .collect(toList());
        checkBundleVocabularies(bundleVocabularyV4Ids, userId);
    }

    private DownloadBundle buildDownloadBundle(CDMVersion version, String uuid, String name, AthenaUser user) {

        DownloadBundle bundle = new DownloadBundle();
        bundle.setUserId(user.getId());
        bundle.setCreated(new Date());
        bundle.setUuid(uuid);
        bundle.setCdmVersion(version);
        bundle.setName(name);
        bundle.setStatus(DownloadBundleStatus.PENDING);
        return bundle;
    }

    @Override
    public void saveContent(DownloadBundle bundle, AthenaUser user) {

        asyncVocabularyService.saveContent(bundle, user);
    }

    @Override
    public DownloadBundle saveDownloadItems(DownloadBundle bundle, List<Integer> idV4s) {

        final DownloadBundle result = downloadBundleRepository.save(bundle);
        List<DownloadItem> items = idV4s.stream()
                .map(id -> new DownloadItem(result, new VocabularyConversion(id)))
                .collect(Collectors.toList());

        result.setVocabularies(downloadItemRepository.save(items));
        return result;
    }

    @Override
    public List<DownloadBundleDTO> getDownloadHistory(AthenaUser user) {

        Sort sort = new Sort(Sort.Direction.DESC, "created");
        List<DownloadBundle> history = downloadBundleRepository.findByUserId(user.getId(), sort);
        Set<Long> bundleIds = history.stream()
                .map(b -> b.getId())
                .collect(Collectors.toSet());

        List<DownloadShare> shares = downloadShareRepository.findByDownloadShareIdUserEmail(user.getEmail());
        List<DownloadBundleDTO> sharedDTOs = new ArrayList<>();
        // add shared bundles to list of available downloads
        if (!shares.isEmpty()) {
            for(DownloadShare share: shares) {
                // do not add to shared list dto with author equals to current user
                if(bundleIds.contains(share.getBundleId())) {
                    continue;
                }
                DownloadBundle bundle = downloadBundleRepository.getOne(share.getBundleId());

                DownloadBundleDTO bundleDTO = conversionService.convert(bundle, DownloadBundleDTO.class);
                DownloadShareDTO sharedBundleDTO = conversionService.convert(share, DownloadShareDTO.class);

                bundleDTO.setDownloadShareDTO(sharedBundleDTO);
                sharedDTOs.add(bundleDTO);

                try {
                    checkBundleVocabularies(bundle, user.getId());
                    sharedBundleDTO.setDownloadShareStatus(DownloadShareStatus.OK);
                } catch (LicenseException e) {
                    // if some vocabularies require licence and current user does not have it -
                    // clear list of vocabularies and link to zip file
                    bundleDTO.setVocabularies(Collections.emptyList());
                    bundleDTO.setLink(StringUtils.EMPTY);
                    sharedBundleDTO.setDownloadShareStatus(DownloadShareStatus.LICENCE_REQUIRED);
                    LOGGER.error("user can not access shared bundle due to licence exception");
                }
            }
        }

        List<DownloadBundleDTO> dtos = converterUtils.convertList(history, DownloadBundleDTO.class);

        // Get list of shared bundles where owner is current user
        List<DownloadShare> ownerShares = downloadShareRepository.findByOwnerId(user.getId());
        dtos.stream()
                .forEach(dto -> {
                    String emails = ownerShares.stream()
                            .filter(o -> o.getBundleId() == dto.getId())
                            .map(o -> o.getUserEmail())
                            .collect(Collectors.joining(", "));
                    // if we get list of users with whom this bundle was shared
                    // then the current user is the owner of this bundle
                    if (emails != null && !emails.isEmpty()) {
                        DownloadShareDTO ownerDto = new DownloadShareDTO();
                        ownerDto.setBundleId(dto.getId());
                        ownerDto.setEmail(emails);
                        ownerDto.setOwnerUsername(user.getEmail());
                        dto.setDownloadShareDTO(ownerDto);
                    }
                });

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
    public void restoreDownloadBundle(DownloadBundle downloadBundle) throws PermissionDeniedException {

        AthenaUser currentUser = userService.getCurrentUser();
        checkBundleUser(currentUser, downloadBundle);
        if (!downloadBundle.isArchived()) {
            return;
        }
        checkBundleVocabularies(downloadBundle, currentUser.getId());
        asyncVocabularyService.updateStatus(downloadBundle, DownloadBundleStatus.PENDING);
        saveContent(downloadBundle, currentUser);
        LOGGER.info("Vocabulary restoring is started, bundle id: {}, user id: {}", downloadBundle.getId(),
                currentUser.getId());
    }

    public void checkBundleUser(AthenaUser user, DownloadBundle bundle){

        if (ObjectUtils.notEqual(user.getId(), bundle.getUserId())) {
            throw new PermissionDeniedException();
        }
    }

    public void checkBundleAndSharedUser(AthenaUser user, DownloadBundle bundle){
        if (ObjectUtils.notEqual(user.getId(), bundle.getUserId())) {
            // check whether this bundle was chared with current user
            List<DownloadShare> shares = downloadShareRepository.findByDownloadShareIdBundleId(bundle.getId());
            shares.stream()
                    .filter(s -> user.getEmail().equals(s.getUserEmail()))
                    .findAny()
                    .orElseThrow(() -> new PermissionDeniedException());
        }
    }

    @Override
    public List<License> saveLicenses(AthenaUser user, List<Integer> vocabularyV4Ids, LicenseStatus status) {

        Iterable<License> licenses = vocabularyV4Ids.stream()
                .map(vocabularyV4Id -> new License(user, new VocabularyConversion(vocabularyV4Id), status))
                .collect(toList());
        List<License> result = new ArrayList<>();
        licenseRepository.save(licenses).iterator().forEachRemaining(result::add);

        conceptService.invalidateGraphCache(user.getId());
        return result;
    }

    @Override
    public Long requestLicenses(AthenaUser user, Integer vocabularyV4Id) {

        return saveLicenses(user, Collections.singletonList(vocabularyV4Id), PENDING).get(0).getId();
    }

    @Override
    public void deleteLicense(Long licenseId) {

        License userLicense = licenseRepository.findOne(licenseId);
        licenseRepository.delete(licenseId);
        conceptService.invalidateGraphCache(userLicense.getUser().getId());
    }

    @Override
    public void acceptLicense(Long id, Boolean accepted) {

        License userLicense = licenseRepository.findOne(id);
        String vocabularyName = userLicense.getVocabularyConversion().getName();
        AthenaUser user = userLicense.getUser();
        if (accepted) {
            userLicense.setStatus(LicenseStatus.APPROVED);
            licenseRepository.save(userLicense);
        } else {
            licenseRepository.delete(id);
        }
        conceptService.invalidateGraphCache(user.getId());
        licenseAcceptanceSender.send(user, accepted, vocabularyName);
    }

    @Override
    public License get(AthenaUser user, Integer vocabularyId) {

        return licenseRepository.findByUserIdAndVocabularyIdV4(user.getId(), vocabularyId);
    }

    @Override
    public License get(Long licenseId) {

        return licenseRepository.findOne(licenseId);
    }

    @Override
    public License get(Long licenseId, String token) {

        return licenseRepository.findByIdAndToken(licenseId, token);
    }


    public void notifyAboutUpdates(Long userId, Integer vocabularyId, boolean notify) {

        Optional<Notification> current = notificationRepository.findByUserIdAndVocabularyV4Id(userId, vocabularyId);
        if (notify == current.isPresent()) {
            return;
        }
        if (notify) {
            notificationRepository.save(new Notification(userId, new VocabularyConversion(vocabularyId)));
        } else {
            notificationRepository.delete(current.get());
        }
    }

    public List<Notification> getNotifications(Long userId) {

        return notificationRepository.findByUserId(userId);
    }

}
