package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.exceptions.ValidationException;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.DownloadItem;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadBundleRepository;
import com.odysseusinc.athena.repositories.athena.DownloadItemRepository;
import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.VocabularyConversionService;
import com.odysseusinc.athena.util.CDMVersion;
import com.odysseusinc.athena.util.DownloadBundleStatus;
import com.odysseusinc.athena.util.Fn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Bundle generation is expensive and shared, so one user may only have a bounded number of
 * bundles waiting at once, and a request cannot multiply its download items by repeating ids.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BundleRequestLimitTest {

    private static final long USER_ID = 9L;

    @InjectMocks
    private VocabularyServiceImpl vocabularyService;

    @Mock
    private DownloadBundleRepository downloadBundleRepository;
    @Mock
    private DownloadItemRepository downloadItemRepository;
    @Mock
    private DownloadBundleService downloadBundleService;
    @Mock
    private VocabularyConversionService vocabularyConversionService;

    private final AthenaUser user = Fn.create(AthenaUser::new, u -> u.setId(USER_ID));

    @BeforeEach
    void setUp() {

        when(downloadBundleService.initBundle(any(), any(AthenaUser.class), any(), any(), anyBoolean(), any()))
                .thenAnswer(i -> new DownloadBundle());
        when(downloadBundleRepository.save(any(DownloadBundle.class))).thenAnswer(i -> i.getArgument(0));
        when(downloadItemRepository.saveAll(any())).thenAnswer(i -> new ArrayList<>((List<?>) i.getArgument(0)));
        when(vocabularyConversionService.findByOmopReqIsNotNull())
                .thenReturn(new ArrayList<>(Collections.singletonList(new VocabularyConversion(1))));
        when(vocabularyConversionService.getUnavailableVocabularies(anyLong(), anyBoolean()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    void aUserAtTheLimitCannotQueueAnotherBundle() {

        when(downloadBundleRepository.countByUserIdAndStatus(USER_ID, DownloadBundleStatus.PENDING)).thenReturn(5L);

        assertThrows(ValidationException.class, () -> save(Arrays.asList(2, 3)));

        verify(downloadBundleService, never()).initBundle(any(), any(AthenaUser.class), any(), any(), anyBoolean(), any());
    }

    @Test
    void aUserBelowTheLimitCanQueueABundle() {

        when(downloadBundleRepository.countByUserIdAndStatus(USER_ID, DownloadBundleStatus.PENDING)).thenReturn(4L);

        save(Arrays.asList(2, 3));

        verify(downloadItemRepository).saveAll(any());
    }

    @Test
    void repeatedIdsProduceOneDownloadItemEach() {

        // 1 is OMOP-required and requested again; 2 is requested three times.
        save(Arrays.asList(1, 2, 2, 2));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DownloadItem>> items = ArgumentCaptor.forClass(List.class);
        verify(downloadItemRepository).saveAll(items.capture());
        List<Integer> ids = items.getValue().stream()
                .map(item -> item.getVocabularyConversion().getIdV4())
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(1, 2), ids);
    }

    private void save(List<Integer> ids) {

        vocabularyService.saveBundle("bundle", ids, user, CDMVersion.V5, 20260101, false, null);
    }
}
