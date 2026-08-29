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
 * Address block of {@link UserRegistrationDTO}, forwarded to the Arachne portal.
 * <p>
 * Vendored from {@code com.odysseusinc.arachne.commons.api.v1.dto.CommonAddressDTO}.
 * The original carried a custom {@code @AnyFieldNotBlank} class-level validator;
 * it is not reproduced because this type is never validated in Athena (see
 * {@link UserRegistrationDTO}).
 */
@Getter
@Setter
public class AddressDTO {

    private String phone;
    private String mobile;
    private String address1;
    private String address2;
    private String city;
    private String zipCode;
    private CountryDTO country;
    private StateProvinceDTO stateProvince;
    private String contactEmail;
}
