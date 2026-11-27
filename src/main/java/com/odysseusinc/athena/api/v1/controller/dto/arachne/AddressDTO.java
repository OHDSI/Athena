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

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Address submitted by the registration form. */
@Getter
@Setter
public class AddressDTO {

    private String phone;
    @NotBlank
    private String mobile;
    @NotBlank
    private String address1;
    private String address2;
    @NotBlank
    private String city;
    @NotBlank
    private String zipCode;
    @NotNull
    private CountryDTO country;
    private StateProvinceDTO stateProvince;
    @Email
    @Size(max = 255)
    private String contactEmail;
}
