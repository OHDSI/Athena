package org.odhsi.athena.services.impl;

import org.odhsi.athena.dao.VocabularyBuildLogDAO;
import org.odhsi.athena.dao.VocabularyDAO;
import org.odhsi.athena.dto.VocabularyBuildLogDTO;
import org.odhsi.athena.dto.VocabularyStatusDTO;
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
    public List<VocabularyStatusDTO> getAllVocabularyStatuses() {
        List<Vocabulary> vocabularies = getAllVocabularies();
        List<VocabularyStatusDTO> result = new ArrayList<>();
        for (Vocabulary current : vocabularies) {
            result.add(makeDTOWithCurrentStatus(current));
        }
        return result;
    }

    @Override
    public List<VocabularyBuildLogDTO> getLogForVocabulary(String vocabularyId) {
        List<VocabularyBuildLog> vocabularyBuildLogs = vocabularyBuildLogDAO.getLogForVocabulary(vocabularyId);
        List<VocabularyBuildLogDTO> result = new ArrayList<>();
        for (VocabularyBuildLog current : vocabularyBuildLogs) {
            result.add(new VocabularyBuildLogDTO(current));
        }
        return result;
    }

    private VocabularyStatusDTO makeDTOWithCurrentStatus(Vocabulary vocabulary) {
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
            LOGGER.error("makeDTOWithCurrentStatus(Vocabulary vocabulary) failed.", e);
            return dto;
        }
        return dto;
    }

    private String getStatusNameForDTO(String status) {
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

}
