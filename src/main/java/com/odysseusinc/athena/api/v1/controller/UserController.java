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

package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.converter.ConverterUtils;
import com.odysseusinc.athena.api.v1.controller.dto.AthenaUserDTO;
import com.odysseusinc.athena.api.v1.controller.dto.BaseAthenaUserWithEmailDTO;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.impl.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@Tag(name = "UserController")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GenericConversionService conversionService;
    private final UserService userService;

    private final ConverterUtils converterUtils;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    public UserController(
            GenericConversionService conversionService,
            UserService userService,
            ConverterUtils converterUtils) {

        this.conversionService = conversionService;
        this.userService = userService;
        this.converterUtils = converterUtils;
    }

    @GetMapping(value = "/me")
    public ResponseEntity<AthenaUserDTO> me(Principal principal) throws PermissionDeniedException {

        final AthenaUser user = userService.getUser(principal);

        AthenaUserDTO dto = conversionService.convert(user, AthenaUserDTO.class);
        return new ResponseEntity<>(dto, OK);
    }

    @GetMapping(value = "/suggest")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<List<BaseAthenaUserWithEmailDTO>> suggest(@RequestParam("query") String query) {

        List<AthenaUser> users = userService.suggest(query);
        List<BaseAthenaUserWithEmailDTO> dtos = converterUtils.convertList(
                users,
                BaseAthenaUserWithEmailDTO.class
        );
        return new ResponseEntity<>(dtos, OK);
    }
}
