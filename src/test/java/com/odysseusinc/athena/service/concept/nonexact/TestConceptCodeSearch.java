package com.odysseusinc.athena.service.concept.nonexact;

import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
public class TestConceptCodeSearch {

    @Autowired
    private ConceptService conceptService;
    private ConceptSearchDTO searchDTO;

    @Before
    public void init() {

        searchDTO = new ConceptSearchDTO();
        searchDTO.setPageSize(50);
    }

    @Test
    public void testSearchFullMatch() throws Exception {

        //Arrange
        String query = "12345";
        searchDTO.setQuery(query);

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultCodes = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getCode)
                .collect(Collectors.toList());

        //Assert
        assertEquals(4, resultCodes.size());
        assertTrue(resultCodes.stream().allMatch(name -> name.toLowerCase().contains(query)));
        //check that first results are the most relevant
        assertThat(resultCodes.get(0), equalToIgnoringCase(query));
    }

    @Test
    public void testSearchWithLeadingZeros() throws Exception {

        //Arrange
        String query = "012345";
        searchDTO.setQuery(query);

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultCodes = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getCode)
                .collect(Collectors.toList());

        //Assert
        assertEquals(1, resultCodes.size());
        assertThat(resultCodes.get(0), equalToIgnoringCase("0" + query));
    }

    @Test
    public void testSearchWithNonExistingCode() throws Exception {

        //Arrange
        String query = "50012345";
        searchDTO.setQuery(query);

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultCodes = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getCode)
                .collect(Collectors.toList());

        //Assert
        assertEquals(0, resultCodes.size());
    }

    @Test
    public void testSearchPartMatch() throws Exception {

        //Arrange
        String query = "123";
        searchDTO.setQuery(query);

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultCodes = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getCode)
                .collect(Collectors.toList());

        //Assert
        assertEquals(8, resultCodes.size());
        assertTrue(resultCodes.stream().allMatch(name -> name.toLowerCase().contains(query)));
        //check that first results are the most relevant
        assertThat(resultCodes.get(0), equalToIgnoringCase(query));
    }
}
