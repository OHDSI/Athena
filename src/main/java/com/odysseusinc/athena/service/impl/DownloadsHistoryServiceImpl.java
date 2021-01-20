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
import com.opencsv.CSVWriter;
import lombok.SneakyThrows;
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
import java.util.Objects;
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
        List<DownloadHistory> bundleHistory = downloadHistoryRepository.findByDownloadTimeBetweenOrderByDownloadTimeAsc(from, to);

        log.trace("map to History: {}, count: {}", LocalDateTime.now(), bundleHistory.size());
        Set<DownloadHistoryDTO> itemHistory = bundleHistory.stream().parallel()
                .flatMap(history -> mapHistory(history, licensedOnly, keywords))
                .collect(Collectors.toSet());

        log.trace("END: retrieveStatistics: {}", LocalDateTime.now());
        return itemHistory;
    }

    @Override
    public void generateCSV(Collection<DownloadHistoryDTO> records, OutputStream osw) throws IOException {

        String name = fileHelper.getTempPath(UUID.randomUUID().toString());
        File temp = new File(name);

        try (CSVWriter csvWriter = new AthenaCSVWriter(name, separator)) {

            csvWriter.writeNext(new String[]{"vocabulary", "date", "user", "email", "organization"}, false);

            writeAll(csvWriter, records);

            csvWriter.flush(true);
        } finally {
            Files.copy(temp.toPath(), osw);
            Files.delete(temp.toPath());
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

    private void writeAll(CSVWriter csvWriter, Collection<DownloadHistoryDTO> records) throws IOException {

        DownloadHistoryExtractor extractor = new DownloadHistoryExtractor();
        csvWriter.writeAll(new ArrayList<>(extractor.extractForAll(records)));
    }

    private Cache<Long,AthenaUser> userCache= CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    @SneakyThrows
    private AthenaUser getUserFromCache(Long userId){
        return userCache.get(userId, () -> userService.get(userId));
    }

    private Stream<DownloadHistoryDTO> mapHistory(DownloadHistory history, boolean licensedOnly, String[] keywords) {

        final AthenaUser athenaUser = getUserFromCache(history.getUserId());
        final List<DownloadItem> vocabularies = history.getVocabularyBundle().getVocabularies();
        final LocalDateTime downloadDate = history.getDownloadTime().truncatedTo(ChronoUnit.DAYS);

        return vocabularies.stream()
                .parallel()
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

        final String userName = String.format("%s, %s", athenaUser.getFirstName(), athenaUser.getLastName());
        DownloadHistoryDTO dto = new DownloadHistoryDTO();
        dto.setUserName(userName);
        dto.setOrganization(athenaUser.getOrganization());
        dto.setCode(vocabularyConversion.getIdV5());
        dto.setDate(downloadDate);
        dto.setEmail(athenaUser.getEmail());
        return dto;
    }

    private boolean licenseOnly(DownloadItem vocab, boolean licensedOnly) {

        return !licensedOnly || StringUtils.isNotBlank(vocab.getVocabularyConversion().getAvailable());
    }

    private static Comparator<DownloadHistoryDTO> pickComparator(String sortBy) {

        switch (sortBy) {
            case "email":
                return (a, b) -> Objects.compare(a.getEmail(), b.getEmail(), String::compareTo);
            case "date":
                return (a, b) -> Objects.compare(a.getDate(), b.getDate(), LocalDateTime::compareTo);
            case "userName":
                return (a, b) -> Objects.compare(a.getUserName(), b.getUserName(), String::compareTo);
            case "organization":
                return (a, b) -> Objects.compare(a.getOrganization(), b.getOrganization(), String::compareTo);
            default:
                return (a, b) -> Objects.compare(a.getCode(), b.getCode(), String::compareTo);
        }
    }
}
