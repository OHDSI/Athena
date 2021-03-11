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
import com.odysseusinc.athena.api.v1.controller.dto.ConceptDetailsDTO;
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
import com.odysseusinc.athena.util.JsonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.odysseusinc.athena.service.graph.RelationshipGraphFactory.getRelationshipGraph;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "ConceptController")
@RestController
@RequestMapping("/api/v1/concepts")
public class ConceptController {
    private final ConceptService conceptService;
    private final GenericConversionService conversionService;
    private final ConverterUtils converterUtils;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public ConceptController(ConceptService conceptService,
                             GenericConversionService conversionService,
                             ConverterUtils converterUtils) {

        this.conceptService = conceptService;
        this.conversionService = conversionService;
        this.converterUtils = converterUtils;
    }

    @Operation(summary = "Get concept details.")
    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<ConceptDetailsDTO>> getConcept(@PathVariable Long id) {

        ConceptV5 concept = conceptService.getByIdWithLicenseCheck(id);
        Resource<ConceptDetailsDTO> conceptResource = new Resource<>(
                conversionService.convert(concept, ConceptDetailsDTO.class),
                linkTo(methodOn(ConceptController.class).getConcept(id)).withSelfRel());
        return new ResponseEntity<>(conceptResource, OK);
    }

    @Operation(summary = "Get relations for concept")
    @GetMapping(value = "/{id}/relations", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ConceptAncestorRelationsDTO> relationsGraph(
            @PathVariable Long id, @RequestParam(name = "depth", defaultValue = "10") Integer depth,
            @RequestParam(name = "zoomLevel", defaultValue = "4") Integer zoomLevel) throws ExecutionException {

        List<ConceptAncestorRelationV5> relations = conceptService.getRelations(id, depth);
        ConceptAncestorRelationsDTO dto = getRelationshipGraph(zoomLevel, id, conversionService, relations).build();
        return new ResponseEntity<>(dto, OK);
    }

    @Operation(summary = "Is any relations for the concept exists")
    @GetMapping(value = "/{id}/relations/any", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<JsonResult> hasAnyRelations(
            @PathVariable Long id) {

        final boolean hasRelations = conceptService.hasAnyRelations(id);
        final JsonResult<Boolean> hasRelationsResult = new JsonResult<>();
        hasRelationsResult.setResult(hasRelations);
        return ResponseEntity.ok(hasRelationsResult);
    }

    @Operation(summary = "Get concept relationships")
    @GetMapping(value = "/{id}/relationships", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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

    @GetMapping(value = "/{id}/relationships/options", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Set<RelationshipDTO>> relationshipOptions(@PathVariable Long id) {

        List<RelationshipV5> options = conceptService.getAllRelationships(id);

        Set<RelationshipDTO> result = options.stream()
                .map(r -> conversionService.convert(r, RelationshipDTO.class))
                .collect(Collectors.toSet());
        return new ResponseEntity<>(result, OK);
    }
}
