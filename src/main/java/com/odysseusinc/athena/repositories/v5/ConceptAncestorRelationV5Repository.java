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

import com.odysseusinc.athena.model.athenav5.ConceptAncestorRelationV5;
import java.util.List;
import javax.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@PersistenceContext(unitName = "athenaEntityManagerFactory")
public interface ConceptAncestorRelationV5Repository extends JpaRepository<ConceptAncestorRelationV5, Long> {
    @Query(value = "-- In concept_ancestor table all concepts are valid by default (business rule), "
            + "-- so no need in explicit check \n"
            + "WITH RECURSIVE r(depth) AS ( "
            + "  SELECT "
            + "    0   AS depth, "
            + "    ca.ancestor_concept_id, "
            + "    ca.descendant_concept_id, "
            + "    1   AS weight, "
            + "    CASE "
            + "    WHEN ca.ancestor_concept_id = ca.descendant_concept_id "
            + "      THEN 1 "
            + "    ELSE 0 "
            + "    END AS is_current "
            + "  FROM "
            + "    concept_ancestor ca "
            + "  WHERE "
            + "    ca.descendant_concept_id = ?1 "
            + "    AND "
            + "    ca.min_levels_of_separation = 0 "
            + "  UNION "
            + "  SELECT "
            + "    depth + 1 AS depth, "
            + "    ca.ancestor_concept_id, "
            + "    ca.descendant_concept_id, "
            + "    1         AS weight, "
            + "    0         AS is_current "
            + "  FROM "
            + "    concept_ancestor ca "
            + "    JOIN r ON "
            + "             ca.descendant_concept_id = r.ancestor_concept_id "
            + "  WHERE "
            + "    ca.min_levels_of_separation = 1 "
            + "    AND depth < ?2 "
            + ") "
            + "SELECT"
            + "  r.*,"
            + "  c.concept_id,"
            + "  c.concept_name,"
            + "  c.vocabulary_id,"
            + "  c.concept_class_id "
            + "FROM r"
            + "  JOIN concepts_view c ON c.concept_id = r.ancestor_concept_id "
            + "ORDER BY concept_id, ancestor_concept_id, descendant_concept_id;", nativeQuery = true)
    List<ConceptAncestorRelationV5> findAncestors(Long conceptId, Integer depth);

    @Query(nativeQuery = true,
            value = "SELECT ca.ancestor_concept_id, ca.descendant_concept_id, ca.min_levels_of_separation AS weight, "
                    + " c.concept_id, c.concept_name, c.vocabulary_id, c.concept_class_id, 0 AS is_current, -1 as depth "
                    + " FROM "
                    + " concept_ancestor ca "
                    + " JOIN "
                    + " concepts_view c ON c.concept_id = ca.descendant_concept_id "
                    + " WHERE "
                    + " ancestor_concept_id = ?1 "
                    + " AND "
                    + " min_levels_of_separation = 1")
    List<ConceptAncestorRelationV5> findOneLevelDescendants(Long conceptId);
}
