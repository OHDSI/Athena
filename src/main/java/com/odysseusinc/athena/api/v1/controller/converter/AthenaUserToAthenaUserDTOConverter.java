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

import com.odysseusinc.athena.api.v1.controller.dto.AthenaUserDTO;
import com.odysseusinc.athena.api.v1.controller.dto.BaseAthenaUserWithEmailDTO;
import com.odysseusinc.athena.model.security.AthenaUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class AthenaUserToAthenaUserDTOConverter implements Converter<AthenaUser, AthenaUserDTO> {

    private GenericConversionService conversionService;

    @Autowired
    public AthenaUserToAthenaUserDTOConverter(GenericConversionService conversionService) {

        conversionService.addConverter(this);
        this.conversionService = conversionService;
    }

    @Override
    public AthenaUserDTO convert(AthenaUser athenaUser) {

        BaseAthenaUserWithEmailDTO baseUser = conversionService.convert(athenaUser, BaseAthenaUserWithEmailDTO.class);

        AthenaUserDTO dto = new AthenaUserDTO(baseUser);
        dto.setAccountNonExpired(athenaUser.getAccountNonExpired());
        dto.setAccountNonLocked(athenaUser.getAccountNonLocked());
        dto.setAuthorities(athenaUser.getAuthorities());
        dto.setCredentialsNonExpired(athenaUser.getCredentialsNonExpired());
        dto.setEnabled(athenaUser.getEnabled());
        dto.setOrigin(athenaUser.getOrigin());
        dto.setRoles(athenaUser.getRoles());
        dto.setUsername(athenaUser.getUsername());
        return dto;
    }
}
