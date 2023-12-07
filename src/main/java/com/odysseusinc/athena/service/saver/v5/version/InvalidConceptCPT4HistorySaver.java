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

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class InvalidConceptCPT4HistorySaver extends ConceptCPT4HistorySaver {

    @Override
    public boolean includedInBundle(List ids) {

        return false;
    }

    @Override
    public String fileName() {

        return "cpt4_ref.csv";
    }

    @Override
    protected String query() {

        return "SELECT " +
                "  concept_id, " +
                "  concept_name, " +
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
                "  AND version = :version " +
                "  AND valid_end_date <= NOW()";
    }

    @Override
    public List getIds() {

        return Collections.singletonList("CPT4");
    }
}
