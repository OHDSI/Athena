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
public class ConceptAncestorHistorySaver extends HistorySaver implements SaverV5History {

    @Override
    public String fileName() {

        return "CONCEPT_ANCESTOR.csv";
    }

    @Override
    protected String query() {

        return "SELECT " +
                "  ancestor_concept_id, " +
                "  descendant_concept_id, " +
                "  min_levels_of_separation, " +
                "  max_levels_of_separation " +
                "FROM concept_ancestor_history " +
                "WHERE (ancestor_vocabulary_id IN (:vocabularyIds) OR descendant_vocabulary_id IN (:vocabularyIds))" +
                "  AND version = :version ";

    }
}
