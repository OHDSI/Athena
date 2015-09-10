package org.odhsi.athena.controllers;

import org.odhsi.athena.dto.VocabularyBuildLogDTO;
import org.odhsi.athena.dto.VocabularyInfoDTO;
import org.odhsi.athena.dto.VocabularyStatusDTO;
import org.odhsi.athena.exceptions.VocabularyProcessingException;
import org.odhsi.athena.services.VocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by GMalikov on 13.05.2015.
 */

@Controller
public class VocabularyBuilderController {

    @Autowired
    private VocabularyService vocabularyService;

    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyBuilderController.class);

    @RequestMapping(value = "/getVocabularyStatuses", method = RequestMethod.GET)
    @ResponseBody
    public List<VocabularyStatusDTO> getVocabularyStatuses(@RequestParam String filter){
        return vocabularyService.getAllVocabularyStatuses(filter);
    }

    @RequestMapping(value = "/getLogForVocabulary", method = RequestMethod.GET)
    @ResponseBody
    public List<VocabularyBuildLogDTO> getLogForVocabulary(@RequestParam String vocabularyId, @RequestParam String filter){
        return vocabularyService.getLogForVocabulary(vocabularyId, filter);
    }

    @RequestMapping(value = "/buildVocabulary", method = RequestMethod.POST)
    @ResponseBody
    public String buildVocabulary(@RequestParam String vocabularyId, HttpServletResponse response){
        try {
            vocabularyService.buildVocabulary(vocabularyId);
        } catch (VocabularyProcessingException e) {
            LOGGER.error("Error during vocabulary building: ",e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return e.getMessage();
        }
        return "Success";
    }

    @RequestMapping(value = "getVocabularyInfo", method = RequestMethod.GET)
    @ResponseBody
    public VocabularyInfoDTO getVocabularyInfo(@RequestParam String id){
        LOGGER.info("Getting info for " + id + " vocabulary");
        return vocabularyService.getInfoForVocabulary(id);
    }
}
