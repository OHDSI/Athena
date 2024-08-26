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

package com.odysseusinc.athena.api.v1.controller.dto.vocabulary;

import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.util.DownloadBundleStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class DownloadBundleDTO {

    private Long id;
    private Date date;
    private String link;
    private String name;
    private Float cdmVersion;
    private DownloadBundleStatus status;
    private DownloadBundleService.BundleType type;
    private String releaseVersion;
    private String vocabularyReleaseVersion;
    private String deltaReleaseVersion;
    private Integer vocabularyReleaseVersionCode;
    private Boolean delta;
    private List<DownloadShareDTO> downloadShareDTOs;
    private String shareEmails;


    private List<VocabularyDTO> vocabularies;


    // user will never have access to all shares because it will be filtered first
    public DownloadShareDTO getDownloadShareDTO() {
        if (downloadShareDTOs != null && !downloadShareDTOs.isEmpty()) {
            return downloadShareDTOs.get(0);
        }
        return null;
    }

    public String getShareEmails() {
        if (downloadShareDTOs != null) {
            return downloadShareDTOs.stream()
                    .filter(o -> o.getBundleId() == id)
                    .map(o -> o.getEmail())
                    .collect(Collectors.joining(", "));
        }
        return null;
    }
}
