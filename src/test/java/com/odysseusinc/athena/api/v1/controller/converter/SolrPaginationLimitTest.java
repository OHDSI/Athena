package com.odysseusinc.athena.api.v1.controller.converter;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.exceptions.ValidationException;
import com.odysseusinc.athena.service.checker.LimitChecker;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

/**
 * {@code page} and {@code pageSize} on the public concept search are bounded before they
 * reach Solr. JUnit 4 on purpose, like the other converter tests.
 */
public class SolrPaginationLimitTest {

    private static final int MAX_PAGE_SIZE = 1000;

    private ConceptSearchDTOToSolrQuery converter;

    @Before
    public void setUp() {

        LimitChecker limitChecker = new LimitChecker();
        ReflectionTestUtils.setField(limitChecker, "maxLimitPageSize", MAX_PAGE_SIZE);
        converter = new ConceptSearchDTOToSolrQuery(null, limitChecker, null, null);
    }

    @Test
    public void appliesRequestedPage() {

        SolrQuery query = paginate(3, 15);

        assertEquals(Integer.valueOf(30), query.getStart());
        assertEquals(Integer.valueOf(15), query.getRows());
    }

    @Test
    public void acceptsConfiguredMaximum() {

        assertEquals(Integer.valueOf(MAX_PAGE_SIZE), paginate(1, MAX_PAGE_SIZE).getRows());
    }

    @Test
    public void leavesQueryUntouchedWithoutPagination() {

        ConceptSearchDTO search = new ConceptSearchDTO();
        search.setPage(null);
        search.setPageSize(null);
        SolrQuery query = new SolrQuery();
        converter.setPagination(search, query);

        assertNull(query.getRows());
        assertNull(query.getStart());
    }

    @Test
    public void rejectsPageSizeAboveMaximum() {

        assertRejected(1, MAX_PAGE_SIZE + 1, "Page size must be between 1 and " + MAX_PAGE_SIZE);
    }

    @Test
    public void rejectsNonPositivePageSize() {

        assertRejected(1, 0, "Page size must be between 1 and " + MAX_PAGE_SIZE);
    }

    @Test
    public void rejectsNonPositivePage() {

        assertRejected(0, 15, "Page must be at least 1");
    }

    @Test
    public void rejectsPageThatOverflowsStart() {

        assertRejected(Integer.MAX_VALUE, MAX_PAGE_SIZE, "Page is out of range");
    }

    private SolrQuery paginate(int page, int pageSize) {

        ConceptSearchDTO search = new ConceptSearchDTO();
        search.setPage(page);
        search.setPageSize(pageSize);
        SolrQuery query = new SolrQuery();
        converter.setPagination(search, query);
        return query;
    }

    private void assertRejected(int page, int pageSize, String message) {

        ValidationException exception = assertThrows(ValidationException.class, () -> paginate(page, pageSize));
        assertEquals(message, exception.getMessage());
    }
}
