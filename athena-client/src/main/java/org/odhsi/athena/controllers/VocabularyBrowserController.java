package org.odhsi.athena.controllers;

import org.odhsi.athena.dto.BrowserConceptPagingResultDTO;
import org.odhsi.athena.dto.BrowserDomainWithConceptCountTableDTO;
import org.odhsi.athena.dto.BrowserRelationWithConceptPagingResultDTO;
import org.odhsi.athena.dto.BrowserVocabularyPagingResultDTO;
import org.odhsi.athena.services.ConceptService;
import org.odhsi.athena.services.DomainService;
import org.odhsi.athena.services.RelationService;
import org.odhsi.athena.services.VocabularyService;
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

    @RequestMapping(value = "/getVocabulariesForBrowser", method = RequestMethod.GET)
    @ResponseBody
    public BrowserVocabularyPagingResultDTO getVocabulariesForBrowser(HttpServletRequest request,
                                            @RequestParam int draw, int start, int length){
        String searchVal = request.getParameter("search[value]");
        String sortOrder = request.getParameter("order[0][dir]");
        BrowserVocabularyPagingResultDTO result = vocabularyService.getVocabulariesForBrowserTable(start, length, draw, sortOrder, searchVal);
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
    public BrowserConceptPagingResultDTO getConceptsForBrowser(HttpServletRequest request, @RequestParam int draw, int start, int length, String vocabularyId, String domainId){
        String searchValue = request.getParameter("search[value]");
        String sortOrder = request.getParameter("order[0][dir]");
        BrowserConceptPagingResultDTO result = conceptService.getPagingConceptsForBrowser(draw, start, length, searchValue, sortOrder, vocabularyId, domainId);
        return result;
    }

    @RequestMapping(value = "/getConceptRelationsForBrowser", method = RequestMethod.GET)
    @ResponseBody
    public BrowserRelationWithConceptPagingResultDTO getConceptRelationsForBrowser(HttpServletRequest request, @RequestParam int draw, int start, int length, Long conceptId){
        String searchValue = request.getParameter("search[value]");
        String sortOrder = request.getParameter("order[0][dir]");
        BrowserRelationWithConceptPagingResultDTO result = relationService.getPagingRelationsForBrowser(draw, start, length, sortOrder, searchValue, conceptId);
        return result;
    }
}
