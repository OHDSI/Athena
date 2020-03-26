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

package com.odysseusinc.athena.repositories.v5;

import com.odysseusinc.athena.model.athenav5.ConceptV5;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConceptV5Repository extends JpaRepository<ConceptV5, Long> {

    @Query(nativeQuery = true,
            value = "SELECT con2.* "
                    + "FROM concept_relationship cr "
                    + "JOIN concept con ON con.concept_id = cr.concept_id_1 "
                    + "JOIN concept con2 ON con2.concept_id = cr.concept_id_2 "
                    + "WHERE "
                    + "CONCEPT_ID_1 = :conceptId "
                    + "AND con.concept_id != con2.concept_id "
                    + "AND con2.invalid_reason IS NULL "
                    + "AND RELATIONSHIP_ID IN ('Maps to', 'Concept replaced by') "
                    + "AND con.VOCABULARY_ID IN :vocabularyIds "
                    + "AND con2.VOCABULARY_ID IN :vocabularyIds")
    ConceptV5 findReplacedBy(@Param("conceptId") Long conceptId,
                                   @Param("vocabularyIds") List<String> vocabularyIds);
}