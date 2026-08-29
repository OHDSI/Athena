/*
 *
 * Copyright 2026 Odysseus Data Services, Inc. (EPAM Systems company)
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
 * Created: August 28, 2026
 *
 */

package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse;
import com.odysseusinc.athena.api.v1.controller.dto.arachne.UserRegistrationDTO;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Covers Athena's adapter contract with Arachne's registration endpoint.
 */
public class UserControllerRegistrationTest {

    @Test
    public void emptyPortalSuccessIsReturnedAsAthenaSuccess() throws Exception {

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer portal = MockRestServiceServer.createServer(restTemplate);
        UserController controller = controller(restTemplate);

        portal.expect(requestTo("https://portal.example/api/v1/auth/registration"))
                .andExpect(method(HttpMethod.POST))
                // Arachne declares this endpoint as void and sends no body on success.
                .andRespond(withSuccess());

        ResponseEntity response = controller.register(new UserRegistrationDTO());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ArachnePortalResponse.ErrorCode.NO_ERROR.getCode(),
                ((ArachnePortalResponse<?>) response.getBody()).getErrorCode());
        portal.verify();
    }

    @Test
    public void portalValidationFailureIsStillReturnedAsBadRequest() throws Exception {

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer portal = MockRestServiceServer.createServer(restTemplate);
        UserController controller = controller(restTemplate);

        portal.expect(requestTo("https://portal.example/api/v1/auth/registration"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"errorCode\":3,\"errorMessage\":\"Invalid password\","
                                + "\"validatorErrors\":{\"password\":\"too short\"}}",
                        MediaType.APPLICATION_JSON));

        ResponseEntity response = controller.register(new UserRegistrationDTO());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ArachnePortalResponse.ErrorCode.VALIDATION_ERROR.getCode(),
                ((ArachnePortalResponse<?>) response.getBody()).getErrorCode());
        portal.verify();
    }

    private UserController controller(RestTemplate restTemplate) {

        UserController controller = new UserController(null, null, restTemplate, null);
        ReflectionTestUtils.setField(controller, "arachneUrl", "https://portal.example");
        ReflectionTestUtils.setField(controller, "registerPath", "/api/v1/auth/registration");
        ReflectionTestUtils.setField(controller, "registerToken", "athena");
        ReflectionTestUtils.setField(controller, "athenaUrl", "https://athena.example");
        return controller;
    }
}
