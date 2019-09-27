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

import com.google.common.base.MoreObjects;
import com.odysseusinc.athena.util.CDMVersion;
import com.odysseusinc.athena.util.DownloadBundleStatus;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "download_bundle")
public class DownloadBundle {
    @Id
    @SequenceGenerator(name = "download_bundle_pk_sequence", sequenceName = "download_bundle_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "download_bundle_pk_sequence")
    private Long id;

    @NotBlank
    @Column
    private String uuid;

    @NotNull
    @Column(name = "cdm_version")
    @Enumerated(EnumType.STRING)
    private CDMVersion cdmVersion;

    @Column
    private Date created;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "downloadBundle", targetEntity = SavedFile.class)
    private List<SavedFile> files;

    @Column(name = "user_id")
    private Long userId;

    @OneToMany(mappedBy = "downloadBundle", targetEntity = DownloadItem.class)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<DownloadItem> vocabularies;

    @OneToMany(mappedBy = "bundle", targetEntity = DownloadShare.class)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<DownloadShare> downloadShares;

    @Column
    private String name;

    @Column(name = "release_version")
    private String releaseVersion;

    @NotNull
    @Column
    @Enumerated(EnumType.STRING)
    private DownloadBundleStatus status;

    private boolean cpt4;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public CDMVersion getCdmVersion() {

        return cdmVersion;
    }

    public void setCdmVersion(CDMVersion cdmVersion) {

        this.cdmVersion = cdmVersion;
    }

    public List<SavedFile> getFiles() {

        return files;
    }

    public void setFiles(List<SavedFile> files) {

        this.files = files;
    }

    public boolean isCpt4() {

        return cpt4;
    }

    public void setCpt4(boolean cpt4) {

        this.cpt4 = cpt4;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public Long getUserId() {

        return userId;
    }

    public void setUserId(Long userId) {

        this.userId = userId;
    }

    public List<DownloadItem> getVocabularies() {

        return vocabularies;
    }

    public void setVocabularies(List<DownloadItem> vocabularies) {

        this.vocabularies = vocabularies;
    }

    public DownloadBundleStatus getStatus() {

        return status;
    }

    public void setStatus(DownloadBundleStatus status) {

        this.status = status;
    }

    public boolean isArchived() {

        return DownloadBundleStatus.ARCHIVED == status;
    }

    public List<Long> getVocabularyV4Ids() {

        return vocabularies.stream().map(each -> each.getVocabularyConversion().getIdV4())
                .map(Integer::longValue).collect(Collectors.toList());
    }

    public List<DownloadItem> getVocabulariesWithoutOmopReq() {

        return vocabularies.stream()
                .filter(item -> !item.getVocabularyConversion().getOmopReqValue())
                .collect(Collectors.toList());
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public List<DownloadShare> getDownloadShares() {
        return downloadShares;
    }

    public void setDownloadShares(List<DownloadShare> downloadShares) {
        this.downloadShares = downloadShares;
    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uuid", uuid)
                .add("cdmVersion", cdmVersion)
                .add("created", created)
                .add("files", files)
                .add("userId", userId)
                .add("vocabularies", vocabularies)
                .add("downloadShares", downloadShares)
                .add("name", name)
                .add("cpt4", cpt4)
                .add("status", status)
                .add("releaseVersion", releaseVersion)
                .toString();
    }
}
