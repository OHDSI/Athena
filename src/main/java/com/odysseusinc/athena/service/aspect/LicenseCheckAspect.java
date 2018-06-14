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

package com.odysseusinc.athena.service.aspect;

import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.service.ConceptService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LicenseCheckAspect {

    @Autowired
    private ConceptService conceptService;

    @Before(value = "@annotation(com.odysseusinc.athena.service.aspect.LicenseCheck) && args(conceptId,..)")
    public void check(long conceptId) throws PermissionDeniedException {

        if (!conceptService.checkLicense(conceptId)) {
            throw new PermissionDeniedException();
        }
    }
}

