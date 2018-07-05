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
 * Created: June 25, 2018
 *
 */

package com.odysseusinc.athena.exceptions;

import java.util.ArrayList;
import java.util.List;

public class LicenseException extends PermissionDeniedException {

    private List<Integer> vocabularyIdV4s = new ArrayList<>();

    public LicenseException() {

        super();
    }

    public LicenseException(String message, List<Integer> vocabularyIdV4s) {

        super(message);
        this.vocabularyIdV4s = vocabularyIdV4s;
    }

    public List<Integer> getVocabularyIdV4s() {

        return vocabularyIdV4s;
    }

    public void setVocabularyIdV4s(List<Integer> vocabularyIdV4s) {

        this.vocabularyIdV4s = vocabularyIdV4s;
    }
}
