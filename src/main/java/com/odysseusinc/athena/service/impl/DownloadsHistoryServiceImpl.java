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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional
@Service
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

    private static DownloadHistoryDTO createDto(VocabularyConversion vocabularyConversion, AthenaUser athenaUser, LocalDateTime downloadDate) {

        final String userName = String.format("%s, %s", athenaUser.getFirstName(), athenaUser.getLastName());
        DownloadHistoryDTO dto = new DownloadHistoryDTO();
        dto.setOrganization(athenaUser.getOrganization());
        dto.setUserName(userName);
        dto.setOrganization(athenaUser.getOrganization());
        dto.setCode(vocabularyConversion.getIdV5());
        dto.setVocabularyName(vocabularyConversion.getName());
        dto.setDate(downloadDate);
        return dto;
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

        return downloadHistoryRepository.findByDownloadTimeBetweenOrderByDownloadTimeAsc(from, to)
                .flatMap(history -> mapHistory(history, licensedOnly, keywords))
                .collect(Collectors.toSet());
    }

    @Override
    public void generateCSV(Collection<DownloadHistoryDTO> records, OutputStream osw) throws IOException {

        String name = fileHelper.getTempPath(UUID.randomUUID().toString());
        File temp = new File(name);

        try (CSVWriter csvWriter = new AthenaCSVWriter(name, separator)) {

            csvWriter.writeNext(new String[]{"user", "organization", "code", "vocabulary", "date"}, false);

            writeAll(csvWriter, records);

            csvWriter.flush(true);
        } finally {
            Files.copy(temp.toPath(), osw);
            Files.delete(temp.toPath());
        }
    }

    private void writeAll(CSVWriter csvWriter, Collection<DownloadHistoryDTO> records) throws IOException {

        DownloadHistoryExtractor extractor = new DownloadHistoryExtractor();
        csvWriter.writeAll(new ArrayList<>(extractor.extractForAll(records)));
    }

    private Stream<DownloadHistoryDTO> mapHistory(DownloadHistory history, boolean licensedOnly, String[] keywords) {

        final AthenaUser athenaUser = userService.get(history.getUserId());
        final List<DownloadItem> vocabularies = history.getVocabularyBundle().getVocabularies();
        final LocalDateTime downloadDate = history.getDownloadTime().truncatedTo(ChronoUnit.DAYS);

        return vocabularies.stream()
                .filter(this::isOmopRequired)
                .filter(vocab -> licenseOnly(vocab, licensedOnly))
                .map(vocab -> createDto(vocab.getVocabularyConversion(), athenaUser, downloadDate))
                .filter(vocabDto -> filterKeywords(vocabDto, keywords));
    }

    private static boolean licenseOnly(DownloadItem vocab, boolean licensedOnly) {

        return !licensedOnly || StringUtils.isNotBlank(vocab.getVocabularyConversion().getAvailable());
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

}
