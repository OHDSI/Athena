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
 * Author: Yaroslav Molodkov
 * Created: December 7, 2023
 *
 */

package com.odysseusinc.athena.service.saver.v5.history.delta;

import com.odysseusinc.athena.service.saver.SaverV5Delta;
import com.odysseusinc.athena.service.saver.v5.history.HistorySaver;
import org.springframework.stereotype.Service;

@Service
public class ConceptClassDeltaSaver extends HistorySaver implements SaverV5Delta {

    @Override
    public String fileName() {

        return "CONCEPT_CLASS.csv";
    }

    @Override
    protected String query() {

        return "SELECT * FROM get_concept_class_delta(:version, :versionDelta, :vocabularyArr, TRUE)";
    }
}
