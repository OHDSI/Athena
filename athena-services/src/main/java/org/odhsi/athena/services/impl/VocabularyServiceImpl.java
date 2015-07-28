package org.odhsi.athena.services.impl;

import org.odhsi.athena.dao.VocabularyBuildLogDAO;
import org.odhsi.athena.dao.VocabularyDAO;
import org.odhsi.athena.dto.*;
import org.odhsi.athena.entity.Vocabulary;
import org.odhsi.athena.entity.VocabularyBuildLog;
import org.odhsi.athena.exceptions.MissingVocabularyAttributeException;
import org.odhsi.athena.exceptions.VocabularyNotFoundException;
import org.odhsi.athena.services.VocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by GMalikov on 14.05.2015.
 */
public class VocabularyServiceImpl implements VocabularyService {
    @Autowired
    private VocabularyDAO vocabularyDAO;

    @Autowired
    private VocabularyBuildLogDAO vocabularyBuildLogDAO;

    private DocumentBuilderFactory factory;

    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyServiceImpl.class);

    private static final String LOG_STATUS_ALL = "All";

    private static final String LOG_STATUS_ERRORS = "Errors";

    private static final String LOG_STATUS_SUCESSFUL = "Successful";

    private static final String VOCABULARY_STATUS_ALL = "All";

    private static final String VOCABULARY_STATUS_AVAILABLE = "Available";

    private static final String VOCABULARY_STATUS_UNAVAILABLE = "Unavailable";

    private static final String VOCABULARY_STATUS_READY = "Ready";

    private static final String VOCABULARY_STATUS_FAILED = "Failed";

    @Override
    public Vocabulary getById(String id) {
        return vocabularyDAO.getVocabularyById(id);
    }

    @Override
    public void buildVocabulary(String id) throws VocabularyNotFoundException, MissingVocabularyAttributeException {
        Vocabulary vocabulary = vocabularyDAO.getVocabularyById(id);
        if (vocabulary == null) {
            throw new VocabularyNotFoundException("Requested vocabulary with id: " + id + " not found");
        }
        if (vocabulary.getVersion() == null) {
            throw new MissingVocabularyAttributeException("Requested vocabulary with id: " + id + " does not have a version");
        }
        Date latestUpdate = vocabularyDAO.getLatestUpdateFromConversion(id);
        if (latestUpdate == null) {
            throw new MissingVocabularyAttributeException("Requested vocabulary with id: " + id + " does not have a latestUpdate");
        } else {
            vocabulary.setLatestUpdate(latestUpdate);
        }

        vocabularyDAO.buildVocabulary(vocabulary.getId(), vocabulary.getVersion(), vocabulary.getLatestUpdate());
    }

    @Override
    public List<Vocabulary> getAllVocabularies() {
        return vocabularyDAO.getAllVocabularies();
    }

    @Override
    public List<VocabularyStatusDTO> getAllVocabularyStatuses(String filter) {
        List<Vocabulary> vocabularies = getAllVocabularies();
        List<VocabularyStatusDTO> result = new ArrayList<>();
        for (Vocabulary current : vocabularies) {
            result.add(makeDTOWithCurrentStatus(current));
        }
        return filterResults(result, filter);
    }

    private List<VocabularyStatusDTO> filterResults(List<VocabularyStatusDTO> statusDTOList, String filter) {
        List<VocabularyStatusDTO> result = new ArrayList<>();
        if (VOCABULARY_STATUS_ALL.equals(filter)) {
            return statusDTOList;
        }
        for (VocabularyStatusDTO current : statusDTOList) {
            switch (filter) {
                case VOCABULARY_STATUS_AVAILABLE:
                    if (!VocabularyStatusDTO.NOT_AVAILABLE.equals(current.getStatusName())) {
                        result.add(current);
                    }
                    break;
                case VOCABULARY_STATUS_UNAVAILABLE:
                    if (VocabularyStatusDTO.NOT_AVAILABLE.equals(current.getStatusName())) {
                        result.add(current);
                    }
                    break;
                case VOCABULARY_STATUS_READY:
                    if (VocabularyStatusDTO.READY.equals(current.getStatusName()) || VocabularyStatusDTO.READY_WITH_NOTICES.equals(current.getStatusName())) {
                        result.add(current);
                    }
                    break;
                case VOCABULARY_STATUS_FAILED:
                    if (VocabularyStatusDTO.FAILED.equals(current.getStatusName())) {
                        result.add(current);
                    }
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    @Override
    public List<VocabularyBuildLogDTO> getLogForVocabulary(String vocabularyId, String filter) {
        List<VocabularyBuildLog> vocabularyBuildLogs = new ArrayList<>();
        if (LOG_STATUS_ERRORS.equals(filter)) {
            vocabularyBuildLogs.addAll(vocabularyBuildLogDAO.getErrorLogsForVocabulary(vocabularyId));
        } else if (LOG_STATUS_SUCESSFUL.equals(filter)) {
            vocabularyBuildLogs.addAll(vocabularyBuildLogDAO.getSuccessLogsForVocabulary(vocabularyId));
        } else {
            vocabularyBuildLogs.addAll(vocabularyBuildLogDAO.getAllLogsForVocabulary(vocabularyId));
        }
        List<VocabularyBuildLogDTO> result = new ArrayList<>();
        for (VocabularyBuildLog current : vocabularyBuildLogs) {
            result.add(new VocabularyBuildLogDTO(current));
        }
        return result;
    }

    private VocabularyStatusDTO makeDTOWithCurrentStatusXML(Vocabulary vocabulary) {
        VocabularyStatusDTO dto = new VocabularyStatusDTO(vocabulary);
        if (this.factory == null) {
            this.factory = DocumentBuilderFactory.newInstance();
        }
        String statusXML = vocabularyDAO.getVocabularyStatus(vocabulary.getId());
        statusXML = statusXML.replace("&lt;", "<");
        statusXML = statusXML.replace("&gt;", ">");
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(statusXML));
            Document document = builder.parse(is);
            dto.setOpNumber(document.getElementsByTagName("op_number").item(0).getTextContent());
            dto.setDescription(document.getElementsByTagName("description").item(0).getTextContent());
            if (!StringUtils.isEmpty(document.getElementsByTagName("status").item(0).getTextContent())) {
                dto.setStatus(document.getElementsByTagName("status").item(0).getTextContent());
            }
            dto.setStatusName(getStatusNameForDTO(dto.getStatus()));
            dto.setDetail(document.getElementsByTagName("detail").item(0).getTextContent());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("makeDTOWithCurrentStatusXML(Vocabulary vocabulary) failed.", e);
            return dto;
        }
        return dto;
    }

    private VocabularyStatusDTO makeDTOWithCurrentStatus(Vocabulary vocabulary) {
        VocabularyStatusDTO dto = new VocabularyStatusDTO(vocabulary);
        dto.setStatus(vocabularyBuildLogDAO.getVocabularyStatus(vocabulary.getId()));
        dto.setStatusName(getStatusNameForDTO(dto.getStatus()));
        return dto;
    }

    private String getStatusNameForDTO(String status) {
        if (status == null) {
            return VocabularyStatusDTO.NOT_AVAILABLE;
        }
        switch (status) {
            case "0":
                return VocabularyStatusDTO.BUILD_IN_PROGRESS;
            case "1":
                return VocabularyStatusDTO.READY;
            case "2":
                return VocabularyStatusDTO.READY_WITH_NOTICES;
            case "3":
                return VocabularyStatusDTO.FAILED;
            default:
                return VocabularyStatusDTO.NOT_AVAILABLE;
        }
    }

    @Override
    public VocabularyInfoDTO getInfoForVocabulary(String vocabularyId) {
        Vocabulary vocabulary = vocabularyDAO.getVocabularyById(vocabularyId);
        VocabularyInfoDTO result = new VocabularyInfoDTO();
        result.setRecordsCount(vocabularyDAO.getRecordsTotalForVocabulary(vocabularyId));
        result.setDomainsCount(vocabularyDAO.getDomainsCountForVocabulary(vocabularyId));
        result.setConceptsCount(vocabularyDAO.getConceptsCountForVocabulary(vocabularyId));
        result.setRelationsCount(vocabularyDAO.getRelationsCountForVocabulary(vocabularyId));
        result.setLastUpdated(vocabularyDAO.getLatestUpdateFromConversion(vocabularyId));
        result.setName(vocabulary.getId());
        result.setSourceName(vocabulary.getName());
        result.setId(vocabulary.getId());
        return result;
    }

    @Override
    public BrowserVocabularyPagingResultDTO getVocabulariesForBrowserTable(int start, int length, int page, String sortOrder, String searchVal) {
        List<Vocabulary> vocabularies = vocabularyDAO.getVocabulariesForBrowserTable(start, length, checkSortOrder(sortOrder), searchVal);
        BrowserVocabularyPagingResultDTO result = new BrowserVocabularyPagingResultDTO();
        List<BrowserVocabularyTableDTO> vocabulariesDTO = new ArrayList<>();
        for(Vocabulary vocabulary : vocabularies){
            vocabulariesDTO.add(new BrowserVocabularyTableDTO(vocabulary));
        }
        result.setData(vocabulariesDTO);
        result.setRecords(vocabularyDAO.getFilteredVocabulariesCountForBrowserTable(searchVal));
        result.setTotalPages(calculateTotalPages(result.getRecords(), length));
        result.setPage(page);
        return result;
    }

    private long calculateTotalPages(long recordsTotal, long recordsPerPage){
        long result = 0;
        if(recordsPerPage > 0){
            result = recordsTotal/recordsPerPage;
            if (recordsTotal%recordsPerPage > 0){
                result = result +1;
            }
        }
        return result;
    }

    private String checkSortOrder(String sortOrder){
        if("asc".equals(sortOrder) || "desc".equals(sortOrder)){
            return sortOrder;
        } else {
            return "desc";
        }
    }
}
