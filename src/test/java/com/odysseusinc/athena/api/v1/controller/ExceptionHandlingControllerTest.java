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
 * Created: July 30, 2026
 *
 */

package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.exceptions.AlreadyExistException;
import com.odysseusinc.athena.exceptions.NotUniquieException;
import com.odysseusinc.athena.exceptions.IORuntimeException;
import com.odysseusinc.athena.exceptions.NotEmptyException;
import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.exceptions.ValidationException;
import com.odysseusinc.athena.exceptions.WrongFileFormatException;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.util.JsonResult;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Every handler used to answer {@code HTTP 200}, so a client could not tell success
 * from failure, and the catch-all echoed {@code ex.getMessage()} to unauthenticated callers.
 * <p>
 * The response <em>body</em> is deliberately unchanged — only the status differs — because
 * AthenaUI reads {@code errorMessage} and {@code validatorErrors} off it.
 * <p>
 * JUnit 4 on purpose.
 */
public class ExceptionHandlingControllerTest {

    private final ExceptionHandlingController controller = new ExceptionHandlingController();

    /**
     * The most consequential one. {@code AccessDeniedException} was swallowed by the
     * {@code Exception} catch-all, so a {@code @Secured("ROLE_ADMIN")} denial reached the
     * caller as 200 — an authorization failure reported as success.
     */
    @Test
    public void accessDeniedIsForbiddenRatherThanSuccess() {

        ResponseEntity<JsonResult> response =
                controller.exceptionHandler(new AccessDeniedException("nope"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(JsonResult.ErrorCode.PERMISSION_DENIED.getCode(),
                response.getBody().getErrorCode());
    }

    /** The catch-all must not hand internal detail to the caller. */
    @Test
    public void theCatchAllDoesNotLeakTheInternalMessage() {

        String internal = "ERROR: relation \"users\" does not exist at /opt/athena/secret";

        ResponseEntity<JsonResult> response =
                controller.exceptionHandler(new IllegalStateException(internal));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse("the internal message must stay in the log, not the response",
                String.valueOf(response.getBody().getErrorMessage()).contains("does not exist"));
    }

    @Test
    public void permissionDeniedIsForbidden() {

        assertEquals(HttpStatus.FORBIDDEN,
                controller.exceptionHandler(new PermissionDeniedException()).getStatusCode());
    }

    @Test
    public void missingEntityIsNotFound() {

        assertEquals(HttpStatus.NOT_FOUND,
                controller.exceptionHandler(
                        new NotExistException("gone", AthenaUser.class)).getStatusCode());
    }

    @Test
    public void conflictsAreReportedAsConflict() {

        assertEquals(HttpStatus.CONFLICT,
                controller.exceptionHandler(new AlreadyExistException("dup")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                controller.exceptionHandler(new NotEmptyException("in use")).getStatusCode());
    }

    @Test
    public void badInputIsBadRequest() {

        ResponseEntity<JsonResult> validationResponse =
                controller.exceptionHandler(new ValidationException("Please provide the bundle name"));

        assertEquals(HttpStatus.BAD_REQUEST, validationResponse.getStatusCode());
        assertEquals(JsonResult.ErrorCode.VALIDATION_ERROR.getCode(),
                validationResponse.getBody().getErrorCode());
        assertEquals("Please provide the bundle name",
                validationResponse.getBody().getErrorMessage());
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.exceptionHandler(new NotUniquieException("email", "invalid")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.exceptionHandler(
                        new WrongFileFormatException("file", "bad")).getStatusCode());
    }

    @Test
    public void ioFailuresAreServerErrors() {

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.exceptionHandler(new IOException("disk")).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.exceptionHandler(new IORuntimeException("disk", new IOException()))
                        .getStatusCode());
    }

    /** A missing static resource is a normal 404, not an application failure. */
    @Test
    public void missingStaticResourceIsNotFoundRatherThanServerError() {

        ResponseEntity<?> response = controller.exceptionHandler(
                new NoResourceFoundException(HttpMethod.GET, "/robots.txt", "robots.txt"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * The body shape AthenaUI depends on must survive: it reads {@code errorMessage} and
     * {@code validatorErrors} off the error, and a field error drives redux-form.
     */
    @Test
    public void validationFailuresStillCarryTheFieldErrorsTheUiNeeds() {

        ResponseEntity<JsonResult> response =
                controller.exceptionHandler(new NotUniquieException("email", "already taken"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("already taken", response.getBody().getValidatorErrors().get("email"));
    }
}
