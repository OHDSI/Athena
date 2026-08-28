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

package com.odysseusinc.athena.service.saver.v5;

import com.odysseusinc.athena.service.saver.CSVSaver;
import com.odysseusinc.athena.service.saver.SaverV5;
import org.springframework.stereotype.Service;

/**
 * Both concepts must be in the selection, exactly as {@code ConceptRelationshipSaver}
 * requires — this file annotates that one, and a row here without its counterpart there
 * violates the foreign key the upstream DDL declares.
 */
@Service
public class ConceptRelationshipMetadataV5Saver extends CSVSaver implements SaverV5 {

    @Override
    protected String requiredTable() {

        return "concept_relationship_metadata";
    }

    @Override
    public String fileName() {

        return "CONCEPT_RELATIONSHIP_METADATA.csv";
    }

    @Override
    protected String query() {

        return "SELECT crm.* FROM concept_relationship_metadata crm WHERE EXISTS "
                + "(SELECT 1 FROM CONCEPT WHERE CONCEPT_ID = crm.CONCEPT_ID_1 AND VOCABULARY_ID IN (?)) "
                + "AND EXISTS (SELECT 1 FROM CONCEPT WHERE CONCEPT_ID = crm.CONCEPT_ID_2 AND VOCABULARY_ID IN (?))";
    }
}
