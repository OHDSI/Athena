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
 * Created: July 29, 2026
 *
 */

package com.odysseusinc.athena.api.v1.controller.dto.arachne;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Response envelope returned by the Arachne portal.
 * <p>
 * Vendored from {@code com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult}
 * so that Athena no longer depends on the retired {@code arachne-commons} library.
 * <p>
 * This is a wire contract with a remote service: the field names and the
 * {@link ErrorCode} numeric values must stay exactly as they are, otherwise
 * registration and password reset break silently. It is deliberately named
 * differently from {@link com.odysseusinc.athena.util.JsonResult}, which is
 * Athena's own unrelated response type with a similar but different error enum.
 */
@Getter
@Setter
public class ArachnePortalResponse<T> implements Serializable {

    private T result;
    private String errorMessage;
    private Integer errorCode;
    private Map<String, Object> validatorErrors = new HashMap<>();

    public ArachnePortalResponse() {
    }

    public ArachnePortalResponse(ErrorCode errorCode) {

        this.errorCode = errorCode.getCode();
    }

    public ArachnePortalResponse(ErrorCode errorCode, T result) {

        this.errorCode = errorCode.getCode();
        this.result = result;
    }

    /**
     * Numeric codes are defined by the portal. Do not renumber.
     */
    public enum ErrorCode {

        NO_ERROR(0),
        UNAUTHORIZED(1),
        PERMISSION_DENIED(2),
        VALIDATION_ERROR(3),
        SYSTEM_ERROR(4),
        ALREADY_EXIST(5),
        DEPENDENCY_EXISTS(6),
        UNACTIVATED(7);

        private final Integer code;

        ErrorCode(Integer code) {

            this.code = code;
        }

        public Integer getCode() {

            return code;
        }
    }
}
