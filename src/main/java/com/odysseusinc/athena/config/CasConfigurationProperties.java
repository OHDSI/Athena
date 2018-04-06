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

package com.odysseusinc.athena.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author vkoulakov
 * @since 2/27/17.
 */
@ConfigurationProperties(prefix = "cas")
public class CasConfigurationProperties {
    private String defaultIDP;
    private String entityId;
    private String metadataLocation;
    private String providerName;
    private KeyManagerProperties keyManager;
    private String idpSelectionPath = "/saml/idpSelection";
    private String defaultTargetUrl = "/";
    private String defaultFailureUrl = "/error";

    public String getDefaultTargetUrl() {

        return defaultTargetUrl;
    }

    public void setDefaultTargetUrl(String defaultTargetUrl) {

        this.defaultTargetUrl = defaultTargetUrl;
    }

    public String getDefaultFailureUrl() {

        return defaultFailureUrl;
    }

    public void setDefaultFailureUrl(String defaultFailureUrl) {

        this.defaultFailureUrl = defaultFailureUrl;
    }

    public String getIdpSelectionPath() {

        return idpSelectionPath;
    }

    public void setIdpSelectionPath(String idpSelectionPath) {

        this.idpSelectionPath = idpSelectionPath;
    }

    public KeyManagerProperties getKeyManager() {

        return keyManager;
    }

    public void setKeyManager(KeyManagerProperties keyManager) {

        this.keyManager = keyManager;
    }

    public String getDefaultIDP() {

        return defaultIDP;
    }

    public void setDefaultIDP(String defaultIDP) {

        this.defaultIDP = defaultIDP;
    }

    public String getEntityId() {

        return entityId;
    }

    public void setEntityId(String entityId) {

        this.entityId = entityId;
    }

    public String getMetadataLocation() {

        return metadataLocation;
    }

    public void setMetadataLocation(String metadataLocation) {

        this.metadataLocation = metadataLocation;
    }

    public String getProviderName() {

        return providerName;
    }

    public void setProviderName(String providerName) {

        this.providerName = providerName;
    }

    public static class KeyManagerProperties {
        private String keyStoreFile;
        private String storePassword;
        private String defaultKey;
        private Map<String, String> passwords;

        public String getKeyStoreFile() {

            return keyStoreFile;
        }

        public void setKeyStoreFile(String keyStoreFile) {

            this.keyStoreFile = keyStoreFile;
        }

        public String getStorePassword() {

            return storePassword;
        }

        public void setStorePassword(String storePassword) {

            this.storePassword = storePassword;
        }

        public String getDefaultKey() {

            return defaultKey;
        }

        public void setDefaultKey(String defaultKey) {

            this.defaultKey = defaultKey;
        }

        public Map<String, String> getPasswords() {

            return passwords;
        }

        public void setPasswords(Map<String, String> passwords) {

            this.passwords = passwords;
        }
    }
}
