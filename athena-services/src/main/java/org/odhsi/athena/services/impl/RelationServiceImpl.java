package org.odhsi.athena.services.impl;

import org.odhsi.athena.dao.RelationWithConceptDAO;
import org.odhsi.athena.dto.BrowserRelationWithConceptPagingResultDTO;
import org.odhsi.athena.dto.BrowserRelationWithConceptTableDTO;
import org.odhsi.athena.entity.RelationWithConcept;
import org.odhsi.athena.services.RelationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GMalikov on 21.07.2015.
 */
public class RelationServiceImpl implements RelationService {

    @Autowired
    private RelationWithConceptDAO relationWithConceptDAO;

    @Override
    public BrowserRelationWithConceptPagingResultDTO getPagingRelationsForBrowser(int draw, int start, int length, String sortOrder, String searchValue, Long conceptId) {
        List<RelationWithConcept> relationWithConcepts = relationWithConceptDAO.getRelationsForBrowserPagingResult(start, length, checkSortOrder(sortOrder), searchValue, conceptId);
        BrowserRelationWithConceptPagingResultDTO result = new BrowserRelationWithConceptPagingResultDTO();
        List<BrowserRelationWithConceptTableDTO> relationsDTO = new ArrayList<>();
        for(RelationWithConcept relationWithConcept : relationWithConcepts){
            relationsDTO.add(new BrowserRelationWithConceptTableDTO(relationWithConcept));
        }
        result.setData(relationsDTO);
        result.setRecordsTotal(relationWithConceptDAO.getTotalRelationsWithConcepts());
        result.setRecordsFiltered(relationWithConceptDAO.getFilteredRelationsForBrowser(searchValue, conceptId));
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
