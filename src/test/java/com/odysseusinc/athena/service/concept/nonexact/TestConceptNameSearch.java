package com.odysseusinc.athena.service.concept.nonexact;

import static org.hamcrest.Matchers.is;
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
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
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
    @Autowired
    private Environment environment;
    private ConceptSearchDTO searchDTO;
    private final static String ASPIRIN = "aspirin";
    private final static String QUILLAIA = "Quillaia liquid extract";

    @Before
    public void init() {

        searchDTO = new ConceptSearchDTO();
        searchDTO.setPageSize(50);
    }

    @Test
    public void testSearchWithoutBrackets() throws Exception {

        //Arrange
        searchDTO.setQuery(ASPIRIN);

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultNames = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getName)
                .collect(Collectors.toList());

        //Assert
        assertEquals(12, resultNames.size());
        assertTrue(resultNames.stream().allMatch(name -> name.toLowerCase().contains(ASPIRIN)));
        //check that first results are the most relevant
        assertThat(resultNames.get(0), equalToIgnoringCase(ASPIRIN));
    }

    @Test
    public void testSearchWithBrackets() throws Exception {

        //Arrange
        String query = "[" + ASPIRIN + "]";
        searchDTO.setQuery(query);

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultNames = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getName)
                .collect(Collectors.toList());

        //Assert
        assertEquals(12, resultNames.size());
        assertTrue(resultNames.stream().allMatch(name -> name.toLowerCase().contains(ASPIRIN)));
        //check that first results are the most relevant
        assertThat(resultNames.get(0), equalToIgnoringCase(query));
        assertThat(resultNames.get(1), equalToIgnoringCase(query));
    }

    @Test
    public void testSearchNotMatchingSeveralWords() throws Exception {

        //test for solr.default.query.operator = AND
        Assume.assumeThat(environment.getProperty("solr.default.query.operator"), is("AND"));

        //Arrange
        String query = QUILLAIA + " " + ASPIRIN;
        searchDTO.setQuery(query);

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultNames = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getName)
                .collect(Collectors.toList());

        //Assert
        assertEquals(0, resultNames.size());
    }

    @Test
    public void testSearchFullMatchSeveralWords() throws Exception {

        //test for solr.default.query.operator = AND
        Assume.assumeThat(environment.getProperty("solr.default.query.operator"), is("AND"));

        //Arrange
        String query = "ibup " + ASPIRIN;
        searchDTO.setQuery(query);

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultNames = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getName)
                .collect(Collectors.toList());

        //Assert
        assertEquals(1, resultNames.size());
        assertThat(resultNames.get(0), equalToIgnoringCase(query));
    }

    @Test
    public void testSearchMatchingSeveralWords() throws Exception {

        //test for solr.default.query.operator = OR
        Assume.assumeThat(environment.getProperty("solr.default.query.operator"), is("OR"));

        //Arrange
        String query = QUILLAIA + " " + ASPIRIN;
        searchDTO.setQuery(query);

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultNames = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getName)
                .collect(Collectors.toList());
        //Assert
        assertEquals(13, resultNames.size());
        assertEquals(12, resultNames.stream().filter(name -> name.toLowerCase().contains(ASPIRIN)).count());
        //check that first results are the most relevant
        assertThat(resultNames.get(0), equalToIgnoringCase(QUILLAIA));
        assertThat(resultNames.get(1), equalToIgnoringCase(ASPIRIN));
    }

    @Test
    public void testSearchQueryWithLetterInTheBeginning() throws Exception {

        //Arrange
        String query = "f" + ASPIRIN;
        searchDTO.setQuery(query);

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultNames = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getName)
                .collect(Collectors.toList());

        //Assert
        assertEquals(0, resultNames.size());
    }

    @Test
    public void testSearchQueryWithLetterInTheEnd() throws Exception {

        //Arrange
        String query = ASPIRIN + "f";
        searchDTO.setQuery(query);

        //Action
        ConceptSearchResultDTO resultDTO = conceptService.search(searchDTO);

        List<String> resultNames = resultDTO.getContent()
                .stream()
                .map(ConceptDTO::getName)
                .collect(Collectors.toList());

        //Assert
        assertEquals(0, resultNames.size());
    }

    @Test
    public void testSearchNotExistingConceptName() throws Exception {

        //Arrange
        searchDTO.setQuery("thisConceptNameDoesn'tExist");

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
