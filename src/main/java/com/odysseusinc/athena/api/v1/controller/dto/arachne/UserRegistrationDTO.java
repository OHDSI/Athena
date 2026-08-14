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

/**
 * Registration payload. Bound from the AthenaUI request body and forwarded
 * verbatim to the Arachne portal.
 * <p>
 * Vendored from {@code com.odysseusinc.arachne.commons.api.v1.dto.CommonUserRegistrationDTO}.
 * Field names are a wire contract on both sides — do not rename them.
 * <p>
 * The original carried bean-validation annotations, which are intentionally not
 * reproduced: {@code UserController.register} binds this type without
 * {@code @Valid}, so they never executed in Athena. Validation is performed by
 * the portal. Adding {@code @Valid} here would be a behaviour change, not a fix.
 */
@Getter
@Setter
public class UserRegistrationDTO {

    private String email;
    private String password;
    private String firstname;
    private String middlename;
    private String lastname;
    private String organization;
    private String department;
    private Long professionalTypeId;
    /**
     * Never read by Athena, but populated by AthenaUI (country / stateProvince)
     * and forwarded to the portal. Removing it silently drops those fields.
     */
    private AddressDTO address;
    private String uuid;
    private String registrantToken;
    private String callbackUrl;
}
