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

package com.odysseusinc.athena.model.athena;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "notifications")
public class Notification {

    public Notification() {
        //empty
    }

    public Notification(Long userId, VocabularyConversion vocabularyConversion, String vocabularyCode, String actualVersion) {
        this.userId = userId;
        this.vocabularyConversion = vocabularyConversion;
        this.vocabularyCode = vocabularyCode;
        this.actualVersion = actualVersion;
    }

    @Id
    @SequenceGenerator(name = "notifications_id_sequence", sequenceName = "notifications_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_id_sequence")
    private Long id;

    @NotNull
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(optional = false, targetEntity = VocabularyConversion.class)
    @JoinColumn(name = "vocabulary_id_v4")
    private VocabularyConversion vocabularyConversion;

    @Column(name = "vocabulary_code")
    private String vocabularyCode;

    @Column(name = "actual_version")
    private String actualVersion;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Long getUserId() {

        return userId;
    }

    public void setUserId(Long userId) {

        this.userId = userId;
    }

    public VocabularyConversion getVocabularyConversion() {

        return vocabularyConversion;
    }

    public void setVocabularyConversion(VocabularyConversion vocabularyConversion) {

        this.vocabularyConversion = vocabularyConversion;
    }

    public String getActualVersion() {

        return actualVersion;
    }

    public void setActualVersion(String actualVersion) {

        this.actualVersion = actualVersion;
    }

    public String getVocabularyCode() {

        return vocabularyCode;
    }

    public void setVocabularyCode(String vocabularyCode) {

        this.vocabularyCode = vocabularyCode;
    }
}
