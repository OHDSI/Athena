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
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.DownloadShareDTO;
import com.odysseusinc.athena.model.athena.DownloadShare;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
public class DownloadShareToDTO implements Converter<DownloadShare, DownloadShareDTO>, InitializingBean {

    private GenericConversionService conversionService;
    private ConverterUtils converterUtils;

    @Autowired
    public DownloadShareToDTO(GenericConversionService conversionService,
                              UrlBuilder urlBuilder,
                              ConverterUtils converterUtils) {

        this.conversionService = conversionService;
        this.converterUtils = converterUtils;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        conversionService.addConverter(this);
    }

    @Override
    public DownloadShareDTO convert(@NotNull DownloadShare downloadShare) {
        DownloadShareDTO dto = new DownloadShareDTO();
        dto.setBundleId(downloadShare.getBundleId());
        dto.setEmail(downloadShare.getUserEmail());
        dto.setOwnerUsername(downloadShare.getOwnerName());
        return dto;
    }
}
