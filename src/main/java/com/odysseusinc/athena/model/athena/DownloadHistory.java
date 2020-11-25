/*
 *
 * Copyright 2020 Odysseus Data Services, inc.
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
 * Authors: Alexandr Cumarav
 * Created: March 24, 2020
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
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "download_history")
public class DownloadHistory {
    @Id
    @SequenceGenerator(name = "download_history_pk_sequence", sequenceName = "download_history_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "download_history_pk_sequence")
    private Long id;
    @ManyToOne(optional = false, targetEntity = DownloadBundle.class)
    @JoinColumn(name = "bundle_id")
    private DownloadBundle vocabularyBundle;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "download_time")
    private LocalDateTime downloadTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DownloadBundle getVocabularyBundle() {
        return vocabularyBundle;
    }

    public void setVocabularyBundle(DownloadBundle vocabularyBundle) {
        this.vocabularyBundle = vocabularyBundle;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(LocalDateTime downloadTime) {
        this.downloadTime = downloadTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DownloadHistory that = (DownloadHistory) o;
        return id.equals(that.id) &&
                vocabularyBundle.equals(that.vocabularyBundle) &&
                userId.equals(that.userId) &&
                downloadTime.equals(that.downloadTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DownloadHistory{" +
                "id=" + id +
                ", vocabularyBundle=" + vocabularyBundle +
                ", userId=" + userId +
                ", downloadTime=" + downloadTime +
                '}';
    }
}
