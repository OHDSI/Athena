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

package com.odysseusinc.athena.util;

import com.odysseusinc.athena.model.security.AthenaProfile;
import java.security.Principal;
import java.util.Optional;
import net.minidev.json.JSONArray;
import org.pac4j.saml.profile.SAML2Profile;
import org.pac4j.springframework.security.authentication.Pac4jAuthenticationToken;

public class UserProfileUtil {
    public static Optional<AthenaProfile> getProfile(Principal principal) {

        AthenaProfile profile = null;
        if (principal instanceof Pac4jAuthenticationToken) {
            Object principalObject = ((Pac4jAuthenticationToken) principal).getPrincipal();
            if (principalObject instanceof Optional && ((Optional) principalObject).isPresent()) {
                profile = (AthenaProfile) ((Optional) principalObject).get();
            } else if (principalObject instanceof AthenaProfile) {
                profile = (AthenaProfile) principalObject;
            }
        }
        return Optional.ofNullable(profile);
    }

    public static String getAttribute(SAML2Profile profile, final String key) {

        return getAttribute(profile, key, null);
    }

    public static String getAttribute(SAML2Profile profile, final String key, final String defaultValue) {

        Object value = profile.getAttribute(key);
        if (value != null) {
            if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                return array.size() > 0 ? array.get(0).toString() : "";
            } else if (value instanceof String) {
                return value.toString();
            }
        }
        return defaultValue;
    }
}
