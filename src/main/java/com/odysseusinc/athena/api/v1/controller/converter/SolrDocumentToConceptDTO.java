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
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package com.odysseusinc.athena.api.v1.controller.converter;

import static com.odysseusinc.athena.api.v1.controller.dto.ConceptDTO.builder;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptDTO;
import org.apache.solr.common.SolrDocument;

public class SolrDocumentToConceptDTO {

    private SolrDocumentToConceptDTO() {

    }

    public static ConceptDTO convert(SolrDocument concept) {

        return builder()
                .setId(Long.parseLong(concept.getFieldValue("concept_id").toString()))
                .setName(concept.getFieldValue("concept_name").toString())
                .setCode(concept.getFieldValue("concept_code").toString())
                .setClassName(concept.getFieldValue("concept_class_id_ci").toString())
                .setDomain(concept.getFieldValue("domain_id_ci").toString())
                .setInvalidReason(concept.getFieldValue("invalid_reason").toString())
                .setStandardConcept(concept.getFieldValue("standard_concept").toString())
                .setVocabulary(concept.getFieldValue("vocabulary_id_ci").toString())
                .build();
    }

}
