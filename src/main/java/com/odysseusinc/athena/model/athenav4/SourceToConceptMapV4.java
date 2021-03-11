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

package com.odysseusinc.athena.model.athenav4;

import com.odysseusinc.athena.model.common.EntityV4;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "source_to_concept_map")
public class SourceToConceptMapV4 extends EntityV4 {
    @Id
    @NotBlank
    @Column(name = "source_code")
    private String sourceCode;

    @Column(name = "source_vocabulary_id")
    private Long sourceVocabularyId;

    @NotBlank
    @Column(name = "source_code_description")
    private String sourceCodeDescription;

    @NotBlank
    @Column(name = "target_concept_id")
    private Long targetConceptId;

    @Column(name = "target_vocabulary_id")
    private Long targetVocabularyId;

    @NotBlank
    @Column(name = "mapping_type")
    private String mappingType;

    @Column(name = "primary_map")
    private String primaryMap;

    @NotNull
    @Column(name = "valid_start_date")
    private Date validStart;

    @NotNull
    @Column(name = "valid_end_date")
    private Date validEnd;

    @Column(name = "invalid_reason")
    private String invalidReason;


    public String getSourceCode() {

        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {

        this.sourceCode = sourceCode;
    }

    public Long getSourceVocabularyId() {

        return sourceVocabularyId;
    }

    public void setSourceVocabularyId(Long sourceVocabularyId) {

        this.sourceVocabularyId = sourceVocabularyId;
    }

    public String getSourceCodeDescription() {

        return sourceCodeDescription;
    }

    public void setSourceCodeDescription(String sourceCodeDescription) {

        this.sourceCodeDescription = sourceCodeDescription;
    }

    public Long getTargetConceptId() {

        return targetConceptId;
    }

    public void setTargetConceptId(Long targetConceptId) {

        this.targetConceptId = targetConceptId;
    }

    public Long getTargetVocabularyId() {

        return targetVocabularyId;
    }

    public void setTargetVocabularyId(Long targetVocabularyId) {

        this.targetVocabularyId = targetVocabularyId;
    }

    public String getMappingType() {

        return mappingType;
    }

    public void setMappingType(String mappingType) {

        this.mappingType = mappingType;
    }

    public String getPrimaryMap() {

        return primaryMap;
    }

    public void setPrimaryMap(String primaryMap) {

        this.primaryMap = primaryMap;
    }

    public Date getValidStart() {

        return validStart;
    }

    public void setValidStart(Date validStart) {

        this.validStart = validStart;
    }

    public Date getValidEnd() {

        return validEnd;
    }

    public void setValidEnd(Date validEnd) {

        this.validEnd = validEnd;
    }

    public String getInvalidReason() {

        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {

        this.invalidReason = invalidReason;
    }
}
