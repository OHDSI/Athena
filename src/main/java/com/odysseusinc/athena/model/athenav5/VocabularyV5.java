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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "vocabulary")
public class VocabularyV5 extends EntityV5 {

    @Id
    @NotBlank
    @Column(name = "vocabulary_id")
    private String id;

    @NotBlank
    @Column(name = "vocabulary_name")
    private String name;

    @Column(name = "vocabulary_reference")
    private String reference;

    @Column(name = "vocabulary_version")
    private String version;

    @NotNull
    @Column(name = "vocabulary_concept_id")
    private Integer conceptId;

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getReference() {

        return reference;
    }

    public void setReference(String reference) {

        this.reference = reference;
    }

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public Integer getConceptId() {

        return conceptId;
    }

    public void setConceptId(Integer conceptId) {

        this.conceptId = conceptId;
    }
}
