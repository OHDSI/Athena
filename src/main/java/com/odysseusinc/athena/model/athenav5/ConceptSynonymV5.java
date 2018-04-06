/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
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

package com.odysseusinc.athena.model.athenav5;

import com.odysseusinc.athena.model.common.EntityV5;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "concept_synonym")
@IdClass(ConceptSynonymV5.class)
public class ConceptSynonymV5 extends EntityV5 implements Serializable {

    @Id
    @NotBlank
    @Column(name = "concept_id")
    private Long id;

    @Id
    @NotBlank
    @Column(name = "concept_synonym_name")
    private String name;

    @Id
    @NotBlank
    @Column(name = "language_concept_id")
    private String languageConceptId;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getLanguageConceptId() {

        return languageConceptId;
    }

    public void setLanguageConceptId(String languageConceptId) {

        this.languageConceptId = languageConceptId;
    }
}
