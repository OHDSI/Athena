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

import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.util.extractor.LicenseStatus;

import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "licenses")
public class License {

    public License() {

    }

    public License(AthenaUser user, VocabularyConversion vocabularyConversion, LicenseStatus status) {

        this.user = user;
        this.vocabularyConversion = vocabularyConversion;
        this.status = status;
        this.token = UUID.randomUUID().toString().replace("-", "");
    }

    @Id
    @SequenceGenerator(name = "licenses_pk_sequence", sequenceName = "licenses_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "licenses_pk_sequence")
    private Long id;

    @ManyToOne(optional = false, targetEntity = AthenaUser.class)
    @JoinColumn(name = "user_id")
    private AthenaUser user;

    @ManyToOne(optional = false, targetEntity = VocabularyConversion.class)
    @JoinColumn(name = "vocabulary_id_v4", referencedColumnName = "vocabulary_id_v4")
    private VocabularyConversion vocabularyConversion;

    @NotNull
    @Column
    @Enumerated(EnumType.STRING)
    private LicenseStatus status;

    @NotNull
    @Column
    private String token;

    @Column(name = "request_date")
    private Date requestDate;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public VocabularyConversion getVocabularyConversion() {

        return vocabularyConversion;
    }

    public void setVocabularyConversion(VocabularyConversion vocabularyConversion) {

        this.vocabularyConversion = vocabularyConversion;
    }

    public AthenaUser getUser() {

        return user;
    }

    public void setUser(AthenaUser user) {

        this.user = user;
    }

    public LicenseStatus getStatus() {

        return status;
    }

    public void setStatus(LicenseStatus status) {

        this.status = status;
    }

    public String getToken() {

        return token;
    }

    public void setToken(String token) {

        this.token = token;
    }

    public Date getRequestDate() {

        return requestDate;
    }

    public void setRequestDate(Date requestDate) {

        this.requestDate = requestDate;
    }
}
