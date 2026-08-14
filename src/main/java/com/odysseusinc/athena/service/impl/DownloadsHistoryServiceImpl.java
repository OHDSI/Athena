/*
 *
 * Copyright 2020 Odysseus Data Services, inc.
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
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Alexandr Cumarav
 * Created: March 20, 2020
 *
 */

package com.odysseusinc.athena.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.odysseusinc.athena.api.v1.controller.dto.DownloadHistoryDTO;
import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.model.athena.DownloadHistory;
import com.odysseusinc.athena.model.athena.DownloadItem;
import com.odysseusinc.athena.model.athena.VocabularyConversion;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.repositories.athena.DownloadHistoryRepository;
import com.odysseusinc.athena.service.DownloadsHistoryService;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.util.extractor.DownloadHistoryExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional
@Service
@Slf4j
public class DownloadsHistoryServiceImpl implements DownloadsHistoryService {

    private final DownloadHistoryRepository downloadHistoryRepository;
    private final UserService userService;
    private final Character separator;
    private final FileHelper fileHelper;

    public DownloadsHistoryServiceImpl(DownloadHistoryRepository downloadHistoryRepository, UserService userService, @Value("${csv.separator:;}") Character separator, FileHelper fileHelper) {

        this.downloadHistoryRepository = downloadHistoryRepository;
        this.userService = userService;
        this.separator = separator;
        this.fileHelper = fileHelper;
    }

    @Override
    public void updateStatistics(DownloadBundle bundle, Long userId) {

        DownloadHistory downloadRecord = new DownloadHistory();
        downloadRecord.setUserId(userId);
        downloadRecord.setVocabularyBundle(bundle);
        downloadRecord.setDownloadTime(LocalDateTime.now());

        downloadHistoryRepository.save(downloadRecord);
    }

    @Override
    public Collection<DownloadHistoryDTO> retrieveStatistics(LocalDateTime from, LocalDateTime to, Boolean licensedOnly, String[] keywords) {

        log.trace("START: retrieveStatistics: {}", LocalDateTime.now());
        // fetch-joins bundle -> items -> conversion, the path mapHistory walks.
        // Previously one query per bundle and per item followed this one.
        List<DownloadHistory> bundleHistory = downloadHistoryRepository.findForStatistics(from, to);

        log.trace("map to History: {}, count: {}", LocalDateTime.now(), bundleHistory.size());
        // deliberately sequential. mapHistory walks lazy Hibernate associations
        // (bundle -> items -> conversion); a Session is not thread safe, so running this on
        // the common ForkJoinPool risked LazyInitializationException and corrupted session
        // state under load. Replacing the in-memory expansion with a query is still outstanding.
        Set<DownloadHistoryDTO> itemHistory = bundleHistory.stream()
                .flatMap(history -> mapHistory(history, licensedOnly, keywords))
                .collect(Collectors.toSet());

        log.trace("END: retrieveStatistics: {}", LocalDateTime.now());
        return itemHistory;
    }

    @Override
    public void generateCSV(Collection<DownloadHistoryDTO> records, OutputStream osw) throws IOException {

        String name = fileHelper.getTempPath(UUID.randomUUID().toString());
        File temp = new File(name);

        // an administrator opens this in a spreadsheet, and the user/email/organization
        // columns carry values supplied at registration.
        try (AthenaCSVWriter csvWriter = new AthenaCSVWriter(name, separator, true)) {

            csvWriter.writeNext(new String[]{"vocabulary", "date", "user", "email", "organization"}, false);

            writeAll(csvWriter, records);

            csvWriter.flush(true);
        }
        // See SearchServiceImpl: copying inside a finally streamed partial output
        // and masked the original failure with NoSuchFileException.
        try {
            Files.copy(temp.toPath(), osw);
        } finally {
            Files.deleteIfExists(temp.toPath());
        }
    }

    @Override
    public Collection<DownloadHistoryDTO> sort(Collection<DownloadHistoryDTO> dtos, String sortBy, boolean sortAsc) {

        Comparator<DownloadHistoryDTO> comparator = pickComparator(sortBy);
        final List<DownloadHistoryDTO> sortedDtos = dtos.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        if (!sortAsc) {
            return Lists.reverse(sortedDtos);
        }
        return sortedDtos;
    }

