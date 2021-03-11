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
 * Created: March 24, 2020
 *
 */

package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchResultDTO;
import com.odysseusinc.athena.service.ConceptService;
import com.odysseusinc.athena.service.SearchService;
import com.odysseusinc.athena.service.checker.CheckResult;
import com.odysseusinc.athena.service.checker.LimitChecker;
import com.odysseusinc.athena.service.search.SearchOverviewStatisticsLoader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@Tag(name = "ConceptSearchController")
@RestController
@RequestMapping("/api/v1/concepts")
public class ConceptSearchController {
    private static final Logger log = LoggerFactory.getLogger(ConceptSearchController.class);

    private final ConceptService conceptService;
    private final LimitChecker checker;
    private final SearchOverviewStatisticsLoader searchOverviewStatisticsLoader;
    private final SearchService searchService;

    @Autowired
    public ConceptSearchController(ConceptService conceptService, LimitChecker checker, SearchOverviewStatisticsLoader searchOverviewStatisticsLoader, SearchService searchService) {

        this.conceptService = conceptService;
        this.checker = checker;
        this.searchOverviewStatisticsLoader = searchOverviewStatisticsLoader;
        this.searchService = searchService;
    }

    @Operation(summary = "Search concepts.")
    @GetMapping
    public ResponseEntity<ConceptSearchResultDTO> search(
            @ModelAttribute ConceptSearchDTO searchDTO,
            @RequestParam(required = false, name = "debug", defaultValue = "false") boolean debug
    )
            throws IOException, SolrServerException {

        if (StringUtils.isNotBlank(searchDTO.getQuery())) {
            JSONObject obj = new JSONObject(searchDTO);
            log.trace("{}", obj);
        }
        return ResponseEntity.ok(searchService.search(searchDTO, debug));
    }

    @Operation(summary = "Show search ")
    @GetMapping("/terms-count")
    public ResponseEntity<Map<String, Long>> showFacetsData() {

        final Map<String, Long> domainTermsStatistics = searchOverviewStatisticsLoader.getDomainTermsStatistics();
        return ResponseEntity.ok(domainTermsStatistics);

    }

    @Operation(summary = "Download csv file.")
    @GetMapping(value = "/download/csv")
    public void downloadCsv(@ModelAttribute ConceptSearchDTO searchDTO, HttpServletResponse response)
            throws IOException, SolrServerException {

        CheckResult checkResult = checker.check(searchDTO);
        if (!checkResult.isSuccess()) {
            response.sendError(SC_BAD_REQUEST, checkResult.getDescription());
            return;
        }
        response.setContentType("text/csv");
        String headerValue = String.format("attachment; filename=\"%s\"", conceptService.getSearchedConceptsFileName());
        response.setHeader("Content-Disposition", headerValue);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        searchService.generateCSV(searchDTO, response.getOutputStream());
        response.flushBuffer();
    }
}
