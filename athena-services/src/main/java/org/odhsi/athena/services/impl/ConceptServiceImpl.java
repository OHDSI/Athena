package org.odhsi.athena.services.impl;

import org.odhsi.athena.dao.ConceptDAO;
import org.odhsi.athena.dto.BrowserConceptPagingResultDTO;
import org.odhsi.athena.dto.BrowserConceptTableDTO;
import org.odhsi.athena.entity.Concept;
import org.odhsi.athena.services.ConceptService;
import org.odhsi.athena.util.DTOHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GMalikov on 20.07.2015.
 */
public class ConceptServiceImpl implements ConceptService {

    @Autowired
    private ConceptDAO conceptDAO;

    @Override
    public BrowserConceptPagingResultDTO getPagingConceptsForBrowser(int start, int length, int page, String searchValue, String sortOrder, String vocabularyId, String domainId) {
        List<Concept> concepts = conceptDAO.getPagingConceptsForBrowser(start, length, searchValue, checkSortOrder(sortOrder), vocabularyId, domainId);
        BrowserConceptPagingResultDTO result = new BrowserConceptPagingResultDTO();
        List<BrowserConceptTableDTO> conceptsDTO = new ArrayList<>();
        for (Concept concept : concepts){
            conceptsDTO.add(new BrowserConceptTableDTO(concept));
        }
        result.setData(conceptsDTO);
        result.setRecords(conceptDAO.getFilteredConceptsCountForBrowser(vocabularyId, domainId, searchValue));
        result.setTotalPages(DTOHelper.calculateTotalPages(result.getRecords(), length));
        result.setPage(page);
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
