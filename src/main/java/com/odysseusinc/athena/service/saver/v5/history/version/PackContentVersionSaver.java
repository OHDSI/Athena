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

@Service
public class PackContentVersionSaver extends HistorySaver implements SaverV5History {

    @Override
    public String fileName() {

        return "PACK_CONTENT.csv";
    }

    @Override
    protected String query() {

        return "SELECT " +
                "  pack_concept_id, " +
                "  drug_concept_id, " +
                "  amount, " +
                "  box_size " +
                "FROM pack_content_history " +
                "WHERE vocabulary_history_id = ANY (get_vocabulary_history_ids(:vocabularyArr, :version)) " +
                "  AND version = :version";
    }
}
