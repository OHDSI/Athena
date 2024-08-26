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

package com.odysseusinc.athena.service.saver.v5.history.version;

import com.odysseusinc.athena.service.saver.SaverV5History;
import com.odysseusinc.athena.service.saver.v5.history.HistorySaver;
import org.springframework.stereotype.Service;

@Service
public class ConceptRelationshipVersionSaver extends HistorySaver implements SaverV5History {

    @Override
    public String fileName() {

        return "CONCEPT_RELATIONSHIP.csv";
    }

    @Override
    protected String query() {
        return "SELECT " +
                "  concept_id_1, " +
                "  concept_id_2, " +
                "  relationship_id, " +
                "  valid_start_date, " +
                "  valid_end_date, " +
                "  invalid_reason " +
                "FROM concept_relationship_history " +
                "WHERE (vocabulary_history_id_1 = ANY (get_vocabulary_history_ids(:vocabularyArr, :version))  " +
                "  AND  vocabulary_history_id_2 = ANY (get_vocabulary_history_ids(:vocabularyArr, :version))) " +
                "  AND version = :version " +
                "UNION ALL " +
                "SELECT " +
                "  concept_id_2 AS concept_id_1, " +
                "  concept_id_1 AS concept_id_2, " +
                "  reverse_relationship_id AS relationship_id, " +
                "  reverse_valid_start_date AS valid_start_date, " +
                "  valid_end_date, " +
                "  invalid_reason " +
                "FROM concept_relationship_history " +
                "WHERE (vocabulary_history_id_1 = ANY (get_vocabulary_history_ids(:vocabularyArr, :version)) " +
                "  AND vocabulary_history_id_2 = ANY (get_vocabulary_history_ids(:vocabularyArr, :version))) " +
                "  AND version = :version";

    }
}
