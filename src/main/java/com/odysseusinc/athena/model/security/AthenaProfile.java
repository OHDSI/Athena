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

package com.odysseusinc.athena.model.security;

import org.pac4j.saml.profile.SAML2Profile;

/**
 * @author vkoulakov
 * @since 3/13/17.
 */
public class AthenaProfile extends SAML2Profile {
    private AthenaUser athenaUser;
    private String token;

    public AthenaUser getAthenaUser() {

        return athenaUser;
    }

    public void setAthenaUser(AthenaUser athenaUser) {

        this.athenaUser = athenaUser;
    }

    public String getToken() {

        return token;
    }

    public void setToken(String token) {

        this.token = token;
    }
}
