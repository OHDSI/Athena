/*
 *
 * Copyright 2026 Odysseus Data Services, Inc. (EPAM Systems company)
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
 * Created: August 14, 2026
 *
 */

package com.odysseusinc.athena.service.saver.v5.history.version;

import com.odysseusinc.athena.service.saver.SaverV5History;
import com.odysseusinc.athena.service.saver.v5.history.HistorySaver;
import org.springframework.stereotype.Service;

/**
 * Unlike {@code ConceptRelationshipVersionSaver} there is no reverse half to union in:
 * the source table holds one row per relationship it annotates, whichever direction that
 * relationship was recorded in, and the history table stores it as it stands.
 */
@Service
public class ConceptRelationshipMetadataVersionSaver extends HistorySaver implements SaverV5History {

    @Override
    public String fileName() {

        return "CONCEPT_RELATIONSHIP_METADATA.csv";
    }

    @Override
    protected String query() {

        return "SELECT " +
                "  concept_id_1, " +
                "  concept_id_2, " +
                "  relationship_id, " +
                "  relationship_predicate_id, " +
                "  relationship_group, " +
                "  mapping_source, " +
                "  confidence, " +
                "  mapping_tool, " +
                "  mapper, " +
                "  reviewer " +
                "FROM concept_relationship_metadata_history " +
                "WHERE (vocabulary_history_id_1 = ANY (get_vocabulary_history_ids(:vocabularyArr, :version)) " +
                "  AND vocabulary_history_id_2 = ANY (get_vocabulary_history_ids(:vocabularyArr, :version))) " +
                "  AND version = :version";
    }
}
