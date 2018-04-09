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
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package com.odysseusinc.athena.api.v1.controller;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserRegistrationDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.athena.api.v1.controller.converter.ConverterUtils;
import com.odysseusinc.athena.api.v1.controller.dto.AthenaUserDTO;
import com.odysseusinc.athena.api.v1.controller.dto.BaseAthenaUserDTO;
import com.odysseusinc.athena.api.v1.controller.dto.RemindPasswordDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ResetPasswordDTO;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.impl.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Api
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Value("${athena.url}")
    private String athenaUrl;

    @Value("${arachne.portal.url}")
    private String arachneUrl;

    @Value("${arachne.portal.professionalTypesPath}")
    private String professionalTypesPath;

    @Value("${arachne.portal.registerPath}")
    private String registerPath;

    @Value("${arachne.portal.remindPasswordPath}")
    private String remindPasswordPath;

    @Value("${arachne.portal.resetPasswordPath}")
    private String resetPasswordPath;

    @Value("${arachne.portal.registerToken}")
    private String registerToken;

    @Value("${arachne.portal.remindToken}")
    private String remindToken;

    private String registerCallbackUrl = "/auth/login";

    private final GenericConversionService conversionService;

    private UserService userService;

    private RestTemplate restTemplate;

    private ConverterUtils converterUtils;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    public UserController(
            GenericConversionService conversionService,
            UserService userService,
            RestTemplate restTemplate,
            ConverterUtils converterUtils) throws IOException {

        this.conversionService = conversionService;
        this.userService = userService;
        this.restTemplate = restTemplate;
        this.converterUtils = converterUtils;
    }

    @RequestMapping(value = "/professional-types", method = GET)
    public JsonResult listProfessionalTypes() throws URISyntaxException {

        String uri = UriComponentsBuilder
                .fromUriString(arachneUrl)
                .replacePath(professionalTypesPath)
                .toUriString();

        ResponseEntity<JsonResult> responseEntity = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                JsonResult.class
        );

        return responseEntity.getBody();
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity register(@RequestBody CommonUserRegistrationDTO dto) throws PermissionDeniedException {

        dto.setRegistrantToken(registerToken);
        dto.setCallbackUrl(athenaUrl + registerCallbackUrl);

        JsonResult res = executeRequest(registerPath, dto);

        if (!res.getErrorCode().equals(JsonResult.ErrorCode.NO_ERROR.getCode())) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private JsonResult executeRequest(String path, Object request) {

        String uri = UriComponentsBuilder
                .fromUriString(arachneUrl)
                .replacePath(path)
                .toUriString();

        ResponseEntity<JsonResult> responseEntity = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                new HttpEntity<>(request),
                JsonResult.class
        );
        return responseEntity.getBody();
    }

    @ApiOperation("Request password reset e-mail.")
    @RequestMapping(value = "/remind-password", method = RequestMethod.POST)
    public ResponseEntity remindPassword(@RequestBody @Valid RemindPasswordDTO dto) {

        dto.setRegistrantToken(remindToken);
        dto.setCallbackUrl(athenaUrl);

        JsonResult res = executeRequest(remindPasswordPath, dto);

        if (!res.getErrorCode().equals(JsonResult.ErrorCode.NO_ERROR.getCode())) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiOperation("Reset password for specified e-mail.")
    @RequestMapping(value = "/reset-password", method = RequestMethod.POST)
    public ResponseEntity resetPassword(@RequestBody @Valid ResetPasswordDTO dto)
            throws URISyntaxException, IOException {

        JsonResult res = executeRequest(resetPasswordPath, dto);

        if (!res.getErrorCode().equals(JsonResult.ErrorCode.NO_ERROR.getCode())) {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/me", method = GET)
    public ResponseEntity<AthenaUserDTO> me(Principal principal) throws PermissionDeniedException {

        final AthenaUser user = userService.getUser(principal);

        AthenaUserDTO dto = conversionService.convert(user, AthenaUserDTO.class);
        return new ResponseEntity<>(dto, OK);
    }

    @RequestMapping(value = "/suggest", method = GET)
    public ResponseEntity<List<BaseAthenaUserDTO>> suggest(@RequestParam("query") String query) {

        List<AthenaUser> users = userService.suggest(query);
        List<BaseAthenaUserDTO> dtos = converterUtils.convertList(users, BaseAthenaUserDTO.class);
        return new ResponseEntity<>(dtos, OK);
    }
}
