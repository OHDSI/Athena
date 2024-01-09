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

package com.odysseusinc.athena.service.saver.v5.history.delta;

import com.odysseusinc.athena.service.saver.SaverV5Delta;
import com.odysseusinc.athena.service.saver.v5.history.HistorySaver;
import org.springframework.stereotype.Service;

@Service
public class DrugStrengthDeltaSaver extends HistorySaver implements SaverV5Delta {

    @Override
    public String fileName() {

        return "DRUG_STRENGTH.csv";
    }

    @Override
    protected String query() {
        return "SELECT * FROM get_drug_strength_delta(:version, :versionDelta, :vocabularyArr, TRUE)";
    }
}
