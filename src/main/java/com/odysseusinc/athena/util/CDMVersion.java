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

package com.odysseusinc.athena.util;

import static java.util.Arrays.stream;

import com.odysseusinc.athena.exceptions.NotExistException;
import java.util.Optional;

public enum CDMVersion {
    V4_5(4.5f),
    V5(5),
    ALL(-1);

    private float value;

    CDMVersion(float value) {

        this.value = value;
    }

    public float getValue() {

        return value;
    }

    public static boolean notExist(Float version) {

        return stream(values()).noneMatch(cdmVersion -> new Float(cdmVersion.value).equals(version));
    }

    public static CDMVersion getByValue(Float version) throws NotExistException {

        Optional<CDMVersion> optional = stream(values())
                .filter(cdmVersion -> new Float(cdmVersion.value).equals(version))
                .findFirst();
        if (!optional.isPresent()) {
            throw new NotExistException("No version " + version, CDMVersion.class);
        }
        return optional.get();
    }

}
