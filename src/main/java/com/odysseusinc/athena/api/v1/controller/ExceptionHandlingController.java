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

import static com.odysseusinc.athena.util.JsonResult.ErrorCode.ALREADY_EXIST;
import static com.odysseusinc.athena.util.JsonResult.ErrorCode.DEPENDENCY_EXISTS;
import static com.odysseusinc.athena.util.JsonResult.ErrorCode.EMAIL_ERROR;
import static com.odysseusinc.athena.util.JsonResult.ErrorCode.PERMISSION_DENIED;
import static com.odysseusinc.athena.util.JsonResult.ErrorCode.SYSTEM_ERROR;
import static com.odysseusinc.athena.util.JsonResult.ErrorCode.VALIDATION_ERROR;

import com.odysseusinc.athena.api.v1.controller.dto.LicenseExceptionDTO;
import com.odysseusinc.athena.exceptions.AlreadyExistException;
import com.odysseusinc.athena.exceptions.FieldException;
import com.odysseusinc.athena.exceptions.IORuntimeException;
import com.odysseusinc.athena.exceptions.LicenseException;
import com.odysseusinc.athena.exceptions.NotEmptyException;
import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.exceptions.ValidationException;
import com.odysseusinc.athena.exceptions.WrongFileFormatException;
import com.odysseusinc.athena.util.JsonResult;
import java.io.IOException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * There used to be a {@code UserNotFoundException} handler here that issued an HTTP redirect
 * to a login page, which also forced it to return 200 because the 302 was already committed
 * by the time the entity was built. That exception was thrown nowhere in the code base, so
 * both the handler and the exception class were deleted rather than reworked.
 */
@ControllerAdvice
public class ExceptionHandlingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlingController.class);

    /**
     * Missing static resources are normal 404s. Internet scanners generate many of these;
     * routing them through the catch-all turned every probe into a 500 and a full stack trace.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> exceptionHandler(NoResourceFoundException ex) {

        LOGGER.debug("Static resource not found: {}", ex.getResourcePath());
        return ResponseEntity.notFound().build();
    }

    /**
     * The response is already unusable when a client closes a streamed download. Do not try
     * to serialize a JSON error into the existing CSV response: that only creates a second
     * exception and two large, non-actionable stack traces.
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void exceptionHandler(AsyncRequestNotUsableException ex, HttpServletResponse response) {

        LOGGER.debug("Client disconnected before the response completed: {}", ex.getMessage());
    }

    /**
     * Last-resort handler. Deliberately does <b>not</b> put {@code ex.getMessage()} in the
     * response: this catches anything unmapped, so the message can carry SQL fragments,
     * entity names or file paths, and these endpoints are reachable unauthenticated. The
     * detail stays in the log where operators can correlate it.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonResult> exceptionHandler(Exception ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(SYSTEM_ERROR);
        result.setErrorMessage("Internal server error");
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Without this, Spring Security's {@code AccessDeniedException} is caught by the
     * {@code Exception} handler above — so a {@code @Secured("ROLE_ADMIN")} denial was
     * reported to the caller as <b>HTTP 200</b>, i.e. as success. Handled explicitly so an
     * authorization failure is a 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<JsonResult> exceptionHandler(AccessDeniedException ex) {

        LOGGER.warn("Access denied: {}", ex.getMessage());
        JsonResult result = new JsonResult<>(PERMISSION_DENIED);
        result.setErrorMessage("Access is denied");
        return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
    }

    /** Request data rejected by the domain validator is a client error, not a server fault. */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<JsonResult> exceptionHandler(ValidationException ex) {

        LOGGER.debug("Validation failed: {}", ex.getMessage());
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    /** A path or query parameter that cannot be converted is malformed client input. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<JsonResult> exceptionHandler(MethodArgumentTypeMismatchException ex) {

        LOGGER.debug("Invalid value for parameter [{}]: {}", ex.getName(), ex.getValue());
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage(String.format("Invalid value for parameter '%s'", ex.getName()));
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<JsonResult> exceptionHandler(MessagingException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(EMAIL_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<JsonResult> exceptionHandler(MailException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(EMAIL_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<JsonResult> exceptionHandler(IOException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(SYSTEM_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IORuntimeException.class)
    public ResponseEntity<JsonResult> exceptionHandler(IORuntimeException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(SYSTEM_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<JsonResult> exceptionHandler(PermissionDeniedException ex) {

        LOGGER.warn("Permission denied: {}", ex.getMessage());
        JsonResult result = new JsonResult<>(PERMISSION_DENIED);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
    }

    /**
     * Deliberately still 200. This is not a generic error: it returns a
     * {@link LicenseExceptionDTO} listing the vocabularies the user lacks a licence for,
     * and AthenaUI's download modal consumes that as a normal result to drive the licence
     * request flow. Making it a 4xx would route it into the client's error handling and
     * break that flow, so it needs a coordinated change rather than a status swap.
     */
    @ExceptionHandler(LicenseException.class)
    public ResponseEntity<LicenseExceptionDTO> exceptionHandler(LicenseException ex) {

        LOGGER.debug("Additional licences required: {}", ex.getMessage());
        return new ResponseEntity<>(new LicenseExceptionDTO(ex.getVocabularyIdV4s()), HttpStatus.OK);
    }

    @ExceptionHandler(NotEmptyException.class)
    public ResponseEntity<JsonResult> exceptionHandler(NotEmptyException ex) {

        LOGGER.debug("Operation conflicts with dependent data: {}", ex.getMessage());
        JsonResult result = new JsonResult<>(DEPENDENCY_EXISTS);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(FieldException.class)
    public ResponseEntity<JsonResult> exceptionHandler(FieldException ex) {

        LOGGER.debug("Invalid field [{}]: {}", ex.getField(), ex.getMessage());
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage("Incorrect data");
        result.getValidatorErrors().put(ex.getField(), ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WrongFileFormatException.class)
    public ResponseEntity<JsonResult> exceptionHandler(WrongFileFormatException ex) {

        LOGGER.debug("Invalid file field [{}]: {}", ex.getField(), ex.getMessage());
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage(ex.getMessage());
        result.getValidatorErrors().put(ex.getField(), ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotExistException.class)
    public ResponseEntity<JsonResult> exceptionHandler(NotExistException ex) {

        LOGGER.debug("Requested entity [{}] was not found: {}",
                ex.getEntity().getSimpleName(), ex.getMessage());
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage(ex.getMessage());
        result.getValidatorErrors().put(ex.getEntity().getSimpleName(), ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<JsonResult> exceptionHandler(AlreadyExistException ex) {

        LOGGER.debug("Requested entity already exists: {}", ex.getMessage());
        JsonResult result = new JsonResult<>(ALREADY_EXIST);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.CONFLICT);
    }
}
