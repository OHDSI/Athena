package com.odysseusinc.athena.service.concept.exact;

import static org.junit.Assert.assertEquals;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchResultDTO;
import com.odysseusinc.athena.config.WebApplicationStarter;
import com.odysseusinc.athena.service.ConceptService;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApplicationStarter.class)
@ActiveProfiles("test")
@TestPropertySource(locations = {"classpath:/test.properties"})
public class TestConceptNameSearch {

    @Autowired
    private ConceptService conceptService;
    private ConceptSearchDTO searchDTO;
    private final static String ASPIRIN_LOWER = "aspirin";
    private final static String ASPIRIN_UPPER = "Aspirin";

    @Before
    public void init() {

        searchDTO = new ConceptSearchDTO();
        searchDTO.setPageSize(50);
    }

    @Test
    public void testSearchWithoutBrackets() throws Exception {

        //Arrange
        searchDTO.setQuery("\"" + ASPIRIN_LOWER + "\"");

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultNames = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getName)
                .collect(Collectors.toList());

        //Assert
        assertEquals(1, resultNames.size());
        assertEquals(resultNames.get(0), ASPIRIN_LOWER);
    }

    @Test
    public void testSearchLowerWithBrackets() throws Exception {

        //Arrange
        searchDTO.setQuery("\"[" + ASPIRIN_LOWER + "]\"");

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultNames = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getName)
                .collect(Collectors.toList());

        //Assert
        assertEquals(1, resultNames.size());
        assertEquals(resultNames.get(0), "[aspirin]");
    }

    @Test
    public void testSearchUpperWithBrackets() throws Exception {

        //Arrange
        searchDTO.setQuery("\"[" + ASPIRIN_UPPER + "]\"");

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultNames = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getName)
                .collect(Collectors.toList());

        //Assert
        assertEquals(1, resultNames.size());
        assertEquals(resultNames.get(0), "[Aspirin]");
    }

    @Test
    public void testSearchNotExistingConceptName() throws Exception {

        //Arrange
        searchDTO.setQuery("\"thisConceptNameDoesn'tExist\"");

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultNames = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getName)
                .collect(Collectors.toList());

        //Assert
        assertEquals(0, resultNames.size());
    }
}