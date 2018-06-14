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

import static com.odysseusinc.athena.service.graph.RelationshipGraphFactory.getRelationshipGraph;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.odysseusinc.athena.api.v1.controller.converter.ConverterUtils;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptDetailsDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchResultDTO;
import com.odysseusinc.athena.api.v1.controller.dto.relations.ConceptAncestorRelationsDTO;
import com.odysseusinc.athena.api.v1.controller.dto.relations.ConceptRelationshipDTO;
import com.odysseusinc.athena.api.v1.controller.dto.relations.GroupedConceptRelationshipDTO;
import com.odysseusinc.athena.api.v1.controller.dto.relations.GroupedConceptRelationshipListDTO;
import com.odysseusinc.athena.api.v1.controller.dto.relations.RelationshipDTO;
import com.odysseusinc.athena.model.athenav5.ConceptAncestorRelationV5;
import com.odysseusinc.athena.model.athenav5.ConceptRelationship;
import com.odysseusinc.athena.model.athenav5.ConceptV5;
import com.odysseusinc.athena.model.athenav5.RelationshipV5;
import com.odysseusinc.athena.service.ConceptService;
import com.odysseusinc.athena.service.checker.CheckResult;
import com.odysseusinc.athena.service.checker.LimitChecker;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping(value = "/api/v1/concepts")
public class ConceptController {
    private final ConceptService conceptService;
    private final LimitChecker checker;
    private final GenericConversionService conversionService;
    private ConverterUtils converterUtils;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public ConceptController(ConceptService conceptService,
                             LimitChecker checker,
                             GenericConversionService conversionService,
                             ConverterUtils converterUtils) {

        this.conceptService = conceptService;
        this.checker = checker;
        this.conversionService = conversionService;
        this.converterUtils = converterUtils;
    }

    @ApiOperation("Get concept details.")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<ConceptDetailsDTO>> getConcept(@PathVariable Long id) {

        ConceptV5 concept = conceptService.getByIdWithLicenseCheck(id);
        Resource<ConceptDetailsDTO> conceptResource = new Resource<>(
                conversionService.convert(concept, ConceptDetailsDTO.class),
                linkTo(methodOn(ConceptController.class).getConcept(id)).withSelfRel());
        return new ResponseEntity<>(conceptResource, OK);
    }

    @ApiOperation("Search concepts.")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<ConceptSearchResultDTO> search(@ModelAttribute ConceptSearchDTO searchDTO)
            throws IOException, SolrServerException {

        return new ResponseEntity<>(conceptService.search(searchDTO), OK);
    }

    @ApiOperation("Download csv file.")
    @RequestMapping(value = "/download/csv", method = RequestMethod.GET)
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

        conceptService.generateCSV(searchDTO, response.getOutputStream());
        response.flushBuffer();
    }

    @ApiOperation("Get relations for concept")
    @RequestMapping(value = "/{id}/relations", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ConceptAncestorRelationsDTO> relationsGraph(
            @PathVariable Long id, @RequestParam(name = "depth", defaultValue = "10") Integer depth,
            @RequestParam(name = "zoomLevel", defaultValue = "4") Integer zoomLevel) throws ExecutionException {

        List<ConceptAncestorRelationV5> relations = conceptService.getRelations(id, depth);
        ConceptAncestorRelationsDTO dto = getRelationshipGraph(zoomLevel, id, conversionService, relations).build();
        return new ResponseEntity<>(dto, OK);
    }

    @ApiOperation("Get concept relationships")
    @RequestMapping(value = "/{id}/relationships", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GroupedConceptRelationshipListDTO> relationships(
            @PathVariable Long id,
            @RequestParam(name = "relationshipId", required = false) String relationshipId,
            @RequestParam(name = "std", required = false, defaultValue = "FALSE") Boolean onlyStandard) {

        List<ConceptRelationship> relationships = conceptService.getConceptRelationships(id, relationshipId,
                onlyStandard);
        List<ConceptRelationshipDTO> resultList = converterUtils.convertList(relationships,
                ConceptRelationshipDTO.class);

        List<GroupedConceptRelationshipDTO> groupedRelationships = resultList.stream()
                .sorted(comparing(ConceptRelationshipDTO::getRelationshipName))
                .collect(groupingBy(ConceptRelationshipDTO::getRelationshipName,
                        mapping((ConceptRelationshipDTO dto) -> dto, toList())))
                .entrySet()
                .stream().map(e -> new GroupedConceptRelationshipDTO(e.getKey(), e.getValue()))
                .sorted(comparing(GroupedConceptRelationshipDTO::getRelationshipName))
                .map(e -> {
                    e.getRelationships().sort(comparing(ConceptRelationshipDTO::getTargetConceptName));
                    return e;
                })
                .collect(toList());
        return new ResponseEntity<>(new GroupedConceptRelationshipListDTO(groupedRelationships, resultList.size()), OK);
    }

    @RequestMapping(value = "/{id}/relationships/options", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Set<RelationshipDTO>> relationshipOptions(@PathVariable Long id) {

        List<RelationshipV5> options = conceptService.getAllRelationships(id);

        Set<RelationshipDTO> result = options.stream()
                .map(r -> conversionService.convert(r, RelationshipDTO.class))
                .collect(Collectors.toSet());
        return new ResponseEntity<>(result, OK);
    }
}
