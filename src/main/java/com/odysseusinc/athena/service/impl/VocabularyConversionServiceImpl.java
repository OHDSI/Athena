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
 * Created: May 17, 2018
 *
 */

package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.api.v1.controller.converter.ConverterUtils;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyDTO;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import com.odysseusinc.athena.repositories.athena.VocabularyConversionRepository;
import com.odysseusinc.athena.service.VocabularyConversionService;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Transactional
public class VocabularyConversionServiceImpl implements VocabularyConversionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyConversionServiceImpl.class);

    private VocabularyConversionRepository vocabularyConversionRepository;
    private UserService userService;
    private ConverterUtils converterUtils;


    @Autowired
    public VocabularyConversionServiceImpl(VocabularyConversionRepository vocabularyConversionRepository,
                                           UserService userService,
                                           ConverterUtils converterUtils) {

        this.vocabularyConversionRepository = vocabularyConversionRepository;
        this.userService = userService;
        this.converterUtils = converterUtils;

    }

    @Override
    public List<String> getUnavailableVocabularies() throws PermissionDeniedException {

        List<VocabularyConversion> conversions = getUnavailableVocabularyConversions();
        return conversions.stream().map(VocabularyConversion::getIdV5).collect(Collectors.toList());
    }

    @Override
    public List<VocabularyConversion> getUnavailableVocabularyConversions() throws PermissionDeniedException {

        List<VocabularyConversion> conversions =
                userService.currentUserExists() ?
                        vocabularyConversionRepository.unavailableVocabularies(userService.getCurrentUser().getId(), false) :
                        vocabularyConversionRepository.unavailableVocabularies();

        return CollectionUtils.isEmpty(conversions) ? Collections.emptyList() : conversions;
    }

    @Override
    public List<VocabularyDTO> getUnavailableVocabularies(Long userId, boolean withoutPending) {

        return converterUtils.convertList(
                vocabularyConversionRepository.unavailableVocabularies(userId, withoutPending), VocabularyDTO.class);
    }

    @Override
    public List<VocabularyConversion> findByOmopReqIsNull(Sort sort) {

        return vocabularyConversionRepository.findByOmopReqIsNull(sort);
    }

    @Override
    public List<VocabularyConversion> findByOmopReqIsNotNull() {

        return vocabularyConversionRepository.findByOmopReqIsNotNull();
    }


}
