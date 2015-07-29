package org.odhsi.athena.controllers;

import org.odhsi.athena.dto.*;
import org.odhsi.athena.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by GMalikov on 09.07.2015.
 */

@Controller
public class VocabularyBrowserController {

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private DomainService domainService;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private RelationService relationService;

    @Autowired
    private SynonymService synonymService;

    @RequestMapping(value = "/getVocabulariesForBrowser", method = RequestMethod.GET)
    @ResponseBody
    public BrowserVocabularyPagingResultDTO getVocabulariesForBrowser(HttpServletRequest request, @RequestParam int page, int rows, String sidx, String sord){
        String searchName = request.getParameter("fullName");
        BrowserVocabularyPagingResultDTO result = vocabularyService.getVocabulariesForBrowserTable((page-1)*rows, rows, page, sord, searchName);
        return result;
    }

    @RequestMapping(value = "/getDomainsForBrowser", method = RequestMethod.GET)
    @ResponseBody
    public List<BrowserDomainWithConceptCountTableDTO> getDomainsForBrowser(@RequestParam String vocabularyId){
        List<BrowserDomainWithConceptCountTableDTO> result = domainService.getDomainsForBrowserByVocabularyId(vocabularyId);
        return result;
    }

    @RequestMapping(value = "/getConceptsForBrowser", method = RequestMethod.GET)
    @ResponseBody
    public BrowserConceptPagingResultDTO getConceptsForBrowser(HttpServletRequest request, @RequestParam int page, int rows, String sidx, String sord, String vocabularyId, String domainId){
        String searchValue = request.getParameter("name");
        BrowserConceptPagingResultDTO result = conceptService.getPagingConceptsForBrowser((page-1)*rows, rows, page, searchValue, sord, vocabularyId, domainId);
        return result;
    }

    @RequestMapping(value = "/getConceptRelationsForBrowser", method = RequestMethod.GET)
    @ResponseBody
    public BrowserRelationWithConceptPagingResultDTO getConceptRelationsForBrowser(HttpServletRequest request, @RequestParam int page, int rows, String sidx, String sord, Long conceptId){
        String searchValue = request.getParameter("conceptName");
        BrowserRelationWithConceptPagingResultDTO result = relationService.getPagingRelationsForBrowser((page-1)*rows, rows, page, sord, searchValue, conceptId);
        return result;
    }

    @RequestMapping(value = "/getSynonymsForBrowser", method = RequestMethod.GET)
    @ResponseBody
    public BrowserSynonymPagingResultDTO getSynonymsForBrowser(HttpServletRequest request, @RequestParam int draw, int start, int length, Long conceptId){
        String searchValue = request.getParameter("search[value]");
        String sortOrder = request.getParameter("order[0][dir]");
        BrowserSynonymPagingResultDTO result = synonymService.getPagingSynonymsForBrowser(draw,start,length,sortOrder,searchValue,conceptId);
        return result;
    }
}
