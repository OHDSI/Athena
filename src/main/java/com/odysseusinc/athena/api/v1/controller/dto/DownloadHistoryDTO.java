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
 * Created: March 31, 2020
 *
 */


package com.odysseusinc.athena.api.v1.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Objects;

public class DownloadHistoryDTO {
    private String vocabularyName;
    private String code;
    private String userName;
    private String organization;
    private LocalDateTime date;

    public String getVocabularyName() {
        return vocabularyName;
    }

    public void setVocabularyName(String vocabularyName) {
        this.vocabularyName = vocabularyName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    @JsonFormat(pattern = "dd-MMM-yyyy")
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DownloadHistoryDTO that = (DownloadHistoryDTO) o;
        return Objects.equals(vocabularyName, that.vocabularyName) &&
                Objects.equals(code, that.code) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(organization, that.organization) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vocabularyName, code, userName, organization, date);
    }

    @Override
    public String toString() {
        return "DownloadHistoryDTO{" +
                ", vocabularyName='" + vocabularyName + '\'' +
                ", code='" + code + '\'' +
                ", userName='" + userName + '\'' +
                ", organization='" + organization + '\'' +
                ", date=" + date +
                '}';
    }
}
