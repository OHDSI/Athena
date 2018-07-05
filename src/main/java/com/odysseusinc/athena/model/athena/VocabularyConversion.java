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

import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vocabulary_conversion")
public class VocabularyConversion {

    public VocabularyConversion() {

    }

    public VocabularyConversion(Integer idV4) {

        this.idV4 = idV4;
    }

    @Id
    @Column(name = "vocabulary_id_v4")
    private Integer idV4;

    @Column(name = "vocabulary_id_v5")
    private String idV5;

    @Column
    private String name;

    @Column(name = "omop_req")
    private String omopReq;

    @Column(name = "click_default")
    private String clickDefault;

    @Column(name = "available")
    private String available;

    @Column(name = "url")
    private String url;

    @Column(name = "click_disabled")
    private String clickDisabled;

    @Column(name = "latest_update")
    private Date latestUpdate;

    public Integer getIdV4() {

        return idV4;
    }

    public void setIdV4(Integer idV4) {

        this.idV4 = idV4;
    }

    public String getIdV5() {

        return idV5;
    }

    public String getOmopReq() {

        return omopReq;
    }

    public boolean getOmopReqValue() {

        return omopReq != null;
    }

    public void setOmopReq(String omopReq) {

        this.omopReq = omopReq;
    }

    public String getClickDefault() {

        return clickDefault;
    }

    public boolean getClickDefaultValue() {

        return clickDefault != null;
    }


    public void setClickDefault(String clickDefault) {

        this.clickDefault = clickDefault;
    }

    public String getAvailable() {

        return available;
    }

    public void setAvailable(String available) {

        this.available = available;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        this.url = url;
    }

    public String getClickDisabled() {

        return clickDisabled;
    }

    public void setClickDisabled(String clickDisabled) {

        this.clickDisabled = clickDisabled;
    }

    public Date getLatestUpdate() {

        return latestUpdate;
    }

    public void setLatestUpdate(Date latestUpdate) {

        this.latestUpdate = latestUpdate;
    }

    public void setIdV5(String idV5) {

        this.idV5 = idV5;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }
}
