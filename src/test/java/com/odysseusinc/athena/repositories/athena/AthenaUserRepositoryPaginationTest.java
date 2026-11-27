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
 */

package com.odysseusinc.athena.repositories.athena;

import com.odysseusinc.athena.TestConfiguration;
import com.odysseusinc.athena.glue.LocalEnvironmentInitializer;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.util.extractor.LicenseStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(initializers = LocalEnvironmentInitializer.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestConfiguration.class,
        properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles("test")
@Transactional("athenaTransactionManager")
class AthenaUserRepositoryPaginationTest {

    private static final int MATCHING_USERS = 8;
    private static final int PAGE_SIZE = 3;

    @Autowired
    private AthenaUserRepository userRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private VocabularyConversionRepository vocabularyConversionRepository;

    @PersistenceContext(unitName = "ATHENA_DB")
    private EntityManager entityManager;

    @Test
    void nativeLicenseQueryAppliesPageSizeAndOffset() {

        VocabularyConversion vocabulary = vocabularyConversionRepository.findById(15).orElseThrow();
        for (int index = 0; index < MATCHING_USERS; index++) {
            saveUserWithLicense("pagination-user-" + index, vocabulary, LicenseStatus.PENDING, index);
        }
        entityManager.flush();
        entityManager.clear();

        Page<AthenaUser> secondPage = userRepository.getUsersWithLicenses(
                "%pagination-user%", false, PageRequest.of(1, PAGE_SIZE));

        assertEquals(PAGE_SIZE, secondPage.getNumberOfElements());
        assertEquals(MATCHING_USERS, secondPage.getTotalElements());
        assertEquals(3, secondPage.getTotalPages());
        assertEquals(1, secondPage.getNumber());
    }

    @Test
    void pendingFilterExcludesUsersWithApprovedLicensesOnly() {

        VocabularyConversion vocabulary = vocabularyConversionRepository.findById(15).orElseThrow();
        saveUserWithLicense("status-filter-pending", vocabulary, LicenseStatus.PENDING, 2);
        saveUserWithLicense("status-filter-approved", vocabulary, LicenseStatus.APPROVED, 1);
        entityManager.flush();
        entityManager.clear();

        Page<AthenaUser> allUsers = userRepository.getUsersWithLicenses(
                "%status-filter-%", false, PageRequest.of(0, PAGE_SIZE));
        Page<AthenaUser> pendingUsers = userRepository.getUsersWithLicenses(
                "%status-filter-%", true, PageRequest.of(0, PAGE_SIZE));

        assertEquals(2, allUsers.getTotalElements());
        assertEquals(1, pendingUsers.getTotalElements());
        assertEquals("status-filter-pending", pendingUsers.getContent().getFirst().getUsername());
    }

    @Test
    void userSuggestionsMatchEmailAddress() {

        AthenaUser user = new AthenaUser();
        user.setUsername("email-suggestion-user");
        user.setEmail("distinct-address@example.com");
        user.setFirstName("Completely");
        user.setLastName("Different");
        user.setOrigin("test");
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        List<AthenaUser> suggestions = userRepository.suggestUsers("%distinct-address%");

        assertEquals(1, suggestions.size());
        assertEquals("email-suggestion-user", suggestions.getFirst().getUsername());
    }

    private void saveUserWithLicense(String username, VocabularyConversion vocabulary,
                                     LicenseStatus status, int activityOrder) {

        AthenaUser user = new AthenaUser();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setFirstName("Pagination");
        user.setLastName("User " + activityOrder);
        user.setOrigin("test");
        user = userRepository.save(user);

        License license = new License(user, vocabulary, status);
        license.setRequestDate(new Date(activityOrder * 1_000L));
        licenseRepository.save(license);
    }
}
