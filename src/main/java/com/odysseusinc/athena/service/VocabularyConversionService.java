/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
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
 * Authors: Maria Pozhidaeva
 * Created: May 17, 2018
 *
 */

package com.odysseusinc.athena.service;

import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyDTO;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import java.util.List;
import org.springframework.data.domain.Sort;

public interface VocabularyConversionService {

    List<String> getUnavailableVocabularies() throws PermissionDeniedException;

    List<VocabularyConversion> getUnavailableVocabularyConversions() throws PermissionDeniedException;

    List<VocabularyDTO> getUnavailableVocabularies(Long userId, boolean withoutPending);

    List<VocabularyConversion> findByOmopReqIsNull(Sort sort);

    List<VocabularyConversion> findByOmopReqIsNotNull();
}
