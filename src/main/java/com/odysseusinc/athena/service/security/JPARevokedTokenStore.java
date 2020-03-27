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

package com.odysseusinc.athena.service.security;

import com.odysseusinc.athena.model.athena.RevokedToken;
import com.odysseusinc.athena.repositories.athena.RevokedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("tokenStore")
@Transactional
public class JPARevokedTokenStore implements RevokedTokenStore {

    private final RevokedTokenRepository repository;

    @Autowired
    public JPARevokedTokenStore(RevokedTokenRepository repository) {

        this.repository = repository;
    }

    @Override
    public boolean contains(String token) {

        return repository.findByToken(token).isPresent();
    }

    @Override
    public void invalidate(String token) {

        RevokedToken expiredToken = new RevokedToken();
        expiredToken.setToken(token);
        repository.save(expiredToken);
    }
}
