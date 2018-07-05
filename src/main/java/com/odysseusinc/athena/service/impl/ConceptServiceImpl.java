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
import com.odysseusinc.athena.model.athenav5.ConceptAncestor;
import com.odysseusinc.athena.model.athenav5.ConceptAncestorRelationV5;
import com.odysseusinc.athena.model.athenav5.ConceptAncestor_;
import com.odysseusinc.athena.model.athenav5.ConceptRelationship;
import com.odysseusinc.athena.model.athenav5.ConceptRelationship_;
import com.odysseusinc.athena.model.athenav5.ConceptV5;
import com.odysseusinc.athena.model.athenav5.RelationshipV5;
import com.odysseusinc.athena.model.athenav5.SolrConcept;
import com.odysseusinc.athena.model.athenav5.SolrConcept_;
import com.odysseusinc.athena.model.athenav5.VocabularyV5_;
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
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Autowired
    @Qualifier("athenaV5EntityManagerFactory")
    private EntityManager entityManager;
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

        return conceptRepository.findOne(id);
    }

    @Override
    public String getSearchedConceptsFileName() {

        return csvFileName;
    }

    private List<ConceptAncestorRelationV5> findOneLevelDescendants(Long conceptId, List<String> v5Ids) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ConceptAncestorRelationV5> query = builder.createQuery(ConceptAncestorRelationV5.class);
        Root<ConceptAncestor> root = query.from(ConceptAncestor.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(root.get(ConceptAncestor_.id), conceptId));
        predicates.add(builder.equal(root.get(ConceptAncestor_.minLevelsOfSeparation), 1));

        Path<SolrConcept> descendantConcept = root.get(ConceptAncestor_.descendantConcept);
        Path<String> vocabularyIdPath = descendantConcept.get(SolrConcept_.vocabulary).get(VocabularyV5_.id);

        if (!v5Ids.isEmpty()) {
            predicates.add(vocabularyIdPath.in(v5Ids).not());
        }
        query = query.where(predicates.toArray(new Predicate[predicates.size()]));

        query = query.select(builder.construct(ConceptAncestorRelationV5.class,
                descendantConcept.get(SolrConcept_.id),
                descendantConcept.get(SolrConcept_.name),
                descendantConcept.get(SolrConcept_.conceptClassId),
                vocabularyIdPath,
                root.get(ConceptAncestor_.id),
                descendantConcept.get(SolrConcept_.id),
                root.get(ConceptAncestor_.minLevelsOfSeparation).alias("weight"),
                builder.literal(false).alias("is_current"),
                builder.literal(-1).alias("depth")
        ));

        return entityManager.createQuery(query).getResultList();
    }

    private List<ConceptAncestorRelationV5> getRelationsFromRepositoryForCurrentUser(Long conceptId, Integer depth) {

        List<String> v5Ids = conversionService.getUnavailableVocabularies();

        List<ConceptAncestorRelationV5> relations;
        if (v5Ids.isEmpty()) {
            relations = ancestorRelationV5Repository.findAncestors(conceptId, depth);
        } else {
            relations = ancestorRelationV5Repository.findAncestors(conceptId, depth, v5Ids);
        }
        relations.addAll(findOneLevelDescendants(conceptId, v5Ids));
        return relations;
    }

    @Override
    @LicenseCheck
    public List<ConceptAncestorRelationV5> getRelations(Long conceptId, Integer depth) throws ExecutionException {

        Long userId = userService.getCurrentUserId();
        return graphCache.get(new RelationGraphParameter(conceptId, userId, depth));
    }

    @Override
    @LicenseCheck
    public List<ConceptRelationship> getConceptRelationships(Long conceptId, String relationshipId, Boolean onlyStandard) {

        List<String> vocabularyV5Ids = conversionService.getUnavailableVocabularies();

        return conceptRelationshipV5Repository.findAll((root, query, cb) -> {

            Predicate predicate = cb.equal(root.get(ConceptRelationship_.sourceConceptId), conceptId);
            if (onlyStandard) {
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
