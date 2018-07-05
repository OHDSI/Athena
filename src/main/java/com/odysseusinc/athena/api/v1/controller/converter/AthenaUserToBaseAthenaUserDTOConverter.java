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

package com.odysseusinc.athena.api.v1.controller.converter;

import com.odysseusinc.athena.api.v1.controller.dto.BaseAthenaUserDTO;
import com.odysseusinc.athena.model.security.AthenaUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class AthenaUserToBaseAthenaUserDTOConverter implements Converter<AthenaUser, BaseAthenaUserDTO> {

    @Autowired
    public AthenaUserToBaseAthenaUserDTOConverter(GenericConversionService conversionService) {

        conversionService.addConverter(this);
    }

    @Override
    public BaseAthenaUserDTO convert(AthenaUser athenaUser) {

        BaseAthenaUserDTO dto = new BaseAthenaUserDTO();
        dto.setId(athenaUser.getId());
        dto.setFirstName(athenaUser.getFirstName());
        dto.setLastName(athenaUser.getLastName());
        dto.setMiddleName(athenaUser.getMiddleName());
        return dto;
    }
}
