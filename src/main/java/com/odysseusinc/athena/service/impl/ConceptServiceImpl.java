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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import com.odysseusinc.athena.service.VocabularyConversionService;
import com.odysseusinc.athena.service.aspect.LicenseCheck;
import com.odysseusinc.athena.service.graph.RelationGraphParameter;
import org.apache.solr.common.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true, transactionManager = "athenaV5TransactionManager")
public class ConceptServiceImpl implements ConceptService {

    @Autowired
    private ConceptV5Repository conceptRepository;
    @Autowired
    private ConceptAncestorRelationV5Repository ancestorRelationV5Repository;
    @Autowired
    private ConceptRelationshipV5Repository conceptRelationshipV5Repository;
    @Autowired
    private RelationshipV5Repository relationshipV5Repository;
    @Autowired
    private VocabularyConversionService conversionService;
    @Autowired
    private UserService userService;
    @Autowired
    @Qualifier("athenaV5EntityManagerFactory")
    private EntityManager entityManager;

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
    public boolean hasAnyRelations(Long conceptId) {

        return conceptRelationshipV5Repository.findFirstBySourceConceptIdIsAndTargetConceptIdNot(conceptId, conceptId).isPresent();
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

}
