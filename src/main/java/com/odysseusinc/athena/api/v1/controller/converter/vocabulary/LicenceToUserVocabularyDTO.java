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

package com.odysseusinc.athena.api.v1.controller.converter.vocabulary;

import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.UserVocabularyDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyDTO;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.util.extractor.LicenseStatus;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class LicenceToUserVocabularyDTO implements Converter<License, UserVocabularyDTO>, InitializingBean {

    private GenericConversionService conversionService;

    @Autowired
    public LicenceToUserVocabularyDTO(GenericConversionService conversionService) {

        this.conversionService = conversionService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        conversionService.addConverter(this);
    }

    @Override
    public UserVocabularyDTO convert(@NotNull License license) {

        UserVocabularyDTO dto = new UserVocabularyDTO(
                conversionService.convert(license.getVocabularyConversion(), VocabularyDTO.class));
        Long id = license.getId();
        dto.setLicenseId(id);
        dto.setAvailable(true);
        dto.setRequestDate(license.getRequestDate());

        LicenseStatus status = license.getStatus();
        dto.setStatus(status);
        if (LicenseStatus.PENDING == status) {
            String token = license.getToken();
            dto.setToken(token);
            dto.setAvailable(false);
        }
        return dto;
    }

}
