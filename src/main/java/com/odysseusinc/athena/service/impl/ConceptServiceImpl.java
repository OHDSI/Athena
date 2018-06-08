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

package com.odysseusinc.athena.service.impl;

import static java.util.stream.Collectors.toList;
import static org.apache.solr.common.params.CursorMarkParams.CURSOR_MARK_PARAM;
import static org.apache.solr.common.params.CursorMarkParams.CURSOR_MARK_START;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.odysseusinc.athena.api.v1.controller.converter.ConceptSearchDTOToSolrQuery;
import com.odysseusinc.athena.api.v1.controller.converter.ConceptSearchResultToDTO;
import com.odysseusinc.athena.api.v1.controller.converter.SolrDocumentToConceptDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchResultDTO;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.athenav5.ConceptAncestorRelationV5;
import com.odysseusinc.athena.model.athenav5.ConceptRelationship;
import com.odysseusinc.athena.model.athenav5.ConceptV5;
import com.odysseusinc.athena.model.athenav5.RelationshipV5;
import com.odysseusinc.athena.model.meta.ConceptRelationship_;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.v5.ConceptAncestorRelationV5Repository;
import com.odysseusinc.athena.repositories.v5.ConceptRelationshipV5Repository;
import com.odysseusinc.athena.repositories.v5.ConceptV5Repository;
import com.odysseusinc.athena.repositories.v5.RelationshipV5Repository;
import com.odysseusinc.athena.service.ConceptService;
import com.odysseusinc.athena.service.SolrService;
import com.odysseusinc.athena.service.VocabularyConversionService;
import com.odysseusinc.athena.service.aspect.LicenseCheck;
import com.odysseusinc.athena.service.graph.RelationGraphParameter;
import com.odysseusinc.athena.service.impl.solr.SearchResult;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.util.extractor.ConceptFieldsExtractor;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotNull;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class ConceptServiceImpl implements ConceptService {
    @Autowired
    private ConceptV5Repository conceptRepository;
    @Autowired
    private SolrService solrService;
    @Autowired
    private ConceptSearchDTOToSolrQuery converterToSolrQuery;
    @Autowired
    private ConceptSearchResultToDTO converter;
    @Autowired
    private ConceptAncestorRelationV5Repository ancestorRelationV5Repository;
    @Autowired
    private ConceptRelationshipV5Repository conceptRelationshipV5Repository;
    @Autowired
    private RelationshipV5Repository relationshipV5Repository;
    @Autowired
    private FileHelper fileHelper;
    @Autowired
    private VocabularyConversionService conversionService;
    @Autowired
    private UserService userService;

    @Value("${csv.separator:;}")
    private Character separator;

    @Value("${csv.file.name.searched.concepts}")
    private String csvFileName;

    private LoadingCache<RelationGraphParameter, List<ConceptAncestorRelationV5>> graphCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(2, TimeUnit.HOURS)
            .build(
                    new CacheLoader<RelationGraphParameter, List<ConceptAncestorRelationV5>>() {
                        public List<ConceptAncestorRelationV5> load(@NotNull RelationGraphParameter parameter) {

                            return getRelationsFromRepositoryForCurrentUser(parameter.getConceptId(), parameter.getDepth());
                        }
                    });

    @Override
    public void invalidateGraphCache(Long userId) {

        List<RelationGraphParameter> keys = graphCache.asMap().keySet().stream()
                .filter(e -> userId.equals(e.getUserId()))
                .collect(Collectors.toList());
        graphCache.invalidateAll(keys);
    }

    @Override
    @LicenseCheck
    public ConceptV5 getByIdWithLicenseCheck(Long id) {

        ConceptV5 conceptV5 = conceptRepository.findOne(id);
        if (!checkLicense(conceptV5)) {
            throw new PermissionDeniedException();
        }
        return conceptV5;
    }

    @Override
    public boolean checkLicense(long conceptId) {

        ConceptV5 conceptV5 = conceptRepository.findOne(conceptId);
        return checkLicense(conceptV5);
    }

    private boolean checkLicense(ConceptV5 conceptV5) {

        List<String> v5Ids = conversionService.getUnavailableVocabularies();
        return !v5Ids.contains(conceptV5.getVocabulary().getId());
    }

    @Override
    public String getSearchedConceptsFileName() {

        return csvFileName;
    }

    private List<ConceptAncestorRelationV5> getRelationsFromRepositoryForCurrentUser(Long conceptId, Integer depth) {

        List<String> v5Ids = conversionService.getUnavailableVocabularies();

        List<ConceptAncestorRelationV5> relations;
        if (v5Ids.isEmpty()) {
            relations = ancestorRelationV5Repository.findAncestors(conceptId, depth);
            relations.addAll(ancestorRelationV5Repository.findOneLevelDescendants(conceptId));
        } else {
            relations = ancestorRelationV5Repository.findAncestors(conceptId, depth, v5Ids);
            relations.addAll(ancestorRelationV5Repository.findOneLevelDescendants(conceptId, v5Ids));
        }
        return relations;
    }

    @Override
    @LicenseCheck
    public List<ConceptAncestorRelationV5> getRelations(Long conceptId, Integer depth) throws ExecutionException {

        AthenaUser currentUser = userService.getCurrentUser();
        Long userId = Optional.ofNullable(currentUser.getId()).orElse(null);
        return graphCache.get(new RelationGraphParameter(conceptId, userId, depth));
    }

    @Override
    @LicenseCheck
    public List<ConceptRelationship> getConceptRelationships(Long conceptId, String relationshipId, Boolean standardsOnly) {

        List<String> vocabularyV5Ids = conversionService.getUnavailableVocabularies();

        return conceptRelationshipV5Repository.findAll((root, query, cb) -> {

            Predicate predicate = cb.equal(root.get(ConceptRelationship_.sourceConceptId), conceptId);
            if (standardsOnly) {
                predicate = cb.and(predicate, cb.equal(root.get(ConceptRelationship_.standard), "S"));
            }
            if (!StringUtils.isEmpty(relationshipId)) {
                predicate = cb.and(predicate, cb.like(root.get(ConceptRelationship_.relationshipId), relationshipId));
            }
            if (!vocabularyV5Ids.isEmpty()) {
                predicate = cb.and(predicate,
                        root.get(ConceptRelationship_.targetConceptVocabularyId).in(vocabularyV5Ids).not());
            }
            return predicate;
        });
    }

    @Override
    @LicenseCheck
    public List<RelationshipV5> getAllRelationships(Long id) {

        return relationshipV5Repository.findRelationships(id);
    }

    @Override
    public ConceptSearchResultDTO search(ConceptSearchDTO searchDTO) throws IOException, SolrServerException {

        List<String> v5Ids = conversionService.getUnavailableVocabularies();
        SolrQuery solrQuery = converterToSolrQuery.createQuery(searchDTO, v5Ids);
        QueryResponse solrResponse = solrService.search(solrQuery);
        List<SolrDocument> solrDocumentList = solrResponse.getResults();
        return converter.convert(new SearchResult<>(solrQuery, solrResponse, solrDocumentList), v5Ids);
    }

    @Override
    public void generateCSV(ConceptSearchDTO searchDTO, OutputStream osw) throws IOException, SolrServerException {

        SolrQuery solrQuery = converterToSolrQuery.convertForCursor(searchDTO);
        String cursorMark = CURSOR_MARK_START;
        boolean done = false;
        boolean first = true;

        String name = fileHelper.getTempPath(UUID.randomUUID().toString());
        File temp = new File(name);
        while (!done) {

            try (CSVWriter csvWriter = new AthenaCSVWriter(name, separator)) {
                if (first) {
                    csvWriter.writeNext(new String[]{"Id", "Code", "Name", "Concept Class Id", "Domain", "Vocabulary",
                            "Invalid Reason", "Standard Concept"}, false);
                    first = false;
                }
                solrQuery.set(CURSOR_MARK_PARAM, cursorMark);
                QueryResponse solrResponse = solrService.search(solrQuery);
                List<SolrDocument> solrDocuments = solrResponse.getResults();
                List<ConceptDTO> concepts = solrDocuments.stream()
                        .map(SolrDocumentToConceptDTO::convert)
                        .collect(toList());
                writeAll(csvWriter, concepts);
                String nextCursorMark = solrResponse.getNextCursorMark();
                if (cursorMark.equals(nextCursorMark)) {
                    done = true;
                }
                cursorMark = nextCursorMark;
                csvWriter.flush(true);
            } finally {
                Files.copy(temp.toPath(), osw);
                temp.delete();
            }
        }
    }

    private void writeAll(CSVWriter csvWriter, List<ConceptDTO> concepts) throws IOException {

        ConceptFieldsExtractor extractor = new ConceptFieldsExtractor();
        csvWriter.writeAll(new ArrayList<>(extractor.extractForAll(concepts)));
    }

}
