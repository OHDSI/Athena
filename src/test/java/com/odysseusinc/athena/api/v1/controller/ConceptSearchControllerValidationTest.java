/*
 * Copyright 2026 Odysseus Data Services, Inc. (EPAM Systems company)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.odysseusinc.athena.api.v1.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.exceptions.ValidationException;
import org.junit.Test;

public class ConceptSearchControllerValidationTest {

    private final ConceptSearchController controller =
            new ConceptSearchController(null, null, null, null);

    @Test
    public void searchRejectsPageZeroBeforeCallingSolr() {

        ConceptSearchDTO search = new ConceptSearchDTO();
        search.setPage(0);

        ValidationException exception = assertThrows(
                ValidationException.class, () -> controller.search(search, false));

        assertEquals("Page must be at least 1", exception.getMessage());
    }

    @Test
    public void csvDownloadRejectsPageSizeZeroBeforeCallingSolr() {

        ConceptSearchDTO search = new ConceptSearchDTO();
        search.setPageSize(0);

        ValidationException exception = assertThrows(
                ValidationException.class, () -> controller.downloadCsv(search, null));

        assertEquals("Page size must be at least 1", exception.getMessage());
    }
}
