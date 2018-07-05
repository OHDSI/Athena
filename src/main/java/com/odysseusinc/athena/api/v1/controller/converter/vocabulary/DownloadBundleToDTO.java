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

import com.odysseusinc.athena.api.v1.controller.converter.ConverterUtils;
import com.odysseusinc.athena.api.v1.controller.converter.UrlBuilder;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadBundleDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyDTO;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.util.DownloadBundleStatus;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class DownloadBundleToDTO implements Converter<DownloadBundle, DownloadBundleDTO>, InitializingBean {

    private GenericConversionService conversionService;
    private UrlBuilder urlBuilder;
    private ConverterUtils converterUtils;

    @Autowired
    public DownloadBundleToDTO(GenericConversionService conversionService,
                               UrlBuilder urlBuilder,
                               ConverterUtils converterUtils) {

        this.conversionService = conversionService;
        this.urlBuilder = urlBuilder;
        this.converterUtils = converterUtils;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        conversionService.addConverter(this);
    }

    @Override
    public DownloadBundleDTO convert(@NotNull DownloadBundle bundle) {

        DownloadBundleDTO dto = new DownloadBundleDTO();

        dto.setDate(bundle.getCreated());
        if (DownloadBundleStatus.READY == bundle.getStatus()) {
            dto.setLink(urlBuilder.downloadVocabulariesLink(bundle.getUuid()));
        }
        dto.setCdmVersion(bundle.getCdmVersion().getValue());
        dto.setName(bundle.getName());
        dto.setId(bundle.getId());
        dto.setStatus(bundle.getStatus());
        List<VocabularyDTO> dtos = converterUtils.convertList(bundle.getVocabulariesWithoutOmopReq(),
                VocabularyDTO.class);
        dto.setVocabularies(dtos);
        return dto;
    }

}
