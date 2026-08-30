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

package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.exceptions.ValidationException;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.LicenseRepository;
import com.odysseusinc.athena.service.ConceptService;
import com.odysseusinc.athena.service.VocabularyConversionService;
import com.odysseusinc.athena.service.mail.EmailService;
import com.odysseusinc.athena.util.extractor.LicenseStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LicenseAdministrationTest {

    @InjectMocks
    private VocabularyServiceImpl vocabularyService;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private VocabularyConversionService vocabularyConversionService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private EmailService emailService;

    private static AthenaUser user(long id, String firstName, String lastName) {

        AthenaUser user = new AthenaUser();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(firstName.toLowerCase());
        return user;
    }

    private static License license(long id, LicenseStatus status) {

        License license = new License();
        license.setId(id);
        license.setStatus(status);
        license.setUser(user(10L, "Requesting", "User"));
        VocabularyConversion vocabulary = new VocabularyConversion();
        vocabulary.setName("Restricted vocabulary");
        license.setVocabularyConversion(vocabulary);
        return license;
    }

    @Test
    void directGrantRecordsAdministratorAndTime() {

        AthenaUser recipient = user(10L, "Requesting", "User");
        AthenaUser administrator = user(20L, "Jane", "Administrator");
        when(vocabularyConversionService.findByVocabularyV4Id(7))
                .thenReturn(new VocabularyConversion());
        when(licenseRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<License> licenses = vocabularyService.grantLicenses(
                recipient, List.of(7), administrator);

        License granted = licenses.getFirst();
        assertEquals(LicenseStatus.APPROVED, granted.getStatus());
        assertEquals(20L, granted.getGrantedByUserId());
        assertEquals("Jane Administrator", granted.getGrantedByName());
        assertNotNull(granted.getGrantedAt());
    }

    @Test
    void approvingRequestRecordsAdministratorAndSendsResolution() {

        License pending = license(42L, LicenseStatus.PENDING);
        AthenaUser administrator = user(20L, "Jane", "Administrator");
        when(licenseRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(pending));

        vocabularyService.acceptLicense(42L, true, administrator);

        assertEquals(LicenseStatus.APPROVED, pending.getStatus());
        assertEquals(20L, pending.getGrantedByUserId());
        assertEquals("Jane Administrator", pending.getGrantedByName());
        assertNotNull(pending.getGrantedAt());
        verify(emailService).sendLicenseAcceptance(
                pending.getUser(), true, "Restricted vocabulary");
    }

    @Test
    void cancellingPendingRequestDeletesWithoutNotification() {

        License pending = license(42L, LicenseStatus.PENDING);
        when(licenseRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(pending));

        vocabularyService.cancelPendingLicense(42L);

        verify(licenseRepository).deleteById(42L);
        verify(emailService, never()).sendLicenseAcceptance(
                pending.getUser(), false, "Restricted vocabulary");
    }

    @Test
    void approvedLicenseCannotBeCancelledAsARequest() {

        License approved = license(42L, LicenseStatus.APPROVED);
        when(licenseRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(approved));

        assertThrows(ValidationException.class,
                () -> vocabularyService.cancelPendingLicense(42L));

        verify(licenseRepository, never()).deleteById(42L);
    }

    @Test
    void pendingTotalCountsRequestsRatherThanUsers() {

        when(licenseRepository.countByStatus(LicenseStatus.PENDING)).thenReturn(17L);

        assertEquals(17L, vocabularyService.countPendingLicenses());
    }
}
