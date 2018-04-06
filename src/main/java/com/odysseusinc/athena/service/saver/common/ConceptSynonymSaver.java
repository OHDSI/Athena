/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
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

package com.odysseusinc.athena.service.saver.common;

import com.odysseusinc.athena.service.saver.Saver;
import com.odysseusinc.athena.service.saver.SaverV4;
import com.odysseusinc.athena.service.saver.SaverV5;
import org.springframework.stereotype.Service;

@Service
public class ConceptSynonymSaver extends Saver implements SaverV4, SaverV5 {

    @Override
    public String fileName() {

        return "CONCEPT_SYNONYM.csv";
    }

    @Override
    protected String query() {

        return  "select * from concept_synonym as t WHERE EXISTS "
                + "(SELECT * FROM CONCEPT c "
                + "WHERE t.CONCEPT_ID = c.CONCEPT_ID AND VOCABULARY_ID IN (?))";
    }

}
