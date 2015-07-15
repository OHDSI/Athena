package org.odhsi.athena.controllers;

import org.odhsi.athena.dto.VocabularyBrowserPagingResultDTO;
import org.odhsi.athena.services.VocabularyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by GMalikov on 09.07.2015.
 */

@Controller
public class VocabularyBrowserController {

    @Autowired
    private VocabularyService vocabularyService;

    @RequestMapping(value = "/getVocabulariesForBrowser", method = RequestMethod.GET)
    @ResponseBody
    public VocabularyBrowserPagingResultDTO getVocabulariesForBrowser(HttpServletRequest request,
                                            @RequestParam int draw, int start, int length, String filterOptions){
        String searchVal = request.getParameter("search[value]");
        String sortOrder = request.getParameter("order[0][dir]");
        VocabularyBrowserPagingResultDTO result = vocabularyService.getVocabulariesForBrowserTable(start, length, draw, sortOrder, searchVal);
        return result;
    }

}
