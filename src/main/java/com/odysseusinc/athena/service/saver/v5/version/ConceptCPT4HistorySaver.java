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

import java.util.Collections;
import java.util.List;

@Service
public class ConceptCPT4HistorySaver extends HistorySaver implements SaverV5History {

    @Override
    public boolean containCpt4(List ids) {

        return ids.contains("CPT4");
    }

    @Override
    public boolean includedInBundle(List ids) {

        return containCpt4(ids);
    }

    @Override
    public List filter(List ids) {

        return containCpt4(ids) ? Collections.singletonList("CPT4") : Collections.EMPTY_LIST;
    }

    @Override
    public String fileName() {

        return "CONCEPT_CPT4.csv";
    }

    @Override
    protected String query() {

        return "SELECT " +
                "  concept_id, " +
                "  '' as concept_name, " +
                "  domain_id, " +
                "  vocabulary_id, " +
                "  concept_class_id, " +
                "  standard_concept, " +
                "  concept_code, " +
                "  valid_start_date, " +
                "  valid_end_date, " +
                "  invalid_reason " +
                "FROM concept_history " +
                "WHERE vocabulary_id IN (:vocabularyIds) " +
                "  AND version = :version";
    }

}