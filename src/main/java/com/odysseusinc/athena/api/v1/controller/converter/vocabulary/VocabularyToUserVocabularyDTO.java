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

package com.odysseusinc.athena.api.v1.controller.converter.vocabulary;

import static com.odysseusinc.athena.service.impl.VocabularyServiceImpl.CPT4_ID_V4;
import static java.util.stream.Collectors.toList;
import static org.apache.solr.common.StringUtils.isEmpty;

import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.UserVocabularyDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyDTO;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.util.extractor.LicenseStatus;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

public class VocabularyToUserVocabularyDTO {

    private List<License> licenses;

    public VocabularyToUserVocabularyDTO(List<License> licenses) {

        this.licenses = licenses;

    }

    public List<UserVocabularyDTO> convert(@NotNull List<VocabularyDTO> vocabularyDTOs) {

        List<Integer> availableIdV4s = licenses.stream()
                .filter(lic -> LicenseStatus.APPROVED == lic.getStatus())
                .map(lic -> lic.getVocabularyConversion().getIdV4()).collect(toList());
        availableIdV4s.add(CPT4_ID_V4);

        Map<Integer, License> map = licenses.stream()
                .collect(Collectors.toMap(lic -> lic.getVocabularyConversion().getIdV4(), lic -> lic));

        return vocabularyDTOs.stream().map(each -> {

            UserVocabularyDTO res = new UserVocabularyDTO(each);
            License license = map.get(each.getId());
            if (license != null) {
                res.setStatus(license.getStatus());
                res.setExpiredDate(license.getExpiredDate());
            }
            res.setAvailable(
                    isEmpty(each.getRequired()) || (availableIdV4s.contains(each.getId()) && each.getUrl() != null));
            return res;

        }).collect(Collectors.toList());

    }
}
