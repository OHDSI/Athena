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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Guards the JSON contract with the Arachne portal.
 * <p>
 * These DTOs used to come from the {@code arachne-commons} library. Now that they
 * are vendored, nothing external pins their shape, so this test does: the portal
 * is a separate deployment and a renamed or dropped field fails silently at
 * runtime rather than at compile time.
 * <p>
 * In particular {@code address} is never read by any Java code in Athena — it is
 * populated by AthenaUI and forwarded verbatim — so it looks unused and is an
 * obvious candidate for a well-meaning cleanup. Do not remove it.
 * <p>
 * Deliberately JUnit 4: surefire 2.19.1 has no JUnit 5 provider, so a
 * {@code org.junit.jupiter} test here would never run.
 */
public class ArachnePortalContractTest {

    /** The mapper Spring builds for RestTemplate's Jackson converter. */
    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

    @Test
    public void registrationPayloadKeepsEveryFieldThePortalExpects() throws Exception {

        List<String> expected = Arrays.asList(
                "address", "callbackUrl", "department", "email", "firstname",
                "lastname", "middlename", "organization", "password",
                "professionalTypeId", "registrantToken", "uuid");

        assertEquals("UserRegistrationDTO field set changed - the portal contract is broken",
                expected, sortedFieldNames(new UserRegistrationDTO()));
    }

    @Test
    public void addressBlockKeepsEveryFieldThePortalExpects() throws Exception {

        List<String> expected = Arrays.asList(
                "address1", "address2", "city", "contactEmail", "country",
                "mobile", "phone", "stateProvince", "zipCode");

        assertEquals("AddressDTO field set changed - country/stateProvince are sent by AthenaUI",
                expected, sortedFieldNames(new AddressDTO()));
    }

    @Test
    public void countryAndStateProvinceUseIsoCode() throws Exception {

        assertEquals(Arrays.asList("isoCode", "name"), sortedFieldNames(new CountryDTO()));
        assertEquals(Arrays.asList("isoCode", "name"), sortedFieldNames(new StateProvinceDTO()));
    }

    @Test
    public void portalResponseEnvelopeKeepsItsShape() throws Exception {

        assertEquals(Arrays.asList("errorCode", "errorMessage", "result", "validatorErrors"),
                sortedFieldNames(new ArachnePortalResponse<String>()));
    }

    /**
     * UserController compares the returned code against NO_ERROR.getCode() on every
     * auth call, so these numbers are defined by the portal and must not be renumbered.
     */
    @Test
    public void errorCodesMatchThePortalNumbering() {

        assertEquals(Integer.valueOf(0), ArachnePortalResponse.ErrorCode.NO_ERROR.getCode());
        assertEquals(Integer.valueOf(1), ArachnePortalResponse.ErrorCode.UNAUTHORIZED.getCode());
        assertEquals(Integer.valueOf(2), ArachnePortalResponse.ErrorCode.PERMISSION_DENIED.getCode());
        assertEquals(Integer.valueOf(3), ArachnePortalResponse.ErrorCode.VALIDATION_ERROR.getCode());
        assertEquals(Integer.valueOf(4), ArachnePortalResponse.ErrorCode.SYSTEM_ERROR.getCode());
        assertEquals(Integer.valueOf(5), ArachnePortalResponse.ErrorCode.ALREADY_EXIST.getCode());
        assertEquals(Integer.valueOf(6), ArachnePortalResponse.ErrorCode.DEPENDENCY_EXISTS.getCode());
        assertEquals(Integer.valueOf(7), ArachnePortalResponse.ErrorCode.UNACTIVATED.getCode());
        assertEquals("ErrorCode gained or lost a constant",
                8, ArachnePortalResponse.ErrorCode.values().length);
    }

    @Test
    public void portalErrorReplyIsDeserialisedAndUnknownFieldsAreTolerated() throws Exception {

        String reply = "{\"result\":null,\"errorMessage\":\"Email already exists\","
                + "\"errorCode\":5,\"validatorErrors\":{\"email\":\"taken\"},"
                + "\"aFieldThePortalAddedLater\":true}";

        ArachnePortalResponse<?> parsed = mapper.readValue(reply, ArachnePortalResponse.class);

        assertEquals(Integer.valueOf(5), parsed.getErrorCode());
        assertEquals("Email already exists", parsed.getErrorMessage());
        assertEquals("taken", parsed.getValidatorErrors().get("email"));
    }

    @Test
    public void addressSurvivesARoundTripFromTheUiPayload() throws Exception {

        String uiPayload = "{\"email\":\"a@b.c\",\"firstname\":\"First\",\"lastname\":\"Last\","
                + "\"address\":{\"country\":{\"isoCode\":\"US\",\"name\":\"United States\"},"
                + "\"stateProvince\":{\"isoCode\":\"CA\",\"name\":\"California\"}}}";

        UserRegistrationDTO dto = mapper.readValue(uiPayload, UserRegistrationDTO.class);

        assertEquals("US", dto.getAddress().getCountry().getIsoCode());
        assertEquals("CA", dto.getAddress().getStateProvince().getIsoCode());

        // and it must still be there when forwarded to the portal
        String forwarded = mapper.writeValueAsString(dto);
        assertTrue("address must be forwarded to the portal, not dropped",
                forwarded.contains("\"isoCode\":\"US\"")
                        && forwarded.contains("\"isoCode\":\"CA\""));
    }

    @SuppressWarnings("unchecked")
    private List<String> sortedFieldNames(Object dto) throws Exception {

        String json = mapper.writeValueAsString(dto);
        TreeMap<String, Object> fields = mapper.readValue(json, TreeMap.class);
        return new java.util.ArrayList<>(fields.keySet());
    }
}
