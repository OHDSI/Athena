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

package com.odysseusinc.athena.service.saver.v4;

import static com.odysseusinc.athena.service.saver.v4.ConceptV4Saver.CPT4;

import com.odysseusinc.athena.service.saver.Saver;
import com.odysseusinc.athena.service.saver.SaverV4;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConceptCPT4V4Saver extends Saver implements SaverV4 {

    @Override
    public boolean containCpt4(List ids) {

        return ids.contains(CPT4);
    }

    @Override
    public boolean includedInBundle(List ids) {

        return containCpt4(ids);
    }

    @Override
    public List<Long> filter(List ids) {

        return containCpt4(ids) ? Collections.singletonList(CPT4) : Collections.EMPTY_LIST;
    }

    @Override
    public String fileName() {

        return "CONCEPT_CPT4.csv";
    }

    @Override
    protected String query() {

        return "SELECT concept_id, '' as concept_name,concept_level,concept_class,vocabulary_id,concept_code,"
                + "valid_start_date,valid_end_date,invalid_reason FROM concept WHERE vocabulary_id in (?)";
    }
}
