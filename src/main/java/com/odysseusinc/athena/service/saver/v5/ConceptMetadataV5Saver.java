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
 * CDM 5.5 only, so V5 rather than {@code common}. The vocabulary filter mirrors
 * {@code ConceptSynonymSaver}: a metadata row is included exactly when the concept it
 * annotates is, otherwise it dangles against the CONCEPT.csv in the same archive.
 */
@Service
public class ConceptMetadataV5Saver extends CSVSaver implements SaverV5 {

    @Override
    protected String requiredTable() {

        return "concept_metadata";
    }

    @Override
    public String fileName() {

        return "CONCEPT_METADATA.csv";
    }

    @Override
    protected String query() {

        return "SELECT cm.* FROM concept_metadata cm WHERE EXISTS "
                + "(SELECT 1 FROM CONCEPT c "
                + "WHERE cm.CONCEPT_ID = c.CONCEPT_ID AND c.VOCABULARY_ID IN (?))";
    }
}
