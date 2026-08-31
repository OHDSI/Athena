/*
 *
 * Copyright 2026 Odysseus Data Services, Inc. (EPAM Systems company)
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
 * Created: August 31, 2026
 *
 */

package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.converter.AthenaUserToAthenaUserWithEmailDTOConverter;
import com.odysseusinc.athena.api.v1.controller.converter.AthenaUserToBaseAthenaUserDTOConverter;
import com.odysseusinc.athena.api.v1.controller.converter.ConverterUtils;
import com.odysseusinc.athena.api.v1.controller.dto.BaseAthenaUserWithEmailDTO;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.impl.UserService;
import org.junit.Test;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserControllerSuggestionTest {

    @Test
    public void suggestionIncludesEmailAddress() {

        GenericConversionService conversionService = new GenericConversionService();
        new AthenaUserToBaseAthenaUserDTOConverter(conversionService);
        new AthenaUserToAthenaUserWithEmailDTOConverter(conversionService);

        AthenaUser user = new AthenaUser();
        user.setId(40352L);
        user.setFirstName("Anna");
        user.setLastName("Ostropolets");
        user.setEmail("ostropolets@ohdsi.org");

        UserService userService = mock(UserService.class);
        when(userService.suggest("anna os")).thenReturn(Collections.singletonList(user));

        UserController controller = new UserController(
                conversionService,
                userService,
                new RestTemplate(),
                new ConverterUtils(conversionService)
        );

        ResponseEntity<List<BaseAthenaUserWithEmailDTO>> response = controller.suggest("anna os");

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("ostropolets@ohdsi.org", response.getBody().get(0).getEmail());
        assertEquals("Anna", response.getBody().get(0).getFirstName());
        assertEquals("Ostropolets", response.getBody().get(0).getLastName());
    }
}
