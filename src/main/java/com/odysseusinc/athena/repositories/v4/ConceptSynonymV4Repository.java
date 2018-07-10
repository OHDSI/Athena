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

package com.odysseusinc.athena.repositories.v4;

import com.odysseusinc.athena.model.athenav4.ConceptSynonymV4;
import java.util.List;
import javax.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@PersistenceContext(unitName = "athenav4EntityManagerFactory")
public interface ConceptSynonymV4Repository extends CrudRepository<ConceptSynonymV4, Long> {

    @Query(nativeQuery = true,
            value = "select * "
                    + " from concept_synonym WHERE EXISTS (SELECT * FROM CONCEPT c"
                    + " WHERE CONCEPT_ID = c.CONCEPT_ID AND VOCABULARY_ID IN :v4Ids)")
    List<ConceptSynonymV4> findByVersionIds(@Param("v4Ids") List<Long> v4Ids);
}
