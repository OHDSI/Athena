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

package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.AthenaUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Profile fields are updated from SAML assertion attributes on every login. An IdP that
 * omits an optional attribute must not wipe the stored value — the pac4j implementation
 * did, because {@code UserProfileUtil.getAttribute} returned null for an absent
 * attribute and that null was passed straight to the setter.
 * <p>
 * JUnit 4 on purpose.
 */
public class UserServiceProvisionTest {

    private UserService userService;
    private AthenaUser existing;

    @Before
    public void setUp() {

        existing = new AthenaUser();
        existing.setUsername("jane");
        existing.setOrigin("saml");
        existing.setEmail("jane@example.org");
        existing.setFirstName("Jane");
        existing.setLastName("Doe");
        existing.setMiddleName("Q");
        existing.setOrganization("Acme");

        AthenaUserRepository repository = mock(AthenaUserRepository.class);
        when(repository.findByUsernameIgnoreCaseAndOrigin(anyString(), anyString()))
                .thenReturn(existing);
        when(repository.save(any(AthenaUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService = new UserService();
        ReflectionTestUtils.setField(userService, "athenaUserRepository", repository);
        ReflectionTestUtils.setField(userService, "emailAttributeName", "email");
        ReflectionTestUtils.setField(userService, "firstNameAttributeName", "firstName");
        ReflectionTestUtils.setField(userService, "lastNameAttributeName", "lastName");
        ReflectionTestUtils.setField(userService, "middleNameAttributeName", "middleName");
        ReflectionTestUtils.setField(userService, "organizationAttributeName", "organization");
    }

    @Test
    public void anOmittedAttributeLeavesTheStoredValueAlone() {

        Map<String, String> onlyEmail = new HashMap<>();
        onlyEmail.put("email", "jane.new@example.org");

        AthenaUser result = userService.provisionUser("jane", "saml", onlyEmail);

        assertEquals("the supplied attribute is applied", "jane.new@example.org", result.getEmail());
        assertEquals("an omitted attribute must be preserved", "Jane", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Q", result.getMiddleName());
        assertEquals("Acme", result.getOrganization());
    }

    @Test
    public void suppliedAttributesOverwriteStoredValues() {

        Map<String, String> all = new HashMap<>();
        all.put("email", "j@example.org");
        all.put("firstName", "Janet");
        all.put("lastName", "Roe");
        all.put("middleName", "R");
        all.put("organization", "Globex");

        AthenaUser result = userService.provisionUser("jane", "saml", all);

        assertEquals("j@example.org", result.getEmail());
        assertEquals("Janet", result.getFirstName());
        assertEquals("Roe", result.getLastName());
        assertEquals("R", result.getMiddleName());
        assertEquals("Globex", result.getOrganization());
    }

    /**
     * An attribute present but explicitly empty is a value, not an omission, so it is
     * applied. Distinguishing the two is the whole point of the containsKey check.
     */
    @Test
    public void anExplicitlyEmptyAttributeIsApplied() {

        Map<String, String> emptyOrganization = new HashMap<>();
        emptyOrganization.put("organization", "");

        AthenaUser result = userService.provisionUser("jane", "saml", emptyOrganization);

        assertEquals("", result.getOrganization());
        assertEquals("unrelated fields untouched", "Jane", result.getFirstName());
    }

    /** No attributes at all must not blank the whole profile. */
    @Test
    public void anAssertionWithNoAttributesPreservesEverything() {

        AthenaUser result = userService.provisionUser("jane", "saml", new HashMap<>());

        assertEquals("jane@example.org", result.getEmail());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Q", result.getMiddleName());
        assertEquals("Acme", result.getOrganization());
    }

    /**
     * A configured attribute name that is itself null (an unset
     * {@code athena.security.saml.attributes.*} property) must be skipped, not used as a
     * map key.
     */
    @Test
    public void anUnconfiguredAttributeNameIsSkipped() {

        ReflectionTestUtils.setField(userService, "middleNameAttributeName", null);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("middleName", "ignored");

        AthenaUser result = userService.provisionUser("jane", "saml", attributes);

        assertEquals("Q", result.getMiddleName());
    }

    @Test
    public void aNewUserGetsItsOwnRoleCollection() {

        AthenaUserRepository repository = mock(AthenaUserRepository.class);
        when(repository.findByUsernameIgnoreCaseAndOrigin(anyString(), anyString())).thenReturn(null);
        when(repository.save(any(AthenaUser.class))).thenAnswer(i -> i.getArgument(0));
        ReflectionTestUtils.setField(userService, "athenaUserRepository", repository);
        ReflectionTestUtils.setField(userService, "defaultRolesValue", "");

        AthenaUser first = userService.provisionUser("new1", "saml", new HashMap<>());
        AthenaUser second = userService.provisionUser("new2", "saml", new HashMap<>());

        // Every user must own its collection, not share one cached instance.
        org.junit.Assert.assertNotSame("role collections must not be shared between users",
                first.getRoles(), second.getRoles());
        assertNull("no attributes supplied, so nothing is set", first.getEmail());
    }
}
