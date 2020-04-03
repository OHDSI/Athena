/*
 *
 * Copyright 2020 Odysseus Data Services, inc.
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
 * Authors: Alexandr Cumarav
 * Created: March 31, 2020
 *
 */


package com.odysseusinc.athena.api.v1.controller.converter;

import com.odysseusinc.athena.api.v1.controller.dto.DownloadHistoryDTO;
import com.odysseusinc.athena.model.athena.DownloadHistory;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class DownloadHistoryToDTOConverter implements Converter<DownloadHistory, DownloadHistoryDTO> {

    private final UserService userService;

    @Autowired
    public DownloadHistoryToDTOConverter(GenericConversionService conversionService, UserService userService) {

        this.userService = userService;
        conversionService.addConverter(this);
    }

    @Override
    public DownloadHistoryDTO convert(DownloadHistory downloadHistory) {

        final AthenaUser athenaUser = userService.get(downloadHistory.getUserId());
        DownloadHistoryDTO dto = new DownloadHistoryDTO();
        dto.setUserName(String.format("%s %s", athenaUser.getFirstName(), athenaUser.getLastName()));
        dto.setVocabularyName(downloadHistory.getVocabularyBundle().getName());
        return dto;
    }
}
