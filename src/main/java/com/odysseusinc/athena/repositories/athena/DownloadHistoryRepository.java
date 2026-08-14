/*
 *
 * Copyright 2020 Odysseus Data Services, inc.
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
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Alexandr Cumarav
 * Created: March 24, 2020
 *
 */

package com.odysseusinc.athena.repositories.athena;

import com.odysseusinc.athena.model.athena.DownloadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DownloadHistoryRepository extends JpaRepository<DownloadHistory, Long> {

    List<DownloadHistory> findByDownloadTimeBetweenOrderByDownloadTimeAsc(LocalDateTime from, LocalDateTime to);

    /**
     * The derived query above returns the history rows in one statement, but the
     * caller then walks {@code bundle -> vocabularies -> vocabularyConversion} for every one
     * of them, and those associations are resolved a bundle at a time — so the admin
     * statistics screen issued on the order of one query per bundle plus one per download
     * item. Fetching that path up front collapses it into a single statement.
     * <p>
     * Only the collection that is actually traversed is fetched. {@code DownloadBundle} also
     * maps {@code files} and {@code shares} as eager bags, and fetching more than one bag in
     * one query is a {@code MultipleBagFetchException}; neither is read here.
     * <p>
     * The fetch joins are inner joins, so a bundle with no download items drops out of the
     * result. That is behaviour-preserving <em>for this caller</em>, which expands every
     * history row over its items and so already contributes nothing for such a bundle. It is
     * also why this is a separate method rather than a change to the derived one.
     */
    @Query("select h from DownloadHistory h "
            + "join fetch h.vocabularyBundle b "
            + "join fetch b.vocabularies v "
            + "join fetch v.vocabularyConversion "
            + "where h.downloadTime between :from and :to "
            + "order by h.downloadTime asc")
    List<DownloadHistory> findForStatistics(@Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

}
