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
 * Created: August 29, 2026
 *
 */

package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.dto.RemindPasswordDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ResetPasswordDTO;
import com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse;
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
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Covers Athena's adapter contract with Arachne's password endpoints.
 */
public class UserControllerPasswordResetTest {

    @Test
    public void emptyRemindPasswordSuccessIsReturnedAsAthenaSuccess() {

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer portal = MockRestServiceServer.createServer(restTemplate);
        UserController controller = controller(restTemplate);

        portal.expect(requestTo("https://portal.example/api/v1/auth/remind-password"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        ResponseEntity response = controller.remindPassword(new RemindPasswordDTO());

        assertSuccessfulResponse(response);
        portal.verify();
    }

    @Test
    public void emptyResetPasswordSuccessIsReturnedAsAthenaSuccess() throws Exception {

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer portal = MockRestServiceServer.createServer(restTemplate);
        UserController controller = controller(restTemplate);

        portal.expect(requestTo("https://portal.example/api/v1/auth/reset-password"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        ResponseEntity response = controller.resetPassword(new ResetPasswordDTO());

        assertSuccessfulResponse(response);
        portal.verify();
    }

    @Test
    public void portalReplyWithoutErrorCodeIsReturnedAsBadRequest() {

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer portal = MockRestServiceServer.createServer(restTemplate);
        UserController controller = controller(restTemplate);

        portal.expect(requestTo("https://portal.example/api/v1/auth/remind-password"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ResponseEntity response = controller.remindPassword(new RemindPasswordDTO());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(((ArachnePortalResponse<?>) response.getBody()).getErrorCode());
        portal.verify();
    }

    private void assertSuccessfulResponse(ResponseEntity response) {

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ArachnePortalResponse.ErrorCode.NO_ERROR.getCode(),
                ((ArachnePortalResponse<?>) response.getBody()).getErrorCode());
    }

    private UserController controller(RestTemplate restTemplate) {

        UserController controller = new UserController(null, null, restTemplate, null);
        ReflectionTestUtils.setField(controller, "arachneUrl", "https://portal.example");
        ReflectionTestUtils.setField(controller, "remindPasswordPath", "/api/v1/auth/remind-password");
        ReflectionTestUtils.setField(controller, "resetPasswordPath", "/api/v1/auth/reset-password");
        ReflectionTestUtils.setField(controller, "remindToken", "athena");
        ReflectionTestUtils.setField(controller, "athenaUrl", "https://athena.example");
        return controller;
    }
}
