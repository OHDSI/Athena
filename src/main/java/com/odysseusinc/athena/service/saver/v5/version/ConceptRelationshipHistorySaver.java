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
 * Author: Yaroslav Molodkov
 * Created: December 7, 2023
 *
 */

package com.odysseusinc.athena.service.saver.v5.version;

import com.odysseusinc.athena.service.saver.SaverV5History;
import org.springframework.stereotype.Service;

@Service
public class ConceptRelationshipHistorySaver extends HistorySaver implements SaverV5History {

    @Override
    public String fileName() {

        return "CONCEPT_RELATIONSHIP.csv";
    }

    @Override
    protected String query() {
         // TODO Dev: Currently, the function only returns half of all relations; there should be duplicates with the reverse direction.
        return "SELECT " +
                "  concept_id_1, " +
                "  concept_id_2, " +
                "  relationship_id, " +
                "  valid_start_date, " +
                "  valid_end_date, " +
                "  invalid_reason " +
                "FROM concept_relationship_history " +
                "WHERE (vocabulary_id_1 IN( :vocabularyIds) OR vocabulary_id_2 IN (:vocabularyIds)) " +
                "  AND version = :version";
    }
}
