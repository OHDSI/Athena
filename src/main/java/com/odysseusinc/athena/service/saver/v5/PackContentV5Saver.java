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
 * Filtered on the pack, the way {@code DrugStrengthSaver} filters on the drug — the pack
 * is what the row is about, and the contained drug follows it into the archive.
 */
@Service
public class PackContentV5Saver extends CSVSaver implements SaverV5 {

    @Override
    protected String requiredTable() {

        return "pack_content";
    }

    @Override
    public String fileName() {

        return "PACK_CONTENT.csv";
    }

    @Override
    protected String query() {

        return "SELECT pack_content.* "
                + " FROM pack_content INNER JOIN CONCEPT ON PACK_CONCEPT_ID = CONCEPT_ID"
                + " WHERE VOCABULARY_ID IN (?)";
    }
}
