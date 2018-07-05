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

package com.odysseusinc.athena.api.v1.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.odysseusinc.athena.repositories.athena.RevokedTokenRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = UserController.class)
@ActiveProfiles("test")
@TestPropertySource(locations = {"classpath:/test.properties"})
public class UserControllerTest {

    @Value("${athena.token.header}")
    private String tokenHeader;

    @Value("${athena.token.value}")
    private String token;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RevokedTokenRepository tokenRepository;

    @MockBean
    @SuppressWarnings(value = "unused")
    private JavaMailSender mailSender; // necessary instead spring boot auto configured bean

    @Before
    public void setup() {

        tokenRepository.deleteAll();
    }

    @Test
    public void me() throws Exception {

        mvc.perform(
                get("/api/v1/users/me")
                        .header(tokenHeader, token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    @Ignore
    public void logout() throws Exception {

        mvc.perform(get("/api/v1/users/logout")
                .header(tokenHeader, token))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/api/v1/users/me")
                .header(tokenHeader, token))
                .andExpect(status().isUnauthorized());
    }

}