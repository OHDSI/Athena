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
import com.odysseusinc.athena.exceptions.UserNotFoundException;
import com.odysseusinc.athena.exceptions.WrongFileFormatException;
import com.odysseusinc.athena.util.JsonResult;
import java.io.IOException;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlingController.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonResult> exceptionHandler(Exception ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(SYSTEM_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<JsonResult> exceptionHandler(MessagingException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(EMAIL_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<JsonResult> exceptionHandler(MailException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(EMAIL_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<JsonResult> exceptionHandler(IOException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(SYSTEM_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(IORuntimeException.class)
    public ResponseEntity<JsonResult> exceptionHandler(IORuntimeException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(SYSTEM_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<JsonResult> exceptionHandler(PermissionDeniedException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(PERMISSION_DENIED);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(LicenseException.class)
    public ResponseEntity<JsonResult> exceptionHandler(LicenseException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(PERMISSION_DENIED);
        result.setErrorMessage(ex.getMessage());
        result.setResult(new LicenseExceptionDTO(ex.getVocabularyIdV4s()));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(NotEmptyException.class)
    public ResponseEntity<JsonResult> exceptionHandler(NotEmptyException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(DEPENDENCY_EXISTS);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(FieldException.class)
    public ResponseEntity<JsonResult> exceptionHandler(FieldException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage("Incorrect data");
        result.getValidatorErrors().put(ex.getField(), ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(WrongFileFormatException.class)
    public ResponseEntity<JsonResult> exceptionHandler(WrongFileFormatException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage(ex.getMessage());
        result.getValidatorErrors().put(ex.getField(), ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<JsonResult> exceptionHandler(UserNotFoundException ex,
                                                       HttpServletResponse response) throws IOException {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage(ex.getMessage());
        result.getValidatorErrors().put(ex.getField(), ex.getMessage());
        response.sendRedirect("/auth/login?message=email-not-confirmed");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(NotExistException.class)
    public ResponseEntity<JsonResult> exceptionHandler(NotExistException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage(ex.getMessage());
        result.getValidatorErrors().put(ex.getEntity().getSimpleName(), ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<JsonResult> exceptionHandler(AlreadyExistException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(ALREADY_EXIST);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