    private void writeAll(AthenaCSVWriter csvWriter, Collection<DownloadHistoryDTO> records) throws IOException {

        DownloadHistoryExtractor extractor = new DownloadHistoryExtractor();
        csvWriter.writeAll(new ArrayList<>(extractor.extractForAll(records)));
    }

    private Cache<Long,AthenaUser> userCache= CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    /**
     * {@code Cache.get(key, loader)} throws {@code InvalidCacheLoadException} when
     * the loader returns null, and {@code userService.get} returns null for a user that no
     * longer exists — so a single deleted downloader made the whole admin statistics
     * endpoint fail. Absent users are now simply not cached and reported as null.
     */
    private AthenaUser getUserFromCache(Long userId) {

        if (userId == null) {
            return null;
        }
        AthenaUser cached = userCache.getIfPresent(userId);
        if (cached != null) {
            return cached;
        }
        AthenaUser user = userService.get(userId);
        if (user != null) {
            userCache.put(userId, user);
        }
        return user;
    }

    private Stream<DownloadHistoryDTO> mapHistory(DownloadHistory history, boolean licensedOnly, String[] keywords) {

        final AthenaUser athenaUser = getUserFromCache(history.getUserId());
        final List<DownloadItem> vocabularies = history.getVocabularyBundle().getVocabularies();
        final LocalDateTime downloadDate = history.getDownloadTime().truncatedTo(ChronoUnit.DAYS);

        return vocabularies.stream()
                .filter(this::isOmopRequired)
                .filter(vocab -> licenseOnly(vocab, licensedOnly))
                .map(vocab -> createDto(vocab.getVocabularyConversion(), athenaUser, downloadDate))
                .filter(vocabDto -> filterKeywords(vocabDto, keywords));
    }

    private boolean filterKeywords(DownloadHistoryDTO vocabDto, String[] keywords) {

        if (keywords == null || keywords.length == 0) {
            return true;
        }

        String vector = vocabDto.toString().toLowerCase();

        return Arrays.stream(keywords)
                .allMatch(vector::contains);
    }

    private boolean isOmopRequired(DownloadItem vocab) {

        return !vocab.getVocabularyConversion().getOmopReqValue();
    }

    private DownloadHistoryDTO createDto(VocabularyConversion vocabularyConversion, AthenaUser athenaUser, LocalDateTime downloadDate) {

        DownloadHistoryDTO dto = new DownloadHistoryDTO();
        // athenaUser is null when the account behind a historical download has since been
        // removed. The download still happened, so the row is kept with the user columns
        // blank rather than dropped, which would silently understate the statistics.
        if (athenaUser != null) {
            dto.setUserName(String.format("%s, %s", athenaUser.getFirstName(), athenaUser.getLastName()));
            dto.setOrganization(athenaUser.getOrganization());
            dto.setEmail(athenaUser.getEmail());
        }
        dto.setCode(vocabularyConversion.getIdV5());
        dto.setDate(downloadDate);
        return dto;
    }

    private boolean licenseOnly(DownloadItem vocab, boolean licensedOnly) {

        return !licensedOnly || StringUtils.isNotBlank(vocab.getVocabularyConversion().getAvailable());
    }

    /**
     * The user columns are null for a download whose account has since been deleted — see
     * {@link #createDto}, which keeps the row rather than dropping it. {@code Objects.compare}
     * does not help there: it short-circuits only when both sides are the <em>same</em>
     * reference, so comparing a null against a non-null still calls
     * {@code null.compareTo(other)} and throws. Sorting the statistics by user, e-mail or
     * organization therefore failed as soon as one deleted user appeared in the range.
     * Null sorts last in ascending order.
     */
    private static Comparator<DownloadHistoryDTO> pickComparator(String sortBy) {

        switch (sortBy) {
            case "email":
                return comparingNullsLast(DownloadHistoryDTO::getEmail);
            case "date":
                return comparingNullsLast(DownloadHistoryDTO::getDate);
            case "userName":
                return comparingNullsLast(DownloadHistoryDTO::getUserName);
            case "organization":
                return comparingNullsLast(DownloadHistoryDTO::getOrganization);
            default:
                return comparingNullsLast(DownloadHistoryDTO::getCode);
        }
    }

    private static <T extends Comparable<T>> Comparator<DownloadHistoryDTO> comparingNullsLast(
            Function<DownloadHistoryDTO, T> field) {

        return Comparator.comparing(field, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
