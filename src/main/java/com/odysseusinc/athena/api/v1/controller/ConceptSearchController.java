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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@Api
@RestController
@RequestMapping(value = "/api/v1/concepts")
public class ConceptSearchController {

    private final LimitChecker checker;
    private final ConceptService conceptService;
    private final SearchService searchService;

    public ConceptSearchController(LimitChecker checker, ConceptService conceptService, SearchService searchService) {

        this.checker = checker;
        this.conceptService = conceptService;
        this.searchService = searchService;
    }

    @ApiOperation("Search concepts.")
    @GetMapping
    public ResponseEntity<ConceptSearchResultDTO> search(@ModelAttribute ConceptSearchDTO searchDTO)
            throws IOException, SolrServerException {

        return new ResponseEntity<>(searchService.search(searchDTO), OK);
    }

    @ApiOperation("Download csv file.")
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
