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
import com.odysseusinc.athena.exceptions.PermissionDeniedException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.DownloadShare;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadBundleRepository;
import com.odysseusinc.athena.repositories.athena.DownloadShareRepository;
import com.odysseusinc.athena.service.VocabularyReleaseVersionService;
import com.odysseusinc.athena.util.Fn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code POST /copy-and-generate} and {@code GET /check/{id}} took a bundle id
 * straight from the request and acted on it with no ownership check, so any authenticated
 * user could:
 * <ul>
 *   <li>clone another user's bundle — its vocabulary set, CDM version and delta
 *       configuration — by guessing a sequential id, and</li>
 *   <li>probe {@code /check/{id}}, whose {@code LicenseException} names the restricted
 *       vocabulary ids inside that bundle.</li>
 * </ul>
 * The licence check that was already there does not close this: it only gates vocabularies
 * the <em>caller</em> lacks a licence for, so everything unrestricted was copied freely.
 * <p>
 * Access is owner-or-shared rather than owner-only because a user a bundle was shared with
 * already sees it in their download history and can download it.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BundleOwnershipTest {

    private static final long BUNDLE_ID = 42L;

    @InjectMocks
    private VocabularyServiceImpl vocabularyService;

    @Mock
    private DownloadBundleRepository downloadBundleRepository;

    @Mock
    private DownloadShareRepository downloadShareRepository;

    /** Read at the top of copyBundle, before the bundle is even loaded. */
    @Mock
    private VocabularyReleaseVersionService vocabularyReleaseVersionService;

    private static AthenaUser user(long id, String email) {

        return Fn.create(AthenaUser::new, u -> {
            u.setId(id);
            u.setEmail(email);
        });
    }

    private DownloadBundle bundleOwnedBy(long ownerId) {

        DownloadBundle bundle = Fn.create(DownloadBundle::new, b -> {
            b.setId(BUNDLE_ID);
            b.setUserId(ownerId);
        });
        when(downloadBundleRepository.findById(BUNDLE_ID)).thenReturn(Optional.of(bundle));
        return bundle;
    }

    @Test
    void aStrangerCannotCopyAnotherUsersBundle() {

        bundleOwnedBy(1L);

        assertThrows(PermissionDeniedException.class, () ->
                vocabularyService.copyBundle(BUNDLE_ID, "stolen", user(2L, "stranger@example.com")));
    }

    /** The copy must be refused before any bundle is created, not cleaned up afterwards. */
    @Test
    void aRefusedCopyCreatesNothing() {

        bundleOwnedBy(1L);

        assertThrows(PermissionDeniedException.class, () ->
                vocabularyService.copyBundle(BUNDLE_ID, "stolen", user(2L, "stranger@example.com")));

        verify(downloadBundleRepository, never()).save(any());
    }

    @Test
    void aStrangerCannotProbeAnotherUsersBundle() {

        bundleOwnedBy(1L);
        when(downloadShareRepository.findByBundle(any())).thenReturn(Collections.emptyList());

        assertThrows(PermissionDeniedException.class, () ->
                vocabularyService.checkBundleAndSharedUser(user(2L, "stranger@example.com"), BUNDLE_ID));
    }

    @Test
    void theOwnerIsStillAllowed() {

        bundleOwnedBy(1L);

        assertDoesNotThrow(() ->
                vocabularyService.checkBundleAndSharedUser(user(1L, "owner@example.com"), BUNDLE_ID));
    }

    /** Shared recipients already see the bundle in their history — they must keep access. */
    @Test
    void aUserTheBundleWasSharedWithIsStillAllowed() {

        DownloadBundle bundle = bundleOwnedBy(1L);
        DownloadShare share = Fn.create(DownloadShare::new, s -> {
            s.setBundle(bundle);
            s.setUserEmail("friend@example.com");
        });
        when(downloadShareRepository.findByBundle(bundle)).thenReturn(Collections.singletonList(share));

        assertDoesNotThrow(() ->
                vocabularyService.checkBundleAndSharedUser(user(2L, "friend@example.com"), BUNDLE_ID));
    }

    /** A share with a *different* user must not grant access. */
    @Test
    void aShareWithSomebodyElseGrantsNothing() {

        DownloadBundle bundle = bundleOwnedBy(1L);
        DownloadShare share = Fn.create(DownloadShare::new, s -> {
            s.setBundle(bundle);
            s.setUserEmail("friend@example.com");
        });
        when(downloadShareRepository.findByBundle(bundle)).thenReturn(Collections.singletonList(share));

        assertThrows(PermissionDeniedException.class, () ->
                vocabularyService.checkBundleAndSharedUser(user(3L, "stranger@example.com"), BUNDLE_ID));
    }

    @Test
    void anUnknownBundleIdIsReportedAsMissing() {

        when(downloadBundleRepository.findById(BUNDLE_ID)).thenReturn(Optional.empty());

        assertThrows(NotExistException.class, () ->
                vocabularyService.checkBundleAndSharedUser(user(1L, "owner@example.com"), BUNDLE_ID));
    }
}
