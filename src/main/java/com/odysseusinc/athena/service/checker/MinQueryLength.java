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

package com.odysseusinc.athena.service.checker;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MinQueryLength extends Checker {

    @Value("${solr.limit.min.query.characters:3}")
    private Integer minCharacters;

    @Override
    public String getDescription() {

        return String.format("It's necessary at least %1$d symbols for full-text search.", minCharacters);
    }

    @Override
    public boolean check(ConceptSearchDTO searchDTO) {

        String query = searchDTO.getQuery();
        return isNotEmpty(query) && searchDTO.getQuery().length() >= minCharacters;
    }
}
