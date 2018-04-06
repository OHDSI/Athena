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

package com.odysseusinc.athena.repositories.v5;

import com.odysseusinc.athena.model.athenav5.ConceptRelationship;
import java.util.List;
import javax.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@PersistenceContext(unitName = "athenaEntityManagerFactory")
public interface ConceptRelationshipV5Repository
        extends JpaRepository<ConceptRelationship, Long> {

    List<ConceptRelationship> findBySourceConceptIdAndRelationshipId(Long conceptId, String relationshipId);

    List<ConceptRelationship> findBySourceConceptId(Long conceptId);

    List<ConceptRelationship> findBySourceConceptIdAndStandardLike(Long conceptId, String standard);

    List<ConceptRelationship> findBySourceConceptIdAndRelationshipIdAndStandardLike(
            Long conceptId, String relationshipId, String standard);
}
