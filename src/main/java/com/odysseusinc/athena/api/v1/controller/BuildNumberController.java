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

import com.odysseusinc.arachne.commons.api.v1.dto.CommonBuildNumberDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
public class BuildNumberController {

    @Value("${build.number}")
    private String buildNumber;
    @Value("${build.id}")
    private String buildId;
    @Value("${project.version}")
    private String projectVersion;

    @ApiOperation(value = "Get build number.", hidden = true)
    @RequestMapping(value = "/api/v1/build-number", method = RequestMethod.GET)
    public CommonBuildNumberDTO buildNumber(HttpServletRequest request) {

        CommonBuildNumberDTO buildNumberDTO = new CommonBuildNumberDTO();
        buildNumberDTO.setBuildNumber(buildNumber);
        buildNumberDTO.setBuildId(buildId);
        buildNumberDTO.setProjectVersion(projectVersion);
        return buildNumberDTO;
    }

}
