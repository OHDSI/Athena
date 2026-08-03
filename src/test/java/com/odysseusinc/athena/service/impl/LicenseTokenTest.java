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

import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.model.athena.License;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.util.extractor.LicenseStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LicenseTokenTest {

    private static final long LICENSE_ID = 7L;

    @InjectMocks
    private LicenseServiceImpl licenseService;

    @Mock
    private VocabularyService vocabularyService;

    private License pendingLicense() {

        return new License(new AthenaUser(), new VocabularyConversion(1), LicenseStatus.PENDING);
    }

    /**
     * Models a pre-migration database: the lookup <em>succeeds</em> for a blank token,
     * because the row really does store {@code ''}. Stubbing it to return nothing would
     * make these tests pass with or without the guard.
     */
    private void databaseMatchesBlankTokens() {

        when(vocabularyService.get(eq(LICENSE_ID), any())).thenReturn(pendingLicense());
    }

    /** The exploit: the empty token every pre-migration row carries. */
    @Test
    void anEmptyTokenIsRejectedEvenWhenARowMatchesIt() {

        databaseMatchesBlankTokens();

        assertThrows(NotExistException.class, () -> licenseService.checkLicense(LICENSE_ID, ""));
    }

    @Test
    void aWhitespaceTokenIsRejectedEvenWhenARowMatchesIt() {

        databaseMatchesBlankTokens();

        assertThrows(NotExistException.class, () -> licenseService.checkLicense(LICENSE_ID, "   "));
    }

    @Test
    void aNullTokenIsRejectedEvenWhenARowMatchesIt() {

        databaseMatchesBlankTokens();

        assertThrows(NotExistException.class, () -> licenseService.checkLicense(LICENSE_ID, null));
    }

    /**
     * The guard must short-circuit before the lookup — otherwise a repository that treats
     * blank and null alike could still match a row.
     */
    @Test
    void aBlankTokenNeverReachesTheDatabase() {

        assertThrows(NotExistException.class, () -> licenseService.checkLicense(LICENSE_ID, ""));

        verify(vocabularyService, never()).get(anyLong(), any());
    }

    /** A genuine token must still work — the fix must not break the e-mail links. */
    @Test
    void aRealTokenIsStillAccepted() {

        when(vocabularyService.get(eq(LICENSE_ID), eq("3f7a2b19c4d84e6f"))).thenReturn(pendingLicense());

        assertDoesNotThrow(() -> licenseService.checkLicense(LICENSE_ID, "3f7a2b19c4d84e6f"));
    }

    /** A well-formed but wrong token still has to be refused. */
    @Test
    void anUnknownTokenIsRejected() {

        when(vocabularyService.get(anyLong(), any())).thenReturn(null);

        assertThrows(NotExistException.class,
                () -> licenseService.checkLicense(LICENSE_ID, "0000000000000000"));
    }
}
