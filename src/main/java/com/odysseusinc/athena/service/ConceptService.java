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

package com.odysseusinc.athena.service;

import com.odysseusinc.athena.model.athenav5.ConceptAncestorRelationV5;
import com.odysseusinc.athena.model.athenav5.ConceptRelationship;
import com.odysseusinc.athena.model.athenav5.ConceptV5;
import com.odysseusinc.athena.model.athenav5.RelationshipV5;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ConceptService {

    ConceptV5 getByIdWithLicenseCheck(Long conceptId);

    String getSearchedConceptsFileName();

    List<ConceptAncestorRelationV5> getRelations(Long conceptId, Integer depth) throws ExecutionException;

    List<ConceptRelationship> getConceptRelationships(Long conceptId, String relationshipId, Boolean onlyStandard);

    List<RelationshipV5> getAllRelationships(Long conceptId);

    void invalidateGraphCache(Long userId);

    boolean hasAnyRelations(Long conceptId);
}
