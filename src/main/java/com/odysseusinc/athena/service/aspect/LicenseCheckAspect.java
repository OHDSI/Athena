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

package com.odysseusinc.athena.service.aspect;

import static java.lang.String.format;

import com.odysseusinc.athena.exceptions.LicenseException;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import com.odysseusinc.athena.model.athenav5.ConceptV5;
import com.odysseusinc.athena.repositories.v5.ConceptV5Repository;
import com.odysseusinc.athena.service.VocabularyConversionService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LicenseCheckAspect {

    @Autowired
    private ConceptV5Repository conceptRepository;
    @Autowired
    private VocabularyConversionService vocabularyConversionService;

    @Before(value = "@annotation(com.odysseusinc.athena.service.aspect.LicenseCheck) && args(conceptId,..)")
    public void check(long conceptId) throws PermissionDeniedException {

        List<VocabularyConversion> unavailable = vocabularyConversionService.getUnavailableVocabularyConversions();
        ConceptV5 conceptV5 = conceptRepository.findOne(conceptId);

        String conceptVocabularyIdV5 = conceptV5.getVocabulary().getId();

        Optional<VocabularyConversion> protectedLicense = unavailable.stream()
                .filter(e -> e.getIdV5().equals(conceptVocabularyIdV5))
                .findAny();

        if (protectedLicense.isPresent()) {
            Integer idV4 = protectedLicense.get().getIdV4();
            throw new LicenseException(
                    format("User must have license for the vocabulary %d ", idV4),
                    Collections.singletonList(idV4));
        }
    }
}

