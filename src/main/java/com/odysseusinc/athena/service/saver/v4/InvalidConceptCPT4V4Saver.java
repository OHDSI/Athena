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

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.odysseusinc.athena.service.saver.v4.ConceptV4Saver.CPT4;

@Service
public class InvalidConceptCPT4V4Saver extends ConceptCPT4V4Saver {

    @Override
    public boolean includedInBundle(List ids) {

        return false;
    }

    @Override
    public String fileName() {

        return "cpt4_ref.csv";
    }

    @Override
    protected String query() {

        return "SELECT * FROM concept WHERE vocabulary_id in (?) AND valid_end_date <= now()";
    }

    @Override
    public List getIds() {

        return Collections.singletonList(CPT4);
    }
}
