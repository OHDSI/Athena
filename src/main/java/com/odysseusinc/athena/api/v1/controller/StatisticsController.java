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


package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.dto.DownloadHistoryDTO;
import com.odysseusinc.athena.service.DownloadsHistoryService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collection;

import static com.odysseusinc.athena.model.common.AthenaConstants.COMMON_DATE_FORMATTER;

@Api
@RestController
@RequestMapping(value = "/api/v1/statistics")
public class StatisticsController {

    private final DownloadsHistoryService downloadsHistoryService;

    public StatisticsController(DownloadsHistoryService downloadsHistoryService) {

        this.downloadsHistoryService = downloadsHistoryService;
    }


    @GetMapping
    @Secured("ROLE_ADMIN")
    public Collection<DownloadHistoryDTO> getStatistics(@RequestParam(name = "from") String from,
                                                        @RequestParam("to") String to,
                                                        @RequestParam(name = "keywords", defaultValue = "", required = false) String keywords,
                                                        @RequestParam(name = "licensedOnly", defaultValue = "false", required = false) Boolean licensedOnly) {

        return downloadsHistoryService.retrieveStatistics(parseDateParameter(from).atStartOfDay(),
                parseDateParameter(to).plusDays(1).atStartOfDay(),
                licensedOnly,
                StringUtils.split(keywords.toLowerCase()));
    }

    @GetMapping("/csv")
    public void getStatisticsCSV(@RequestParam(name = "from") String from,
                                 @RequestParam("to") String to,
                                 @RequestParam(name = "keywords", defaultValue = "", required = false) String keywords,
                                 @RequestParam(name = "licensedOnly", defaultValue = "false", required = false) Boolean licensedOnly,
                                 HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        String headerValue = String.format("attachment; filename=\"%s\"", "statistics.csv");
        response.setHeader("Content-Disposition", headerValue);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        final Collection<DownloadHistoryDTO> statistics = getStatistics(from, to, keywords, licensedOnly);
        downloadsHistoryService.generateCSV(statistics, response.getOutputStream());
        response.flushBuffer();
    }

    private LocalDate parseDateParameter(String dateValue) {

        return LocalDate.parse(dateValue, COMMON_DATE_FORMATTER);
    }


}
