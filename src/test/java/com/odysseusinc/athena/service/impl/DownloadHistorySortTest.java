/*
 *
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
 *
 * Company: Odysseus Data Services, Inc.
 * Created: July 30, 2026
 *
 */

package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.api.v1.controller.dto.DownloadHistoryDTO;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Sorting the admin download statistics.
 * <p>
 * A download whose account has since been deleted keeps its row, with the user, e-mail and
 * organization columns left blank — dropping the row instead would understate the statistics.
 * That makes null values normal in exactly the three columns the screen can be sorted by, so
 * the comparators have to tolerate them.
 * <p>
 * JUnit 4 on purpose.
 */
public class DownloadHistorySortTest {

    private final DownloadsHistoryServiceImpl service =
            new DownloadsHistoryServiceImpl(null, null, ';', null);

    private static DownloadHistoryDTO row(String code, String user, String email, String org) {

        DownloadHistoryDTO dto = new DownloadHistoryDTO();
        dto.setCode(code);
        dto.setUserName(user);
        dto.setEmail(email);
        dto.setOrganization(org);
        dto.setDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        return dto;
    }

    /**
     * The two live rows carry distinct values in all three sortable columns, and the e-mail
     * order is the reverse of the user/organization order — asserting only null placement
     * would pass for a comparator that ordered the live rows wrongly, or read another column.
     */
    private static DownloadHistoryDTO ada() {

        return row("SNOMED", "Ada Lovelace", "zoe@example.org", "Analytical Engines");
    }

    private static DownloadHistoryDTO grace() {

        return row("RxNorm", "Grace Hopper", "ada@example.org", "Bell Labs");
    }

    /** A row whose user was deleted: the three user columns are null. */
    private static DownloadHistoryDTO deletedUser(String code) {

        return row(code, null, null, null);
    }

    private List<String> sortedBy(String field, boolean ascending,
                                  Function<DownloadHistoryDTO, String> column) {

        Collection<DownloadHistoryDTO> input = Arrays.asList(ada(), deletedUser("LOINC"), grace());
        return new ArrayList<>(service.sort(input, field, ascending))
                .stream().map(column).collect(Collectors.toList());
    }

    /** Null sorts last ascending; descending is served by reversing, so null leads. */
    @Test
    public void sortsByEmailWithADeletedUserPresent() {

        assertEquals(Arrays.asList("ada@example.org", "zoe@example.org", null),
                sortedBy("email", true, DownloadHistoryDTO::getEmail));
        assertEquals(Arrays.asList(null, "zoe@example.org", "ada@example.org"),
                sortedBy("email", false, DownloadHistoryDTO::getEmail));
    }

    @Test
    public void sortsByUserNameWithADeletedUserPresent() {

        assertEquals(Arrays.asList("Ada Lovelace", "Grace Hopper", null),
                sortedBy("userName", true, DownloadHistoryDTO::getUserName));
        assertEquals(Arrays.asList(null, "Grace Hopper", "Ada Lovelace"),
                sortedBy("userName", false, DownloadHistoryDTO::getUserName));
    }

    @Test
    public void sortsByOrganizationWithADeletedUserPresent() {

        assertEquals(Arrays.asList("Analytical Engines", "Bell Labs", null),
                sortedBy("organization", true, DownloadHistoryDTO::getOrganization));
        assertEquals(Arrays.asList(null, "Bell Labs", "Analytical Engines"),
                sortedBy("organization", false, DownloadHistoryDTO::getOrganization));
    }

    /** The default comparator sorts by vocabulary code, which is never null. */
    @Test
    public void sortsByCodeByDefault() {

        assertEquals(Arrays.asList("LOINC", "RxNorm", "SNOMED"),
                sortedBy("code", true, DownloadHistoryDTO::getCode));
    }
}
