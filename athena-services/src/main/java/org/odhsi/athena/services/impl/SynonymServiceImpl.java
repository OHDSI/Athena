package org.odhsi.athena.services.impl;

import org.odhsi.athena.dao.SynonymWithLanguageDAO;
import org.odhsi.athena.dto.BrowserSynonymPagingResultDTO;
import org.odhsi.athena.dto.BrowserSynonymTableDTO;
import org.odhsi.athena.entity.SynonymWithLanguage;
import org.odhsi.athena.services.SynonymService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GMalikov on 22.07.2015.
 */
public class SynonymServiceImpl implements SynonymService{
    @Autowired
    private SynonymWithLanguageDAO synonymWithLanguageDAO;

    @Override
    public BrowserSynonymPagingResultDTO getPagingSynonymsForBrowser(int draw, int start, int length, String searchValue, String sortOrder, Long conceptId) {
        List<SynonymWithLanguage> synonymWithLanguageList = synonymWithLanguageDAO.getConceptSynonymsForBrowser(start, length, searchValue, checkSortOrder(sortOrder), conceptId);
        BrowserSynonymPagingResultDTO result = new BrowserSynonymPagingResultDTO();
        List<BrowserSynonymTableDTO> synonymsDTO = new ArrayList<>();
        for (SynonymWithLanguage synonymWithLanguage : synonymWithLanguageList){
            synonymsDTO.add(new BrowserSynonymTableDTO(synonymWithLanguage));
        }
        result.setData(synonymsDTO);
        result.setRecordsTotal(synonymWithLanguageDAO.getTotalSynonyms());
        result.setRecordsFiltered(synonymWithLanguageDAO.getFilteredSynonymsForBrowser(searchValue, conceptId));
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
