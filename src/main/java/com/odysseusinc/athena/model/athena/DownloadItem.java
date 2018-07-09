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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "download_item")
public class DownloadItem {

    public DownloadItem() {

    }

    public DownloadItem(DownloadBundle downloadBundle, VocabularyConversion vocabularyConversion) {

        this.downloadBundle = downloadBundle;
        this.vocabularyConversion = vocabularyConversion;
    }

    @Id
    @SequenceGenerator(name = "download_item_pk_sequence", sequenceName = "download_item_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "download_item_pk_sequence")
    private Long id;

    @ManyToOne(optional = false, targetEntity = DownloadBundle.class)
    @JoinColumn(name = "download_bundle_id")
    private DownloadBundle downloadBundle;

    @ManyToOne(optional = false, targetEntity = VocabularyConversion.class)
    @JoinColumn(name = "vocabulary_id_v4")
    private VocabularyConversion vocabularyConversion;


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

    public DownloadBundle getDownloadBundle() {

        return downloadBundle;
    }

    public void setDownloadBundle(DownloadBundle downloadBundle) {

        this.downloadBundle = downloadBundle;
    }
}
