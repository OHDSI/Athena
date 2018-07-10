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

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AddingUserLicensesDTO {

    @NotNull
    private Long userId;

    @NotNull
    @Size(min = 1)
    private List<Integer> vocabularyV4Ids;

    public Long getUserId() {

        return userId;
    }

    public void setUserId(Long userId) {

        this.userId = userId;
    }

    public List<Integer> getVocabularyV4Ids() {

        return vocabularyV4Ids;
    }

    public void setVocabularyV4Ids(List<Integer> vocabularyV4Ids) {

        this.vocabularyV4Ids = vocabularyV4Ids;
    }
}
