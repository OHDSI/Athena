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

import com.odysseusinc.athena.model.athenav5.RelationshipV5;
import java.util.List;
import javax.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@PersistenceContext(unitName = "athenaEntityManagerFactory")
public interface RelationshipV5Repository extends JpaRepository<RelationshipV5, Integer> {

    @Query(nativeQuery = true,
            value = "SELECT DISTINCT r.* FROM relationship r "
                    + "JOIN concept_relationship cr ON cr.relationship_id = r.relationship_id "
                    + "WHERE cr.concept_id_1 = :conceptId "
                    + "AND CURRENT_DATE BETWEEN cr.valid_start_date AND cr.valid_end_date")
    List<RelationshipV5> findRelationships(@Param("conceptId") Long conceptId);
}